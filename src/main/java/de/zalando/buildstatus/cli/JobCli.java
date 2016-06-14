package de.zalando.buildstatus.cli;

import de.zalando.buildstatus.job.Job;
import de.zalando.buildstatus.job.JobsIO;

import java.io.IOException;
import java.util.Collection;

public class JobCli {

    public static void main(String[] args) {

        if("list".equals(args[0])) {
            listJobDetails();
        }
    }

    private static Collection<Job> readJobsFromFile() {
        Collection<Job> jobs = null;
        String jobsPath = System.getenv("TE_CONF_DIR");
        if(jobsPath == null) {
            jobsPath = "/var/te-jobs";
        }
        try {
            jobs = JobsIO.readJobs(jobsPath);
        } catch (IOException e) {
            System.err.println("Failed to read jobs from path [" + jobsPath + "]");
            System.err.println(e.getMessage());
            System.exit(-1);
        }
        return jobs;
    }

    private static void listJobDetails() {
        Collection<Job> jobs = readJobsFromFile();
        for(Job job : jobs) {
            System.out.print(job.getPrintableDetails());
        }
    }
}
