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
	 * The writer used to send messages to the client.
	 */
	private PrintWriter _socketWriter;

	/**
	 * The reader used to receive messages from the client.
	 */
	private BufferedReader _socketReader;

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
			// Set up the objects and variables for communication with the client
			this._socketWriter = new PrintWriter(_clientSocket.getOutputStream(), true);
			this._socketReader = new BufferedReader(new InputStreamReader(_clientSocket.getInputStream()));
			String inputLine;

			// Check that the first message is an identity that this server recognises
			this._clientIdentity = this._socketReader.readLine();
			String knownClientIdentitiesString = this._config.getSetting("knownClientIdentities");
			List knownClientIdentities = Arrays.asList(knownClientIdentitiesString.split(","));
			if (!knownClientIdentities.contains(this._clientIdentity)) {
				this._socketWriter.println("I can't talk to you, I don't recognise your identity.");
				this.cleanUp();
				System.out.println("Refused client as I didn't recognise their identity: " + this._clientIdentity);
				return;
			}
			this._socketWriter.println("Recognised");

			// Listen to and handle the client's commands
			while ((inputLine = this._socketReader.readLine()) != null) {
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
								this._socketWriter.println("Succeeded");
							}
							else {
								String errorMessage = "Pull failed with exit code: " + exitCode;
								System.out.println(errorMessage);
								this._socketWriter.println(errorMessage);
							}
						}
						catch (Exception e) {
							System.out.println("The pull failed: " + e.getMessage());
							this._socketWriter.println("The pull failed: " + e.getMessage());
						}
						break;

					default:
						System.out.println("Client sent unknown command: " + inputLine);
						this._socketWriter.println("Unknown command");
					}
			}

			this.cleanUp();
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

	/**
	 * Closes socket and objects used for communicating with the socket.
	 */
	private void cleanUp() {
		try {
			this._socketWriter.close();
			this._socketReader.close();
			this._clientSocket.close();
		}
		catch (IOException e) {
			// Absorb any communication errors as we don't care at this point
		}
	}
}
