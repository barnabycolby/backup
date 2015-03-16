import java.net.*;
import java.io.*;

/**
 * The client that interacts with a backup server. Currently it allows the user to send a message to the server and print's the server's response.
 */
public class BackupClient {
	public static void main(String[] args) {
		try {
			// Load the config file
			ConfigReader config = null;
			try {
				config = new ConfigReader("./clientConfig");
			}
			catch (IOException e) {
				throw new Exception("Could not open config file: " + e.getMessage());
			}

			String serverHostname = "127.0.0.1";

			// Get the port number to connect to the server on
			String portNumberAsString = config.getSetting("port");
			int portNumber;
			try {
				portNumber = Integer.parseInt(portNumberAsString);
			}
			catch (NumberFormatException e) {
				throw new Exception("The port number in the config file was not a number.");
			}

			// Create a socket and the objects for communication
			Socket socket = new Socket(serverHostname, portNumber);
			PrintWriter socketWriter = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// Send the client's identity to the server
			String identity = config.getSetting("identity");
			socketWriter.println(identity);

			// Send user's message to the server and print the response
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			String userInput;

			System.out.println("If you would like to exit at any time, simply type exit.");
			System.out.print("What would you like to send to the server? ");
			while ((userInput = stdIn.readLine()) != null) {
				// We send the input before checking if it was the exit message to give the server an opportunity to close its connection cleanly
				socketWriter.println(userInput);

				// Check if the user wants to exit
				if (userInput.equals("exit")) {
					System.out.println("Exiting.");
					break;
				}

				System.out.println("response: " + socketReader.readLine());
				System.out.print("What would you like to send to the server? ");
			}

			// Close the socket
			socket.close();
		}
		catch (Exception e) {
			System.out.println("Something went wrong: " + e.getMessage());
		}
	}
}
