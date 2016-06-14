package de.zalando.buildstatus.cli;

import de.zalando.buildstatus.display.Display;
import de.zalando.buildstatus.BuildStatusMonitor;
import de.zalando.buildstatus.job.Job;
import de.zalando.buildstatus.job.JobsIO;
import de.zalando.buildstatus.display.ClewareTrafficLightDisplay;
import de.zalando.buildstatus.display.SystemOutDisplay;

import java.io.IOException;
import java.util.Collection;

public class CommandLineInterface {

    public static void main(String[] args) {

        CommandLineOptions options = new CommandLineOptions(args);

        Collection<Job> jobs = readJobsFromFile();
        Display display = initDisplay(options);
        BuildStatusMonitor buildStatusMonitor = new BuildStatusMonitor(jobs, display);

        if("list".equals(args[0])) {
            listJobs(buildStatusMonitor);
        }
    }

    private static Collection<Job> readJobsFromFile() {
        Collection<Job> jobs = null;
        String jobsPath = System.getenv("CONF_DIR");
        try {
            jobs = JobsIO.readJobs(jobsPath);
        } catch (IOException e) {
            System.err.println("Failed to read jobs from path [" + jobsPath + "]");
            System.err.println(e.getMessage());
            System.exit(-1);
        }
        return jobs;
    }

    private static Display initDisplay(CommandLineOptions options) {
        Display display;
        if(options.isSystemOutDisplay()) {
            display = new SystemOutDisplay();
        } else {
            display = new ClewareTrafficLightDisplay();
        }
        return display;
    }

    private static void listJobs(BuildStatusMonitor buildStatusMonitor) {
        for(String line : buildStatusMonitor.listJobDetails()) {
            System.out.println(line);
        }
    }
}
