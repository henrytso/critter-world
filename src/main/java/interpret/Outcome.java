package interpret;

/**
 * An example interface for representing an outcome of interpreting
 * a critter program.
 */
public interface Outcome {
	
	/**
	 * Returns whether outcome is a MateOutcome
	 * @return Whether outcome is a MateOutcome
	 */
	public boolean isMate();
	
	/**
	 * Returns whether any additional hexes could
	 * have changed based on action.
	 * @return true if another hex was affected
	 */
	public boolean changed();
	
	/**
	 * Returns whether this outcome involved an action.
	 * @return true if this represents and action. False otherwise.
	 */
	public boolean isAction();
	
	/**
	 * Return the value of the outcome.  
	 * Used to evaluate conditions.  Will be 0 for all actions.
	 * @return value of outcome
	 */
	public int value();
	
	/**
	 * Changes the value of this outcome.
	 * @param i the new value
	 */
	public void setValue(int i);
	
	/**
	 * Sets this class to either an action or not an action
	 * @param action  true, if this class should represent and action
	 *       false, if this class is an update or integer.
	 */
	public void setAction(boolean action);
	
	/**
	 * Returns column of hex that changed
	 * @return -1 if no additional hex was changed
	 */
	public int getCol();
		
	/**
	 * Returns row of hex that changed
	 * @return -1 if no additional hex was changed
	 */
	public int getRow();
}
