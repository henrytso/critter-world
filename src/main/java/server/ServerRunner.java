package server;

/**
 * Runs the server representing the critter world.
 */
public class ServerRunner {
    public static void main(String[] args) {
		Server server = new Server();
		server.run();
    }
}