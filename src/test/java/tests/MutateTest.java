package tests;

import java.io.FileNotFoundException;
import java.io.FileReader;

import ast.*;
import parse.*;

/**
 * This class contains tests for the mutate() and mutateNode() methods.
 */
public class MutateTest {
	public static void main(String[] args) {
		try {
			FileReader fr = new FileReader("test.txt");
			Parser parser = ParserFactory.getParser();
			Program p = parser.parse(fr);
			for (int i = 0; i < 10000; i++) {
				p = p.mutate();
				System.out.println(p);
			}
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		}
	}
}