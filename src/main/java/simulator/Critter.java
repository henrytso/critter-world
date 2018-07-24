package simulator;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;

import ast.Program;
import ast.Rule;
import interpret.CritterRunner;
import interpret.Interpreter;
import interpret.Outcome;

public class Critter {
	Program program;
	String species;
	int col;
	int row;
	int[] mem;
	int dir;

	SimulatorImpl sim;
	Rule lastRule = null;

	int critterId;
	int creatorId;

	/**
	 * Constructs a Critter with a program AST {@code p} at specified location and
	 * direction, attributes, and species
	 * 
	 * @param p
	 *            Program AST head node
	 * @param s
	 *            Species of critter
	 * @param c
	 *            Column of critter
	 * @param r
	 *            Row of critter
	 * @param mem
	 *            Attributes of critter
	 * @param dir
	 *            Direction critter is facing
	 */
	public Critter(Program p, String s, int c, int r, int[] mem, int dir, SimulatorImpl sim) {
		program = p;
		species = s;
		col = c;
		row = r;
		this.mem = mem;
		this.dir = dir; // Has to be less than 6
		this.sim = sim;
	}

	public void setIds(int critId, int createId) {
		critterId = critId;
		creatorId = createId;
	}

	public int critterId() {
		return critterId;
	}

	public int creatorID() {
		return creatorId;
	}

	/**
	 * Executes rules until an action or until max rules is reached
	 * 
	 * @return Outcome of running an action (could be mate)
	 */
	public Outcome advanceTime() {
		Interpreter runner = new CritterRunner(this);
		return runner.interpret();
	}

	/**
	 * May not be needed, but in case more attributes are added than the original
	 * MIN_MEMORY size of 8, resizes the mem array
	 * 
	 * @param size
	 *            Size of new array
	 */
	public void resizeArray(int size) {
		System.arraycopy(mem, 0, new int[size], 0, mem.length);
	}

	/**
	 * Returns the head of the critter's AST
	 * 
	 * @return Head of AST (program node)
	 */
	public Program getProgram() {
		return program;
	}

	/**
	 * Returns species of critter
	 * 
	 * @return Species of critter
	 */
	public String getSpecies() {
		return species;
	}

	/**
	 * Returns the array of attributes
	 * 
	 * @return Array of attributes
	 */
	public int[] getMem() {
		return mem;
	}

	/**
	 * Returns the last rule executed
	 * 
	 * @return Last rule
	 */
	public Rule getLastRule() {
		return lastRule;
	}

	/**
	 * Sets the last rule to {@code r}
	 * 
	 * @param r
	 *            Rule to set as last rule
	 */
	public void setLastRule(Rule r) {
		lastRule = r;
	}

	/**
	 * Returns direction the critter is facing
	 * 
	 * @return Direction critter is facing
	 */
	public int getDir() {
		return dir;
	}

	/**
	 * Returns appearance of critter in the form SSTTPPD
	 * 
	 * @return Appearance
	 */
	public int appearence() {
		return mem[3] * 100000 + mem[6] * 1000 + mem[7] * 10 + dir;
	}

	// returns 0 if this hex is not in bounds.
	/**
	 * Returns what is {@code dist} units ahead of the critter
	 * 
	 * @param dist
	 *            Units to look ahead
	 * @param dir
	 *            Direction the critter is facing. {@code dir} should equal this.dir
	 *            This design is used for easy reusability in nearby method
	 * @return 0 if out of bounds -1 if is a rock -X - 1 if is food == X appearance
	 *         of critter if is a critter
	 */
	public int ahead(int dist, int dir) {
		if (dist < 0)
			dist = 0;

		Hex h;
		int newRow = row;
		int newCol = col;

		switch (dir) {
		case 0:
			newRow = row + dist;
			break;
		case 1:
			newCol = col + dist;
			newRow = row + dist;
			break;
		case 2:
			newCol = col + dist;
			break;
		case 3:
			newRow = row - dist;
			break;
		case 4:
			newRow = row - dist;
			newCol = col - dist;
			break;
		default: // case 5
			newCol = col - dist;
		}

		if (!sim.isInBounds(newCol, newRow))
			return 0;

		h = sim.getHex(newCol, newRow);

		if (h.isRock())
			return -1;
		if (h.isFood())
			return -h.getFood() - 1;
		if (h.isCritter())
			return h.getCritter().appearence();
		else
			return 0;
	}

	/**
	 * Returns what is 1 unit away from the critter in direction {@code dir}
	 * 
	 * @param dir
	 *            Direction to look
	 * @return 0 if out of bounds -1 if is a rock -X - 1 if is food == X appearance
	 *         of critter if is a critter
	 */
	public int nearby(int dir) {
		return ahead(1, dir);
	}

	public int smell() {
		PriorityQueue<HexNode> frontier = new PriorityQueue<HexNode>();
		HexNode root = new HexNode(col, row, dir, 0, null);
		frontier.add(root);

		while (!frontier.isEmpty()) {
			HexNode n = frontier.poll();
			if (n.dist() == 10) // max smell distance
				return 1000000;

			HexNode node1 = new HexNode(n.col(), n.row(), (n.dir() + 1) % 6, n.dist() + 1, n);
			HexNode node2 = new HexNode(n.col(), n.row(), n.dir() == 0 ? 5 : n.dir() - 1, n.dist() + 1, n);
			HexNode node3 = n.forward();

			ArrayList<HexNode> edges = new ArrayList<HexNode>();
			edges.add(node1);
			edges.add(node2);
			if (sim.isInBounds(node3.col(), node3.row())) {
				if (sim.world[node3.col()][node3.row()].isFood()) {
					// Have found closest food
					HexNode current = n;
					while (!(current.col() == col && current.row() == row)) {
						current = current.parent();
					}
					return n.dist() * 1000 + current.dir();
				}

				if (sim.getHex(node3.col(), node3.row()).isEmpty())
					edges.add(node3);
			}

			for (HexNode edge : edges) {
				if (!frontier.contains(edge))
					frontier.add(edge);
			}

		}

		return 1000000;
	}

	// Action methods

	/**
	 * Helper method: Moves critter to hex in a given direction
	 * 
	 * @param dir
	 *            Direction to move
	 * @return Hex in direction {@code dir} or null if out of bounds
	 */
	public Hex moveToDir(int dir) {
		switch (dir) {
		case 0:
			if (sim.isInBounds(col, row + 1)) {
				return sim.getWorld()[col][++row];
			}
			break;
		case 1:
			if (sim.isInBounds(col + 1, row + 1))
				return sim.getWorld()[++col][++row];
			break;
		case 2:
			if (sim.isInBounds(col + 1, row))
				return sim.getWorld()[++col][row];
			break;
		case 3:
			if (sim.isInBounds(col, row - 1))
				return sim.getWorld()[col][--row];
			break;
		case 4:
			if (sim.isInBounds(col - 1, row - 1))
				return sim.getWorld()[--col][--row];
			break;
		default:
			if (sim.isInBounds(col - 1, row))
				return sim.getWorld()[--col][row];
			break;
		}
		return null;
	}

	/**
	 * Returns adjacent hex in direction {@code dir}
	 * 
	 * @param dir
	 *            Direction of hex in relation to critter
	 * @return Hex in given direction
	 */
	public Hex hexAtDir(int dir) {
		switch (dir) {
		case 0:
			if (sim.isInBounds(col, row + 1)) {
				return sim.getWorld()[col][row + 1];
			}
			break;
		case 1:
			if (sim.isInBounds(col + 1, row + 1))
				return sim.getWorld()[col + 1][row + 1];
			break;
		case 2:
			if (sim.isInBounds(col + 1, row))
				return sim.getWorld()[col + 1][row];
			break;
		case 3:
			if (sim.isInBounds(col, row - 1))
				return sim.getWorld()[col][row - 1];
			break;
		case 4:
			if (sim.isInBounds(col - 1, row - 1))
				return sim.getWorld()[col - 1][row - 1];
			break;
		default:
			if (sim.isInBounds(col - 1, row))
				return sim.getWorld()[col - 1][row];
			break;
		}
		return null; // maybe change
	}

	/**
	 * Returns column of hex in direction {@code dir}
	 * 
	 * @param dir
	 *            Direction of critter
	 * @return Column of hex in direction {@code dir}
	 */
	public int colAtDir(int dir) {
		switch (dir) {
		case 0:
			return col;
		case 1:
			return col + 1;
		case 2:
			return col + 1;
		case 3:
			return col;
		case 4:
			return col - 1;
		default:
			return col - 1;
		}
	}

	/**
	 * Returns row of hex in direction {@code dir}
	 * 
	 * @param dir
	 *            Direction of critter
	 * @return Row of hex in direction {@code dir}
	 */
	public int rowAtDir(int dir) {
		switch (dir) {
		case 0:
			return row + 1;
		case 1:
			return row + 1;
		case 2:
			return row;
		case 3:
			return row - 1;
		case 4:
			return row - 1;
		default:
			return row;
		}
	}

	/**
	 * Uses {@code n} energy and returns whether critter died (kills it too)
	 * 
	 * @param n
	 *            Energy to decrease
	 * @return True if died, false if still alive
	 */
	public boolean decEnergy(int n) {
		mem[4] -= n;
		if (mem[4] <= 0) {
			sim.die(this);
			return true;
		}

		return false;
	}

	/**
	 * Does nothing and gains energy from sun
	 */
	public void waitAction() {
		mem[4] += sim.constants.get("SOLAR_FLUX");
		if (mem[4] > mem[3] * sim.constants.get("ENERGY_PER_SIZE"))
			mem[4] = mem[3] * sim.constants.get("ENERGY_PER_SIZE").intValue();
	}

	/**
	 * Moves critter forward one step if possible
	 */
	public boolean forward() {
		if (decEnergy(sim.constants.get("MOVE_COST").intValue() * mem[3]))
			return false;

		if (nearby(dir) == 0) {
			sim.getWorld()[col][row] = new Hex();
			Hex h = moveToDir(dir);
			if (h != null) {
				h.addCritter(this);
			} else
				sim.getWorld()[col][row].addCritter(this);
		}
		return true;
	}

	/**
	 * Moves critter backward one step if possible
	 */
	public boolean backward() {
		if (decEnergy(sim.constants.get("MOVE_COST").intValue() * mem[3]))
			return false;

		int backwardDir = (dir + 3) % 6;
		if (nearby(backwardDir) == 0) {
			sim.getWorld()[col][row] = new Hex();
			Hex h = moveToDir(backwardDir);
			if (h != null) {
				h.addCritter(this);
			} else
				sim.getWorld()[col][row].addCritter(this);
		}
		return true;
	}

	/**
	 * Turns critter left if {@code i} is -1 or right if 1
	 * 
	 * @param i
	 *            -1 to turn left, 1 to turn right
	 */
	public boolean turn(int i) {
		if (decEnergy(mem[3]))
			return false;

		dir += i;
		if (dir == -1)
			dir = 5;
		dir %= 6;

		return true;
	}

	/**
	 * Eats food in front if possible, increasing critter's energy
	 */
	public boolean eat() {
		if (decEnergy(mem[3]))
			return false;

		// If there is food
		if (nearby(dir) < -1) {
			int maxEdible = mem[3] * sim.constants.get("ENERGY_PER_SIZE").intValue() - mem[4];
			if ((-(nearby(dir) + 1)) > maxEdible) {
				mem[4] += maxEdible;
				hexAtDir(dir).addFood(-maxEdible);
			} else {
				mem[4] += hexAtDir(dir).getFood();
				hexAtDir(dir).addFood(-hexAtDir(dir).getFood());
			}
		}
		return true;
	}

	/**
	 * Decrease energy by {@code amount} and places it as food in Hex in front
	 * 
	 * @param amount
	 *            Amount of food to serve
	 */
	public boolean serve(int amount) {
		if (nearby(dir) == 0 || nearby(dir) < -1) {
			if (amount > mem[4])
				amount = mem[4];
			decEnergy(amount + mem[3]);
			if (hexAtDir(dir) != null)
				hexAtDir(dir).addFood(amount);
			return true;
		}
		return false;
	}

	/**
	 * Attacks critter in front if able
	 */
	public boolean attack() {
		if (decEnergy(mem[3] * sim.constants.get("ATTACK_COST").intValue()))
			return false;

		// If there is a critter
		if (nearby(dir) > 0)
			dealDamage(hexAtDir(dir).getCritter());
		return true;
	}

	/**
	 * Tags a critter in front
	 * 
	 * @param t
	 *            Tag to apply
	 */
	public boolean tag(int t) {
		if (decEnergy(mem[3]))
			return false;

		if (nearby(dir) > 0)
			hexAtDir(dir).getCritter().mem[6] = t;
		return true;
	}

	/**
	 * Grows by one size if possible
	 */
	public boolean grow() {
		int energy = mem[3] * complexity() * sim.constants.get("GROW_COST").intValue();
		if (decEnergy(energy))
			return false;

		mem[3] = mem[3] + 1;
		return true;
	}

	/**
	 * Buds and spawns a critter behind if possible (chance of mutation)
	 */
	public boolean bud() {
		if (decEnergy(sim.constants.get("BUD_COST").intValue() * complexity()))
			return false;

		Random r = new Random();
		int p = r.nextInt(4); // probability of mutation

		int newCol = col;
		int newRow = row;

		switch (dir) {
		case 0:
			newRow--;
			break;
		case 1:
			newCol--;
			newRow--;
			break;
		case 2:
			newCol--;
			break;
		case 3:
			newRow++;
			break;
		case 4:
			newCol++;
			newRow++;
			break;
		default:
			newCol++;
			break;
		}

		Critter baby = new Critter(program, species, newCol, newRow, mem, dir, this.sim);

		while (p == 0) {
			int i = r.nextInt(2); // probability of mutating Program

			if (i == 0)
				baby.program = baby.program.mutate();
			else {
				i = r.nextInt(3); // probability of changing each attribute
				int a = r.nextInt(2); // increment or decrement

				switch (i) {
				case 0:
					baby.mem[0] = a == 0 ? baby.mem[0] + 1 : baby.mem[0] - 1;
					if (baby.mem[0] < 8)
						baby.mem[0] = 8;
					break;
				case 1:
					baby.mem[1] = a == 0 ? baby.mem[1] + 1 : baby.mem[1] - 1;
					if (baby.mem[1] < 1)
						baby.mem[1] = 1;
					break;
				default:
					baby.mem[2] = a == 0 ? baby.mem[2] + 1 : baby.mem[2] - 1;
					if (baby.mem[2] < 1)
						baby.mem[2] = 1;
					break;
				}
			}

			p = r.nextInt(4);
		}

		baby.mem[3] = 1;
		baby.mem[4] = sim.constants.get("INITIAL_ENERGY").intValue();
		baby.mem[6] = 0;
		baby.mem[7] = 0;

		sim.addCritter(newCol, newRow, baby);
		return true;
	}

	/**
	 * Returns complexity factor
	 * 
	 * @return Complexity
	 */
	public int complexity() {
		// r · RULE COST + (offense + defense) · ABILITY COST
		int r = program.getRules().size();
		int ruleCost = sim.constants.get("RULE_COST").intValue();
		int abilityCost = sim.constants.get("ABILITY_COST").intValue();
		return (r * ruleCost) + ((mem[1] + mem[2]) * abilityCost);
	}

	/**
	 * Deals damage to {@code victim} critter
	 * 
	 * @param victim
	 *            Critter getting attacked
	 */
	public void dealDamage(Critter victim) {
		double base = sim.constants.get("BASE_DAMAGE");
		double inc = sim.constants.get("DAMAGE_INC");
		int s1 = mem[3];
		int s2 = victim.mem[3];
		int o1 = mem[2];
		int d2 = victim.mem[1];
		double x = inc * (s1 * o1 - s2 * d2);
		double p = 1. / (1 + Math.pow(Math.E, -x));

		victim.mem[4] -= (int) (base * s1 * p);
	}

	/**
	 * Returns column of critter
	 * 
	 * @return Critter's column
	 */
	public int col() {
		return col;
	}

	/**
	 * Returns row of critter
	 * 
	 * @return Critter's row
	 */
	public int row() {
		return row;
	}

	/**
	 * Returns index of rule {@code r} in program
	 * 
	 * @param r
	 *            Rule to look for
	 * @return Index of rule {@code r}
	 */
	public int indexOf(Rule r) {
		for (int i = 0; i < program.getRules().size(); i++) {
			if (program.getRules().get(i).equals(r))
				return i;
		}
		return 0;
	}

	/**
	 * Returns if critter's id matches {@code id} or if id is an admin id which has
	 * a "2" in front
	 * 
	 * @param id
	 *            Id to check
	 * @return Whether IDs match
	 */
	public boolean viewableBy(int id) {
		String prefix = String.valueOf(id).substring(0, 1);
		return prefix.equals("2") || creatorId == id;
	}
}
