package com.amazon.framework.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigManager — reads environment-specific config files.
 *
 * File resolution order:
 *   1. Looks for config/{env}.properties  (e.g. config/staging.properties)
 *   2. Falls back to config/local.properties
 *
 * Usage: ConfigManager.get("base.url")
 */
public class ConfigManager {

    private static final Logger log = LogManager.getLogger(ConfigManager.class);
    private static final Properties props = new Properties();

    static {
        String env = System.getProperty("env", "local");
        String configFile = "config/" + env + ".properties";
        try (InputStream is = ConfigManager.class
                .getClassLoader().getResourceAsStream(configFile)) {
            if (is != null) {
                props.load(is);
                log.info("Loaded config: {}", configFile);
            } else {
                log.warn("Config file not found: {} — falling back to local.properties", configFile);
                try (InputStream fallback = ConfigManager.class
                        .getClassLoader().getResourceAsStream("config/local.properties")) {
                    if (fallback != null) props.load(fallback);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config: " + configFile, e);
        }
    }

    public static String get(String key) {
        String value = props.getProperty(key);
        if (value == null) throw new RuntimeException("Config key not found: " + key);
        return value;
    }

    public static String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }
}
