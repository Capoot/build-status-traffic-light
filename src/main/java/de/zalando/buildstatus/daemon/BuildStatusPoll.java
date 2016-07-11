package de.zalando.buildstatus.daemon;

import de.zalando.buildstatus.BuildStatusMonitor;
import de.zalando.buildstatus.display.ClewareTrafficLightDisplay;
import de.zalando.buildstatus.http.SimpleHttpClient;
import de.zalando.buildstatus.job.JobsIO;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class BuildStatusPoll {

    private static final Logger LOG = LoggerFactory.getLogger(BuildStatusPoll.class);

    public static void main(String[] args) throws Exception {

        if("-version".equals(args[0])) {
            printVersion();
            System.exit(0);
        }

        BuildStatusMonitor monitor = null;
        try {
            monitor = new BuildStatusMonitor(
                    new ClewareTrafficLightDisplay(),
                    new SimpleHttpClient());
        } catch(Exception e) {
            LOG.error("failed to init BuildStatusMonitor", e);
            System.exit(1);
        }

        try {
            monitor.updateDisplay(JobsIO.readJobs(args[0]));
        } catch(Exception e) {
            LOG.error("failed to update traffic light status", e);
            System.exit(1);
        }
    }

    private static void printVersion() throws IOException {
        InputStream in = BuildStatusPoll.class.getResourceAsStream("/version");
        String version = IOUtils.toString(in);
        System.out.println(version);
    }
}
