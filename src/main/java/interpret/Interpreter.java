package interpret;

import ast.Action;
import ast.Condition;
import ast.ConditionNode;
import ast.Expr;
import ast.Expression;
import ast.FactorNode;
import ast.MemNode;
import ast.NumNode;
import ast.Program;
import ast.SensorNode;
import ast.Update;

/**
 * An example interface for interpreting a critter program. This is just a starting
 * point and may be changed as much as you like.
 */
public interface Interpreter {
    /**
     * Execute program of critter until either the maximum number of rules per
     * turn is reached or some rule whose command contains an action is
     * executed.
     * @return a result containing the action to be performed;
     * the action may be null if the maximum number of rules
     * per turn was exceeded.
     */
    Outcome interpret();

    /**
     * Evaluate given condition.
     * @param c
     * @return a boolean that results from evaluating c.
     */
    public Outcome eval(ConditionNode c);

    /**
     * Evaluate given expression
     * @param e
     * 		Expression to evaluate
     * @return Integer result of evaluating e
     */
    public Outcome eval(Expression e);
	
    /**
     * Evaluate given factor node
     * @param f
     * 		Factor node to evaluate
     * @return Integer result of evaluating f
     */
	public Outcome eval(FactorNode f);
	
	/**
     * Evaluate given mem node
     * @param m
     * 		Mem node to evaluate
     * @return Integer result of evaluating m
     */
	public Outcome eval(MemNode m);
	
	/**
     * Evaluate given num node
     * @param f
     * 		Num node to evaluate
     * @return Integer result of evaluating f
     */
	public Outcome eval(NumNode f);
	
	/**
     * Evaluate given sensor node
     * @param s
     * 		Sensor node to evaluate
     * @return Integer result of evaluating s
     */
	public Outcome eval(SensorNode s);
	
	/**
	 * Evaluate given update
	 * @param u
	 * 		Update to evaluate
	 * @return LogOutcome of update (no other changed hex)
	 */
	public Outcome eval(Update u);
	/**
     * Evaluate given action
     * @param a
     * 		Action to evaluate
     * @return LogOutcome of the action
     */
	public Outcome eval(Action a);
}
