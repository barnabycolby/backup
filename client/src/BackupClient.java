import java.net.*;
import java.io.*;

/**
 * The client that interacts with a backup server. Currently it allows the user to send a message to the server and print's the server's response.
 */
public class BackupClient {

	public static void main(String[] args) {
		BackupClient backupClient = null;
		try {
			// Create a new instance of a BackupClient and initalise the conversation with the server
			backupClient = new BackupClient();
			backupClient.start();

			// Finally, we need to exit
			backupClient.exit();
		}
		catch (Exception e) {
			System.out.println("Something went wrong: " + e.getMessage());
		}
		finally {
			try {
				backupClient.cleanUp();
			}
			catch (IOException e) {}
		}
	}

	private ConfigReader _config;
	private Socket _socket;
	private PrintWriter _socketWriter;
	private BufferedReader _socketReader;

	/**
	 * Loads the config file and initialises the socket and it's reader/writer helpers.
	 */
	public BackupClient() throws Exception {
		// Load the config file
		try {
			_config = new ConfigReader("./clientConfig");
		}
		catch (IOException e) {
			throw new Exception("Could not open config file: " + e.getMessage());
		}

		String serverIP = this._config.getSetting("serverIP");

		// Get the port number to connect to the server on
		String portNumberAsString = this._config.getSetting("port");
		int portNumber;
		try {
			portNumber = Integer.parseInt(portNumberAsString);
		}
		catch (NumberFormatException e) {
			throw new Exception("The port number in the config file was not a number.");
		}

		// Create a socket and the objects for communication
		this._socket = new Socket(serverIP, portNumber);
		this._socketWriter = new PrintWriter(this._socket.getOutputStream(), true);
		this._socketReader = new BufferedReader(new InputStreamReader(this._socket.getInputStream()));
	}

	/**
	 * Start the communication with the server.
	 */
	public void start() throws Exception {
		// Send the client's identity to the server
		String identity = this._config.getSetting("identity");
		this._socketWriter.println(identity);

		// Check that the server recognised our identity
		String serverResponse = this._socketReader.readLine();
		if (!serverResponse.equals("Recognised")) {
			throw new Exception("The server didn't recognise our identity: " + serverResponse);
		}
	}

	/**
	 * Sends the exit command to the server.
	 */
	public void exit() {
		System.out.println("Exiting.");
		this._socketWriter.println("exit");
		return;
	}

	/**
	 * Cleans up any buffers, sockets, etc. that need to be closed.
	 */
	public void cleanUp() throws IOException {
		this._socket.close();
		this._socketWriter.close();
		this._socketReader.close();
	}
}
