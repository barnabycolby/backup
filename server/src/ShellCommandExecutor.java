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
	 * Stores the output of the last command that was executed, including stderr and stdout output.
	 */
	private String _lastCommandOutput;

	/**
	 * Executes the given command in the system environment.
	 * @param command The command to execute.
	 * @return The exit code of the command.
	 */
	public int execute(String... command) throws IOException,InterruptedException {
		// Exectue the command
		// Redirect error stream allows us to easily capture the entire output later
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.redirectErrorStream(true);
		processBuilder.command(command);
		Process process = processBuilder.start();
		process.waitFor();

		// Store the exit code of the command so that we can return it to the user if they ask for it
		this._lastExitCode = process.exitValue();

		// Collect and store the command output
		StringBuffer commandOutput = new StringBuffer();
		BufferedReader processOutputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line = "";
		while ((line = processOutputReader.readLine()) != null) {
			commandOutput.append(line + "\n");
		}
		this._lastCommandOutput = commandOutput.toString();

		// We print the command output so that it appears to be coming from the java process
		// We use a print instead of a println, because the process should already have added the newlines in most cases
		System.out.print(commandOutput.toString());

		return this._lastExitCode;
	}

	/**
	 * Gets the exit code of the last command that was executed.
	 * @return The exit code of the last command that was executed.
	 */
	public int getLastExitCode() {
		return this._lastExitCode;
	}

	/**
	 * Gets the output of the last command executed, including stderr and stdout.
	 */
	public String getLastCommandOutput() {
		return this._lastCommandOutput;
	}
}
