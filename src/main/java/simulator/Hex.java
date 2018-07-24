package simulator;

public class Hex {
	
	private boolean rock;
	private int food;
	private Critter critter;
	
	/**
	 * Constructs an empty Hex
	 */
	public Hex() {
		//represents no food, critter, rock
		rock = false;
		food = 0;
		critter = null;
	}
	
	/**
	 * Adds a rock on this Hex
	 */
	public void addRock() {
		rock = true;
	}
	
	/**
	 * Adds {@code amount} food to this hex
	 * @param amount
	 * 		Food to add
	 */
	public void addFood(int amount) {
		food += amount;
	}
	
	/**
	 * Places a critter on this hex
	 * @param c
	 * 		Critter program to place here
	 */
	public void addCritter(Critter c) {
		critter = c;
	}
	
	/**
	 * Returns food value of this hex
	 * @return Amount of food
	 */
	public int getFood() {
		return food;
	}
	
	/**
	 * Returns critter on this hex
	 * @return Critter on this hex
	 */
	public Critter getCritter() {
		return critter;
	}
	
	/**
	 * Returns whether hex is empty
	 * @return Whether is empty
	 */
	public boolean isEmpty() {
		if (!rock && food == 0 && critter == null)
			return true;
		return false;
	}
	
	/**
	 * Returns whether hex is rock
	 * @return True if is rock
	 */
	public boolean isRock() {
		return rock;
	}
	
	/**
	 * Returns whether hex has food
	 * @return True if has food
	 */
	public boolean isFood() {
		return food > 0;
	}
	
	/**
	 * Returns whether hex is occupied by a critter
	 * @return True if critter is present
	 */
	public boolean isCritter() {
		return critter != null;
	}
	
}
