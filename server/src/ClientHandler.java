import java.net.*;
import java.io.*;

public class ClientHandler extends Thread {
	private Socket _clientSocket;

	public ClientHandler(Socket clientSocket) {
		// Save the client socket and start the thread
		this._clientSocket = clientSocket;
		start();
	}

	public void run() {
		try {
			System.out.println("New client handler started.");

			// Read the message from the client and send a response
			PrintWriter socketWriter = new PrintWriter(_clientSocket.getOutputStream(), true);
			BufferedReader socketReader = new BufferedReader(new InputStreamReader(_clientSocket.getInputStream()));
			String inputLine;

			while ((inputLine = socketReader.readLine()) != null) {
				System.out.println("Message from client: " + inputLine);

				// Check whether the client wants to exit
				if (inputLine.equals("exit")) {
					System.out.println("Client is exiting, closing the connection this side as well.");
					break;
				}

				// Send an echo back to the client
				socketWriter.println("Echo: " + inputLine);
			}

			// Clean up
			socketWriter.close();
			socketReader.close();
			_clientSocket.close();
		}
		catch (IOException e) {
			System.err.println("Something went wrong with the client handler: " + e.getMessage());
			System.exit(1);
		}
	}
}
