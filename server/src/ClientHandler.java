import java.net.*;
import java.io.*;
import java.util.List;
import java.util.Arrays;

/**
 * This class handles a single backup client.
 */
public class ClientHandler extends Thread {
	/**
	 * The socket used to communicate with the client.
	 */
	private Socket _clientSocket;

	/**
	 * The config reader used to get the server's settings.
	 */
	private ConfigReader _config;

	/**
	 * The string used to uniquely identify the client.
	 */
	private String _clientIdentity;

	/**
	 * The object used to help us execute commands on the local system.
	 */
	private ShellCommandExecutor _shellCommandExecutor;

	/**
	 * The constructor saves the socket it's given and start's the execution of the thread.
	 * @param config The config reader that this client handler should get settings from.
	 * @param clientSocket The socket used to communicate with the client.
	 */
	public ClientHandler(ConfigReader config, Socket clientSocket) {
		// Save the objects passed in as arguments
		this._clientSocket = clientSocket;
		this._config = config;

		// Initialise any other instance variables
		this._shellCommandExecutor = new ShellCommandExecutor();

		// Start the thread
		start();
	}

	/**
	 * The main method of the thread. It listens for messages from the client and sends an echo back to the client.
	 */
	public void run() {
		try {
			System.out.println("New client connnected.");

			// Set up the objects and variables for communication with the client
			PrintWriter socketWriter = new PrintWriter(_clientSocket.getOutputStream(), true);
			BufferedReader socketReader = new BufferedReader(new InputStreamReader(_clientSocket.getInputStream()));
			String inputLine;

			// Check that the first message is an identity that this server recognises
			this._clientIdentity = socketReader.readLine();
			String knownClientIdentitiesString = this._config.getSetting("knownClientIdentities");
			List knownClientIdentities = Arrays.asList(knownClientIdentitiesString.split(","));
			if (!knownClientIdentities.contains(this._clientIdentity)) {
				socketWriter.println("I can't talk to you, I don't recognise your identity.");
				socketWriter.close();
				socketReader.close();
				this._clientSocket.close();
				System.out.println("Refused client as I didn't recognise their identity: " + this._clientIdentity);
				return;
			}
			socketWriter.println("Recognised");

			// Listen to and handle the client's commands
			while ((inputLine = socketReader.readLine()) != null) {
				switch (inputLine) {
					// Check whether the client wants to exit
					case "exit":
						System.out.println("Client is exiting, closing the connection this side as well.");
						break;

					// Check whether the client has requested a pull
					case "PullRequest":
						try {
							System.out.println("Performing pull for client " + this._clientIdentity);
							this.performPull();
							int exitCode = this._shellCommandExecutor.getLastExitCode();
							if (exitCode == 0) {
								System.out.println("Pull succeeded.");
								socketWriter.println("Succeeded");
							}
							else {
								String errorMessage = "Pull failed with exit code: " + exitCode;
								System.out.println(errorMessage);
								socketWriter.println(errorMessage);
							}
						}
						catch (Exception e) {
							System.out.println("The pull failed: " + e.getMessage());
							socketWriter.println("The pull failed: " + e.getMessage());
						}
						break;

					default:
						System.out.println("Client sent unknown command: " + inputLine);
						socketWriter.println("Unknown command");
					}
			}

			// Clean up
			socketWriter.close();
			socketReader.close();
			this._clientSocket.close();
		}
		catch (Exception e) {
			System.err.println("Something went wrong with the client handler: " + e.getMessage());
			System.exit(1);
		}
	}

	/**
	 * Performs a pull operation, pulling the client's files across a share and into the continuous backup directory.
	 * @throws Exception Throws an exception if the pull failed for any reason. The exception will contain a message detailing why the pull failed.
	 */
	private void performPull() throws Exception {
		// Build up the command
		String rsyncPullScriptPath = this._config.getSetting("rsyncPullScriptPath");
		String rsyncPullCommand = "sh " + rsyncPullScriptPath + " " + this._clientIdentity;

		// Execute the command
		String commandOutput = this._shellCommandExecutor.execute(rsyncPullCommand);
		System.out.println("Pull output: " + commandOutput);
	}
}
