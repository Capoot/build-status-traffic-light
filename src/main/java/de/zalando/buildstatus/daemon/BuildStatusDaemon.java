package de.zalando.buildstatus.daemon;

import de.zalando.buildstatus.BuildStatusMonitor;
import de.zalando.buildstatus.display.ClewareTrafficLightDisplay;
import de.zalando.buildstatus.display.Display;
import de.zalando.buildstatus.display.SystemOutDisplay;
import de.zalando.buildstatus.job.JobsIO;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class BuildStatusDaemon implements Daemon {

    private static final String DEFAULT_CONF_DIR = "/etc/te/";
    private static final String DEFAULT_DATA_DIR = "/var/te/";

    // TODO: ask colleagues if they know a good strategy to test daemon code

    private String confDir;
    private String dataDir;

    private boolean isStopped = false;
    private DaemonConfig config;
    private BuildStatusMonitor buildStatusMonitor;

    @Override
    public void start() {

        readConfig();
        initBuildStatusMonitor();

        do {
            pollStatus(config, buildStatusMonitor);
        } while(!isStopped);
    }

    private void readConfig() {
        Properties p = new Properties();
        try {
            p.load(new FileInputStream(confDir));
        } catch (IOException e) {
            throw new RuntimeException("failed to read config", e);
        }
        config = new DaemonConfig(p);
    }

    private void initBuildStatusMonitor() {
        Display display = initDisplay(config);
        try {
            buildStatusMonitor = new BuildStatusMonitor(JobsIO.readJobs(dataDir), display);
        } catch (IOException e) {
            throw new RuntimeException("failed to init build status monitor", e);
        }
    }

    private Display initDisplay(DaemonConfig config) {
        if(config.isSystemOutDisplay()) {
            return new SystemOutDisplay();
        }
        return new ClewareTrafficLightDisplay();
    }

    private void pollStatus(DaemonConfig config, BuildStatusMonitor buildStatusMonitor) {
        buildStatusMonitor.update();
        try {
            Thread.sleep(config.getPollingInterval());
        } catch (InterruptedException e) {
            // TODO: research whether we have to exit when we were interrupted
            System.err.println("Got interrupted; trying to exit gracefully...");
            isStopped = true;
        }
    }

    @Override
    public void stop() {
        isStopped = true;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void init(DaemonContext daemonContext) throws Exception {
        confDir = readFromEnvOrDefault("TE_CONF_DIR", DEFAULT_CONF_DIR);
        dataDir = readFromEnvOrDefault("TE_DATA_DIR", DEFAULT_DATA_DIR);
    }

    public String readFromEnvOrDefault(String envKey, String defaultValue) {
        String value = System.getenv(envKey);
        if(value == null || value.isEmpty()) {
            return defaultValue;
        }
        return value;
    }
}
