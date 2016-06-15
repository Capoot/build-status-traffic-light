package de.zalando.buildstatus.cli;

import de.zalando.buildstatus.daemon.BuildStatusDaemon;
import de.zalando.buildstatus.job.JenkinsJob;
import de.zalando.buildstatus.job.Job;
import de.zalando.buildstatus.job.JobsIO;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static de.zalando.buildstatus.cli.CliFlagsAndOptions.getOption;

public class JobCli {

    public static void main(String[] args) {

        try {
            switch(args[0]) {
                case "list" : listJobDetails(); break;
                case "add-jenkins" : addJenkinsJob(args); break;
                default: printUsage();
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

    private static void addJenkinsJob(String[] args) throws IOException {

        String host = getOption("host", args);
        String job = getOption("job", args);
        String user = getOption("user", args);
        String password = getOption("password", args);

        new JenkinsJob(host, job, user, password); // to validate
        JobsIO.writeJenkinsJobToFile(host, job, user, password, new File(getDataDir()));
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("------------------------");
        System.out.println("  - list");
        System.out.println("  - add-jenkins --host http://my-jenkins.com --job my-job --user ldap-user --password " +
                "ldap-password");
    }
}
