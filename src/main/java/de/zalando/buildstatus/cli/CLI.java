package de.zalando.buildstatus.cli;

import de.zalando.buildstatus.BuildStatusMonitor;
import de.zalando.buildstatus.Job;
import de.zalando.buildstatus.JobsIO;
import de.zalando.buildstatus.LinuxClewareTrafficLight;

import java.io.IOException;
import java.util.Collection;

public class CLI {

    public static void main(String[] args) {

        Collection<Job> jobs = null;
        String jobsPath = System.getenv("CONF_DIR");
        try {
            jobs = JobsIO.readJobs(jobsPath);
        } catch (IOException e) {
            System.err.println("Failed to read jobs from path [" + jobsPath + "]");
            System.err.println(e.getMessage());
            System.exit(-1);
        }

        BuildStatusMonitor buildStatusMonitor = new BuildStatusMonitor(jobs, new LinuxClewareTrafficLight());

        if("list".equals(args[0])) {
            for(String line : buildStatusMonitor.listJobDetails()) {
                System.out.println(line);
            }
        }
    }
}
