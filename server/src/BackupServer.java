import java.net.*;
import java.io.*;

public class BackupServer {
	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(10008);
			System.out.println("Server socket created.");
			while (true) {
				System.out.println("Waiting for connection.");
				new ClientHandler(serverSocket.accept());
			}
		}
		catch (IOException e) {
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
