package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.Gson;

import javafx.scene.control.TextArea;
import client.ClientGrid.WorldInfoBundle;

/**
 * A class that send and receives info about
 * users commands and critter world.
 */
public class Client {

	Gson gson;
	private URL url;
	private static String sessionId;
	private TextArea messages;

	/**
	 * Creates a class that will connect with the specified server.
	 * @param url the url of the server
	 */
	public Client(URL url) {
		gson = new Gson();
		this.url = url;
	}
	
	/**
	 * Adds a text area where this class can print
	 * user friendly error messages when something goes wrong, 
	 * or other helpful information.
	 * @param messages  A textArea displayed in the GUI
	 */
	public void connectMessages(TextArea messages) {
		this.messages = messages;
	}

	/**
	 * Makes a world on the server with from the given description.
	 * No critters will be placed in the world.   
	 * @param text  A description of the world, continaing name,
	 * size, rock and food placements.  " " will make a randomly
	 * generated world.
	 */
	public void makeWorld(String text) {
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:8080/world?session_id=2101010").openConnection();
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			
			PrintWriter w = new PrintWriter(connection.getOutputStream());
			MakeWorldBundle bundle = new MakeWorldBundle(text);
			w.write(gson.toJson(bundle, MakeWorldBundle.class));
			w.flush();

			BufferedReader r = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			r.readLine();
			
		} catch (MalformedURLException e) {
			usage();
		} catch (IOException e) {
			messages.appendText("Unusable world file");
		}
	}

	/**
	 * Gets information about the world and all hexes.
	 * @return Information needed to visually display
	 * the world with all critters, food and rocks.
	 * Also includes information about how the world in running.
	 */
	public WorldInfoBundle getWorld() {
		return getWorld(0);
	}
	
	/**
	 * Gets all information about the world that it is authorized
	 * to access, but gets information only about hexes that have
	 * been changed sine {@code updateSince}
	 * @param updateSince the version number of the world that is
	 * compared to the current one. 
	 * @return  The differences between current world and the world
	 * with the specified version number.
	 */
	public WorldInfoBundle getWorld(int updateSince) {
		try {
			URL newURL = new URL(url + "/world?session_id=" + sessionId + "&update_since=" + updateSince);
			HttpURLConnection connection = (HttpURLConnection) newURL.openConnection();
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestMethod("GET");
			connection.connect();
			
			BufferedReader r = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			WorldInfoBundle bundle = gson.fromJson(r, WorldInfoBundle.class);
			return bundle;
		} catch (MalformedURLException e) {
			usage();
		} catch (IOException e) {
			messages.appendText("Add a world\n");
		}
		return null;
	}

	/**
	 * Adds one critter at row, col OR multiple critters at random locations. 
	 * Requires that row and col will are -1 if creating multiple critters.
	 * and num is -1 if creating one critter at row, col 
	 * @param species  Name of critter(s)
	 * @param program  Program as defined by grammar
	 * @param mem   mem array of critter's attributes
	 * @param row  row to insert critter, -1 is no row
	 * @param col  col to insert critter, -1 is no col
	 * @param num number of critters to make
	 */
	public void addCritter(String species, String program, int[] mem, int row, int col, int num) {
		try {
			URL newURL = new URL(url + "/critters?session_id=" + sessionId);
			HttpURLConnection connection = (HttpURLConnection) newURL.openConnection();
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true); // send a POST message
			connection.setRequestMethod("POST");

			PrintWriter w = new PrintWriter(connection.getOutputStream());
			MakeCritterBundle bundle;
			if (num == -1)
				bundle = new MakeCritterBundle(species, program, mem, row, col);
			else
				bundle = new MakeCritterBundle(species, program, mem, num);
			w.println(gson.toJson(bundle, MakeCritterBundle.class));
			w.flush();
			BufferedReader r = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			if (connection.getResponseCode() == 401)
				messages.appendText(r.readLine());
			else {
				CrittersResponseBundle response = gson.fromJson(r, CrittersResponseBundle.class);
				int count = 0;
				for (int i = 0; i < response.ids().length; i++) {
					if (response.ids()[i] != -1)
						count++;
				}
				messages.appendText("Successfully loaded " + (count) + "/" + response.ids().length + " "
						+ response.species_id + " critters.\n");
			}
		} catch (MalformedURLException e) {
			usage();
		} catch (IOException e) {
			messages.appendText("Could not use critter defintion in file\n");
		}
	}

	/**
	 * Steps the world {@code count} turns for each critter
	 * @param count the number of steps to increase timestep by
	 * Usually = 1
	 */
	public void step(int count) {
		try {
			URL newURL = new URL(url + "/step?session_id=" + sessionId);
			HttpURLConnection connection = (HttpURLConnection) newURL.openConnection();
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			
			PrintWriter w = new PrintWriter(connection.getOutputStream());
			w.write(gson.toJson(new StepRequestBundle(count)));
			w.flush();
			
			BufferedReader r = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			messages.appendText(r.readLine() + "\n");
			
		} catch (MalformedURLException e) {
			usage();
		} catch (IOException e) {
			messages.appendText("Failed to step the world\n");
		}
	}
	
	/**
	 * Changes the rate that the simulation progresses automatically.
	 * @param rate The desired rate, in steps per second.
	 * 0 means the simulation will stop.
	 */
	public void changeRate(float rate) {
		try {
			URL newURL = new URL(url + "/run?session_id=" + sessionId);
			HttpURLConnection connection = (HttpURLConnection) newURL.openConnection();
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			
			PrintWriter w = new PrintWriter(connection.getOutputStream());
			w.write(gson.toJson(new RateChangeBundle(rate)));
			w.flush();
			
			if (connection.getResponseCode() == 200)
				messages.appendText("Successfully changed rate to " + rate + " steps per second.\n");
			else
				messages.appendText("Failed to change step rate.\n");
			
		} catch (MalformedURLException e) {
			usage();
		} catch (IOException e) {
			messages.appendText("Failed to change step rate.\n");
		}
	}
	
	/**
	 * Tries to login to server
	 * @param level either "read", "write" or "admin".
	 * 		determines access to info/commands
	 * @param password  password associated with desired access
	 * @return true if user sucessfully logged in.  False otherwise.
	 */
	public boolean login(String level, String password) {
		try {
			URL newURL = new URL(url + "/login");
			HttpURLConnection connection = (HttpURLConnection) newURL.openConnection();
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			
			PrintWriter w = new PrintWriter(connection.getOutputStream());
			w.write(gson.toJson(new LoginBundle(level, password)));
			w.flush();
			
			BufferedReader r = new BufferedReader( new InputStreamReader(connection.getInputStream()));
			if (connection.getResponseCode() == 200) {
				SessionIdBundle bundle = gson.fromJson(r, SessionIdBundle.class);
				sessionId = bundle.id;
				return true;
			}
			else
				return false;
		} catch (MalformedURLException e) {
			usage();
		} catch (IOException e) {
			return false;
		}
		return false;
	}
	
	//for sending login info
	private class LoginBundle {
		String level;
		String password;
		
		public LoginBundle(String level, String password) {
			this.level = level;
			this.password = password;
		}
	}
	
	//receiving session id after login
	private class SessionIdBundle {
		String id;
		
		public SessionIdBundle(String id) {
			this.id = id;
		}
	}
	
	//for seding info about making a world
	private class MakeWorldBundle {
		private String description;

		public MakeWorldBundle(String description) {
			this.description = description;
		}
	}

	//for sending info  about making a critter/critters
	private class MakeCritterBundle {
		private String speciesId;
		private String program;
		private int[] mem;
		private LocationBundle[] positions;
		private int num;

		// Make one critter at row, col
		public MakeCritterBundle(String id, String program, int[] mem, int row, int col) {
			speciesId = id;
			this.program = program;
			this.mem = mem;
			positions = new LocationBundle[1];
			positions[0] = new LocationBundle(row, col);
		}

		// Make {@code num} critters at random locations
		public MakeCritterBundle(String id, String program, int[] mem, int num) {
			speciesId = id;
			this.program = program;
			this.mem = mem;
			positions = null;
			this.num = num;
		}
	}

	//describe a location of a hex
	private class LocationBundle {
		private int row;
		private int col;

		public LocationBundle(int r, int c) {
			row = r;
			col = c;
		}
	}

	//stores info for stepping world
	private class StepRequestBundle {
		private int count;
		
		public StepRequestBundle(int count) {
			this.count = count;
		}
	}
	
	private class RateChangeBundle {
		private float rate;
		
		public RateChangeBundle(float rate) {
			this.rate = rate;
		}
	}
	
	//stores critter's ids and names
	private class CrittersResponseBundle {
		private String species_id;
		private int[] ids;

		public String speciesId() {
			return species_id;
		}

		public int[] ids() {
			return ids;
		}
	}

	private void usage() {
		System.err.println("Usage: MyClient " + url);
		System.exit(1);
	}

}
