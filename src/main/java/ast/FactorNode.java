package ast;

import java.util.Random;

import interpret.Interpreter;
import interpret.Outcome;
import parse.TokenType;

public class FactorNode extends TermNode {

	/**
	 * Constructs a FactorNode with left and right children and token type {@code tt}
	 * @param left
	 * 		Left child of node
	 * @param tt
	 * 		Token type of the factor
	 * @param right
	 * 		Right child of node
	 */
	public FactorNode(BinaryNode left, TokenType tt, BinaryNode right) {
		super(left, tt, right);
	}

	@Override
	public FactorNode clone() {
		// Left child of factor node is always null
		if (right != null)
			return new FactorNode(null, tt, right.clone());
		return new FactorNode(null, tt, null);
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
			FactorNode newFactor = new FactorNode(null, TokenType.MINUS, this);
			newFactor.setParent(parent);
			if (this == ((BinaryNode) this.parent).left)
				((BinaryNode) parent).left = newFactor;
			else
				((BinaryNode) parent).right = newFactor;
			return true;
		}
		
		return false;
	}

	@Override
	public FactorNode generateSubtree() {
		Random r = new Random();
		if (right instanceof SensorNode)
			return (SensorNode) right.generateSubtree();
		return new NumNode(r.nextInt(17) + "");
	}
}
