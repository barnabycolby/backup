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
	 * The constructor saves the socket it's given and start's the execution of the thread.
	 * @param config The config reader that this client handler should get settings from.
	 * @param clientSocket The socket used to communicate with the client.
	 */
	public ClientHandler(ConfigReader config, Socket clientSocket) {
		// Save the objects passed in as arguments and start the thread
		this._clientSocket = clientSocket;
		this._config = config;
		start();
	}

	/**
	 * The main method of the thread. It listens for messages from the client and sends an echo back to the client.
	 */
	public void run() {
		try {
			System.out.println("New client handler started.");

			// Set up the objects and variables for communication with the client
			PrintWriter socketWriter = new PrintWriter(_clientSocket.getOutputStream(), true);
			BufferedReader socketReader = new BufferedReader(new InputStreamReader(_clientSocket.getInputStream()));
			String inputLine;

			// Check that the first message is an identity that this server recognises
			String clientIdentity = socketReader.readLine();
			String knownClientIdentitiesString = this._config.getSetting("knownClientIdentities");
			List knownClientIdentities = Arrays.asList(knownClientIdentitiesString.split(","));
			if (!knownClientIdentities.contains(clientIdentity)) {
				socketWriter.println("I can't talk to you, I don't recognise your identity.");
				socketWriter.close();
				socketReader.close();
				this._clientSocket.close();
				System.out.println("Refused client as I didn't recognise their identity: " + clientIdentity);
				return;
			}
			socketWriter.println("Recognised");

			// Echo whatever messages the client sends
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
			this._clientSocket.close();
		}
		catch (Exception e) {
			System.err.println("Something went wrong with the client handler: " + e.getMessage());
			System.exit(1);
		}
	}
}
