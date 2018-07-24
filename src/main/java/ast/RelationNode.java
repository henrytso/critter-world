package ast;

import java.util.Random;

import parse.TokenType;

public class RelationNode extends ConjunctionNode {

	/**
	 * Constructs a RelationNode with two Expressions as the left 
	 * 		and right children
	 * @param l
	 * 			Left child expression
	 * @param tt
	 * 			Token type of the relation
	 * @param r
	 * 			Right child expression
	 */
	public RelationNode(Expression l, TokenType tt, Expression r) {
		super(l, tt, r);
	}

	/**
	 * Removes either the left or right child with equal probability
	 * @return
	 * 			Whether the removal was successful
	 */
	public boolean removeChild() {
		if (!(left instanceof FactorNode)) {
			left = (new Random().nextInt(2) == 0) ? left.left : left.right; // randomly replaces node
			return true;
		}

		if (!(right instanceof FactorNode)) {
			right = (new Random().nextInt(2) == 0) ? right.left : right.right; // randomly replaces node
			return true;
		}

		return false;
	}

	@Override
	public RelationNode clone() {
		return new RelationNode((Expression) left.clone(), tt, (Expression) right.clone());
	}

	@Override
	public boolean mutateNode(Mutation m) {
		if (super.mutateNode(m))
			return true;
		if (m.equals(MutationFactory.getRemove())) {
			if (new Random().nextInt(2) == 0) {
				if (!(left instanceof FactorNode)) {
					left = left.left;
					return true;
				}
			} else {
				if (!(right instanceof FactorNode)) {
					right = right.left;
					return true;
				}
			}
		}
		if (m.equals(MutationFactory.getTransform())) {
			this.transform();
			return true;
		}
		
		return false;
	}

	@Override
	public RelationNode generateSubtree() {
		Random r = new Random();
		TokenType newType;
		switch (r.nextInt(6)) {
		case 0:
			newType = TokenType.LT;
			break;
		case 1:
			newType = TokenType.LE;
			break;
		case 2:
			newType = TokenType.EQ;
			break;
		case 3:
			newType = TokenType.GE;
			break;
		case 4:
			newType = TokenType.GT;
			break;
		default:
			newType = TokenType.NE;
			break;
		}
		return new RelationNode((Expression) left.generateSubtree(), newType, (Expression) right.generateSubtree());
	}

	/**
	 * Sets the token type of this relation node to another valid type
	 */
	public void transform() {
		Random r = new Random();
		TokenType newType;
		switch (r.nextInt(6)) {
		case 0:
			newType = TokenType.LT;
			break;
		case 1:
			newType = TokenType.LE;
			break;
		case 2:
			newType = TokenType.EQ;
			break;
		case 3:
			newType = TokenType.GE;
			break;
		case 4:
			newType = TokenType.GT;
			break;
		default:
			newType = TokenType.NE;
			break;
		}

		tt = newType;
	}

}
