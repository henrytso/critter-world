package tests;

import java.io.FileNotFoundException;
import java.io.FileReader;

import ast.*;
import parse.*;

/**
 * This class contains tests for the methods involving 
 * 		finding, getting, and generating subtrees.
 */
public class SubtreeTest {
	public static void main(String[] args) {
		try {
			FileReader fr = new FileReader("test.txt");
			Parser parser = ParserFactory.getParser();
			Program p = parser.parse(fr);
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < p.size(); i++) {
				System.out.println(p.nodeAt(i));
				System.out.println(p.findSubtree(p.nodeAt(i)));
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		}
	}
}
