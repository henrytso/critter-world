package simulator;

import java.util.Comparator;

public class HexNode implements Comparator<HexNode>, Comparable<HexNode>{
	private int col;
	private int row;
	private int direction;
	private int distance;
	private HexNode parent;
	
	public HexNode(int c, int r, int dir, int dist, HexNode parent) {
		col = c;
		row = r;
		direction = dir;
		distance = dist;
		this.parent = parent;
	}
	
	public int col() {
		return col;
	}
	
	public int row() {
		return row;
	}
	
	public int dir() {
		return direction;
	}
	
	public int dist() {
		return distance;
	}
	
	public HexNode parent() {
		return parent;
	}
	
	public void setDist(int distance) {
		this.distance = distance;
	}
	
	public void setParent(HexNode parent) {
		this.parent = parent;
	}
	
	/**
	 * Makes a HexNode representing the hex in front of the current one,
	 * given the orientation of the critter.
	 * Distance is one more than distance of this object.
	 * @return
	 */
	public HexNode forward() {
		int newCol;
		int newRow;
		
		switch (direction) {
		case 0:
			newCol = col;
			newRow = row + 1;
			break;
		case 1:
			newCol = col + 1;
			newRow = row + 1;
			break;
		case 2:
			newCol = col + 1;
			newRow = row;
			break;
		case 3:
			newCol = col;
			newRow = row - 1;
			break;
		case 4:
			newCol = col - 1;
			newRow = row - 1;
			break;
		default:
			newCol = col - 1;
			newRow = row;
			break;
		}
		return new HexNode(newCol, newRow, direction, distance + 1, this);
	}
	
	public boolean equals(Object obj) {
		if(!(obj instanceof HexNode))
			return false;
		
		HexNode n = (HexNode) obj;
		return row == n.row() && direction == n.dir() && col == n.col();
	}

	@Override
	public int compare(HexNode o1, HexNode o2) {
		return o1.distance - o2.distance; 
	}

	@Override
	public int compareTo(HexNode o) {
		return distance - o.distance; 
	}
	
}
