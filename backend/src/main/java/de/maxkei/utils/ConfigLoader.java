package de.maxkei.utils;

import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;

/**
 * An interface for loading configuration values from property files.
 */
public interface ConfigLoader
{
    /**
     * Loads a configuration value from a property file.
     *
     * @param configPath   The path to the configuration file.
     * @param key          The key of the configuration value.
     * @param defaultValue The default value to return if the key is not found.
     * @return The value associated with the given key in the configuration file, or the default value if not found.
     */
    static @NotNull String loadConfigValue(String configPath, String key, String defaultValue)
    {
        if(!configPath.endsWith(".properties"))
            return defaultValue;

        Properties properties = new Properties();
        URL resource = ConfigLoader.class.getClassLoader().getResource(configPath);
        if(resource == null)
            return defaultValue;

        try(FileInputStream input = new FileInputStream(resource.getPath()))
        {
            properties.load(input);
            return properties.getProperty(key, defaultValue);
        } catch(Exception ignored)
        {
        }
        return defaultValue;
    }
}
