package de.zalando.buildstatus.daemon;

import de.zalando.buildstatus.BuildStatusMonitor;
import de.zalando.buildstatus.display.ClewareTrafficLightDisplay;
import de.zalando.buildstatus.display.Display;
import de.zalando.buildstatus.display.SystemOutDisplay;
import de.zalando.buildstatus.job.Job;
import de.zalando.buildstatus.job.JobsIO;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

public class BuildStatusDaemon implements Daemon {

    public static final String DEFAULT_CONF_DIR = "/etc/te/";
    public static final String DEFAULT_DATA_DIR = "/var/te/";
    public static final String CONF_DIR_ENV_KEY = "TE_CONF_DIR";
    public static final String DATA_DIR_ENV_KEY = "TE_DATA_DIR";

    private String confDir;
    private String dataDir;

    private boolean isStopped = false;
    private DaemonConfig config;
    private BuildStatusMonitor buildStatusMonitor;

    private Thread daemonThread;

    @Override
    public void init(DaemonContext daemonContext) throws Exception {

        confDir = readFromEnvOrDefault(CONF_DIR_ENV_KEY, DEFAULT_CONF_DIR);
        dataDir = readFromEnvOrDefault(DATA_DIR_ENV_KEY, DEFAULT_DATA_DIR);

        daemonThread = new Thread(() -> {

            readConfig();
            initBuildStatusMonitor();

            do {
                pollStatus();
            } while(!isStopped);
        });
    }

    private String readFromEnvOrDefault(String envKey, String defaultValue) {
        String value = System.getenv(envKey);
        if(value == null || value.isEmpty()) {
            return defaultValue;
        }
        return value;
    }

    @Override
    public void start() {
        isStopped = false;
        daemonThread.start();
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
        buildStatusMonitor = new BuildStatusMonitor(display);
    }

    private Display initDisplay(DaemonConfig config) {
        if(config.isSystemOutDisplay()) {
            return new SystemOutDisplay();
        }
        return new ClewareTrafficLightDisplay();
    }

    private void pollStatus() {

        Collection<Job> jobs;
        try {
            jobs = JobsIO.readJobs(dataDir);
        } catch (IOException e) {
            throw new RuntimeException("failed to read jobs", e);
        }

        buildStatusMonitor.update(jobs);
        try {
            Thread.sleep(config.getPollingInterval());
        } catch (InterruptedException e) {
            System.err.println("Got interrupted; trying to exit gracefully...");
            isStopped = true;
        }
    }

    @Override
    public void stop() {
        isStopped = true;
        try {
            daemonThread.join(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() {

    }
}
