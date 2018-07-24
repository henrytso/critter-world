package server;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.delete;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.gson.Gson;

import ast.Program;
import ast.Rule;
import client.ClientGrid.HexInfo;
import simulator.Critter;
import simulator.Hex;
import simulator.ServerSimulator;

public class Server {

	ServerSimulator sim;

	int versionOld; // the version number before current world was created
	Integer[][] log; // log of last version number when each hex was changed
	ArrayList<Critter> deadCritters;

	private ArrayList<String> users;

	private static final String READ_PASSWORD = "bilbo";
	private static final String WRITE_PASSWORD = "frodo";
	private static final String ADMIN_PASSWORD = "gandalf";

	private static final String READER_PREFIX = "0";
	private static final String WRITER_PREFIX = "1";
	private static final String ADMIN_PREFIX = "2";

	private int nextSuffix;
	private int nextCritterId;

	private float rate;

	// used to deal with race conditions with simulator info
	ReentrantReadWriteLock lock;

	/**
	 * Creates a new server
	 */
	public Server() {
		users = new ArrayList<String>();
		nextSuffix = 101010; // 6-digit suffix
		nextCritterId = 1010;
		versionOld = 0;
		deadCritters = new ArrayList<Critter>();
		rate = 0;
		log = new Integer[0][0];

		lock = new ReentrantReadWriteLock();
	}

	/**
	 * Handles commands that the user sends
	 */
	public void run() {

		port(8080);

		Gson gson = new Gson();

		// trying to log in
		post("/login", (request, response) -> {
			response.header("Content-Type", "application/json");

			String json = request.body();
			LoginBundle login = gson.fromJson(json, LoginBundle.class);

			switch (login.level()) {
			case "read":
				if (login.password().equals(READ_PASSWORD)) {
					int id = Integer.valueOf(READER_PREFIX + nextSuffix++);
					users.add(id + "");
					return new SessionIDBundle(id);
				}
				response.status(401);
				response.body("Unauthorized");
				break;
			case "write":
				if (login.password().equals(WRITE_PASSWORD)) {
					int id = Integer.valueOf(WRITER_PREFIX + nextSuffix++);
					users.add(id + "");
					return new SessionIDBundle(id);
				}
				response.status(401);
				response.body("Unauthorized");
				break;
			case "admin":
				if (login.password().equals(ADMIN_PASSWORD)) {
					int id = Integer.valueOf(ADMIN_PREFIX + nextSuffix++);
					users.add(id + "");
					return new SessionIDBundle(id);
				}
				response.status(401);
				response.body("Unauthorized");
				break;
			default:
				response.status(401);
				response.body("Unauthorized");
			}
			return "Wrong level or password";
		}, gson::toJson);

		// trying to get ids of all critters
		get("/critters", (request, response) -> {
			response.header("Content-Type", "application/json");

			if (!users.contains(request.queryParams("session_id"))) {
				response.status(401);
				response.body("Unauthorized");
				return "Do not have access to this";
			}
			int clientID = Integer.parseInt(request.queryParams("session_id"));

			ArrayList<CritterInfo> result = new ArrayList<CritterInfo>();

			lock.readLock().lock();
			for (Critter c : sim.getCritters()) {
				result.add(new CritterInfo(c, clientID));
			}
			lock.readLock().unlock();

			return new CritterInfoBundle(result);
		}, gson::toJson);

		// Makes critters
		post("/critters", (request, response) -> {
			response.header("Content-Type", "application/json");
			String seshIdStr = request.queryParams("session_id");
			if (!users.contains(seshIdStr) || seshIdStr.substring(0, 1).equals("0")) {
				response.status(401);
				response.body("Unauthorized");
				return "Do not have access to this";
			}

			if (sim == null) {
				response.status(403);
				return "Create a world before critters";
			}

			int seshId = Integer.parseInt(seshIdStr);

			String json = request.body();
			MakeCritterBundle newCritter = gson.fromJson(json, MakeCritterBundle.class);
			String speciesId = newCritter.speciesId();
			String program = newCritter.program();
			int[] mem = newCritter.mem();
			LocationBundle[] positions = newCritter.positions();
			int num = newCritter.num();

			if (positions == null) {
				int[] ids = new int[num];
				for (int i = 0; i < num; i++) {
					int critterId = nextCritterId++;
					lock.writeLock().lock();
					boolean added = sim.loadCritter(program, speciesId, mem, critterId, seshId);
					lock.writeLock().unlock();
					if (added)
						ids[i] = critterId;
					else
						ids[i] = -1;
				}

				response.status(201);
				response.body("OK");
				return new CrittersResponseBundle(speciesId, ids);
			} else {

				int[] ids = new int[positions.length];
				for (int i = 0; i < positions.length; i++) {
					int critterId = nextCritterId++;
					int col = positions[i].col();
					int row = positions[i].row();
					lock.writeLock().lock();
					boolean added = sim.loadCritter(program, col, row, 0, speciesId, mem, critterId, seshId);
					lock.writeLock().unlock();
					if (added)
						ids[i] = critterId;
					else
						ids[i] = -1;
				}

				response.status(201);
				response.body("OK");
				return new CrittersResponseBundle(speciesId, ids);
			}
		}, gson::toJson);

		// gets info about a specific critter
		get("/critter/:id", (request, response) -> {
			response.header("Content-Type", "application/json");
			int id = Integer.parseInt(request.params(":id"));

			if (!users.contains(request.queryParams("session_id"))) {
				response.status(401);
				response.body("Unauthorized");
				return "Do not have access to this";
			}
			int sessionId = Integer.parseInt(request.queryParams("session_id"));

			if (sim == null) {
				response.status(403);
				return "Need a world";
			}

			lock.readLock().lock();
			for (Critter c : sim.getCritters()) {
				if (c.critterId() == id)
					return new CritterInfo(c, sessionId);
			}
			lock.readLock().unlock();

			response.status(404);
			return "Could not find specified critter";
		}, gson::toJson);

		// removes a critter form a simulation without having it die.
		delete("/critter/:id", (request, response) -> {
			response.header("Content-Type", "application/json");
			int id = Integer.parseInt(request.params(":id"));
			String sessionIdStr = request.queryParams("session_id");
			if (!users.contains(sessionIdStr) || sessionIdStr.substring(0, 1).equals("0")) {
				response.status(401);
				response.body("Unauthorized");
				return "Do not have access to this";
			}
			int sessionId = Integer.parseInt(sessionIdStr);

			if (sim == null) {
				response.status(403);
				return "Need a world";
			}

			lock.writeLock().lock();
			for (int i = sim.getCritters().size() - 1; i > -1; i--) {
				Critter c = sim.getCritters().get(i);
				if (c.critterId() == id) {
					if (c.viewableBy(sessionId)) {
						sim.die(c);
						deadCritters.remove(c);
					}
				}
			}
			lock.writeLock().lock();

			response.status(204);
			return "";
		}, gson::toJson);

		// Makes a world
		post("/world", (request, response) -> {
			response.header("Content-Type", "application/json");
			String json = request.body();
			MakeWorldBundle newWorld = gson.fromJson(json, MakeWorldBundle.class);

			String seshIdStr = request.queryParams("session_id");
			if (!users.contains(seshIdStr) || seshIdStr.substring(0, 1).equals("0")) {
				response.status(401);
				response.body("Unauthorized");
				return "Do not have access to this";
			}

			if (sim != null) {
				versionOld = sim.version();
				lock.readLock().lock();
				for (Critter c : sim.getCritters()) {
					deadCritters.add(c);
				}
				lock.readLock().unlock();
			}

			lock.writeLock().lock();
			try {
				if (newWorld.description().equals(" "))
					sim = new ServerSimulator(deadCritters, versionOld);
				else
					sim = new ServerSimulator(newWorld.description(), deadCritters, versionOld);
			} catch (IllegalArgumentException e) {
				response.status(406);
				return "Incorrect world defintion";
			}

			lock.writeLock().unlock();

			if (log.length == 0) {
				log = new Integer[sim.getWorld().length][sim.getWorld()[0].length];
			}
			if (sim.getWorld().length > log.length) {
				log = new Integer[sim.getWorld().length][log[0].length];
			}
			if (sim.getWorld()[0].length > log[0].length) {
				log = new Integer[log.length][sim.getWorld()[0].length];
			}

			for (int i = 0; i < log.length; i++) {
				for (int j = 0; j < log[0].length; j++) {
					lock.readLock().lock();
					log[i][j] = sim.version();
					lock.readLock().unlock();
				}
			}

			response.status(201);
			response.body("OK");
			return "";
		}, gson::toJson);

		get("/world", (request, response) -> {

			response.header("Content-Type", "application/json");
			String json = request.body();
			if (!users.contains(request.queryParams("session_id"))) {
				response.status(401);
				response.body("Unauthorized");
				return "Do not have access to this";
			}
			String seshIdStr = request.queryParams("session_id");
			String update_since = request.queryParams("update_since");
			int u;
			if (update_since == null) {
				u = 0;
			} else {
				u = Integer.parseInt(update_since);
			}

			if (sim == null) {
				response.status(404);
				return "No world yet.";
			}

			int from_r;
			int to_r;
			int from_c;
			int to_c;

			String from_row = request.queryParams("from_row");

			if (from_row == null) {
				lock.readLock().lock();

				from_r = 0;
				to_r = sim.getWorld()[0].length;
				from_c = 0;
				to_c = sim.getWorld().length;

				lock.readLock().unlock();
			} else {
				from_r = Integer.parseInt(from_row);
				to_r = Integer.parseInt("to_row");
				from_c = Integer.parseInt("from_col");
				to_c = Integer.parseInt("to_col");
			}

			lock.readLock().lock();
			ArrayList<Critter> d = sim.getDeadCritters();
			Integer[] dead = new Integer[d.size()];
			for (int i = 0; i < dead.length; i++) {
				dead[i] = new Integer(d.get(i).critterId());
			}
			WorldInfoBundle w = new WorldInfoBundle(sim.getTimeStep(), sim.version(), u, rate, sim.getName(),
					sim.getCritters().size(), sim.getWorld().length, sim.getWorld()[0].length, dead);

			int maxRows = 0;
			while (2 * maxRows < 2 * sim.getWorld()[0].length - sim.getWorld().length)
				maxRows++;

			for (int col = Math.max(0, from_c); col < Math.min(sim.getWorld().length, to_c); col++) {
				int tempRows = maxRows;
				if (col % 2 == 1) {
					tempRows--;
				}
				int startRow = Math.max((col / 2) + (col % 2), from_r);

				for (int row = 0; row < Math.min(tempRows, to_r); row++) {
					HexInfo h = new HexInfo(col, row + startRow, sim.getWorld()[col][row + startRow],
							Integer.parseInt(seshIdStr));
					w.addHexInfo(h);
				}

			}

			lock.readLock().unlock();

			return w;
		}, gson::toJson);

		// Creates either a rock of a piece of food.
		post("/world/create_entity", (request, response) -> {

			response.header("Content-Type", "application/json");
			String seshIdStr = request.queryParams("session_id");
			if (!users.contains(seshIdStr) || seshIdStr.substring(0, 1).equals("0")) {
				response.status(401);
				response.body("Unauthorized");
				return "Do not have access to this";
			}

			String json = request.body();
			MakeEntityBundle entity = gson.fromJson(json, MakeEntityBundle.class);
			boolean added = false;
			if (entity.type().equals("Rock")) {
				lock.writeLock().lock();
				added = sim.addRock(entity.col(), entity.row());
				lock.writeLock().unlock();
			} else if (entity.type().equals("Food")) {
				lock.writeLock().lock();
				added = sim.addFood(entity.col(), entity.row(), entity.amount());
				lock.writeLock().lock();
			} else {
				response.status(406);
				return "Could not create entity";
			}
			if (added) {
				response.status(201);
				response.body("OK");
				return ("");
			} else {
				response.status(406);
				response.body("Not Acceptable");
				return "hex is either invalid or occupied";
			}

		}, gson::toJson);

		// steps the world
		post("/step", (request, response) -> {
			response.header("Content-Type", "application/json");
			String json = request.body();
			StepBundle step = gson.fromJson(json, StepBundle.class);

			String seshIdStr = request.queryParams("session_id");
			if (!users.contains(seshIdStr) || seshIdStr.substring(0, 1).equals("0")) {
				response.status(401);
				response.body("Unauthorized");
				return "Do not have access to this";
			}

			if (rate != 0) {
				response.status(406);
				response.body("Not Acceptable");
				return "Cannot step while world is runnning";
			}

			int numSteps;
			if (step.count() == 0)
				numSteps = 0;
			else
				numSteps = step.count();

			if (sim == null) {
				response.status(404);
				return "No world yet.";
			}

			lock.writeLock().lock();
			sim.advanceTime(numSteps, log);
			lock.writeLock().unlock();

			response.status(200);
			response.body("OK");
			return "OK";
		}, gson::toJson);

		// csets the rate of the simulation
		post("/run", (request, response) -> {
			response.header("Content-Type", "application/json");
			String json = request.body();
			RunBundle running = gson.fromJson(json, RunBundle.class);

			String seshIdStr = request.queryParams("session_id");
			if (!users.contains(seshIdStr) || seshIdStr.substring(0, 1).equals("0")) {
				response.status(401);
				response.body("Unauthorized");
				return "Do not have access to this";
			}

			float newSpeed = running.rate();
			if (newSpeed < 0) {
				response.status(406);
				response.body("Not Acceptable");
				return "Rate cannot be negative";
			}

			synchronized (this) {
				if (newSpeed == 0) {
					rate = 0;
					response.status(200);
					response.body("OK");
					return "OK";
				}
			}

			// If newSpeed is greater than 0
			synchronized (this) {
				if (rate > 0)
					rate = newSpeed;
				else {
					rate = newSpeed;
					Thread advanceTime = new Thread() {
						public void run() {
							while (rate > 0) {
								int time = (int) (1000 / rate);
								lock.writeLock().lock();
								sim.advanceTime(1, log);
								lock.writeLock().unlock();

								try {
									sleep(time);
								} catch (InterruptedException e) {
									System.out.println("Problem sleeping");
								}
							}
						}
					};
					advanceTime.start();
				}
			}
			return "";
		}, gson::toJson);
	}

	// Request for POST /login
	private class LoginBundle {
		private String level;
		private String password;

		public String level() {
			return level;
		}

		public String password() {
			return password;
		}
	}

	// Response to POST /login
	private class SessionIDBundle {
		private int id;

		public SessionIDBundle(int id) {
			this.id = id;
		}
	}

	// Info for one critter
	private class CritterInfo {
		private int id;
		private String species_id;
		String program;
		int row, col, dir;
		int[] mem;
		int recently_executed_rule;

		public CritterInfo(Critter c, int sessionId) {
			id = c.critterId();
			species_id = c.getSpecies();
			row = c.row();
			col = c.col();
			dir = c.getDir();
			mem = c.getMem();
			if (c.viewableBy(sessionId)) { // can see if admin
				program = c.getProgram().toString();
				recently_executed_rule = c.indexOf(c.getLastRule());
			} else {
				program = null;
				recently_executed_rule = -1;
			}
		}
	}

	// Response to GET /critters
	private class CritterInfoBundle {
		private ArrayList<CritterInfo> critters;

		public CritterInfoBundle(ArrayList<CritterInfo> critters) {
			this.critters = critters;
		}
	}

	// part of request of POST /critters
	private class LocationBundle {
		private int row;
		private int col;

		public int col() {
			return col;
		}

		public int row() {
			return row;
		}
	}

	// request of POST /critters
	private class MakeCritterBundle {
		private String speciesId;
		private String program;
		private int[] mem;
		private LocationBundle[] positions;
		private int num;

		public String speciesId() {
			return speciesId;
		}

		public int num() {
			return num;
		}

		public LocationBundle[] positions() {
			return positions;
		}

		public int[] mem() {
			return mem;
		}

		public String program() {
			return program;
		}

	}

	// response to POST /critters
	private class CrittersResponseBundle {
		private String species_id;
		private int[] ids;

		public CrittersResponseBundle(String species_id, int[] ids) {
			this.species_id = species_id;
			this.ids = ids;
		}
	}

	// part of request of GET /world
	private class WorldInfoBundle {
		private int current_timestep;
		private int current_version_number;
		private int update_since;
		private double rate;
		private String name;
		private int population;
		private int rows;
		private int cols;
		private Integer[] dead_critters;
		private ArrayList<HexInfo> state;

		public WorldInfoBundle(int timestep, int version, int updateSince, double rate, String name, int pop, int cols,
				int rows, Integer[] dead_critters) {
			this.current_timestep = timestep;
			this.current_version_number = version;
			this.update_since = updateSince;
			this.rate = rate;
			this.name = name;
			this.population = pop;
			this.rows = rows;
			this.cols = cols;
			this.dead_critters = dead_critters;
			state = new ArrayList<HexInfo>();
		}

		public void addHexInfo(HexInfo h) {
			state.add(h);
		}
	}

	// request of POST /world
	private class MakeWorldBundle {
		private String description;

		public String description() {
			return description;
		}
	}

	// request of POST /world/create_entity
	private class MakeEntityBundle {
		private int col;
		private int row;
		private String type; // either "food" or "rock"
		private int amount;

		private int col() {
			return col;
		}

		private int row() {
			return row;
		}

		private String type() {
			return type;
		}

		private int amount() {
			return amount;
		}
	}

	// request of POST /step
	private class StepBundle {
		private int count;

		public int count() {
			return count;
		}
	}

	// request of POST /run
	private class RunBundle {
		private float rate;

		public float rate() {
			return rate;
		}
	}

	// part of request of GET /world
	private class HexInfo {
		int row;
		int col;
		String type;
		int value; // for food
		int id; // for critter
		String species_id;
		String program;
		int direction;
		int[] mem;
		int recently_executed_rule;

		public HexInfo(int col, int row, Hex h, int sessionId) {
			this.col = col;
			this.row = row;

			if (h.isEmpty())
				type = "nothing";

			if (h.isRock())
				type = "rock";

			if (h.isFood()) {
				type = "food";
				value = h.getFood();
			}

			if (h.isCritter()) {
				type = "critter";
				Critter c = h.getCritter();
				id = c.critterId();
				species_id = c.getSpecies();

				direction = c.getDir();
				mem = c.getMem();
				if (c.viewableBy(sessionId)) { // can see if admin
					program = c.getProgram().toString();
					recently_executed_rule = c.indexOf(c.getLastRule());
				} else {
					program = null;
					recently_executed_rule = -1;
				}
			}

		}
	}
	
	public ServerSimulator getSim() {
		return sim;
	}
}
