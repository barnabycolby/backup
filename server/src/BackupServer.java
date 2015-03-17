import java.net.*;
import java.io.*;

/**
 * The backup server that allows communication with the backup clients. Currently it will handle multiple clients at the same time and send an echo of what the client sends.
 */
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

		// Load the log file writer
		String logFilePath = "./log";
		try {
			logFilePath = config.getSetting("logFilePath");
		}
		catch (Exception e) {}
		Tee tee = new Tee(logFilePath);

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
			tee.println("Server socket created.");
			while (true) {
				tee.println("Waiting for connection.");
				new ClientHandler(config, tee, serverSocket.accept());
				tee.println("New client connected.");
			}
		}
		catch (Exception e) {
			tee.println("Something went wrong: " + e.getMessage());
			System.exit(1);
		}
		finally {
			tee.println("Closing server socket.");
			try {
				serverSocket.close();
			}
			catch (IOException e) {
				tee.println("Failed to close server socket: " + e.getMessage());
			}

			tee.cleanUp();
		}
	}
}
