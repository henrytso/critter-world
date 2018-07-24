package ast;

import java.util.Random;

/**
 * A RemoveMutation
 */
public class RemoveMutation implements Mutation{
	
	@Override
	public boolean equals(Mutation m) {
		return m instanceof RemoveMutation;
	}
	
	@Override
	public String toString() {
		return "----Remove Mutation----";
	}

}
