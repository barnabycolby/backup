import java.io.*;

/**
 * The Tee class allows printing to both stdout and a log file. It is named after the *nix command tee.
 */
public class Tee {

	/**
	 * An object used to write to the log file.
	 */
	private FileWriter _logFileWriter;

	/**
	 * Opens the specified log file, creating it if it exists.
	 * @param logFilePath The path of the log file to open.
	 */
	public Tee(String logFilePath) {
		try {
			// Open/Create the file
			File logFile = new File(logFilePath);
			if (!logFile.exists()) {
				logFile.createNewFile();
			}

			// Create the log file writer and store it
			this._logFileWriter = new FileWriter(logFile.getAbsoluteFile());
		}
		catch (IOException e) {
			System.out.println("Cannot open log file: " + logFilePath);
		}
	}

	/**
	 * Print to both the log and stdout.
	 * @param message The message to print
	 */
	public void println(String message) {
		// Print to log file
		if (this._logFileWriter != null) {
			try {
				message = message + "\n";
				this._logFileWriter.write(message);

				// Flush straight away just in case the program closes unexpectedly
				this._logFileWriter.flush();
			}
			catch (IOException e) {}
		}

		// Print to stdout
		System.out.print(message);
	}

	/**
	 * Cleans up any open objects.
	 */
	public void cleanUp() {
		try {
			this._logFileWriter.close();
		}
		catch (IOException e) {}
	}
}
