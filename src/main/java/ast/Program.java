package ast;

import java.util.ArrayList;

/**
 * An abstraction of a critter program.
 */
public interface Program extends Node {
   /**
    * Mutates this program with a single mutation
    * 
    * @return The root of the mutated AST
    */
   Program mutate();
   
   /**
    * Clones this program
    * 
    * @return The cloned program's head node
    */
   Program clone();
   
   ArrayList<Rule> getRules();
   
   Node findSubtree(Node n);
   
   /**
	 * Adds a rule to the program tree
	 * @param r
	 * 		The rule to be added
	 */
	public void add(Rule r);

   /**
    * Mutates {@code nodeAt(index)} (and not its children) with mutation
    * {@code m}.
    * 
    * @param index
    *           The index of the node to mutate
    * @param m
    *           The mutation to perform on the node
    * @return The mutated program, or {@code null} if {@code m} represents an
    *         invalid mutation for the node at {@code index}
    * @throws IndexOutOfBoundsException
    *            if {@code index} is not valid
    */
   Program mutate(int index, Mutation m);
}
