package tests;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import ast.*;
import parse.*;

/**
 * This class contains tests for the mutate() and mutateNode() methods.
 */
public class SpecParserTest {
	public static void main(String[] args) throws IOException {
		try {
			FileReader fr = new FileReader("test.txt");

			BufferedReader br = new BufferedReader(fr);
			
			SpecParser sp = new SpecParser();
			br.readLine();
			Reader r = sp.parseSpecs(br);
			
			int[] mem = sp.getAttributes();
			for (int i = 0; i < mem.length; i++) {
				System.out.println(mem[i]);
			}
			
			Parser parser = ParserFactory.getParser();
			Program p = parser.parse(r);
			
			System.out.println(p);
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		}
	}
}