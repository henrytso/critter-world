package ast;

import java.util.Random;

import interpret.Interpreter;
import interpret.Outcome;
import parse.TokenType;

/**
 * A representation of a number.
 */
public class NumNode extends FactorNode {

	String num;
	
	/**
	 * Constructs a NumNode with no children with value {@code num}
	 * @param num
	 * 		Number value of the node
	 */
	public NumNode(String num) {
		super(null, TokenType.NUM, null);
		this.num = num;
	}
	
	@Override
	public NumNode clone() {
		return new NumNode(num);
	}
	
	public int getNum() {
		return Integer.parseInt(num);
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
		
		return false;
	}
	
	@Override
	public StringBuilder prettyPrint(StringBuilder sb) {
		return sb.append(num);
	}
	
	/**
	 * Adjusts the number value to another valid value
	 */
	public void transform() {
		Random r = new Random();
		int adjust = java.lang.Integer.MAX_VALUE/r.nextInt();
		
		if(r.nextInt(2) == 0)
			num = (Integer.parseInt(num) + adjust) + "";
		else
			num = (Integer.parseInt(num) - adjust) + "";
	}
}
