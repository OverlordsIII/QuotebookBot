package io.github.overlordsiii.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import io.github.overlordsiii.Main;

public class PropertiesHandler {

	private final Path propertiesPath;

	private final Map<String, String> configValues;

	public static final Path CONFIG_HOME_DIRECTORY = Paths.get(System.getProperty("user.home")).resolve("QuoteBookBot Config");

	static {
		if (!Files.exists(CONFIG_HOME_DIRECTORY)) {
			try {
				Files.createDirectory(CONFIG_HOME_DIRECTORY);
			} catch (IOException e) {
				Main.LOGGER.error("Error while creating config home directory at: \"" + CONFIG_HOME_DIRECTORY + "\"");
				e.printStackTrace();
			}
		}
	}

	private PropertiesHandler(String filename, Map<String, String> configValues, boolean serverConfig) {

		String fileNameNoExtension = filename.substring(0, filename.indexOf(".properties"));

		this.propertiesPath = serverConfig ? CONFIG_HOME_DIRECTORY.resolve(fileNameNoExtension).resolve("guild-config.properties") : CONFIG_HOME_DIRECTORY.resolve(filename);
		this.configValues = configValues;

		if (serverConfig) {
			if (!Files.exists(propertiesPath.getParent())) {
				try {
					Files.createDirectories(propertiesPath.getParent());
				} catch (IOException e) {
					Main.LOGGER.error("Error while initializing Server Config for server \"" + filename  + "\"!");
					e.printStackTrace();
				}
			}
		}
	}

	public PropertiesHandler initialize() {
		try {
			load();
			save();
		} catch (IOException e) {
			Main.LOGGER.error("Error while initializing Properties Config for file " + "\"" + propertiesPath + "\"" + "!");
			e.printStackTrace();
		}

		return this;
	}

	public void load() throws IOException {

		if (!Files.exists(propertiesPath)) {
			// return bc the file has not been saved yet
			return;
		}

		Properties properties = new Properties();

		properties.load(Files.newInputStream(propertiesPath));

		properties.forEach((o, o2) -> configValues.put(o.toString(), o2.toString()));

	}

	public void save() throws IOException {

		if (!Files.exists(propertiesPath.getParent())) {
			throw new RuntimeException("Could not find directory \"" + propertiesPath.getParent() + "\"!");
		}

		Properties properties = new Properties();

		configValues.forEach(properties::put);

		properties.store(Files.newOutputStream(propertiesPath), "This stores the configuration properties for the quotebook bot");

	}

	public static Builder builder() {
		return new Builder();
	}

	public void setConfigOption(String option, String newValue) {
		configValues.replace(option, newValue);
	}

	public void reload() {
		try {
			save();
			load();
		} catch (IOException e) {
			Main.LOGGER.error("Error while initializing Properties Config for file " + "\"" + propertiesPath + "\"" + " for Guild " + "\"" + configValues.get("name") + "\"" + "!");
			e.printStackTrace();
		}
	}

	public <T> T getConfigOption(String key, Function<String, T> parser) {
		return parser.apply(configValues.get(key));
	}

	public boolean hasConfigOption(String key) {
		return configValues.get(key) != null && !configValues.get(key).isEmpty();
	}

	public boolean containsKey(String key) {
		return configValues.containsKey(key);
	}

	public Map<String, String> getConfigValues() {
		return this.configValues;
	}

	/**
	 * Returns a string representation of the object.
	 *
	 * @return a string representation of the object.
	 * @apiNote In general, the
	 * {@code toString} method returns a string that
	 * "textually represents" this object. The result should
	 * be a concise but informative representation that is easy for a
	 * person to read.
	 * It is recommended that all subclasses override this method.
	 * The string output is not necessarily stable over time or across
	 * JVM invocations.
	 * @implSpec The {@code toString} method for class {@code Object}
	 * returns a string consisting of the name of the class of which the
	 * object is an instance, the at-sign character `{@code @}', and
	 * the unsigned hexadecimal representation of the hash code of the
	 * object. In other words, this method returns a string equal to the
	 * value of:
	 * <blockquote>
	 * <pre>
	 * getClass().getName() + '@' + Integer.toHexString(hashCode())
	 * </pre></blockquote>
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder
			.append("\"")
			.append(propertiesPath.getFileName())
			.append("\"")
			.append(": ")
			.append("{\n");

		configValues.forEach((s, s2) -> {
			builder
				.append("\"")
				.append(s)
				.append("\"")
				.append(": ")
				.append("\"")
				.append(s2)
				.append("\"")
				.append("\n");
		});

		builder.append("}");

		return builder.toString();
	}

	public static class Builder {

		private final Map<String, String> configValues = new HashMap<>();
		private String filename;
		private boolean serverConfig = false;

		private Builder() {}

		public Builder addConfigOption(String key, String defaultValue) {
			configValues.put(key, defaultValue);
			return this;
		}

		public Builder setFileName(String fileName) {
			if (!fileName.endsWith(".properties")) {
				fileName += ".properties";
			}

			this.filename = fileName;

			return this;
		}

		public Builder serverConfig() {
			this.serverConfig = true;
			return this;
		}

		public PropertiesHandler build() {
			PropertiesHandler propertiesHandler = new PropertiesHandler(filename, configValues, serverConfig);
			propertiesHandler.initialize();
			System.out.println("Properties Handler with file name \"" + filename + "\" created on path \"" + propertiesHandler.propertiesPath + "\"");
			return propertiesHandler;
		}
	}

}
