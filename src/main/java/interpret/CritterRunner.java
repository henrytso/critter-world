package interpret;

import java.util.ArrayList;
import java.util.Random;

import ast.Action;
import ast.ConditionNode;
import ast.Expression;
import ast.FactorNode;
import ast.MemNode;
import ast.NumNode;
import ast.Program;
import ast.Rule;
import ast.SensorNode;
import ast.Update;
import parse.TokenType;
import simulator.Critter;

public class CritterRunner implements Interpreter {
	Critter critter;
	
	public CritterRunner(Critter c) {
		critter = c;
	}
	
	@Override
	public Outcome interpret() {
		Program p = critter.getProgram();
		ArrayList<Rule> rules = p.getRules();
		Outcome result = new LogOutcome();
		result.setAction(false);
		
		critter.getMem()[5] = 1;
		
		Rule r = null;
		while (critter.getMem()[5] < 1000 && !result.isAction()) {
			boolean trueCond = false; // a condition has not evaluated to true.
			int count = 0; // which rule
			while (!trueCond && count < rules.size()) {
				r = rules.get(count);
				
				if (getBool(r.getCond().critAccept(this).value())) {
					trueCond = true;
					for (Update u : r.getUpdates()) {
						u.critAccept(this);
					}
					
					result = r.getComm().critAccept(this);
				}
				count++;
				
			}
			
			if (count == rules.size()) {
				result.setAction(true);
				critter.waitAction();
			}
			
			critter.getMem()[5]++;
		}
		
		if (result.isAction())
			critter.setLastRule(r);
		
		critter.getMem()[5] = 1;

		return result;
	}
	
	@Override
	public Outcome eval(ConditionNode c) {
		Outcome o = new LogOutcome();
		boolean trueCond = false;
		TokenType t = c.getTokenType();
		int left = c.left().critAccept(this).value();
		int right = c.right().critAccept(this).value();
		
		switch (t) {
		case OR:
			trueCond = getBool(left) || getBool(right);
			break;
		case AND:
			trueCond = getBool(left) && getBool(right);
			break;
		case LT:
			trueCond = left < right;
			break;
		case LE:
			trueCond = left <= right;
			break;
		case GE:
			trueCond = left >= right;
			break;
		case GT:
			trueCond = left > right;
			break;
		case NE:
			trueCond = left != right;
			break;
		case EQ:
			trueCond = left == right;
			break;
		default:
			throw new AssertionError();
			
		}
		
		o.setValue(trueCond ? 1 : 0);
		return o;
	}
	
	/**
	 * Requires that i either equals 0 or 1.
	 * @param i an integer representing the boolean
	 * @return true if i = 1 and false if i = 0
	 */
	private boolean getBool(int i) {
		return i == 1;
	}
	
	@Override
	public Outcome eval(Expression e) {
		Outcome o = new LogOutcome();
		TokenType t = e.getTokenType();
		int left = e.left().critAccept(this).value();
		int right = e.right().critAccept(this).value();
		
		switch (t) {
		case PLUS:
			o.setValue(left + right);
			break;
		case MINUS:
			o.setValue(left - right);
			break;
		case MUL:
			o.setValue(left * right);
			break;
		case DIV:
			if (right == 0)
				o.setValue(0);
			o.setValue(left / right);
			break;
		case MOD:
			if (right == 0)
				o.setValue(0);
			o.setValue(left % right);
			break;
		default:
			throw new AssertionError();
			
		}
		
		return o;
	}
	
	@Override
	public Outcome eval(FactorNode f) {
		//Only run in case of - (factor)
		Outcome o = new LogOutcome();
		
		int right = f.right().critAccept(this).value();
		o.setValue(-1 * right);
		return o;
	}
	
	@Override
	public Outcome eval(SensorNode s) {
		TokenType type = s.getTokenType();
		Outcome o = new LogOutcome();
		
		if (type == TokenType.SMELL) {
			o.setValue(critter.smell());
			return o;
		}
		
		int right = s.right().critAccept(this).value();
		switch (type) {
		case AHEAD:
			o.setValue(critter.ahead(right, critter.getDir()));
			return o;
		case NEARBY:
			o.setValue(critter.nearby(right));
			return o;
		case RANDOM:
			Random r = new Random();
			o.setValue(r.nextInt(right));
			return o;
		default:
			throw new AssertionError();
		}
		
	}
	
	@Override
	public Outcome eval(MemNode m) {
		Outcome o = new LogOutcome();
		
		int right = m.right().critAccept(this).value();
		if (right < 0 || right > critter.getMem().length)
			o.setValue(0);
		
		o.setValue(critter.getMem()[right]);
		return o;
	}
	
	@Override
	public Outcome eval(NumNode n) {
		Outcome o = new LogOutcome();
		o.setValue(n.getNum());
		return o;
	}
	
	@Override
	public Outcome eval(Update u) {
		int index = u.left().right().critAccept(this).value(); // Want expression in
														// memnode
		int right = u.right().critAccept(this).value(); // Value that should be assigned
												// to mem[index]
		
		if (index == 7)
			if (right > 0 && right < 99)
				critter.getMem()[7] = right;
			
		if (index > 7 && index < critter.getMem().length)
			critter.getMem()[index] = right;
		
		return new LogOutcome();
	}
	
	@Override
	public Outcome eval(Action a) {
		TokenType t = a.getTokenType();
		int c, r;
		LogOutcome lo = new LogOutcome();
		
		switch (t) {
		case WAIT:
			critter.waitAction();
			lo = new LogOutcome();
			break;
		case FORWARD:
			if (critter.forward()) {
				c = critter.colAtDir(critter.getDir());
				r = critter.rowAtDir(critter.getDir());
				lo = new LogOutcome(c, r);
			}
			break;
		case BACKWARD:
			if (critter.backward()) {
				c = critter.colAtDir((critter.getDir() + 3) % 6);
				r = critter.rowAtDir((critter.getDir() + 3) % 6);
				lo = new LogOutcome(c, r);
			}
			break;
		case LEFT:
			critter.turn(-1);
			lo = new LogOutcome();
			break;
		case RIGHT:
			critter.turn(1);
			lo = new LogOutcome();
			break;
		case EAT:
			if (critter.eat()) {
				c = critter.colAtDir(critter.getDir());
				r = critter.rowAtDir(critter.getDir());
				lo = new LogOutcome(c, r); 
			}
			break;
		case ATTACK:
			if (critter.attack()) {
				c = critter.colAtDir(critter.getDir());
				r = critter.rowAtDir(critter.getDir());
				lo = new LogOutcome(c, r); 
			}
			break;
		case GROW:
			critter.grow();
			lo = new LogOutcome();
			break;
		case BUD:
			if (critter.bud()) {
				c = critter.colAtDir((critter.getDir() + 3) % 6);
				r = critter.rowAtDir((critter.getDir() + 3) % 6);
				lo = new LogOutcome(c, r);
			}
			break;
		case MATE:
			c = critter.colAtDir((critter.getDir() + 3) % 6);
			r = critter.rowAtDir((critter.getDir() + 3) % 6);
			lo = new LogOutcome(c, r, true);
			break;
		case TAG:
			if (critter.tag(a.right().critAccept(this).value())) {
				c = critter.colAtDir(critter.getDir());
				r = critter.rowAtDir(critter.getDir());
				lo = new LogOutcome(c, r); 
			}
			break;
		default: // Action is serve
			if (critter.serve(a.right().critAccept(this).value())) {
				c = critter.colAtDir(critter.getDir());
				r = critter.rowAtDir(critter.getDir());
				lo = new LogOutcome(c, r);
			}
			break;
		}
		
		lo.setAction(true);
		return lo;
	}
}
