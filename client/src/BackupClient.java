import java.net.*;
import java.io.*;
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;

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

			// We need to send a pull request to the server when we start to make sure everything's in sync
			backupClient.sendPullRequest();

			// Create the file watcher that sends a pull request every time the directory changes
			backupClient.sendPullRequestOnDirectoryChange();

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
			System.out.println("Reading config file.");
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
		System.out.println("Opening socket for communication with the server.");
		this._socket = new Socket(serverIP, portNumber);
		this._socketWriter = new PrintWriter(this._socket.getOutputStream(), true);
		this._socketReader = new BufferedReader(new InputStreamReader(this._socket.getInputStream()));
	}

	/**
	 * Start the communication with the server.
	 */
	public void start() throws Exception {
		// Send the client's identity to the server
		System.out.println("Sending identitifier to server.");
		String identity = this._config.getSetting("identity");
		this._socketWriter.println(identity);

		// Check that the server recognised our identity
		String serverResponse = this._socketReader.readLine();
		if (!serverResponse.equals("Recognised")) {
			throw new Exception("The server didn't recognise our identity: " + serverResponse);
		}
	}

	/**
	 * Send a pull request to the server asking it to sync the files.
	 */
	public void sendPullRequest() throws Exception {
		System.out.println("Sending pull request to server.");
		this._socketWriter.println("PullRequest");
		String response = this._socketReader.readLine();

		// Check that the pull request was successful
		if (!response.equals("Succeeded")) {
			throw new Exception("The pull request was not successful: " + response);
		}
	}

	/**
	 * Registers the directory specified in the config with a watch service. Every time the directory changes, a pull request is sent to the server.
	 */
	public void sendPullRequestOnDirectoryChange() throws Exception {
		WatchService watcher = FileSystems.getDefault().newWatchService();
		String directoryToWatchAsString = this._config.getSetting("directoryToWatch");
		Path directoryToWatch = Paths.get(directoryToWatchAsString);
		WatchKey key = directoryToWatch.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

		while (true) {
			// Take events from the watcher queue
			WatchKey signalledWatchKey = null;
			try {
				signalledWatchKey = watcher.take();
			}
			catch (InterruptedException e) {
				return;
			}

			// It appears that we have to call pollEvents on the watch key even if we don't care
			// Otherwise the key just keeps getting added to the queue over and over
			signalledWatchKey.pollEvents();

			// We don't care what the event is because we need to issue a pull request in every case
			this.sendPullRequest();

			// Now we need to reset the signalled key so that we will receive future watch events
			boolean isWatchKeyValid = signalledWatchKey.reset();
			if (!isWatchKeyValid) {
				break;
			}
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
		System.out.println("Cleaning up.");
		this._socket.close();
		this._socketWriter.close();
		this._socketReader.close();
	}
}
