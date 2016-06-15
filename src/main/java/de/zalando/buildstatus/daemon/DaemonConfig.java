package de.zalando.buildstatus.daemon;

import java.util.Properties;

import static java.lang.Long.parseLong;

public class DaemonConfig {

    private final long pollingInterval;
    private final boolean systemOutDisplay;

    public DaemonConfig(Properties properties) {
        pollingInterval = parseLong(readMandatoryProperty("pollingInterval", properties));
        systemOutDisplay = "true".equalsIgnoreCase(properties.getProperty("useSystemOutDisplay"));
    }

    private String readMandatoryProperty(String key, Properties properties) {
        if(!properties.containsKey(key)) {
            throw new IllegalStateException("missing mandatory property [" + key + "] in config");
        }
        return properties.getProperty(key);
    }

    public long getPollingInterval() {
        return pollingInterval;
    }

    public boolean isSystemOutDisplay() {
        return systemOutDisplay;
    }
}
