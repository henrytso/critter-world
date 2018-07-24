package ast;

import parse.TokenType;

/**
 * A representation of a critter command.
 */
public abstract class Command extends BinaryNode {

	/**
	 * Constructs a Command that is either an action or update with a 
	 * 		left and right child and type
	 * @param left
	 * 			Left child to attach
	 * @param tt
	 * 			Type of command (update or action)
	 * @param right
	 * 			Right child to attach
	 */
	public Command(BinaryNode left, TokenType tt, BinaryNode right) {
		super(left, tt, right);
	}
	
}
