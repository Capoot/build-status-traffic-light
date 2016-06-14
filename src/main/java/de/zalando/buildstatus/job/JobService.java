package de.zalando.buildstatus.job;

import de.zalando.buildstatus.BuildStatusMonitor;

import java.io.File;
import java.io.IOException;

import static java.util.Collections.singletonList;

public class JobService {

    private final File jobsDirectory;
    private final BuildStatusMonitor buildStatusMonitor;

    public JobService(String jobsDirectory, BuildStatusMonitor buildStatusMonitor) {
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
        buildStatusMonitor.removeJob(jobName);
    }

    public void updateJenkinsJob(String host, String jobName, String user, String password) throws IOException {
        JobsIO.deleteJobFile(jobsDirectory, jobName);
        JobsIO.writeJenkinsJobToFile(host, jobName, user, password, jobsDirectory);
        buildStatusMonitor.removeJob(jobName);
        JenkinsJob job = new JenkinsJob(host, jobName, user, password);
        buildStatusMonitor.addJob(job);
        buildStatusMonitor.update(singletonList(job));
    }
}
