import java.io.*;
import java.util.Calendar;

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
	public synchronized void println(String message) {
		// Print to log file
		if (this._logFileWriter != null) {
			try {
				// Get the current time
				Calendar now = Calendar.getInstance();
				int hour = now.get(Calendar.HOUR_OF_DAY);
				int minute = now.get(Calendar.MINUTE);

				// Construct and write the log message
				String logMessage = "[" + hour + ":" + minute + "] " + message + "\n";
				this._logFileWriter.write(logMessage);

				// Flush straight away just in case the program closes unexpectedly
				this._logFileWriter.flush();
			}
			catch (IOException e) {}
		}

		// Print to stdout
		System.out.println(message);
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
