import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * This class starts allows commands to be executed on the local system.
 */
public class ShellCommandExecutor {

	/**
	 * Stores the exit code of the last command that was executed.
	 */
	private int _lastExitCode;

	/**
	 * Executes the given command in the system environment.
	 * @param command The command to execute.
	 * @return The output of the command.
	 */
	public String execute(String command) throws IOException,InterruptedException {
		System.out.println("Executing command: " + command);
		// Exectue the command
		Process process = Runtime.getRuntime().exec(command);
		process.waitFor();

		// Store the exit code of the command so that we can return it to the user if they ask for it
		System.out.println("Storing exit code.");
		this._lastExitCode = process.exitValue();

		// Collect the commands output
		System.out.println("Collecting command output.");
		StringBuffer commandOutput = new StringBuffer();
		BufferedReader processOutputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line = "";
		while ((line = processOutputReader.readLine()) != null) {
			commandOutput.append(line + "\n");
		}

		System.out.println("Returning command output.");
		return commandOutput.toString();
	}

	/**
	 * Gets the exit code of the last command that was executed.
	 * @return The exit code of the last command that was executed.
	 */
	public int getLastExitCode() {
		return this._lastExitCode;
	}
}
