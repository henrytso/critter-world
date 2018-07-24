package parse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class SpecParser {
	
	int[] mem = new int[8];
	
	/**
	 * Uses a reader to read and parse initial specs, returning the reader with
	 * the marked position at the end of the specs, so it is ready to parse the
	 * program afterward.
	 * 
	 * @param r
	 *        Reader to use (we usually use a BufferedReader)
	 * @return Reader with marked position at the end of specs.
	 */
	public Reader parseSpecs(Reader r) {
		BufferedReader br = new BufferedReader(r);
		// Parsing species line elsewhere
		try {
			boolean done = false;
			while (br.ready() && !done) {
				br.mark(1000); // # of characters in a rule line must be < 1000
				done = parseSpec(br.readLine());
			}
			if (done) {
				br.reset();
			}
			if (mem[0] < 8) // memsize
				mem[0] = 8;
			if (mem[3] == 0) // size
				mem[3] = 1;
			
		} catch (IOException e) {
			System.out.println("The critter ");
		}
		return br;
	}
	
	/**
	 * Parses attributes line and stores value in array Returns false if
	 * attributes was found (to keep parsing) or true if no attributes was found
	 * (we are done) parsing attributes or not
	 * 
	 * @param line
	 *        Attributes line to parse
	 * @return Done parsing attributes or not
	 */
	boolean parseSpec(String line) {
		if (line.contains(":")) {
			int colon = line.indexOf(":");
			switch (line.substring(0, colon)) {
			case "memsize":
				mem[0] = Integer.parseInt(line.substring(colon + 2));
				break;
			case "defense":
				mem[1] = Integer.parseInt(line.substring(colon + 2));
				break;
			case "offense":
				mem[2] = Integer.parseInt(line.substring(colon + 2));
				break;
			case "size":
				mem[3] = Integer.parseInt(line.substring(colon + 2));
				break;
			case "energy":
				mem[4] = Integer.parseInt(line.substring(colon + 2));
				break;
			case "posture":
				mem[7] = Integer.parseInt(line.substring(colon + 2));
				break;
			default:
				return true;
			}
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * Returns an int array of the attributes
	 * 
	 * @return int array of the attributes
	 */
	public int[] getAttributes() {
		int[] newMem = new int[mem[0]];
		System.arraycopy(mem, 0, newMem, 0, mem.length);
		mem = newMem;
		return mem;
	}
}