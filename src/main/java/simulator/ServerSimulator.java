package simulator;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Random;

import ast.Program;
import interpret.Outcome;
import parse.Constants;
import parse.Parser;
import parse.ParserFactory;

/**
 * A model of the critter world.  This is a version of 
 * the simulator that works with a server to send out
 * information.
 */
public class ServerSimulator extends SimulatorImpl {

	ArrayList<Critter> deadCritters; //pointer to Server's deadcritter list
	int version; //version number

	/**
	 * Makes a new world, size specified in constants and populated
	 * randomly with rocks
	 * @param deadCritters  the list to add to when critters die
	 * @param oldVersion  the version number of the world that this
	 * is replacing.  If this is the first world, oldVersion = 0
	 */
	public ServerSimulator(ArrayList<Critter> deadCritters, int oldVersion) {
		super();
		this.deadCritters = deadCritters;
		version = oldVersion + 1;
	}

	/**
	 * Creates a new world according to the specificied description (worldDef).
	 * Ignores any critters 
	 * @param worldDef  A String containing the sizes and inhabitants of world
	 * @param deadCritters  the list to add to when critters die
	 * @param oldVersion  the version number of the world that this
	 * is replacing.  If this is the first world, oldVersion = 0
	 */
	public ServerSimulator(String worldDef, ArrayList<Critter> deadCritters, int oldVersion) {

		Reader r = new InputStreamReader(new ByteArrayInputStream(worldDef.getBytes()));
		BufferedReader br = new BufferedReader(r);

		critters = new ArrayList<Critter>();
		constants = new Constants().getConstants();

		try {
			readWord(br); // should be "name"
			String name = br.readLine();
			readWord(br); // should be "size"
			int col = Integer.parseInt(readWord(br));
			int row = Integer.parseInt(readWord(br));

			this.name = name;
			timeStep = 0;
			world = new Hex[col][row];
			for (int i = 0; i < col; i++)
				for (int j = 0; j < row; j++)
					world[i][j] = new Hex();

			while (br.ready()) {
				String word = readWord(br);
				if (word.equals("rock")) {
					col = Integer.parseInt(readWord(br));
					row = Integer.parseInt(readWord(br));
					addRock(col, row);
				} else if (word.equals("food")) {
					col = Integer.parseInt(readWord(br));
					row = Integer.parseInt(readWord(br));
					int amount = Integer.parseInt(readWord(br));
					addFood(col, row, amount);
				} else if (word.equals("critter")) {
					readWord(br); // filename
					readWord(br); // col
					readWord(br); // row
					readWord(br); // dir
				} else if (word.equals("//")) {
					br.readLine(); // Comment lines
				}
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException();
		} catch (IOException e) {
			throw new IllegalArgumentException();
		}

		this.deadCritters = deadCritters;
		version = oldVersion + 1;
	}

	/**
	 * Advances time n steps and updates log by adding version
	 * numbers of new changes to hexes that were changed.
	 * @param n number of steps
	 * @param log log of the last version in which each hex was changed
	 */
	public void advanceTime(int n, Integer[][] log) {

		for (int i = 0; i < n; i++) {
			ArrayList<Critter> matingCritters = new ArrayList<Critter>();
			version++;
			
			for (int j = critters.size() - 1; j >= 0; j--) {
				Critter c = critters.get(j);
				log[c.col][c.row] = version;

				Outcome o = c.advanceTime();
				if (o.isMate())
					matingCritters.add(c);

				if (o.changed()) {
					if(isInBounds(o.getCol(), o.getRow()))
						log[o.getCol()][o.getRow()] = version;
				}

			}

			mateCritters(matingCritters);
			timeStep++;
		}

	}

	/**
	 * Add the specified critter to the simulation in a random location
	 * @param program 
	 * 			Critter's program
	 * @param species
	 * 			Name of species
	 * @param mem
	 * 			mem array of critter
	 * @param critterId
	 * 			Id that serve assigned critter
	 * @param createId
	 * 			sessionId of user who created critter
	 * @return
	 * 			whether or not critter was added
	 */	
	public boolean loadCritter(String program, String species, int[] mem, int critterId, int createId) {
		Random rand = new Random();
		int col = -1;
		int row = -1;
		while (!isInBounds(col, row)) {
			col = rand.nextInt(world.length);
			row = rand.nextInt(world[0].length);
		}
		int dir = rand.nextInt(6);
		version++;
		return loadCritter(program, col, row, dir, species, mem, critterId, createId);
	}

	/**
	 * Add the specified critter to the simulation at specified location
	 * @param program 
	 * 			Critter's program
	 * @param col  column
	 * @param row  row
	 * @param species
	 * 			Name of species
	 * @param mem
	 * 			mem array of critter
	 * @param critterId
	 * 			Id that serve assigned critter
	 * @param createId
	 * 			sessionId of user who created critter
	 * @return
	 * 			whether or not critter was added
	 */	
	public boolean loadCritter(String program, int col, int row, int dir, String species, int[] mem, int critId,
			int createId) {
		Reader r = new InputStreamReader(new ByteArrayInputStream(program.getBytes()));
		BufferedReader br = new BufferedReader(r);

		Parser parser = ParserFactory.getParser();
		Program p = parser.parse(br);
		if (p == null)
			return false;

		Critter c = new Critter(p, species, col, row, mem, dir, this);
		c.setIds(critId, createId);

		// check if is in bounds and is empty
		if (isInBounds(col, row) && isEmpty(col, row)) {
			world[col][row].addCritter(c);
			critters.add(c);
			version++;
			return true;
		}

		return false;
	}

	@Override
	public void die(Critter c) {
		super.die(c);
		deadCritters.add(c);
		version++;
	}
	
	/**
	 * @return version number of latest simulation
	 */
	public int version() {
		return version;
	}
	
	/**
	 * Returns a list of critters that have died
	 * @return dead critters list
	 */
	public ArrayList<Critter> getDeadCritters() {
		return deadCritters;
	}
}
