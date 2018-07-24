package parse;

import java.io.Reader;
import java.util.ArrayList;

import ast.*;
import exceptions.SyntaxError;

class ParserImpl implements Parser {

	@Override
	public Program parse(Reader r) {
		Tokenizer t = new Tokenizer(r);
		try {
			return parseProgram(t);
		} catch (SyntaxError e) {
			System.out.println("Not a valid critter program");
			return null;
		}
	}

	/**
	 * Parses a program from the stream of tokens provided by the Tokenizer,
	 * consuming tokens representing the program. All following methods with a name
	 * "parseX" have the same spec except that they parse syntactic form X.
	 * 
	 * @return the created AST
	 * @throws SyntaxError
	 *             if there the input tokens have invalid syntax
	 */
	public static ProgramImpl parseProgram(Tokenizer t) throws SyntaxError {
		ProgramImpl programImpl = new ProgramImpl();
		while (t.hasNext()) {
			Rule r = parseRule(t);
			r.setParent(programImpl);
			programImpl.add(r);
		}
		return programImpl;
	}

	public static Rule parseRule(Tokenizer t) throws SyntaxError {
		ConditionNode cond = parseCondition(t);

		consume(t, TokenType.ARR);

		ArrayList<Update> updates = new ArrayList<Update>();
		Command last = null;
		while (!t.peek().toString().equals(";")) {
			last = parseCommand(t);
			if (t.peek().toString().equals(";")) {
				t.next();
				if (last == null)
					throw new SyntaxError();
				Rule r = new Rule(cond, updates, last);
				return r;
			}

			if (last instanceof Action) {
				throw new SyntaxError();
			}

			updates.add((Update) last);
		}

		return null;

	}

	public static Command parseCommand(Tokenizer t) throws SyntaxError {
		if (t.peek().getType() == TokenType.MEM || 
				t.peek().getType().category() == TokenCategory.MEMSUGAR)
			return parseUpdate(t);

		return parseAction(t);
	}

	public static Action parseAction(Tokenizer t) throws SyntaxError {
		Token next = t.next();

		if (!next.isAction())
			throw new SyntaxError();

		if (next.getType() == TokenType.TAG || next.getType() == TokenType.SERVE) {
			consume(t, TokenType.LBRACKET);
			Expression e = parseExpression(t);
			consume(t, TokenType.RBRACKET);

			return new Action(next.getType(), e);
		} else
			return new Action(next.getType(), null);

	}

	public static Update parseUpdate(Tokenizer t) throws SyntaxError {
		MemNode m = parseMem(t);

		consume(t, TokenType.ASSIGN);

		Expression e = parseExpression(t);
		return new Update(m, e);
	}

	public static MemNode parseMem(Tokenizer t) throws SyntaxError {
		Token next = t.next();

		if (next.getType() == TokenType.MEM) {
			consume(t, TokenType.LBRACKET);
			Expression e = parseExpression(t);
			consume(t, TokenType.RBRACKET);
			return new MemNode(e);
		} 
		else { // syntactic sugar
			
			int index; //index of mem
			switch (next.getType()) {

			case ABV_MEMSIZE: index = 0;
				break;
			case ABV_DEFENSE: index = 1;
				break;
			case ABV_OFFENSE: index = 2;
				break;
			case ABV_SIZE: index = 3;
				break;
			case ABV_ENERGY: index = 4;
				break;
			case ABV_PASS: index = 5;
				break;
			case ABV_TAG: index = 6;
				break;
			case ABV_POSTURE: index = 7;
				break;
			default:
				throw new SyntaxError();
				
			}
			
			NumNode number = new NumNode(index + "");
			return new MemNode(number);
		}
	}

	public static ConditionNode parseCondition(Tokenizer t) throws SyntaxError {
		
		ConditionNode cond1 = parseConjunction(t);
		while(t.peek().getType() == TokenType.OR) {
			consume(t, TokenType.OR);
			ConditionNode cond2 = parseConjunction(t);
			cond1 = new ConditionNode(cond1, TokenType.OR, cond2);
		}
		
		return cond1;
	}


	public static ConditionNode parseConjunction(Tokenizer t) throws SyntaxError {
		ConditionNode conj1 = parseRelation(t);
		while(t.peek().getType() == TokenType.AND) {
			consume(t, TokenType.AND);
			ConditionNode conj2 = parseRelation(t);
			conj1 = new ConjunctionNode(conj1, TokenType.AND, conj2);
		}
		
		return conj1;
	}

	public static ConditionNode parseRelation(Tokenizer t) throws SyntaxError {
		//{condition}
		if(t.peek().getType() == TokenType.LBRACE) {
			consume(t, TokenType.LBRACE);
			ConditionNode c = parseCondition(t);
			consume(t, TokenType.RBRACE);
			c.addBraces();
			return c;
		}
		
		//expr rel expr
		Expression e1 = parseExpression(t);
		Token rel = t.next();
		if(rel.getType().category() == TokenCategory.RELOP) {
			Expression e2 = parseExpression(t);
			return new RelationNode(e1, rel.getType(), e2);
		}
		else
			throw new SyntaxError();
	}

	public static Expression parseExpression(Tokenizer t) throws SyntaxError {
		Expression t1 = parseTerm(t);
		while (t.peek().getType().category() == TokenCategory.ADDOP) {
			TokenType tt = t.next().getType();
			Expression t2 = parseTerm(t);
			t1 = new Expression(t1, tt, t2);
		}
		return t1;
	}

	public static Expression parseTerm(Tokenizer t) throws SyntaxError {
		Expression f1 = parseFactor(t);
		while (t.peek().getType().category() == TokenCategory.MULOP) {
			TokenType tt = t.next().getType();
			Expression f2 = parseFactor(t);
			f1 = new Expression(f1, tt, f2);
		}
		return f1;
	}

	public static Expression parseFactor(Tokenizer t) throws SyntaxError {
		if (t.peek().getType() == TokenType.MEM || t.peek().getType().category() == TokenCategory.MEMSUGAR)
			return parseMem(t);
		
		if (t.peek().getType() == TokenType.LPAREN) {
			consume(t, TokenType.LPAREN);
			Expression e = parseExpression(t);
			consume(t, TokenType.RPAREN);
			e.addParen();
			return e;
		}
		
		if (t.peek().getType() == TokenType.NUM)
			return new NumNode(t.next().toNumToken().getValue() + "");
		
		if (t.peek().getType() == TokenType.MINUS) {
			consume(t, TokenType.MINUS);
			Expression f = parseFactor(t);
			return new FactorNode(null, TokenType.MINUS, f);
		}
		
		return parseSensor(t);
	}

	public static Expression parseSensor(Tokenizer t) throws SyntaxError {
		if (t.peek().getType().category() == TokenCategory.SENSOR) {
			if (t.peek().getType() == TokenType.SMELL)
				return new SensorNode(null, t.next().getType(), null);
			
			TokenType tt = t.next().getType();
			consume(t, TokenType.LBRACKET);
			Expression e = parseExpression(t);
			consume(t, TokenType.RBRACKET);
			return new SensorNode(null, tt, e);
		}
		else
			throw new SyntaxError();
	}

	/**
	 * Consumes a token of the expected type.
	 * 
	 * @throws SyntaxError
	 *             if the wrong kind of token is encountered.
	 */
	public static void consume(Tokenizer t, TokenType tt) throws SyntaxError {
		if (t.next().getType() != tt)
			throw new SyntaxError();
	}
}
