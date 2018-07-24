package ast;

import java.util.ArrayList;
import java.util.Random;

import parse.TokenType;

/**
 * A data structure representing a critter program.
 */
public class ProgramImpl implements Program {

	private ArrayList<Rule> children;

	/**
	 * Constructs a ProgramImpl with an empty list of rule children 
	 */
	public ProgramImpl() {
		children = new ArrayList<Rule>();
	}

	@Override
	public int size() {
		int size = 0;
		for (Rule r : children) {
			size = size + r.size();
		}

		return size + 1;
	}

	@Override
	public void add(Rule r) {
		children.add(r);
		r.parent = this;
	}
	
	public ArrayList<Rule> getRules(){
		return children;
	}

	@Override
	public Node nodeAt(int index) {
		if (index == 0)
			return this;

		int count = index;
		for (Rule r : children) {
			int size = r.size();
			if (count <= size)
				return r.nodeAt(count - 1);
			count -= size;
		}

		return null;
	}

	@Override
	public Program clone() {
		ProgramImpl copy = new ProgramImpl();
		for (Rule r : children) {
			Rule rClone = r.clone();
			rClone.setParent(copy);
			copy.add(rClone);
		}
		return copy;
	}

	/**
	 * Returns a subtree of same type as node {@code n} or null if not found
	 * @return
	 * 		Subtree of same type as node {@code n} or null if not found
	 */
	public Node findSubtree(Node n) {
		Node ret = null;
		for (Rule r : children) {
			if (r.replaceable(n))
				return r.clone();
			if (ret == null)
				ret = r.findSubtree(n);
		}

		if (ret != null)
			return ret.clone();
		return ret;
	}

	/**
	 * Returns whether node can be replaced by node {@code n}
	 * @param n
	 * 		Node to see whether node can be replaced by it
	 * @return
	 * 		Whether node can be replaced by node {@code n}
	 */
	public boolean replaceable(Node n) {
		return n instanceof Program;
	}

	@Override
	public boolean mutateNode(Mutation m) {
		Random r = new Random();
		if (m.equals(MutationFactory.getRemove())) {
			return this.removeRule(r);
		}
		if (m.equals(MutationFactory.getSwap())) {
			return this.swapRules(r);
		}
		if (m.equals(MutationFactory.getDuplicate())) {
			if (children.size() > 0) {
				children.add((Rule) children.get(0).getSubtree());
				return true;
			} else {
				// Create dummy Rule to call generate method on
				NumNode newNum = new NumNode("1");
				RelationNode newCond = new RelationNode(newNum, TokenType.EQ, newNum.clone());
				Action newComm = new Action(TokenType.WAIT, null);
				Rule dummyRule = new Rule(newCond, new ArrayList<Update>(), newComm);
				
				// Add the generated node, remove the dummy node
				this.add(dummyRule);
				this.add((Rule) children.get(0).generateSubtree());
				this.children.remove(0);
				
				return true;
			}
		}
		if (m.equals(MutationFactory.getReplace())) {
			if(children.size() == 0)
				return false;
			int index = new Random().nextInt(children.size());
			Rule rule = children.remove(index);
			children.add((Rule)rule.getSubtree());
			return true;
		}
		
		return false;
	}

	@Override
	public Program mutate() {
		Random r = new Random();
		Mutation m;
		Program copy = null;
		while (copy == null) {
			switch (r.nextInt(6)) {
			case 0:
				m = MutationFactory.getRemove();
				break;
			case 1:
				m = MutationFactory.getSwap();
				break;
			case 2:
				m = MutationFactory.getReplace();
				break;
			case 3:
				m = MutationFactory.getTransform();
				break;
			case 4:
				m = MutationFactory.getInsert();
				break;
			default:
				m = MutationFactory.getDuplicate();
				break;
			}
			copy = this.mutate(r.nextInt(this.size()), m);
		}
		return copy;
	}

	@Override
	public Program mutate(int index, Mutation m) {
		Program copy = this.clone();
		boolean mutated = copy.nodeAt(index).mutateNode(m);
		if (mutated) {
			return copy;
		}
		return null;
	}

	@Override
	public StringBuilder prettyPrint(StringBuilder sb) {
		for (Rule r : children) {
			r.prettyPrint(sb);
			sb.append(";\n");
		}

		return sb;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		return prettyPrint(sb).toString();
	}

	public boolean removeRule(Random r) {
		if (children.size() == 0)
			return false;

		children.remove(r.nextInt(children.size()));
		return true;
	}

	/**
	 * Swaps the rules of this program
	 * @param r
	 * 		A Random object to be used
	 * @return
	 * 		Whether the swap was successful
	 */
	public boolean swapRules(Random r) {
		if (children.size() < 2)
			return false;

		int index = r.nextInt(children.size());
		Rule rule = children.remove(index);
		children.add(rule);
		return true;
	}

}
