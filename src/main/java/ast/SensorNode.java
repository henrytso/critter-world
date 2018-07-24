package ast;

import java.util.Random;

import interpret.Interpreter;
import interpret.Outcome;
import parse.TokenType;

public class SensorNode extends FactorNode{

	/**
	 * Constructs a SensorNode with left and right children with TokenType {@code tt}
	 * @param left
	 * 		Left child of node
	 * @param tt
	 * 		Token type of node
	 * @param right
	 * 		Right child of node
	 */
	public SensorNode(BinaryNode left, TokenType tt, BinaryNode right) {
		super(left, tt, right);
	}
	
	@Override
	public SensorNode clone() {
		if (right != null)
			return new SensorNode(null, tt, right.clone());
		return new SensorNode(null, tt, null);
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
			if(tt == TokenType.SMELL)
				return false;
			this.transform();
			return true;
		}
		
		return false;
	}
	
	/**
	 * Sets the TokenType to another one of valid type
	 */
	public void transform() {
		Random r = new Random();
		switch(r.nextInt(3)) {
			case 0:
				tt = TokenType.NEARBY;
				break;
			case 1:
				tt = TokenType.AHEAD;
				break;
			default:
				tt = TokenType.RANDOM;
		}
	}
	
	@Override
	public SensorNode generateSubtree() {
		Random r = new Random();
		switch (r.nextInt(4)) {
			case 0:
				return new SensorNode(null, TokenType.NEARBY, new NumNode(r.nextInt(17) + ""));
			case 1:
				return new SensorNode(null, TokenType.AHEAD, new NumNode(r.nextInt(17) + ""));
			case 2:
				return new SensorNode(null, TokenType.RANDOM, new NumNode(r.nextInt(17) + ""));
			default:
				return new SensorNode(null, TokenType.SMELL, null);
		}
	}
	
	@Override
	public StringBuilder prettyPrint(StringBuilder sb) {
		if(tt != TokenType.SMELL) {
		
			sb.append(" " + tt.toString());
			
			sb.append("[");
			right.prettyPrint(sb);
			sb.append("]");
	
			return sb;
		}
		else
			return super.prettyPrint(sb);
	}


}
