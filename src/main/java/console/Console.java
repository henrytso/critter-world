package console;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import com.sun.org.apache.xalan.internal.xsltc.compiler.Constants;

import simulator.Critter;
import simulator.Hex;
import simulator.Simulator;
import simulator.SimulatorImpl;

/**
 * The console user interface for Assignment 5.
 */
public class Console {
	private Scanner scan;
	public boolean done;
	public PrintStream out;
	
	/* =========================== */
	/* DO NOT EDIT ABOVE THIS LINE */
	/* (except imports...) */
	/* =========================== */
	
	private Simulator world;
	private char[][] asciiMap;
	
	/**
	 * Starts new random world simulation.
	 */
	public void newWorld() {
		world = new SimulatorImpl();
		createAscii();
	}
	
	/**
	 * Starts new simulation with world specified in filename.
	 *
	 * @param filename
	 *        File from which the world is loaded from
	 */
	public void loadWorld(String filename) {
		try {
			world = new SimulatorImpl(filename);
			createAscii();
		} catch (Exception e) {
			System.out.println("File not valid");
			e.printStackTrace();
		}
	}
	
	/**
	 * Initializes the asciiMap instance field to an array of correct height
	 */
	public void createAscii() {
		// Calculate height of ASCII map
		int r = 0;
		while (2 * r < 2 * world.getWorld()[0].length - world.getWorld().length)
			r++;
		
		asciiMap = new char[2 * r - 1][world.getWorld().length];
		for (int i = 0; i < asciiMap.length; i++)
			for (int j = 0; j < asciiMap[0].length; j++)
				asciiMap[i][j] = ' ';
	}
	
	/**
	 * Loads critter definition from filename and randomly places n critters
	 * with that definition into the world.
	 *
	 * @param filename
	 *        Critter program file
	 * @param n
	 *        Number of critters with that program to load
	 */
	public void loadCritters(String filename, int n) {
		try {
		if (n > world.emptyHexes())
			System.out.println("Not enough spaces to add " + n + " critters!");
		else {
			for (int i = 0; i < n; i++) {
				if (!world.loadCritter(filename))
					i--;
			}
		}
		} catch (NumberFormatException e) {//Exception e) {
			System.out.println("File not valid");
			e.printStackTrace();
		}
	}
	
	/**
	 * Advances the world by n time steps.
	 *
	 * @param n
	 *        Steps to advance
	 */
	public void advanceTime(int n) {
		world.advanceTime(n);
	}
	
	/**
	 * Prints current time step, number of critters, and world map of the
	 * simulation.
	 */
	public void worldInfo() {
		
		worldInfo(world.getTimeStep(), world.numCritters());
		
		for (int j = 0; j < asciiMap[0].length; j++) {
			int c = j;
			int r = (c / 2) + (c % 2);
			int i = (asciiMap.length - 1) - (c % 2);
			while (i >= 0) {
				asciiMap[i][j] = setChar(c, r);
				i = i - 2;
				r++;
			}
		}
		
		for (int i = 0; i < asciiMap.length; i++) {
			for (int j = 0; j < asciiMap[0].length; j++) {
				System.out.print(asciiMap[i][j]);
			}
			
			System.out.println();
		}
		
	}
	
	/**
	 * Returns the char in the asciiMap to the correct char based on hex world
	 * 
	 * @param col
	 *        Column of hex
	 * @param row
	 *        Row of hex
	 * @return '-' if empty '#' if rock 'F' if food <dir> if critter
	 */
	private char setChar(int col, int row) {
		Hex h = world.getHex(col, row);
		
		if (h.isEmpty())
			return '-';
		if (h.isRock())
			return '#';
		if (h.isFood())
			return 'F';
		if (h.isCritter())
			return (char) (h.getCritter().getDir() + 48); // ASCII value of int
			
		return 'G'; // Shouldn't ever be G
	}
	
	/**
	 * Prints description of the contents of hex (c,r).
	 * 
	 * @param c
	 *        column of hex
	 * @param r
	 *        row of hex
	 */
	public void hexInfo(int c, int r) {
		Hex h = world.getHex(c, r);
		
		if (h.isEmpty())
			terrainInfo(0);
		if (h.isRock())
			terrainInfo(-1);
		if (h.isFood())
			terrainInfo(-h.getFood() - 1);
		if (h.isCritter()) {
			Critter cr = h.getCritter();
			critterInfo(cr.getSpecies(), cr.getMem(),
					cr.getProgram().toString(), cr.getLastRule().toString());
		}
	}
	
	/* =========================== */
	/* DO NOT EDIT BELOW THIS LINE */
	/* =========================== */
	
	/**
	 * Be sure to call this function, we will override it to grade.
	 *
	 * @param numSteps
	 *        The number of steps that have passed in the world.
	 * @param crittersAlive
	 *        The number of critters currently alive.
	 */
	public void worldInfo(int numSteps, int crittersAlive) {
		out.println("steps: " + numSteps);
		out.println("critters: " + crittersAlive);
	}
	
	/**
	 * Be sure to call this function, we will override it to grade.
	 *
	 * @param species
	 *        The species of the critter.
	 * @param mem
	 *        The memory of the critter.
	 * @param program
	 *        The program of the critter pretty printed as a String. This should
	 *        be able to be parsed back to the same AST.
	 * @param lastrule
	 *        The last rule executed by the critter pretty printed as a String.
	 *        This should be able to be parsed back to the same AST. If no rule
	 *        has been executed, this parameter should be null.
	 */
	protected void critterInfo(String species, int[] mem, String program,
			String lastrule) {
		out.println("Species: " + species);
		StringBuilder sbmem = new StringBuilder();
		for (int i : mem) {
			sbmem.append(" ").append(i);
		}
		out.println("Memory:" + sbmem.toString());
		out.println("Program: " + program);
		out.println("Last rule: " + lastrule);
	}
	
	/**
	 * Be sure to call this function, we will override it to grade.
	 *
	 * @param terrain
	 *        0 is empty, -1 is rock, -X is (X-1) food
	 */
	protected void terrainInfo(int terrain) {
		if (terrain == 0) {
			out.println("Empty");
		} else if (terrain == -1) {
			out.println("Rock");
		} else {
			out.println("Food: " + (-terrain - 1));
		}
	}
	
	/**
	 * Prints a list of possible commands to the standard output.
	 */
	public void printHelp() {
		out.println("new: start a new simulation with a random world");
		out.println("load <world_file>: start a new simulation with "
				+ "the world loaded from world_file");
		out.println("critters <critter_file> <n>: add n critters "
				+ "defined by critter_file randomly into the world");
		out.println("step <n>: advance the world by n timesteps");
		out.println("info: print current timestep, number of critters "
				+ "living, and map of world");
		out.println(
				"hex <c> <r>: print contents of hex " + "at column c, row r");
		out.println("exit: exit the program");
	}
	
	/**
	 * Constructs a new Console capable of reading a given input.
	 */
	public Console(InputStream in, PrintStream out) {
		this.out = out;
		scan = new Scanner(in);
		done = false;
	}
	
	/**
	 * Constructs a new Console capable of reading the standard input.
	 */
	public Console() {
		this(System.in, System.out);
	}
	
	/**
	 * Processes a single console command provided by the user.
	 */
	public void handleCommand() {
		out.print("Enter a command or \"help\" for a list of commands.\n> ");
		String command = scan.next();
		switch (command) {
		case "new": {
			newWorld();
			break;
		}
		case "load": {
			String filename = scan.next();
			loadWorld(filename);
			break;
		}
		case "critters": {
			String filename = scan.next();
			int n = scan.nextInt();
			loadCritters(filename, n);
			break;
		}
		case "step": {
			int n = scan.nextInt();
			advanceTime(n);
			break;
		}
		case "info": {
			worldInfo();
			break;
		}
		case "hex": {
			int c = scan.nextInt();
			int r = scan.nextInt();
			hexInfo(c, r);
			break;
		}
		case "help": {
			printHelp();
			break;
		}
		case "exit": {
			done = true;
			break;
		}
		default:
			out.println(command + " is not a valid command.");
		}
	}
	
	public static void main(String[] args) {
		Console console = new Console();
		while (!console.done) {
			console.handleCommand();
		}
	}
	
}
