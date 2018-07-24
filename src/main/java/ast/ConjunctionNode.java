package ast;

import parse.TokenType;

/**
 * A representation of any Conjuction with AND
 */
public class ConjunctionNode extends ConditionNode {

	/**
	 * Constructs a ConjunctionNode with a left and right child with the 
	 * 		TokenType as {@code tt}
	 * @param l
	 * 			Left child of the node
	 * @param tt
	 * 			Token type of the node
	 * @param r
	 * 			Right child of the node
	 */
	public ConjunctionNode(BinaryNode l, TokenType tt, BinaryNode r) {
		super(l, tt , r);
	}
	
	@Override
	public ConjunctionNode clone() {
		return new ConjunctionNode(left.clone(), tt, right.clone());
	}
	
	@Override
	public boolean mutateNode(Mutation m) {
		if (super.mutateNode(m))
			return true;
		if (m.equals(MutationFactory.getInsert())) {
			if (tt == TokenType.AND) {
				ConditionNode newConj = (ConjunctionNode) this.getSubtree();
				ConditionNode newConj2 = new ConjunctionNode(this, tt, newConj);
				newConj2.setParent(this.parent);
				
				if (parent instanceof Rule)
					((Rule) parent).addCond(newConj2);
				else if (this == ((BinaryNode) parent).left)
					((BinaryNode) parent).left = newConj2;
				else if (this == ((BinaryNode) parent).right)
					((BinaryNode) parent).right = newConj2;
				return true;
			}
		}
		return false;
	}
	
	@Override
	public ConjunctionNode generateSubtree() {
		if (left.hashCode() / 4 % 2 == 0)
			return (ConjunctionNode) left.generateSubtree();
		return (ConjunctionNode) right.generateSubtree();
	}
}
