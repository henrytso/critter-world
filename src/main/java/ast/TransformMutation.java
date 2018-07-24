package ast;

/**
 * A TransformMutation
 */
public class TransformMutation implements Mutation {
	
	@Override
	public boolean equals(Mutation m) {
		return m instanceof TransformMutation;
	}
	
	@Override
	public String toString() {
		return "----Transform Mutation----";
	}
}
