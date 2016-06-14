package de.zalando.buildstatus.job;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JobsIO {

    private static final Pattern JOB_NAME_PATTERN = Pattern.compile(".*\\/(.*).json");

    public static void writeJenkinsJobToFile(String host, String jobName, String userName, String password, File dest)
            throws IOException {

        JSONObject json = new JSONObject();
        json.put("host", host);
        json.put("userName", userName);
        json.put("password", password);
        writeJobJsonToFile(dest, jobName, json);
    }

    private static void writeJobJsonToFile(File dest, String jobName, JSONObject json) throws IOException {

        String path = dest.getAbsolutePath();
        if(dest.isDirectory()) {
            path = dest.getAbsolutePath() + "/" + jobName + ".json";
        }
        File file = new File(path);

        if(file.exists()) {
            throw new JobAlreadyExistsException(jobName);
        }

        FileOutputStream out = new FileOutputStream(path);
        String data = json.toString();
        out.write(data.getBytes());
        out.close();
    }

    public static void deleteJobFile(File jobsDirectory, String jobName) throws IOException {
        String absolutePath = jobsDirectory.getAbsolutePath() + "/" + jobName + ".json";
        if(!new File(absolutePath).delete()) {
            throw new IOException("failed to delete job file [" + absolutePath + "]");
        }
    }

    public static Collection<Job> readJobs(String folderPath) throws IOException {

        LinkedList<Job> jobs = new LinkedList<>();

        if(!folderPath.endsWith("/")) {
            folderPath = folderPath + "/";
        }

        File file = new File(folderPath);
        String[] list = file.list();
        if(list == null) {
            return jobs;
        }
        for(String f : list) {
            if(!f.endsWith(".json")) {
                continue;
            }
            jobs.add(readJobFromFile(folderPath + f));
        }

        return jobs;
    }

    private static Job readJobFromFile(String path) throws IOException {

        String jobName = extractJobNameFromFilePath(path);
        JSONObject json = new JSONObject(IOUtils.toString(new FileInputStream(path)));
        String type = json.getString("type");

        if("jenkins".equalsIgnoreCase(type)) {
            return new JenkinsJob(json.getString("host"), jobName, json.getString("userName"), json.getString("password"));
        }

        throw new RuntimeException("unknown job type: [" + type);
    }

    private static String extractJobNameFromFilePath(String path) {
        if(!path.contains("/")) {
            return path.replace(".json", "");
        }
        Matcher m = JOB_NAME_PATTERN.matcher(path);
        if(!m.matches()) {
            throw new IllegalArgumentException("failed to read job name from path [" + path + "]");
        }
        return m.group(1);
    }
}
