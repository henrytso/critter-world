package simulator;

public interface Simulator {
	
	/**
	 * Returns the hex map
	 * 
	 * @return The hex map
	 */
	public Hex[][] getWorld();
	
	/**
	 * Loads a critter from {@code file} to a random hex in the world
	 * 
	 * @param file
	 *        Critter program file
	 * @return Whether critter was successfully placed
	 */
	public boolean loadCritter(String file);
	
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
	public boolean loadCritter(String file, int col, int row, int dir);
	
	/**
	 * Returns if the hex is empty (no rock, no food, no critter)
	 * 
	 * @param col
	 *            Column of hex
	 * @param row
	 *            Row of hex
	 * @return Whether hex is empty
	 */
	public boolean isEmpty(int col, int row);
	
	/**
	 * Advances time by {@code n} steps for each critter in world
	 * 
	 * @param n
	 *        Steps to advance
	 */
	public void advanceTime(int n);
	
	/**
	 * Returns current time step
	 * 
	 * @return Current time step
	 */
	public int getTimeStep();
	
	/**
	 * Returns the number of critters alive in this world.
	 * 
	 * @return the number of critters.
	 */
	public int numCritters();
	
	/**
	 * Returns number of empty hexes in the world
	 * 
	 * @return Number of empty hexes
	 */
	public int emptyHexes();
	
	/**
	 * Returns if the hex is in bounds
	 * 
	 * @param col
	 *        Column of hex
	 * @param row
	 *        Row of hex
	 * @return Whether hex is in bounds
	 */
	public boolean isInBounds(int col, int row);
	
	/**
	 * Returns the hex at specified column and row
	 * 
	 * @param col
	 *        Column of hex
	 * @param row
	 *        Row of hex
	 * @return Hex at the specified location.
	 */
	public Hex getHex(int col, int row);
}
