package ast;

import java.util.Random;

import interpret.Interpreter;
import interpret.Outcome;
import parse.TokenType;

public class MemNode extends FactorNode{

	/**
	 * Constructs a MemNode with one meaningful child on the right
	 * @param right
	 * 		Single child, which we put on the right
	 */
	public MemNode(Expression right) {
		super(null, TokenType.MEM, right);
	}
	
	@Override
	public MemNode clone() {
		return new MemNode((Expression) right.clone());
	}
	
	@Override
	public Outcome critAccept(Interpreter i) {
		return i.eval(this);
	}
	
	@Override
	public boolean mutateNode(Mutation m) {
		if (super.mutateNode(m))
			return true;
		
		return false;
	}
	
	@Override
	public MemNode generateSubtree() {
		return new MemNode(new NumNode(new Random().nextInt(17) + ""));
	}
	
	@Override
	public StringBuilder prettyPrint(StringBuilder sb) {
		sb.append(" " + tt.toString());
		
		sb.append("[");
		right.prettyPrint(sb);
		sb.append("]");

		return sb;
	}

}
