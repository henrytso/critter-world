package ast;

import java.util.Random;

import interpret.Interpreter;
import interpret.Outcome;
import parse.TokenType;

/**
 * A representation of a critter action.
 */
public class Action extends Command {
	
	/**
	 * Constructs an Action node of type {@code tt} with child
	 * @param tt
	 * 			Type of action
	 * @param right
	 * 			Child of action
	 */
	public Action(TokenType tt, BinaryNode right) {
		super(null, tt, right);
	}

	@Override
	public Action clone() {
		if (right == null)
			return new Action(tt, null);
		return new Action(tt, right.clone());
	}
	
	@Override
	public Outcome critAccept(Interpreter i) {
		return i.eval(this);
	}
	
	@Override
	public boolean mutateNode(Mutation m) {
		if (super.mutateNode(m))
			return true;
		if (m.equals(MutationFactory.getTransform())) {
			if(tt == TokenType.SERVE || tt == TokenType.TAG)
				return false;
			this.transform();
			return true;
		}
		
		return false;
	}
	
	@Override
	public Action generateSubtree() {
		Random r = new Random();
		TokenType newAct;
		
		switch(r.nextInt(11)) {
			case 0:
				newAct = TokenType.WAIT;
				break;
			case 1:
				newAct = TokenType.FORWARD;
				break;
			case 2:
				newAct = TokenType.BACKWARD;
				break;
			case 3:
				newAct = TokenType.LEFT;
				break;
			case 4:
				newAct = TokenType.RIGHT;
				break;
			case 5:
				newAct = TokenType.EAT;
				break;
			case 6:
				newAct = TokenType.ATTACK;
				break;
			case 7:
				newAct = TokenType.GROW;
				break;
			case 8:
				newAct = TokenType.BUD;
				break;
			case 9:
				newAct = TokenType.MATE;
				break;
			case 10:
				return new Action(TokenType.SERVE, new NumNode(r.nextInt(17) + ""));
			default:
				return new Action(TokenType.TAG, new NumNode(r.nextInt(17) + ""));
		}
		
		return new Action(newAct, null);
	}
	
	/**
	 * Sets the TokeenType to another valid type with equal probability
	 */
	public void transform() {
		Random r = new Random();
		TokenType newAct;
		switch(r.nextInt(10)) {
		case 0:
			newAct = TokenType.WAIT;
			break;
		case 1:
			newAct = TokenType.FORWARD;
			break;
		case 2:
			newAct = TokenType.BACKWARD;
			break;
		case 3:
			newAct = TokenType.LEFT;
			break;
		case 4:
			newAct = TokenType.RIGHT;
			break;
		case 5:
			newAct = TokenType.EAT;
			break;
		case 6:
			newAct = TokenType.ATTACK;
			break;
		case 7:
			newAct = TokenType.GROW;
			break;
		case 8:
			newAct = TokenType.BUD;
			break;
		default:
			newAct = TokenType.MATE;
		}
		
		tt = newAct;
	}

	@Override
	public StringBuilder prettyPrint(StringBuilder sb) {
		if (tt == TokenType.SERVE || tt == TokenType.TAG) {

			sb.append(tt.toString());

			sb.append("[");
			right.prettyPrint(sb);
			sb.append("]");

			return sb;
		} else
			return super.prettyPrint(sb);
	}

}
