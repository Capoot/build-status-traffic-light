package de.zalando.buildstatus.daemon;

import de.zalando.buildstatus.BuildStatusMonitor;
import de.zalando.buildstatus.display.ClewareTrafficLightDisplay;
import de.zalando.buildstatus.display.Display;
import de.zalando.buildstatus.display.SystemOutDisplay;
import de.zalando.buildstatus.job.JobsIO;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class BuildStatusDaemon implements Runnable {

    private static final String DEFAULT_CONF_DIR = "/etc/te-buildstatus";

    // TODO: ask colleagues if they know a good strategy to test daemon code

    private boolean isStopped = false;
    private DaemonConfig config;
    private BuildStatusMonitor buildStatusMonitor;

    @Override
    public void run() {

        readConfig();
        initBuildStatusMonitor();

        do {
            pollStatus(config, buildStatusMonitor);
        } while(!isStopped);
    }

    private void readConfig() {
        String confDir = System.getenv("TE_CONF_DIR");
        if(confDir == null || confDir.isEmpty()) {
            confDir = DEFAULT_CONF_DIR;
        }
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
            buildStatusMonitor = new BuildStatusMonitor(JobsIO.readJobs(config.getDataPath()), display);
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

    public void stop() {
        isStopped = true;
    }

    // TODO: we need something to re-read the config
}
