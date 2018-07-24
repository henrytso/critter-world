package tests;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import ast.Program;
import client.Client;
import client.ClientGrid.WorldInfoBundle;
import client.ClientGrid.HexInfo;
import parse.Parser;
import parse.ParserFactory;
import parse.SpecParser;
import server.Server;
import simulator.Critter;
import simulator.SimulatorImpl;

public class ServerTest {
	public static void main(String[] args) {
		Server server = new Server();
		server.run();
		
		try {
			Client client = new Client(new URL("http://localhost:8080"));
			client.login("reader", "bilbo");
			client.makeWorld(" "); // Reader shouldn't be able to make world
			
			client.login("admin", "gandalf");
			client.makeWorld(" "); // Admin can
			WorldInfoBundle info = client.getWorld(); // State has all hexes (all hexes were changed)
			
			assertTrue(info.timestep() == 0);
			assertTrue(info.version() == 0);
			assertTrue(info.update() == 0);
			assertTrue(info.rate() == 0);
			
			SimulatorImpl dummy = new SimulatorImpl();
			
			FileReader fr = null;
			try {
				fr = new FileReader("smell-critter");
			} catch (FileNotFoundException e) {
				System.out.println("o nah");
			}
			
			BufferedReader br = new BufferedReader(fr);
			dummy.readWord(br); // should be "species:"
			String species = " ";
			try {
				species = br.readLine();
			} catch (IOException e) {
				System.out.println("eh IO not good");
			}
			SpecParser sp = new SpecParser();
			Reader r = sp.parseSpecs(br);
			int[] mem = sp.getAttributes();
			Parser parser = ParserFactory.getParser();
			Program p = parser.parse(r);
			
			Critter c = new Critter(p, species, 2, 2, mem, 0, dummy);
			
			// Load one heckin smelly critter to 2, 2
			client.addCritter("smelly one", c.getProgram().toString(), mem, 2, 2, -1);
			info = client.getWorld();
			
			assertTrue(info.state().length == 1);
//			HexInfo h = new HexInfo(2, 2, "critter");
//			assertTrue(info.state()[0].equals(h));
			
			
		} catch (MalformedURLException e) {
			System.out.println("Invalid URL");
		}
	}
}
