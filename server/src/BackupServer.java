import java.net.*;
import java.io.*;

public class BackupServer {
	public static void main(String[] args) {
		// Load the config file that contains the settings for the server
		String configFilePath = "./serverConfig";
		ConfigReader config = null;
		try {
			config = new ConfigReader(configFilePath);
		}
		catch (IOException e) {
			System.err.println("Could not read the config file: " + configFilePath);
			System.exit(1);
		}

		ServerSocket serverSocket = null;
		try {
			// Read the port number from the config file
			String portNumberAsString = config.getSetting("port");
			int portNumber;
			try {
				portNumber = Integer.parseInt(portNumberAsString);
			}
			catch (NumberFormatException e) {
				throw new Exception("The port number in the config file was not a number.");
			}

			// Listen to the port
			serverSocket = new ServerSocket(portNumber);
			System.out.println("Server socket created.");
			while (true) {
				System.out.println("Waiting for connection.");
				new ClientHandler(serverSocket.accept());
			}
		}
		catch (Exception e) {
			System.err.println("Something went wrong: " + e.getMessage());
			System.exit(1);
		}
		finally {
			System.out.println("Closing server socket.");
			try {
				serverSocket.close();
			}
			catch (IOException e) {
				System.out.println("Failed to close server socket: " + e.getMessage());
			}
		}
	}
}
