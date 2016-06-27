package de.zalando.buildstatus.daemon;

import de.zalando.buildstatus.BuildStatusMonitor;
import de.zalando.buildstatus.display.ClewareTrafficLightDisplay;
import de.zalando.buildstatus.job.JobsIO;

public class BuildStatusPoll {

    public static void main(String[] args) throws Exception {
        BuildStatusMonitor monitor = new BuildStatusMonitor(new ClewareTrafficLightDisplay());
        monitor.update(JobsIO.readJobs(args[0]));
    }
}
