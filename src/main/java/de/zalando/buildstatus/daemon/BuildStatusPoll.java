package de.zalando.buildstatus.daemon;

import de.zalando.buildstatus.BuildStatusMonitor;
import de.zalando.buildstatus.display.ClewareTrafficLightDisplay;
import de.zalando.buildstatus.http.SimpleHttpClient;
import de.zalando.buildstatus.job.JobsIO;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class BuildStatusPoll {

    public static void main(String[] args) throws Exception {

        if("-version".equals(args[0])) {
            printVersion();
            System.exit(0);
        }

        BuildStatusMonitor monitor = new BuildStatusMonitor(
                new ClewareTrafficLightDisplay(),
                new SimpleHttpClient());

        monitor.updateDisplay(JobsIO.readJobs(args[0]));
    }

    private static void printVersion() throws IOException {
        InputStream in = BuildStatusPoll.class.getResourceAsStream("/version");
        String version = IOUtils.toString(in);
        System.out.println(version);
    }
}
