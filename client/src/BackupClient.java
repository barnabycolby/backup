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

			while ((userInput = stdIn.readLine()) != null) {
				socketWriter.println(userInput);

				System.out.println("response: " + socketReader.readLine());
			}

			// Close the socket
			socket.close();
		}
		catch (Exception e) {
			System.out.println("Something went wrong: " + e.getMessage());
		}
	}
}
