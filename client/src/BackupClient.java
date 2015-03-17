import java.net.*;
import java.io.*;
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * The client that interacts with a backup server. Currently it allows the user to send a message to the server and print's the server's response.
 */
public class BackupClient extends Thread {

	public static void main(String[] args) {
		Tee tee = null;
		try {
			// Load the config file
			ConfigReader config = null;
			try {
				System.out.println("Reading config file.");
				config = new ConfigReader("./clientConfig");
			}
			catch (IOException e) {
				System.out.println("Could not open config file: " + e.getMessage());
				System.exit(1);
			}

			// Load the log file writer
			String logFilePath = "./log";
			try {
				logFilePath = config.getSetting("logFilePath");
			}
			catch (Exception e) {}
			tee = new Tee(logFilePath);

			// Create the backup client
			BackupClient backupClient = new BackupClient(config, tee);

			// Add shutdown hook so that we can gracefully shutdown
			final Tee finalTee = tee;
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				@Override
				public void run() {
					BackupClient.gracefullyExit(finalTee, backupClient);				
				}
			}));

			// Listen for the user's input and exit if they ever press 'q'
			Console console = System.console();
			String userInput = "";
			while (!userInput.equals("q")) {
				tee.println("Press 'q' and then enter if you want to exit.");
				userInput = console.readLine();
			}

			gracefullyExit(tee, backupClient);
		}
		catch (Exception ex) {
			tee.println("Something went wrong: " + ex.getMessage());
		}
	}

	public static void gracefullyExit(Tee tee, BackupClient backupClient) {
		// Tell the client to exit and wait for it
		tee.println("Waiting for backup client to exit.");
		backupClient.interrupt();
		backupClient.exit();
		try {
			backupClient.join();
		}
		catch (InterruptedException ex) {}
	}

	private ConfigReader _config;
	private Tee _tee;
	private Socket _socket;
	private PrintWriter _socketWriter;
	private BufferedReader _socketReader;

	/**
	 * Loads the config file.
	 */
	public BackupClient(ConfigReader config, Tee tee) throws Exception {
		// Store the arguments passed to us in instance variables
		this._config = config;
		this._tee = tee;


		// Start the thread
		this.start();
	}

	public void initialiseCommunication() throws Exception {
		// Get the port number and serverIP
		String serverIP = this._config.getSetting("serverIP");
		String portNumberAsString = this._config.getSetting("port");
		int portNumber;
		try {
			portNumber = Integer.parseInt(portNumberAsString);
		}
		catch (NumberFormatException e) {
			throw new Exception("The port number in the config file was not a number.");
		}

		// Create a socket and the objects for communication
		try {
			this._tee.println("Opening socket for communication with the server.");
			this._socket = new Socket(serverIP, portNumber);
			this._socketWriter = new PrintWriter(this._socket.getOutputStream(), true);
			this._socketReader = new BufferedReader(new InputStreamReader(this._socket.getInputStream()));

			// Send the client's identity to the server
			this._tee.println("Sending identitifier to server.");
			String identity = this._config.getSetting("identity");
			this.writeToSocket(identity);

			// Check that the server recognised our identity
			String serverResponse = this._socketReader.readLine();
			if (!serverResponse.equals("Recognised")) {
				throw new IOException("The server didn't recognise our identity: " + serverResponse);
			}
		}
		catch (IOException e) {
			throw new Exception("Failed to communicate with the server: " + e.getMessage());
		}
	}

	/**
	 * Thread-safe way to send message to the backup server.
	 * @param message The message you want to send.
	 */
	private synchronized void writeToSocket(String message) {
		this._socketWriter.println(message);
	}

	/**
	 * Gets the timeout length specified in the config settings, or uses a default value if the setting does not exist.
	 */
	public int getTimeoutLength() {
		// Default timeout length (10 minutes)
		int timeoutLength = 600000;

		// Get the timeout length from the config file
		try {
			String timeoutLengthAsString = this._config.getSetting("timeoutLength");
			timeoutLength = Integer.parseInt(timeoutLengthAsString);
		}
		catch (Exception e) {
			// We absorb any exceptions and use the default timeout length instead
		}

		return timeoutLength;
	}

	/**
	 * Communicates with the server.
	 */
	public void run() {
		// We want to keep trying to communicate with the server until it's successful
		while (true) {
			try {
				// Create a new instance of a BackupClient and initalise the conversation with the server
				boolean startSuccessful = false;
				while (!startSuccessful) {
					// Try and initialise the communication with the server
					try {
						this.initialiseCommunication();
						startSuccessful = true;
					}
					catch (Exception e) {
						startSuccessful = false;
					}

					if (!startSuccessful) {
						this._tee.println("Waiting before we try to start communication again.");
						this.sleep(this.getTimeoutLength());
					}
				}

				// We need to send a pull request to the server when we start to make sure everything's in sync
				this.sendPullRequest();

				// Create the file watcher that sends a pull request every time the directory changes
				this.sendPullRequestOnDirectoryChange();

				// If we make it to this point then we can exit the loop
				break;
			}
			catch (Exception e) {
				// If an Exception occurs then communication with the server probably failed
				// The best thing we can do is close all communication with the server, wait for a while, and try again
				this._tee.println("Something went wrong with the backup client: " + e.getMessage());
				this._tee.println("Waiting before trying again.");
				this.cleanUp();
				try {
					Thread.sleep(this.getTimeoutLength());
				}
				catch (InterruptedException ex) {}
			}
		}
	}

	/**
	 * Send a pull request to the server asking it to sync the files.
	 * @return Returns true if the pull request was successful and false otherwise.
	 * @throws IOException Thrown if communication with the server fails for some reason.
	 */
	public boolean sendPullRequest() throws IOException {
		this._tee.println("Sending pull request to server.");
		this.writeToSocket("PullRequest");
		String response = this._socketReader.readLine();

		// Check that the server hasn't finished communication
		if (response == null) {
			throw new IOException("The pull request was not successful: The server appears to have closed the connection.");
		}

		// Check that the pull request was successful
		if (!response.equals("Succeeded")) {
			this._tee.println("The pull request was not successful: " + response);
			return false;
		}

		return true;
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
	 * Sends the exit command to the server and causes the backup client thread to exit.
	 */
	public void exit() {
		// Send the exit command to the server
		this._tee.println("Exiting.");
		this.writeToSocket("exit");

		this.cleanUp();
	}

	/**
	 * Cleans up any buffers, sockets, etc. that need to be closed.
	 */
	public void cleanUp() {
		try {
			this._socket.close();
			this._socketWriter.close();
			this._socketReader.close();
		}
		catch (IOException e) {
			// We just absorb any IOExceptions as we're trying to close the objects anyway
		}
	}
}
