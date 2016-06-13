package de.zalando.buildstatus;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
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
        storeJobAsJson(host, jobName, userName, password);

        buildStatusMonitor.addJob(job);
        buildStatusMonitor.update(singletonList(job));
    }

    private void storeJobAsJson(String host, String jobName, String userName, String password) throws IOException {
        JSONObject json = new JSONObject();
        json.put("url", host + jobName + "/api/json");
        json.put("userName", userName);
        json.put("password", password);
        writeJobJsonToFile(jobName, json);
    }

    private void writeJobJsonToFile(String jobName, JSONObject json) throws IOException {

        String path = jobsDirectory.getAbsolutePath() + "/" + jobName + ".json";
        File file = new File(path);

        if(file.exists()) {
            throw new JobAlreadyExistsException(jobName);
        }

        FileOutputStream out = new FileOutputStream(path);
        String data = json.toString();
        out.write(data.getBytes());
        out.close();
    }
}
