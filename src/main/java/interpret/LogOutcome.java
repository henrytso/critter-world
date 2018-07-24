package interpret;

public class LogOutcome implements Outcome {
	
	// Col and row of changed hex
	// Maximum one hex other than the current critter's could have changed
	// 		after a single action
	private int col;
	private int row;
	boolean mate;
	boolean action;
	int value;
	
	/**
	 * Constructs LogOutcome denoting no changed hex
	 */
	public LogOutcome() {
		col = -1;
		row = -1;
		mate = false;
	}
	
	/**
	 * Constructs LogOutcome denoting changed hex
	 * @param c
	 * 		Column of hex changed
	 * @param r
	 * 		Row of hex changed
	 */
	public LogOutcome(int c, int r) {
		col = c;
		row = r;
		mate = false;
	}
	
	/**
	 * Constructs a LogOutcome with changed hex and mate outcome
	 * @param c
	 * 		Column of changed hex
	 * @param r
	 * 		Row of changed hex
	 * @param mate
	 * 		Whether critter action was mate
	 */
	public LogOutcome(int c, int r, boolean mate) {
		col = c;
		row = r;
		this.mate = mate;
		action = mate;
	}
	
	/**
	 * Returns whether a hex other than the critter's
	 * original hex was changed
	 * @return
	 * 		Whether hex was changed
	 */
	public boolean changed() {
		return col != -1 && row != -1;
	}
	
	/**
	 * Returns column index of hex that changed
	 * @return
	 * 		column index of hex that changed.
	 * 		If no additional hex has changed it return -1;
	 */
	public int getCol() {
		return col;
	}
	
	/**
	 * Returns row index of hex that changed
	 * @return
	 * 		row index of hex that changed.
	 * 		If no additional hex has changed it return -1;
	 */
	public int getRow() {
		return row;
	}
	
	/**
	 * Returns whether outcome was mate
	 * @return Whether outcome was mate
	 */
	public boolean isMate() {
		return mate;
	}
	
	/**
	 * Returns whether outcome was an action
	 * @return Whether outcome was an action
	 */
	public boolean isAction() {
		return action;
	}
	
	/**
	 * Returns integer value of result.
	 * Requires that isAction() is false (value is meaningless for action outcome)
	 */
	public int value() {
		return value;
	}

	/**
	 * Sets the value of this outcome. 
	 */
	public void setValue(int i) {
		value = i;
	}

	/**
	 * Makes outcome either an action or not an action
	 * @param action 
	 * 			true, if outcome represents an action,
	 * 			false, if outcome is an update
	 */
	public void setAction(boolean action) {
		this.action = action;
	}
}