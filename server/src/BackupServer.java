import java.net.*;
import java.io.*;
import java.util.ArrayList;

/**
 * The backup server that allows communication with the backup clients. Currently it will handle multiple clients at the same time and send an echo of what the client sends.
 */
public class BackupServer extends Thread {
	public static void main(String[] args) {
		// Check that we've been given the config file path as an argument
		if (args.length != 1) {
			System.out.println("Usage: BackupServer pathOfConfigFile");
			System.exit(1);
		}

		// Load the config file that contains the settings for the server
		String configFilePath = args[0];
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

		tee.println("Starting backup server.");
		BackupServer backupServer = null;
		try {
			// Start the backup server
			backupServer = new BackupServer(config, tee);

			// Add hook to shutdown so that we can exit gracefully
			final Tee finalTee = tee;
			final BackupServer finalBackupServer = backupServer;
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				@Override
				public void run() {
					BackupServer.gracefullyExit(finalTee, finalBackupServer);
				}
			}));

			// Listen for the user's input and exit if they ever press 'q'
			Console console = System.console();
			if (console == null) {
				System.out.println("No console available to listen for exit command.");
				System.out.println("Waiting until we receive a shutdown signal.");
				while (true) {
					Thread.sleep(1000);
				}
			}
			else {
				String userInput = "";
				while (!userInput.equals("q")) {
					tee.println("Press 'q' and then enter if you want to exit.");
					userInput = console.readLine();
				}

				gracefullyExit(tee, backupServer);
			}

			// We have  some clean up of our own to do too
			tee.cleanUp();
		}
		catch (Exception e) {
			tee.println("Something went wrong: " + e.getMessage());
			e.printStackTrace();
			if (backupServer != null) {
				backupServer.cleanUp();
			}
			System.exit(1);
		}
	}

	/**
	 * Shuts down the server gracefully.
	 * @param tee The tee used to print output.
	 * @param backupServer The backup server to shutdown.
	 */
	public static void gracefullyExit(Tee tee, BackupServer backupServer) {
		// We need to stop the backup server
		tee.println("Stopping backup server.");
		backupServer.exit();
		try {
			backupServer.join();
		}
		catch (InterruptedException ex) {}
	}

	/**
	 * The config reader to retrieve settigns from.
	 */
	private ConfigReader _config;

	/**
	 * The tee to write output to.
	 */
	private Tee _tee;

	/**
	 * The socket for this server.
	 */
	private ServerSocket _serverSocket;

	/**
	 * Indicates whether the exit method has been called.
	 */
	private boolean _exitCalled = false;

	/**
	 * Stores a list of created client objects so that we can tell them to close on exit.
	 */
	private ArrayList<ClientHandler> _clients;

	/**
	 * Gets the port number from the config file and initialises a server socket.
	 * @param config The config reader to retrieve settings from.
	 * @param tee The tee to write output to.
	 */
	public BackupServer(ConfigReader config, Tee tee) throws Exception {
		// Store objects passed via arguments
		this._config = config;
		this._tee = tee;

		// Initalise the array list
		this._clients = new ArrayList<ClientHandler>();

		// Read the port number from the config file
		String portNumberAsString = this._config.getSetting("port");
		int portNumber;
		try {
			portNumber = Integer.parseInt(portNumberAsString);
		}
		catch (NumberFormatException e) {
			throw new Exception("The port number in the config file was not a number.");
		}

		// Create the server socket
		this._serverSocket = new ServerSocket(portNumber);

		// Start the thread
		this.start();
	}

	/**
	 * The main method of the thread. Listens for new clients and spins up a new ClientHandler for each.
	 */
	public void run() {
		// Listen to the port
		this._tee.println("Server socket created.");
		try {
			while (true) {
				this._tee.println("Waiting for connection.");
				this._clients.add(new ClientHandler(this._config, this._tee, this._serverSocket.accept()));
				this._tee.println("New client connected.");
			}
		}
		catch (IOException e) {
			// We only want to print an error message if we didn't exit cleanly
			if (!this._exitCalled) {
				this._tee.println("An IOException occurred: " + e.getMessage());
			}
		}
	} /**
	 * Causes the thread to exit in a clean way. It also calls the cleanUp method internally.
	 */
	public void exit() {
		this._exitCalled = true;

		// This interrupts the blocking call to accept in the run method
		try {
			this._serverSocket.close();
		}
		catch (IOException e) {
			this._tee.println("Server socket failed to close: " + e.getMessage());
		}

		// Close each client handler
		for (ClientHandler clientHandler : this._clients) {
			if (clientHandler.isAlive()) {
				this._tee.println("Closing connection to client.");
				clientHandler.exit();
			}
			try {
				clientHandler.join();
			}
			catch (InterruptedException e) {}
		}

		// Finally, we need to clean up
		this.cleanUp();
	}

	/**
	 * Cleans up all objects stored in instance variables that need closing.
	 */
	public void cleanUp() {
		this._tee.println("Closing server socket.");
		try {
			if (this._serverSocket != null) {
				this._serverSocket.close();
			}
		}
		catch (IOException e) {
			this._tee.println("Failed to close server socket: " + e.getMessage());
		}
	}
}
