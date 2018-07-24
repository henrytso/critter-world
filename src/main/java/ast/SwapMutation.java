package ast;

import java.util.Random;

/**
 * A SwapMutation
 */
public class SwapMutation implements Mutation{
	
	@Override
	public boolean equals(Mutation m) {
		return m instanceof SwapMutation;
	}
	
	@Override
	public String toString() {
		return "----Swap Mutation----";
	}

}
