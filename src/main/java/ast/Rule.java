package ast;

import java.util.ArrayList;
import java.util.Random;

import parse.TokenType;

/**
 * A representation of a critter rule.
 */
public class Rule implements Node {

	ConditionNode condition;
	TokenType tt;
	ArrayList<Update> updates;
	Command command;
	Program parent;

	/**
	 * Constructs a Rule node with a Condition {@code cond} and a Command {@code comm}
	 * 		and an ArrayLists of updates {@code u}
	 * @param cond
	 * 		Condition child
	 * @param u
	 * 		ArrayList of updates children
	 * @param comm
	 * 		Command child
	 */
	public Rule(ConditionNode cond, ArrayList<Update> u, Command comm) {
		tt = TokenType.ARR;
		updates = u;
		this.condition = cond;
		this.command = comm;
		
		cond.setParent(this);
		comm.setParent(this);
		for(Update up : updates) {
			up.setParent(this);
		}
	}
	
	/**
	 * Sets the parent to {@code parent}
	 */
	public void setParent(Program parent) {
		this.parent = parent;
	}
	
	/**
	 * Returns the parent node
	 * @return
	 * 		Parent node
	 */
	public Program getParent() {
		return parent;
	}
	
	public ConditionNode getCond() {
		return condition;
	}
	
	public Command getComm() {
		return command;
	}
	
	public ArrayList<Update> getUpdates(){
		return updates;
	}
	
	/**
	 * Returns a subtree of same type as node {@code n} or null if not found
	 * @param n
	 * 		The node whose type is the desired type of the subtree to look for
	 * @return
	 * 		Subtree of same type as node {@code n} or null if not found
	 */
	public Node findSubtree(Node n) {
		Node ret = null;
		
		if(condition.replaceable(n))
			return condition.clone();
		ret = condition.findSubtree(n);
		
		if(ret == null) { 
			if(command.replaceable(n))
				return command.clone();
			ret = command.findSubtree(n);
		}
		
		if(ret == null) {
			for(Update u : updates) {
				if(ret == null)
					ret = u.findSubtree(n);
			}
		}
		
		if(ret != null)
			return ret.clone();
		
		return null;
	}
	
	/**
	 * Searches the entire program tree for a subtree of type Rule and 
	 * 		returns it or a generated one if no rules are present
	 * @return
	 * 		Rule subtree in the AST or a generated rule if there are no rules
	 */
	public Node getSubtree() {
		Node temp = parent.findSubtree(this);
		if (temp != null)
			return temp;
		return this.generateSubtree();
	}
	
	/**
	 * Generates a subtree of the type of the node (Rule in this case)
	 * @return
	 * 		Generated rule subtree
	 */
	public Node generateSubtree() {
		ConditionNode newCond = condition.generateSubtree();
		Command newComm = (Command) command.generateSubtree();
		return new Rule(newCond, new ArrayList<Update>(), newComm);
		
	}
	
	/**
	 * Returns whether node is replaceable by node {@code n}
	 * @param n
	 * 		Node to check if the current node is replaceable by it
	 * @return
	 * 		Whether node is replaceable to still have a valid AST
	 */
	public boolean replaceable(Node n) {
		if(n instanceof Rule)
			if(n != this)
				return true;
		
		return false;
	}
	
	/**
	 * Conducts a mutation {@code m} on the node if possible
	 * @return
	 * 		Whether the mutation was successful
	 */
	public boolean mutateNode(Mutation m) {
		Random r = new Random();
		if (m.equals(MutationFactory.getRemove())) {
			if(r.nextInt(2) == 0)
				return removeCondition(r);
			else
				return removeUpdate(r);
		}
		if(m.equals(MutationFactory.getReplace())) {
			if(r.nextInt(2) == 0)
				condition = (ConditionNode) condition.getSubtree();
			else
				command = (Command) command.getSubtree();
			
			assert(command != null && condition != null);
			return true;
		}
		if (m.equals(MutationFactory.getDuplicate())) {
			if (updates.size() == 0)
				return false;
			updates.add((Update) updates.get(0).getSubtree());
			return true;
		}
		
		return false;
	}
	
	@Override
	public int size() {
		int count = 0;
		for (Update u : updates) {
			count += u.size();
		}
		return 1 + condition.size() + command.size() + count;
	}

	/**
	 * Adds an update to the array list
	 * @param u
	 * 		Update to add
	 */
	public void add(Update u) {
		updates.add(u);
		u.parent = this;
	}
	
	/**
	 * Adds a condition to the rule
	 * @param cond
	 * 		Condition to add
	 */
	public void addCond(ConditionNode cond) {
		assert(cond != null);
		this.condition = cond;
	}
	
	/**
	 * Sets the command to {@code comm}
	 * @param comm
	 * 		Command to be added
	 */
	public void addComm(Command comm) {
		assert(comm != null);
		this.command = comm;
	}
	
	@Override
	public Node nodeAt(int index) {
		if (index == 0)
			return this;

		int count = index;

		if (count <= condition.size())
			return condition.nodeAt(count - 1);
		else
			count -= condition.size();

		for (Update u : updates) {
			int size = u.size();
			if (count <= size)
				return u.nodeAt(count - 1);
			count -= size;
		}

		return command.nodeAt(count - 1);
	}

	@Override
	public StringBuilder prettyPrint(StringBuilder sb) {

		condition.prettyPrint(sb);
		
		sb.append(" " + tt.toString() + " ");
		
		for (Update u : updates) {
			u.prettyPrint(sb);
			sb.append("\n\t\t");
		}
		
		command.prettyPrint(sb);
		
		return sb;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		return prettyPrint(sb).toString();
	}
	
	@Override
	public Rule clone() {
		ConditionNode condCopy = condition.clone();
		Command commCopy = (Command) command.clone();
		ArrayList<Update> updatesCopy = new ArrayList<Update>();
		for (Update u : updates)
			updatesCopy.add(u.clone());
		return new Rule(condCopy, updatesCopy, commCopy);
	}
	
	/** Replaces the conditionNode or conjunctionNode with a child of theirs
	 * @param r
	 * 		A Random object to be used
	 * @return
	 * 		Whether the removal was successful
	 */
	public boolean removeCondition(Random r) {
		if(condition instanceof RelationNode)
			return false;
		
		int i = r.nextInt(2);
		condition = i == 1? (ConditionNode) condition.left : (ConditionNode) condition.right; // randomly replaces node
		return true;
	}
	
	/**
	 * Removes an update from the rule
	 * @param r
	 * 		A Random object to be used
	 * @return
	 * 		Whether the removal was successful
	 */
	public boolean removeUpdate(Random r) {
		if(updates.size() == 0)
			return false;
		
		int i = r.nextInt(updates.size());
		updates.remove(i);
		return true;
	}
	
	/**
	 * Swaps updates in the rule
	 * @param r
	 * 		A Random object to be used 
	 * @return
	 * 		Whether the swap was successful
	 */
	public boolean swapUpdates(Random r) {
		int numUpdates = updates.size();
		if(command instanceof Update && numUpdates != 0) {
			Update u1 = updates.get(0);
			updates.add((Update) command);
			command = u1;
			return true;
		}
		
		if(numUpdates > 2) {
			Update u1 = updates.remove(0);
			updates.add(u1);
			return true;
		}
		
		return false;
	}
}
