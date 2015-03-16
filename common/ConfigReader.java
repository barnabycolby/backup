import java.nio.file.*;
import java.util.stream.Stream;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

/**
 * The config reader class allows easy access to bash variables stored in a file. This allows the variables to store settings retrievable in code.
 */
public class ConfigReader {
	/**
	 * Stores the mapping between config settings and their values.
	 */
	private Map<String, String> _configMap;

	/**
	 * The constructors takes the path of the config file to read and stores the key/value pairs that it contains.
	 * @param path The path of the config file to read.
	 * @throws IOException Throws IOException if the file could not be read or does not exist.
	 */
	public ConfigReader(String path) throws IOException {
		// Attempt to read the file specified by the path
		Path filePath = Paths.get(path);
		Stream<String> lines = Files.lines(filePath);

		// Initialise the config map
		this._configMap = new HashMap<String, String>();

		// For each line in the file
		lines.forEach(line -> {
			// Check whether the line is a comment
			if (line.startsWith("#")) {
				return;
			}

			// We can discard lines that don't even contain an equals sign
			if (!line.contains("=")) {
				return;
			}

			// Next we need to get the key
			int indexOfFirstEqualsSign = line.indexOf('=');
			String key = line.substring(0, indexOfFirstEqualsSign);

			// Check that the key is a single word by trimming the whitespace and splitting on ' '
			key = key.trim();
			String[] keyPieces = key.split(" ");
			if (keyPieces.length != 1) {
				return;
			}

			// And then we need to get the value
			String value = "";
			if (line.length() > (indexOfFirstEqualsSign + 1)) {
				value = line.substring(indexOfFirstEqualsSign + 1, line.length());
			}

			// Finally, we need to store the key value pair so that we can use it later
			this._configMap.put(key, value);
		});
	}

	/**
	 * Gets a setting stored in the config file.
	 * @param settingName The name of the setting to retrieve.
	 * @return The value of the setting.
	 * @throws Exception If the setting does not exist within the file.
	 */
	public String getSetting(String settingName) throws Exception {
		if (!this._configMap.containsKey(settingName)) {
			throw new Exception("The config file did not contain the " + settingName + " setting.");
		}

		return this._configMap.get(settingName);
	}
}
