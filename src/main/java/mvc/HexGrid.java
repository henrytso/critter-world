package mvc;

import java.util.ArrayList;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Affine;
import javafx.scene.transform.MatrixType;
import javafx.scene.transform.NonInvertibleTransformException;
import simulator.Critter;
import simulator.Hex;
import simulator.Simulator;

/**
 * A canvas that supports a hexagonal grid 
 * populated by critters, food and rocks.
 */
public class HexGrid extends Canvas {
	private Simulator sim;
	private TextArea infoArea; //Area with info on critters
	private int ROWS;
	private int COLUMNS;
	private int highlightedRow = -1;
	private int highlightedCol = -1;
	private static Color[] speciesColors = { Color.RED, Color.YELLOW,
			Color.GREEN, Color.BLUE, Color.MAGENTA, Color.ANTIQUEWHITE,
			Color.BURLYWOOD, Color.CORNFLOWERBLUE, Color.BISQUE,
			Color.NAVAJOWHITE, Color.KHAKI, Color.TOMATO, Color.BLACK,
			Color.SILVER, Color.HOTPINK, Color.ORANGE, Color.GAINSBORO };
	//Names of critter species
	private static ArrayList<String> names = new ArrayList<String>();
	
	// Size is horizontal length in pixels
	public static double SIDE_OF_HEX = 80;
	public static double CRITTER_SIZE = 10;
	public static double CRITTER_INCR = 10;
	public static double FOOD_SIZE = 5;
	public static double FOOD_INCR = 5;
	public static double ROCK_SIZE = 80;
	public static double PADDING = 50;
	
	/**
	 * Creates a hexagonal grid that displays information
	 * from the specified world and prints characteristics of a
	 * selected critter in infoBox.
	 * @param sim
	 * 			The world that is displayed
	 * @param infoBox
	 * 			Place for displaying info about critters.
	 */
	public HexGrid(Simulator sim, TextArea infoBox) {
		COLUMNS = sim.getWorld().length;
		ROWS = sim.getWorld()[0].length;
		this.sim = sim;
		this.infoArea = infoBox;
		
		// Setting size of canvas with padding
		this.setWidth(2 * SIDE_OF_HEX + 1.5 * SIDE_OF_HEX * (COLUMNS - 1)
				+ 2 * PADDING);
		
		int maxRows = 0;
		while (2 * maxRows < 2 * ROWS - COLUMNS)
			maxRows++;
		
		this.setHeight(maxRows * SIDE_OF_HEX * Math.sqrt(3.) + 2 * PADDING);
		
		setupEventHandlers();
	}
	
	/**
	 * Rescales the world by either 3/4 or 4/3
	 * 
	 * @param i
	 *        1 for zooming in -1 for zooming out
	 */
	public void rescale(int i) {
		double factor = i == -1 ? 3./4. :  4./3.;
		SIDE_OF_HEX *= factor;
		CRITTER_SIZE *= factor;
		CRITTER_INCR *= factor;
		FOOD_SIZE *= factor;
		FOOD_INCR *= factor;
		ROCK_SIZE *= factor;
		PADDING *= factor;
	}
	
	/**
	 * This method is run when a user clicks on the grid.
	 * If the user clicked on a hex, that hex is highlighted
	 * and the information about any critters in it is displayed.
	 */
	private void setupEventHandlers() {
		setOnMousePressed(new EventHandler<MouseEvent>() {
			
			@Override
			public void handle(MouseEvent e) {
				if (infoArea == null)
					System.out.println("info area ia null");
				infoArea.clear();
				
				double x = e.getX();
				double y = e.getY();
				
				int col = 0;
				double xcm = PADDING + SIDE_OF_HEX;
				while (xcm < x) {
					xcm += 1.5 * SIDE_OF_HEX;
					col++;
				}
				int row = (col / 2) + (col % 2);
				double ycm = getHeight() - PADDING
						- Math.sqrt(3.) / 2 * SIDE_OF_HEX;
				if (col % 2 == 1)
					ycm = ycm - Math.sqrt(3.) / 2 * SIDE_OF_HEX;
				
				while (ycm > y) {
					ycm -= Math.sqrt(3) * SIDE_OF_HEX;
					row++;
				}
				
				double distance = Integer.MAX_VALUE;
				int finalCol = -1;
				int finalRow = -1;
				if (sim.isInBounds(col, row)) {
					distance = distance(col, row, x, y);
					finalCol = col;
					finalRow = row;
				}
				if (sim.isInBounds(col - 1, row)) {
					if (distance(col - 1, row, x, y) < distance) {
						distance = distance(col - 1, row, x, y);
						finalCol = col - 1;
						finalRow = row;
					}
				}
				if (sim.isInBounds(col - 1, row - 1)) {
					if (distance(col - 1, row - 1, x, y) < distance) {
						distance = distance(col - 1, row - 1, x, y);
						finalCol = col - 1;
						finalRow = row - 1;
					}
				}
				if (sim.isInBounds(col, row - 1)) {
					if (distance(col, row - 1, x, y) < distance) {
						distance = distance(col, row - 1, x, y);
						finalCol = col;
						finalRow = row - 1;
					}
				}
				
				highlightedCol = finalCol;
				highlightedRow = finalRow;
				draw();
				
				printCritterInfo();
			}
		});
	}
	
	/**
	 * Prints information about the species, memory array, program and 
	 * last executed rule of the critter in the hex that was last clicked on.
	 */
	public void printCritterInfo() {
		if (highlightedCol == -1 || highlightedRow == -1)
			return;
		if (sim.getHex(highlightedCol, highlightedRow).isCritter()) {
			Critter c = sim.getHex(highlightedCol, highlightedRow).getCritter();
			infoArea.appendText("Species: " + c.getSpecies() + "\n");
			int[] mem = c.getMem();
			infoArea.appendText("Memory: ");
			for (int i : mem) {
				infoArea.appendText(" " + i);
			}
			infoArea.appendText("\n");
			infoArea.appendText("Program: " + c.getProgram());
			infoArea.appendText("Last rule: " + c.getLastRule());
		}
	}
	
	/**
	 * Returns the distance between center of hex with coordinates (col, row)
	 * and the position on the canvas(in pixels) (x,y)
	 * @param col the column of the desired hex
	 * @param row the row of the desired hex
	 * @param x the x-coordinate of position
	 * @param y the y-coordinate of position
	 * @return distance in pixels
	 */
	public double distance(double col, double row, double x, double y) {
		double x0 = PADDING + SIDE_OF_HEX;
		double y0 = getHeight() - PADDING - Math.sqrt(3.) / 2 * SIDE_OF_HEX;
		double x2 = x0;
		double y2 = y0 - Math.sqrt(3.) * SIDE_OF_HEX;
		double x1 = x0 + 3 * SIDE_OF_HEX / 2;
		double y1 = y0 + Math.sqrt(3.) * SIDE_OF_HEX / 2;
		
		double xHex = col * (x1 - x0) + row * (x2 - x0) + x0;
		double yHex = col * (y1 - y0) + row * (y2 - y0) + y0;
		
		return Math.hypot(xHex - x, yHex - y);
	}
	
	/**
	 * Draws the grid and all of the critters, food and rocks in the world.
	 */
	public void draw() {
		GraphicsContext g = getGraphicsContext2D();
		g.setFill(Color.ANTIQUEWHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		int maxRows = 0;
		while (2 * maxRows < 2 * ROWS - COLUMNS)
			maxRows++;
		
		double cx = PADDING + SIDE_OF_HEX; // x coordinate of center
		for (int col = 0; col < COLUMNS; col++) {
			int tempRows = maxRows;
			double cy = getHeight() - PADDING - Math.sqrt(3.) / 2 * SIDE_OF_HEX;
			if (col % 2 == 1) {
				tempRows--;
				cy = cy - Math.sqrt(3.) / 2 * SIDE_OF_HEX;
			}
			int startRow = (col / 2) + (col % 2);
			
			for (int row = 0; row < tempRows; row++) {
				if (col == highlightedCol && row + startRow == highlightedRow)
					drawHex(col, row + startRow, cx, cy, true);
				else
					drawHex(col, row + startRow, cx, cy, false);
				cy -= Math.sqrt(3.) * SIDE_OF_HEX;
			}
			
			cx += 1.5 * SIDE_OF_HEX;
		}
		
	}
	
	/**
	 * Draws a hexagon representing (c, r) in the world
	 * and draws anything found in that hex.
	 * @param c the column coordinate of hex
	 * @param r the row coordinate of hex
	 * @param cx x-coordinate of hex's center
	 * @param cy y-coordinate of hex's center
	 * @param fill 
	 * 		whether or not to fill in the background of the hexagon
	 * 		true if you want to highlight it.
	 */
	public void drawHex(int c, int r, double cx, double cy, boolean fill) {
		GraphicsContext g = getGraphicsContext2D();
		
		g.setStroke(Color.BLACK);
		
		double s = SIDE_OF_HEX;
		double rt3 = Math.sqrt(3.);
		
		if (fill) {
			g.setFill(Color.AQUA);
			g.fillPolygon(
					new double[] { cx - s / 2., cx + (s / 2.), cx + s,
							cx + (s / 2.), cx - (s / 2.), cx - s },
					new double[] { cy + rt3 * (s / 2.), cy + rt3 * (s / 2.), cy,
							cy - rt3 * (s / 2.), cy - rt3 * (s / 2.), cy },
					6);
		}
		
		// Draws sides
		g.strokeLine(cx - s / 2., cy + rt3 * (s / 2.), cx + (s / 2.),
				cy + rt3 * (s / 2.));
		g.strokeLine(cx + (s / 2.), cy + rt3 * (s / 2.), cx + s, cy);
		g.strokeLine(cx + s, cy, cx + (s / 2.), cy - rt3 * (s / 2.));
		g.strokeLine(cx + (s / 2.), cy - rt3 * (s / 2.), cx - (s / 2.),
				cy - rt3 * (s / 2.));
		g.strokeLine(cx - (s / 2.), cy - rt3 * (s / 2.), cx - s, cy);
		g.strokeLine(cx - s, cy, cx - (s / 2.), cy + rt3 * (s / 2.));
		
		// Draws object
		Hex h = sim.getHex(c, r);
		if (h.isRock()) {
			g.setFill(Color.DARKGREY);
			g.fillOval(cx - ROCK_SIZE / 2, cy - ROCK_SIZE / 2, ROCK_SIZE,
					ROCK_SIZE);
		} else if (h.isFood()) {
			g.setFill(Color.ORANGE);
			double size = FOOD_SIZE + (FOOD_INCR * (h.getFood() / 100));
			size = (int) Math.min(size, SIDE_OF_HEX * (Math.sqrt(3.)));
			g.fillOval(cx - size / 2, cy - size / 2, size, size);
		} else if (h.isCritter()) {
			String species = h.getCritter().getSpecies();
			int i = names.indexOf(species);
			if (i == -1) {
				names.add(species);
				i = names.size() - 1;
			}
			g.setFill(speciesColors[i % 17]);
			drawCritter(h.getCritter(), cx, cy, g);
		}
		
	}
	
	/**
	 * Draws the critter in the hex centered at (cx, cy).
	 * The tip of the triangle is pointed in the direction
	 * in which the critter is facing.
	 * @param c 
	 * 		 the critter to be drawn
	 * @param cx
	 * 		 the x-coordinate of the center of the critter's hex
	 * @param cy 
	 * 		 the y-coordinate of the center of the critters's hex
	 * @param g
	 * 		 graphics context which will be used to draw
	 */
	private void drawCritter(Critter c, double cx, double cy,
			GraphicsContext g) {
		double size = CRITTER_SIZE + (CRITTER_INCR * c.getMem()[3]);
		size = (int) Math.min(size, SIDE_OF_HEX * (Math.sqrt(3.)));
		
		int dir = c.getDir();
		double theta = Math.PI / 2.; // Angle btwn +x axis and ray to tip of
										// triangle
		while (dir > 0) {
			theta -= Math.PI / 3.;
			dir--;
		}
		double p1x = cx + size * Math.cos(theta); //x,y of tip of triangle
		double p1y = cy - size * Math.sin(theta);
		
		double p2x = cx + size * Math.cos(theta + 5. / 6. * Math.PI);
		double p2y = cy - size * Math.sin(theta + 5. / 6. * Math.PI);
		
		double p3x = cx + size * Math.cos(theta + 7. / 6. * Math.PI);
		double p3y = cy - size * Math.sin(theta + 7. / 6. * Math.PI);
		
		g.fillPolygon(new double[] { p1x, p2x, p3x },
				new double[] { p1y, p2y, p3y }, 3);
		
		// InputStream in =
		// getClass().getResourceAsStream("/resources/kozen.jpg");
		// Image img = new Image(in, size, size, true, false);
		//
		// ImageView iv = new ImageView(img);
		//
		// int dir = c.getDir();
		// int rotateBy = 0;
		// while (dir > 0) {
		// rotateBy += 60;
		// dir--;
		// }
		//
		// iv.setRotate(rotateBy);
		// SnapshotParameters params = new SnapshotParameters();
		// params.setFill(Color.TRANSPARENT);
		// Image rotatedImage = iv.snapshot(params, null);
		//
		// g.drawImage(rotatedImage, cx - size / 2., cy - size / 2.);
	}
	
}
