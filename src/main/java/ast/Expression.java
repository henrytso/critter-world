package ast;

import java.util.Random;

import interpret.Interpreter;
import interpret.Outcome;
import parse.TokenCategory;
import parse.TokenType;

/**
 * A representation of an expression.
 */
public class Expression extends BinaryNode implements Expr {
	private boolean paren;

	/**
	 * Constructs an Expression with a left and right child and with the 
	 * 		TokenType as {@code tt} which will be an ADDOP due to our design
	 * @param left
	 * 			Left child of node
	 * @param tt
	 * 			Token type which will be ADDOP due to our design
	 * @param right
	 * 			Right child of node
	 */
	public Expression(BinaryNode left, TokenType tt, BinaryNode right) {
		super(left, tt, right);
		paren = false;
	}

	/**
	 * Sets the flag denoting parentheses to true
	 * Useful for factor expressions
	 */
	public void addParen() {
		paren = true;
	}

	@Override
	public Expression clone() {
		Expression newExpr = new Expression((BinaryNode) left.clone(), tt, (BinaryNode) right.clone());
		if (paren)
			newExpr.addParen();
		return newExpr;
	}
	
	@Override
	public Outcome critAccept(Interpreter i) {
		return i.eval(this);
	}

	@Override
	public boolean mutateNode(Mutation m) {
		if (super.mutateNode(m))
			return true;
		if (m.equals(MutationFactory.getTransform())) {
			this.transform();
			return true;
		}
		if (m.equals(MutationFactory.getInsert())) {
			if (tt.category() == TokenCategory.MULOP) {
				Expression newExpr = (Expression) this.getSubtree();
				Expression newExpr2 = new Expression(newExpr, tt, this);
				newExpr2.setParent(this.parent);
				if (this == ((BinaryNode) this.parent).left)
					((BinaryNode) parent).left = newExpr2;
				else
					((BinaryNode) parent).right = newExpr2;
				return true;
			}
		}
		
		return false;
	}

	@Override
	public Expression generateSubtree() {
		if (left.hashCode() / 4 % 2 == 0)
			return (Expression) left.generateSubtree();
		return (Expression) right.generateSubtree();
	}

	/**
	 * Sets the token type to PLUS or MINUS with equal probability
	 */
	public void transform() {
		Random r = new Random();
		if (r.nextInt(2) == 0)
			tt = TokenType.PLUS;
		else
			tt = TokenType.MINUS;
	}

	@Override
	public StringBuilder prettyPrint(StringBuilder sb) {
		if (paren) {

			sb.append("(");

			if (left != null)
				left.prettyPrint(sb);

			sb.append(" " + tt.toString() + " ");

			if (right != null)
				right.prettyPrint(sb);

			sb.append(")");

			return sb;
		} else
			return super.prettyPrint(sb);
	}

}
