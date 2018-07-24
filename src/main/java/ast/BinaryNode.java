package ast;

import java.util.Random;

import interpret.Interpreter;
import interpret.Outcome;
import parse.TokenCategory;
import parse.TokenType;

/**
 * A binary node of the AST.
 */
public abstract class BinaryNode implements Node {

	Node parent;
	TokenType tt;
	BinaryNode left;
	BinaryNode right;

	/**
	 * Constructs a binary node with left and right children and of given type
	 * @param left
	 * 			Left child to append
	 * @param tt
	 * 			Type of node
	 * @param right
	 * 			Right child to append
	 */
	public BinaryNode(BinaryNode left, TokenType tt, BinaryNode right) {
		this.left = left;
		this.tt = tt;
		this.right = right;
		
		if(right != null)
			right.setParent(this);
		if(left != null)
			left.setParent(this);
	}
	
	/**
	 * Creates a copy of the node and returns it
	 * @return
	 * 			A copy of the node	
	 */
	public abstract BinaryNode clone();
	
	//todo --> can actually define method bc all are the same.
	public abstract Outcome critAccept(Interpreter i);
	
	/**
	 * Creates a subtree of the same type as the current node and returns it.
	 * @return
	 * 			Head node of the generated subtree
	 */
	public abstract BinaryNode generateSubtree();
	
	/**
	 * Gets a subtree of the type of the current node, in the entire program,
	 * 		and if there is not one, then generate a new one and return it
	 * @return
	 * 			A clone of an existing subtree of the same 
	 * 			type or a generated subtree of that type.
	 */
	public Node getSubtree() {
		Node temp = parent;
		while(!(temp instanceof Rule)) {
			temp = ((BinaryNode) temp).parent;
		}
		
		Program p = ((Rule) temp).getParent();
		Node n = p.findSubtree(this);
		if(n == null)
			n = this.generateSubtree();
		
		return n;
	}
	
	/**
	 * Looks for a subtree under the current node of the type of {@code n}. 
	 * @param n
	 * 			Node whose type of subtree to look for
	 * @return
	 * 			Head node of the subtree, or null if no valid subtree was found
	 */
	public Node findSubtree(Node n) {
		Node ret = null;
		
		if(left != null) {
			if(left.replaceable(n))
				return left.clone();
			else
				ret = left.findSubtree(n);
		}
		
		if(right != null) {
			if(right.replaceable(n))
				return right.clone();
			else
				ret = right.findSubtree(n);
		}
		
		return ret;
	}
	
	/**
	 * Returns whether the node in the is replaceable by node {@code n}
	 * 		for the program tree to still be valid
	 * @param n
	 * 			Node to check if it can replace the current node
	 * @return
	 * 			Whether the node is replaceable by {@code n}
	 */
	public boolean replaceable(Node n) {
		if(!(n instanceof BinaryNode) || n == this)
			return false;
		
		if(tt.category() == TokenCategory.OTHER)
			return tt == ((BinaryNode) n).tt;
		
		return ((BinaryNode) n).tt.category() == tt.category();
	}
	
	/**
	 * Conducts mutation {@code m} on the current node if possible
	 * @param m
	 * 			The mutation to conduct
	 * @return
	 * 			Whether the mutation was successful
	 */
	public boolean mutateNode(Mutation m) {
		if (m.equals(MutationFactory.getSwap()))
			return switchChildren();
		if (m.equals(MutationFactory.getReplace())) {
			if (new Random().nextInt(2) == 0) {
				if (left != null) {
					left = (BinaryNode) left.getSubtree();
					return true;
				}
			} else {
				if (right != null) {
					right = (BinaryNode) right.getSubtree();
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public int size() {
		int size = 1;
		if (left != null)
			size = size + left.size();
		if (right != null)
			size = size + right.size();

		return size;
	}

	@Override
	public Node nodeAt(int index) {
		if (index == 0)
			return this;
		
		if(left == null)
			return right.nodeAt(index - 1);
		
		if (left != null) {
			int leftSize = left.size();
			if (index > leftSize) {
				return right.nodeAt(index - 1 - leftSize);
			}
		}
		
		return left.nodeAt(index - 1);
	}
	
	@Override
	public StringBuilder prettyPrint(StringBuilder sb) {
		
		if(left != null)
			left.prettyPrint(sb);
		
		if(sb.length() != 0 && !Character.isWhitespace(sb.charAt(sb.length() - 1)))
			sb.append(" ");
		sb.append(tt.toString() + " ");
		
		if(right != null)
			right.prettyPrint(sb);

		return sb;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		return prettyPrint(sb).toString();
	}

	/**
	 * Sets the parent to {@code parent}
	 * @param parent
	 * 			Node to set as the parent
	 */
	public void setParent(Node parent) {
		this.parent = parent;
	}
	
	/**
	 * Return the parent of the node
	 * @return
	 * 			The parent node
	 */
	public Node getParent() {
		return parent;
	}

	/**
	 * Sets the left child of the node to {@code child}
	 * @param child
	 * 			Node to set as the left child
	 */
	public void setLeft(BinaryNode child) {
		left = child;
	}

	/**
	 * Sets the right child of the node to {@code child}
	 * @param child
	 * 			Node to set as the right child
	 */
	public void setRight(BinaryNode child) {
		right = child;
	}
	
	/**
	 * Returns the left child of the node
	 * @return
	 * 			The left child
	 */
	public BinaryNode left() {
		return left;
	}
	
	/**
	 * Returns the right child of the node
	 * @return
	 * 			The right child
	 */
	public BinaryNode right() {
		return right;
	}
	
	public TokenType getTokenType() {
		return tt;
	}
	
	/**
	 * Switches the left and right children of the binary node
	 * @return
	 * 			Whether the swap was successful
	 */
	public boolean switchChildren() {
		if(left != null && right != null) {
			BinaryNode temp = right;
			right = left;
			left = temp;
			return true;
		}
		
		return false;
	}
}
