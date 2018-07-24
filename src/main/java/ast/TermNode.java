package ast;

import java.util.Random;

import parse.TokenCategory;
import parse.TokenType;

public class TermNode extends Expression{

	/**
	 * Constructs a TermNode with left and right children with TokenType {@code tt}
	 * @param left
	 * 		Left child of node
	 * @param tt
	 * 		Token type of node which will be a MULOP due to our design
	 * @param right
	 * 		Right child of node
	 */
	public TermNode(BinaryNode left, TokenType tt, BinaryNode right) {
		super(left, tt, right);
	}
	
	@Override
	public TermNode clone() {
		return new TermNode(left.clone(), tt, right.clone());
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
			if (tt.category() == TokenCategory.ADDOP) {
				TermNode newTerm = (TermNode) this.getSubtree();
				TermNode newTerm2 = new TermNode(newTerm, tt, this);
				newTerm2.setParent(this.parent);
				if (this == ((BinaryNode) this.parent).left)
					((BinaryNode) parent).left = newTerm2;
				else
					((BinaryNode) parent).right = newTerm2;
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public TermNode generateSubtree() {
		if (left.hashCode() / 4 % 2 == 0)
			return (TermNode) left.generateSubtree();
		return (TermNode) right.generateSubtree();
	}
	
	/**
	 * Sets the TokenType to another one of valid type with equal probability
	 */
	public void transform() {
		Random r = new Random();
		switch(r.nextInt(3)) {
			case 0:
				tt = TokenType.MUL;
				break;
			case 1:
				tt = TokenType.DIV;
				break;
			default:
				tt = TokenType.MOD;
		}
	}
	
	@Override
	public StringBuilder prettyPrint(StringBuilder sb) {
		if(tt.category() == TokenCategory.MULOP) {		
			sb.append(tt.toString());
			
			sb.append("(");
			right.prettyPrint(sb);
			sb.append(") ");
	
			return sb;
		}
		else
			return super.prettyPrint(sb);
	}

}
