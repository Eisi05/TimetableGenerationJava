package de.maxkei.lang;

import de.maxkei.settings.Settings;
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * An interface for translating text.
 */
public interface Translator
{
    HashMap<Language, HashMap<String, String>> cache = new HashMap<>();

    /**
     * Retrieves the translation for the given key and path.
     *
     * @param key  The key to lookup in the translation file.
     * @param path The path to the translation file.
     * @return The translated text if found, or the key itself if not found.
     */
    default String getTranslation(String key, String path)
    {
        if(cache.containsKey(Settings.language) && cache.get(Settings.language).containsKey(path + "/" + key))
            return cache.get(Settings.language).get(path + "/" + key);

        String filePath = "lang/" + Settings.language.name() + "/" + path + ".yml";
        ClassLoader classLoader = Translator.class.getClassLoader();
        try(InputStream inputStream = classLoader.getResourceAsStream(filePath))
        {
            if(inputStream != null)
            {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream));
                if(config.contains(key))
                {
                    HashMap<String, String> map = new HashMap<>();
                    map.put(path + "/" + key, config.getString(key));
                    cache.put(Settings.language, map);
                    return config.getString(key);
                }
                return key;
            }
        } catch(Exception ignored)
        {
        }
        return key;
    }
}
