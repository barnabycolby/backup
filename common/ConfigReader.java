import java.nio.file.*;
import java.util.stream.Stream;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

public class ConfigReader {
	private Map<String, String> _configMap;

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

	public String getSetting(String settingName) throws Exception {
		if (!this._configMap.containsKey(settingName)) {
			throw new Exception("The config file did not contain the " + settingName + " setting.");
		}

		return this._configMap.get(settingName);
	}
}
