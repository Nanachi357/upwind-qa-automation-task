package com.ponomarenko.qa.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

/** Defaults from test.properties, overridden by JVM system properties. */
public final class TestConfig {
    private static final Properties DEFAULTS = loadDefaults();

    private TestConfig() {
    }

    public static String get(String key) {
        String value = System.getProperty(key, DEFAULTS.getProperty(key));
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing test configuration: " + key);
        }
        return value;
    }

    public static boolean getBoolean(String key) {
        String value = get(key);
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
            throw new IllegalArgumentException(key + " must be true or false");
        }
        return Boolean.parseBoolean(value);
    }

    public static long getPositiveLong(String key) {
        long value = Long.parseLong(get(key));
        if (value <= 0) {
            throw new IllegalArgumentException(key + " must be greater than zero");
        }
        return value;
    }

    private static Properties loadDefaults() {
        Properties properties = new Properties();
        try (InputStream stream = TestConfig.class.getResourceAsStream("/test.properties")) {
            if (stream == null) {
                throw new IllegalStateException("test.properties is missing from the classpath");
            }
            properties.load(stream);
            return properties;
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot load test.properties", exception);
        }
    }
}
