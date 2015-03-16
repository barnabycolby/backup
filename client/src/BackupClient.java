import java.net.*;
import java.io.*;

public class BackupClient {
	public static void main(String[] args) {
		try {
			String serverHostname = "127.0.0.1";

			// Create a socket and the objects for communication
			Socket socket = new Socket(serverHostname, 10008);
			PrintWriter socketWriter = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

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
