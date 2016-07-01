package de.zalando.buildstatus.daemon;

import de.zalando.buildstatus.BuildStatusMonitor;
import de.zalando.buildstatus.display.ClewareTrafficLightDisplay;
import de.zalando.buildstatus.http.SimpleHttpClient;
import de.zalando.buildstatus.job.JobsIO;

public class BuildStatusPoll {

    public static void main(String[] args) throws Exception {

        BuildStatusMonitor monitor = new BuildStatusMonitor(
                new ClewareTrafficLightDisplay(),
                new SimpleHttpClient());

        monitor.updateDisplay(JobsIO.readJobs(args[0]));
    }
}
