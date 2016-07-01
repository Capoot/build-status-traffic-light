package de.zalando.buildstatus.job;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JobsIO {

    private static final Pattern JOB_NAME_PATTERN = Pattern.compile(".*\\/(.*).json");

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

        if(JenkinsJob.TYPE.equalsIgnoreCase(type)) {
            return readJenkinsJob(jobName, json);
        }
        if(GenericRestApiJob.TYPE.equalsIgnoreCase(type)) {
            return readGenericRestApiJob(json);
        }

        throw new RuntimeException("unknown job type: [" + type);
    }

    private static Job readJenkinsJob(String jobName, JSONObject json) {
        return new JenkinsJob(
                json.getString("host"),
                jobName,
                readOptionalJsonAttribute(json, "userName"),
                readOptionalJsonAttribute(json, "password"),
                json.getBoolean("acceptInsecureSslCert"));
    }

    private static Job readGenericRestApiJob(JSONObject json) {
        return new GenericRestApiJob(
                json.getString("url"),
                readOptionalJsonAttribute(json, "userName"),
                readOptionalJsonAttribute(json, "password"),
                json.getString("successRegex"),
                readOptionalJsonAttribute(json, "unstableRegex"),
                json.getBoolean("acceptInsecureSslCert"));
    }

    private static String readOptionalJsonAttribute(JSONObject json, String key) {
        try {
            return json.getString(key);
        } catch(JSONException e) {
            return null;
        }
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
