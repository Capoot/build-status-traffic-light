package de.zalando.buildstatus;

import java.io.File;
import java.io.IOException;

import static java.util.Collections.singletonList;

public class CommandLineInterface {

    private final File jobsDirectory;
    private final BuildStatusMonitor buildStatusMonitor;

    public CommandLineInterface(String jobsDirectory, BuildStatusMonitor buildStatusMonitor) {
        this.buildStatusMonitor = buildStatusMonitor;
        this.jobsDirectory = new File(jobsDirectory);
    }

    public void addJenkinsJob(String host, String jobName, String userName, String password) throws IOException {

        if(!host.endsWith("/")) {
            host = host.concat("/");
        }

        JenkinsJob job = new JenkinsJob(host, jobName, userName, password);
        JobsIO.writeJenkinsJobToFile(host, jobName, userName, password, jobsDirectory);

        buildStatusMonitor.addJob(job);
        buildStatusMonitor.update(singletonList(job));
    }

    public void removeJob(String jobName) throws IOException {
        JobsIO.deleteJobFile(jobsDirectory, jobName);
    }
}
