package tests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.*;

import console.Console;

public class ConsoleTester {
	@Test
	public void test1() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		// Loads world.txt and 
		Console c = new Console(new ByteArrayInputStream(("load examples/world.txt\n" + "info\n" + "exit\n").getBytes()),
				new PrintStream(output));
		while (!c.done) {
			c.handleCommand();
		}
		
		// Yea looks good
		
		System.out.println(output.toString());
	}
}
