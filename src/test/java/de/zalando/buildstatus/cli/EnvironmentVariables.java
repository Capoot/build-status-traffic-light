package de.zalando.buildstatus.cli;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * A Helper for manipulating environment variables in tests
 */
public class EnvironmentVariables {

    /**
     * Sets an environment variable in the JVM. This variable will not be visible outside of the JVM.
     * @param key environment variable's name
     * @param value environment variable's value
     */
    public static void set(String key, String value) {
        getModifyableEnvironmentMap().put(key, value);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> getModifyableEnvironmentMap() {

        Map<String, String> env = System.getenv();
        Class<?> cu = env.getClass();

        try {
            Field m = cu.getDeclaredField("m");
            m.setAccessible(true);
            return (Map<String, String>) m.get(env);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("failed to retrieve modifyable env map", e);
        }
    }

    /**
     * Removes the given environment variable. The effect will not be visible outside the JVM.
     * @param key environment variable's name
     */
    public static void remove(String key) {
        getModifyableEnvironmentMap().remove(key);
    }
}