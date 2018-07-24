package ast;

import interpret.Interpreter;
import interpret.Outcome;
import parse.TokenType;

/**
 * A representation of any condition with OR
 */
public class ConditionNode extends BinaryNode implements Condition {

	private boolean braces;

	/**
	 * Create an AST representation of l op r.
	 * 
	 * @param l
	 * 			Left child node
	 * @param tt
	 * 			Token type of the ConditionNode
	 * @param r
	 * 			Right child node
	 */
	public ConditionNode(BinaryNode l, TokenType tt, BinaryNode r) {
		super(l, tt, r);
		braces = false;
	}

	/**
	 * Sets the flag denoting that this node has braces to true
	 */
	public void addBraces() {
		braces = true;
	}

	@Override
	public ConditionNode clone() {
		ConditionNode newCond = new ConditionNode((BinaryNode) left.clone(), tt, ((BinaryNode) right.clone()));
		if (braces)
			newCond.addBraces();
		return newCond;
	}
	
	@Override
	public Outcome critAccept(Interpreter i) {
//		boolean result = i.eval(this);
//		if(result)
//			return 1;
//		
//		return 0;
		return i.eval(this);
	}
	
	@Override
	public boolean mutateNode(Mutation m) {
		if (super.mutateNode(m))
			return true;
		if (m.equals(MutationFactory.getInsert())) {
			if (tt == TokenType.OR) {
				ConditionNode newCond = (ConditionNode) this.getSubtree();
				ConditionNode newCond2 = new ConditionNode(this, tt, newCond);
				newCond2.setParent(this.parent);
				
				if (parent instanceof Rule)
					((Rule) parent).addCond(newCond2);
				else if (this == ((BinaryNode) parent).left)
					((BinaryNode) parent).left = newCond2;
				else if (this == ((BinaryNode) parent).right)
					((BinaryNode) parent).right = newCond2;
				return true;
			}
		}
		return false;
	}

	@Override
	public ConditionNode generateSubtree() {
		if (left.hashCode() / 4 % 2 == 0)
			return (ConditionNode) left.generateSubtree();
		return (ConditionNode) right.generateSubtree();
	}

	@Override
	public StringBuilder prettyPrint(StringBuilder sb) {
		if (!braces)
			return super.prettyPrint(sb);

		sb.append("{");

		if (left != null)
			left.prettyPrint(sb);

		sb.append(" " + tt.toString() + " ");

		if (right != null)
			right.prettyPrint(sb);
		sb.append("}");

		return sb;
	}

	/**
	 * An enumeration of all possible binary condition operators.
	 */
	public enum Operator {
		OR, AND;
	}
}
