package tests;

import java.io.FileNotFoundException;
import java.io.FileReader;

import ast.*;
import parse.*;

/**
 * This class contains tests for the clone() method.
 */
public class CloneTest {
	public static void main(String[] args) {
		try {
			FileReader fr = new FileReader("test.txt");
			Parser parser = ParserFactory.getParser();
			Program p = parser.parse(fr);
			StringBuilder sb = new StringBuilder();
			System.out.println(p.prettyPrint(sb));
			Program copy = p.clone();
			StringBuilder sb2 = new StringBuilder();
			System.out.println(copy.prettyPrint(sb2));
			assert(sb.equals(sb2));
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		}
	}
}
