package parse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Constants {
	HashMap<String, Double> constants;
	
	/**
	 * Constructs a Constants object, parsing the {@code "constants.txt"} file
	 */
	public Constants() {
		constants = new HashMap<String, Double>();
		try {
			InputStream in = getClass().getResourceAsStream("/resources/constants.txt");
			InputStreamReader inReader = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(inReader);
			while (br.ready()) {
				String s = br.readLine();
				String[] tokens = s.split(" ");
				String name = tokens[0];
				Double value = Double.parseDouble(tokens[1]);
				constants.put(name, value);
			}
		} catch (IOException e) {
			System.out.println("No constants.txt file found");
		}
	}
	
	/**
	 * Returns a HashMap containing the parsed constants
	 * @return HashMap of constants
	 */
	public HashMap<String, Double> getConstants() {
		return constants;
	}
}
