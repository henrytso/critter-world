package ast;

/**
 * A ReplaceMutation
 */
public class ReplaceMutation implements Mutation {
	
	@Override
	public boolean equals(Mutation m) {
		return m instanceof ReplaceMutation;
	}
	
	@Override
	public String toString() {
		return "----Replace Mutation----";
	}
}
