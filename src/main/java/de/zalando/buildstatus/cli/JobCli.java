package de.zalando.buildstatus.cli;

import de.zalando.buildstatus.daemon.BuildStatusDaemon;
import de.zalando.buildstatus.job.Job;
import de.zalando.buildstatus.job.JobsIO;

import java.io.IOException;
import java.util.Collection;

public class JobCli {

    public static void main(String[] args) {

        try {
            if ("list".equals(args[0])) {
                listJobDetails();
            }
        } catch(Exception e) {
            System.err.println(e.toString());
            System.exit(-1);
        }
    }

    private static void listJobDetails() throws IOException {
        Collection<Job> jobs = readJobsFromFile();
        for(Job job : jobs) {
            System.out.print(job.getPrintableDetails());
        }
    }

    private static Collection<Job> readJobsFromFile() throws IOException {
        String jobsPath = getDataDir();
        return JobsIO.readJobs(jobsPath);
    }

    private static String getDataDir() {
        String jobsPath = System.getenv(BuildStatusDaemon.DATA_DIR_ENV_KEY);
        if(jobsPath == null) {
            jobsPath = BuildStatusDaemon.DEFAULT_DATA_DIR;
        }
        return jobsPath;
    }
}
