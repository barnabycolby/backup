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
	 * The tee used to write output.
	 */
	private Tee _tee;

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
	 * @param tee The tee object used to print output.
	 */
	public ClientHandler(ConfigReader config, Tee tee, Socket clientSocket) {
		// Save the objects passed in as arguments
		this._clientSocket = clientSocket;
		this._config = config;
		this._tee = tee;

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
				this._tee.println("Refused client as I didn't recognise their identity: " + this._clientIdentity);
				return;
			}
			this._socketWriter.println("Recognised");

			// Listen to and handle the client's commands
			while ((inputLine = this._socketReader.readLine()) != null) {
				switch (inputLine) {
					// Check whether the client wants to exit
					case "exit":
						this._tee.println("Client is exiting, closing the connection this side as well.");
						break;

					// Check whether the client has requested a pull
					case "PullRequest":
						try {
							this._tee.println("Performing pull for client " + this._clientIdentity);
							int exitCode = this.performPull();

							String pullOutcome = null;
							switch (exitCode) {
								case 0:
									// This exact text indicates to the client that the pull was successful
									pullOutcome = "Succeeded";
									break;
								case 1:
									pullOutcome = "Unknown error occurred.";
									String pullCommandOutput = this._shellCommandExecutor.getLastCommandOutput();
									this.attemptToEmailBarney("Rsync Pull Failed", pullCommandOutput);
									break;
								case 2:
									pullOutcome = "The pull script was passed the wrong number of arguments.";
									break;
								case 3:
									pullOutcome = "The share to pull from could not be mounted.";
									break;
								case 4:
									pullOutcome = "Backup drive destination folder is not a folder.";
									break;
								case 5:
									pullOutcome = "Rsync operation failed for an unknown reason.";
									break;
								default:
									pullOutcome = "Unknown error occurred.";
									break;

							}
							this._tee.println(pullOutcome);
							this._socketWriter.println(pullOutcome);
						}
						catch (Exception e) {
							this._tee.println("The pull failed: " + e.getMessage());
							this._socketWriter.println("The pull failed: " + e.getMessage());
						}
						break;

					default:
						this._tee.println("Client sent unknown command: " + inputLine);
						this._socketWriter.println("Unknown command");
					}
			}

			this.cleanUp();
		}
		catch (Exception e) {
			this._tee.println("Something went wrong with the client handler: " + e.getMessage());
			this.cleanUp();
			return;
		}
	}

	/**
	 * Performs a pull operation, pulling the client's files across a share and into the continuous backup directory.
	 * @throws Exception Throws an exception if the pull failed for any reason. The exception will contain a message detailing why the pull failed.
	 * @return Returns the exit code of the pull command.
	 */
	private int performPull() throws Exception {
		// Get the path of the rsync script
		String rsyncPullScriptPath = this._config.getSetting("rsyncPullScriptPath");

		// Execute the command
		int exitCode = this._shellCommandExecutor.execute("sh", rsyncPullScriptPath, this._clientIdentity);

		// Return the exit code
		return exitCode;
	}

	/**
	 * Attempts to send an email to Barney with the specified subject and body.
	 * @param subject The subject of the email.
	 * @param body The contents of the email.
	 */
	public void attemptToEmailBarney(String subject, String body) {
		// Retrieve the path of the send email script
		String scriptPath = null;
		try {
			scriptPath = this._config.getSetting("sendMailScriptPath");
		}
		catch (Exception ex) {
			// This is only supposed to be an attempt so we can absorb any exceptions that occur
			// Don't attempt to send the email if the email address could not be retrieved
			this._tee.println("Couldn't read sendMailScriptPath from config file.");
			return;
		}

		// Actually send the email
		try {
			this._shellCommandExecutor.execute("sh", scriptPath, "\"" + subject + "\"", "\"" + body + "\"");
		}
		catch (Exception ex) {
			// This is only supposed to be an attempt so we can absorb any exceptions that occur
		}
	}

	/**
	 * Exits this client handler cleanly.
	 */
	public void exit() {
		this.cleanUp();
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
