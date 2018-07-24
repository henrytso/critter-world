package ast;

/**
 * A DuplicateMutation
 */
public class DuplicateMutation implements Mutation {
	
	@Override
	public boolean equals(Mutation m) {
		return m instanceof DuplicateMutation;
	}
	
	@Override
	public String toString() {
		return "----Duplicate Mutation----";
	}
}
