package tests;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;

import ast.Program;
import client.Client;
import parse.Parser;
import parse.ParserFactory;
import server.Server;
import simulator.Critter;
import simulator.SimulatorImpl;

public class SmellTest {
	
	public static void main(String[] args) {
		SimulatorImpl s = new SimulatorImpl(10, 15);
		s.loadCritter("examples/smell-critter.txt", 2, 2, 0);
		s.addRock(3, 0);
		s.addRock(3, 1);
		s.addRock(3, 2);
		s.addRock(3, 3);
		s.addFood(4, 3, 50); // closer
		s.addFood(6, 20, 300); // further
		
		assertTrue("Should have a critter", !s.getCritters().isEmpty());
		int x = s.getCritters().get(0).smell();
		System.out.println(x);
		assertTrue("Should be 6000", x == 6000);
		
		s.addRock(3, 4);
		
		int y = s.getCritters().get(0).smell();
		System.out.println(y);
		assertTrue("Should be 8000", y == 8000);
		
		s.addRock(3, 5);
		
		int z = s.getCritters().get(0).smell();
		System.out.println(z);
		assertTrue("Should be 1000000", z == 1000000);
	}
}
