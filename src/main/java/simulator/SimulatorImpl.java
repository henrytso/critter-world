package simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import ast.Program;
import ast.ProgramImpl;
import ast.Rule;
import interpret.Outcome;
import parse.Constants;
import parse.Parser;
import parse.ParserFactory;
import parse.SpecParser;

public class SimulatorImpl implements Simulator {
	protected String name; //Was private
	protected Hex[][] world; //Was private
	protected ArrayList<Critter> critters; //Was private
	protected HashMap<String, Double> constants;
	protected int timeStep;
	String filepath;
	
	/**
	 * Constructs a new Simulator with world of the size from constants.txt with
	 * 1/8 of the world being rocks and no food.
	 */
	public SimulatorImpl() {
		critters = new ArrayList<Critter>();
		constants = new Constants().getConstants();
		int col = constants.get("COLUMNS").intValue();
		int row = constants.get("ROWS").intValue();
		world = new Hex[col][row];
		for (int i = 0; i < col; i++)
			for (int j = 0; j < row; j++)
				world[i][j] = new Hex();
		timeStep = 0;
		
		int numRocks = (col * row) / 8; // 1/8 of world will be rocky
		Random r = new Random();
		for (int i = 0; i < numRocks; i++)
			addRock(r.nextInt(col), r.nextInt(row));
		
		name = "Gates G01";
	}
	
	/**
	 * Blank hex map of cols x rows
	 * @param cols
	 * 		Number of col
	 * @param rows
	 * 		Number of rows
	 */
	public SimulatorImpl(int col, int row) {
		critters = new ArrayList<Critter>();
		
		world = new Hex[col][row];
		for (int i = 0; i < col; i++)
			for (int j = 0; j < row; j++)
				world[i][j] = new Hex();
		timeStep = 0;
		
		name = "krusty krab";
	}
	
	/**
	 * Constructs a Simulator based on info of the world file {@code file}
	 * 
	 * @param file
	 *        File on which the world is based
	 */
	public SimulatorImpl(String file) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			critters = new ArrayList<Critter>();
			constants = new Constants().getConstants();
			// figuring out folder for loading critters
			// String[] paths = file.split("\\");
			// filepath = "";
			// for (int i = 0; i < paths.length - 1; i++) {
			// filepath += paths[i];
			// }
			// filepath = filepath + "\\";
			
			int a = file.length() - 1;
			while (file.charAt(a) != '\\')
				a--;
			filepath = file.substring(0, a + 1);
			
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
					String filename = readWord(br);
					col = Integer.parseInt(readWord(br));
					row = Integer.parseInt(readWord(br));
					int dir = Integer.parseInt(readWord(br));
					loadCritter(filename, col, row, dir);
				} else if (word.equals("//")) {
					br.readLine(); // Comment lines
				}
			}
			
		} catch (IOException e) {
			System.out.println("World file is incorrect");
		} catch(NumberFormatException n) {
			System.out.println("World file is incorrect");
		}
		
	}
	
	/**
	 * Advances the BufferedReader by one word and returns the word read
	 * 
	 * @param br
	 *        BufferedReader to use
	 * @return The word that was read
	 */
	public String readWord(BufferedReader br) {
		char c;
		String word = "";
		try {
			while (!Character.isWhitespace((c = (char) br.read())) && c!= 65535) {
				word+= c;
			}
		} catch (IOException e) {
			return null;
		}
		return word;
	}
	
	/**
	 * Adds a rock to the world at specified location
	 * 
	 * @param col
	 *        Column of rock
	 * @param row
	 *        Row of rock
	 */
	public boolean addRock(int col, int row) {
		if (isInBounds(col, row) && isEmpty(col, row)) {
			world[col][row].addRock();
			return true;
		}
		
		return false;
	}
	
	/**
	 * Adds food to the world at specified location
	 * 
	 * @param col
	 *        Column of food
	 * @param row
	 *        Row of food
	 * @param amount
	 *        Amount of food to add
	 */
	public boolean addFood(int col, int row, int amount) {
		if (isInBounds(col, row) && isEmpty(col, row)) {
			world[col][row].addFood(amount);
			return true;
		}
		
		return false;
	}
	
	public void addCritter(int col, int row, Critter c) {
		if (isInBounds(col, row) && isEmpty(col, row)) {
			world[col][row].addCritter(c);
			critters.add(c);
		}
	}
	
	/**
	 * Loads a critter from {@code file} to a random hex in the world
	 * 
	 * @param file
	 *        Critter program file
	 * @return Whether critter was successfully placed
	 */
	public boolean loadCritter(String file) {
		Random rand = new Random();
		int col = -1;
		int row = -1;
		while (!isInBounds(col, row)) {
			col = rand.nextInt(constants.get("COLUMNS").intValue());
			row = rand.nextInt(constants.get("ROWS").intValue());
		}
		int dir = rand.nextInt(6);
		return loadCritter(file, col, row, dir);
	}
	
	/**
	 * Loads critter from {@code file} to hex at (col, row) facing {@code dir}
	 * If a world was loaded, the filename has to be just the file it has to be
	 * in the same directory as the world file.
	 * 
	 * @param file
	 *        Critter program file
	 * @param col
	 *        Column of hex
	 * @param row
	 *        Row of hex
	 * @param dir
	 *        Direction critter is facing
	 * @return Whether critter was successfully placed
	 */
	public boolean loadCritter(String file, int col, int row, int dir) {
		try {
			if (filepath != null)
				file = filepath + file;
			FileReader fr;
			
			try {
				fr = new FileReader(file);
			} catch (FileNotFoundException e) {
				fr = new FileReader(file.substring(filepath.length()));
			}
			
			BufferedReader br = new BufferedReader(fr);
			readWord(br); // should be "species:"
			String species = br.readLine();
			SpecParser sp = new SpecParser();
			Reader r = sp.parseSpecs(br);
			int[] mem = sp.getAttributes();
			Parser parser = ParserFactory.getParser();
			Program p = parser.parse(r);
			if(p == null)
				return false;
			
			Critter c = new Critter(p, species, col, row, mem, dir, this);
			
			// check if is in bounds and is empty
			if (isInBounds(col, row) && isEmpty(col, row)) {
				world[col][row].addCritter(c);
				critters.add(c);
				return true;
			}
		} catch (FileNotFoundException e) {
			System.out.println("Critter program file not found.");
			return false; // Don't want to keep having error.
		} catch (IOException e) {
			System.out.println("File is not valid - IO Exception");
			return false; // Don't want to keep having error.
		}
		return false;
	}
	
	/**
	 * Advances time by {@code n} steps for each critter in world
	 * 
	 * @param n
	 *        Steps to advance
	 */
	public void advanceTime(int n) {
		
		for (int i = 0; i < n; i++) {
			ArrayList<Critter> matingCritters = new ArrayList<Critter>();
			
			for (int j = critters.size() - 1; j >= 0; j--) {
				Critter c = critters.get(j);
				Outcome o = c.advanceTime();
				if (o.isMate())
					matingCritters.add(c);
			}
		
			mateCritters(matingCritters);
		}
		
		timeStep += n;
	}
	
	public void mateCritters(ArrayList<Critter> matingCritters) {
		for (int i = 0; i < matingCritters.size(); i++) {
			Critter c1 = matingCritters.get(i);
			Critter c2 = c1.hexAtDir(c1.getDir()).getCritter();
			
			if (c2 != null) {
				if (matingCritters.contains(c2)
						&& c2.getDir() == (c1.getDir() + 3) % 6) {
					if (mate(c1, c2)) {
						c1.decEnergy(c1.complexity()
								* constants.get("MATE_COST").intValue());
						c2.decEnergy(c2.complexity()
								* constants.get("MATE_COST").intValue());
						
					} else {
						c1.decEnergy(c1.getMem()[3]);
						c2.decEnergy(c2.getMem()[3]);
					}
					matingCritters.remove(i);
					i--;
				}
			}
		}
	}
	
	/**
	 * Officially mates two critters (checked beforehand that mating is valid)
	 * 
	 * @param c1
	 *        First parent
	 * @param c2
	 *        Second parent
	 * @return
	 */
	public boolean mate(Critter c1, Critter c2) {
		Critter baby;
		
		ArrayList<Rule> rules1 = c1.program.getRules();
		ArrayList<Rule> rules2 = c2.program.getRules();
		
		// Rules
		Program babyPr = new ProgramImpl();
		Random r = new Random();
		int rulesSize = r.nextInt(2) == 0 ? rules1.size() : rules2.size();
		
		int min = Math.min(rules1.size(), rules2.size());
		
		for (int i = 0; i < min; i++) {
			babyPr.add(r.nextInt(2) == 0 ? rules1.get(i) : rules2.get(i));
		}
		
		if (rulesSize > rules1.size()) {
			for (int i = min; i < rules1.size(); i++) {
				babyPr.add(rules1.get(i));
			}
		} else {
			for (int i = min; i < rules2.size(); i++) {
				babyPr.add(rules2.get(i));
			}
		}
		
		// Attributes
		int babyMemSize = r.nextInt(2) == 0 ? c1.mem[0] : c2.mem[0];
		int[] babyMem = new int[babyMemSize];
		babyMem[0] = babyMemSize;
		babyMem[1] = r.nextInt(2) == 0 ? c1.mem[1] : c2.mem[1];
		babyMem[2] = r.nextInt(2) == 0 ? c1.mem[2] : c2.mem[2];
		
		babyMem[3] = 1;
		babyMem[4] = constants.get("INITIAL_ENERGY").intValue();
		babyMem[6] = 0;
		babyMem[7] = 0;
		
		baby = new Critter(babyPr, c1.getSpecies(), -1, -1, babyMem, 0, this);
		
		// Location
		if ((c1.nearby((c1.getDir() + 3) % 6) == 0)) {
			birthBehind(c1, baby);
			return true;
		} else if (c2.nearby((c2.getDir() + 3) % 6) == 0) {
			birthBehind(c2, baby);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Places a birthed baby behind critter {@code parent}
	 * 
	 * @param parent
	 *        Parent behind with baby is born
	 * @param baby
	 *        Baby critter to place behind the parent
	 */
	private void birthBehind(Critter parent, Critter baby) {
		int newCol = parent.col;
		int newRow = parent.row;
		
		switch (parent.getDir()) {
		case 0:
			newRow--;
			break;
		case 1:
			newCol--;
			newRow--;
			break;
		case 2:
			newCol--;
			break;
		case 3:
			newRow++;
			break;
		case 4:
			newCol++;
			newRow++;
			break;
		default:
			newCol++;
			break;
		}
		
		baby.col = newCol;
		baby.row = newRow;
		
		addCritter(newCol, newRow, baby);
	}
	
	/**
	 * Causes a critter {@code c} to die and removes it from the arraylist
	 * 
	 * @param c
	 *        Critter that dies
	 */
	public void die(Critter c) {
		world[c.col][c.row].addCritter(null);
		int food = c.getMem()[3] * constants.get("FOOD_PER_SIZE").intValue();
		world[c.col][c.row].addFood(food);
		critters.remove(c);
	}
	
	/**
	 * Returns the number of critters alive in this world.
	 * 
	 * @return the number of critters.
	 */
	public int numCritters() {
		synchronized (this) {
			return critters.size();
		}
	}
	
	/**
	 * Returns the hex at specified column and row
	 * 
	 * @param col
	 *        Column of hex
	 * @param row
	 *        Row of hex
	 * @return Hex at the specified location.
	 */
	public Hex getHex(int col, int row) {
		return world[col][row];
	}
	
	/**
	 * Returns the hex map
	 * 
	 * @return The hex map
	 */
	public Hex[][] getWorld() {
		return world;
	}
	
	/**
	 * Returns current time step
	 * 
	 * @return Current time step
	 */
	public int getTimeStep() {
		return timeStep;
	}
	
	/**
	 * Returns array list of living critters
	 * @return Arraylist of living critters
	 */
	public ArrayList<Critter> getCritters() {
		return critters;
	}
	
	/**
	 * Returns if the hex is in bounds
	 * 
	 * @param col
	 *        Column of hex
	 * @param row
	 *        Row of hex
	 * @return Whether hex is in bounds
	 */
	public boolean isInBounds(int col, int row) {
		return col < world.length && row < world[0].length
				&& 2 * row - col < 2 * world[0].length - world.length
				&& 2 * row - col >= 0 && row >= 0 && col >= 0;
	}
	
	/**
	 * Returns if the hex is empty (no rock, no food, no critter)
	 * 
	 * @param col
	 *        Column of hex
	 * @param row
	 *        Row of hex
	 * @return Whether hex is empty
	 */
	public boolean isEmpty(int col, int row) {
		return getHex(col, row).isEmpty();
	}
	
	/**
	 * Returns number of empty hexes in the world
	 * 
	 * @return Number of empty hexes
	 */
	public int emptyHexes() {
		int count = 0;
		for (int i = 0; i < world.length; i++)
			for (int j = (i / 2) + (i % 2); 2 * j - i < 2 * world[0].length
					- world.length; j++) {
				if (world[i][j].isEmpty())
					count++;
			}
		return count;
	}
	
	public String getName() {
		return name;
	}
}
