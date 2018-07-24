package ast;

import interpret.Interpreter;
import interpret.Outcome;
import parse.TokenType;

/**
 * A representation of a critter update.
 */
public class Update extends Command {

	/**
	 * Constructs an Update with a left MemNode child {@code left} and 
	 * 		right Expression child {@code right}
	 * @param left
	 * 		Left MemNode child
	 * @param right
	 * 		Right Expression child
	 */
	public Update(MemNode left, Expression right) {
		super(left, TokenType.ASSIGN, right);
	}
	
	@Override
	public Update clone() {
		return new Update((MemNode) left.clone(), (Expression) right.clone());
	}
	
	@Override
	public Outcome critAccept(Interpreter i) {
		return i.eval(this);
	}
	
	@Override
	public Update generateSubtree() {
		return new Update((MemNode) left.generateSubtree(), (Expression) right.generateSubtree());
	}
	
	@Override
	public boolean mutateNode(Mutation m) {
		if (super.mutateNode(m))
			return true;
		
		return false;
	}	
	
	@Override
	public boolean switchChildren() {
		if(right instanceof MemNode)
			return super.switchChildren();
		
		return false;
	}
}
