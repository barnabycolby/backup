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
			_clientSocket.close();
		}
		catch (IOException e) {
			System.err.println("Something went wrong with the client handler: " + e.getMessage());
			System.exit(1);
		}
	}
}
