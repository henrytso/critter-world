package main;

import java.io.FileNotFoundException;
import java.io.FileReader;

import ast.Program;
import parse.Parser;
import parse.ParserFactory;

public class ParseAndMutateApp {

	public static void main(String[] args) {
		int n = 0;
		String file;
		try {
			if (args.length == 1) {
				file = args[0];
			} else if (args.length == 3 && args[0].equals("--mutate")) {
				n = parsePositive(args[1]);
				file = args[2];
			} else {
				throw new IllegalArgumentException();
			}
			
			try {
				FileReader fr = new FileReader(file);
				Parser parser = ParserFactory.getParser();
				Program p = parser.parse(fr);
				StringBuilder sb = new StringBuilder();
				System.out.println(p.prettyPrint(sb));
				
				for(int i = 0; i < n; i++) {
					p = p.mutate();
					sb = new StringBuilder();
					System.out.println(p.prettyPrint(sb));
				}
				
			} catch (FileNotFoundException e) {
				System.out.println("File not found");
			}
			
		} catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Usage:\n" + "  <input_file>\n" + " --mutate <n> <input_file");
		}
	}

	/**
     * Parses {@code str} to an integer.
     * 
     * @param str
     *            the string to parse
     * @return the integer represented by {@code str}
     * @throws NumberFormatException
     *             if {@code str} does not contain a parsable integer
     * @throws IllegalArgumentException
     *             if {@code str} represents a negative integer
     */
	public static int parsePositive(String str) {
		int n = Integer.parseInt(str);
		if (n < 0) { throw new IllegalArgumentException(); }
		else { return n; }
	}
}
