package ast;

/**
 * An InsertMutation
 */
public class InsertMutation implements Mutation {
	
	@Override
	public boolean equals(Mutation m) {
		return m instanceof InsertMutation;
	}
	
	@Override
	public String toString() {
		return "----Insert Mutation----";
	}
}
