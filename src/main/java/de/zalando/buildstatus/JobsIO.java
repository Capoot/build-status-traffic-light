package de.zalando.buildstatus;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class JobsIO {

    public static void writeJenkinsJobToFile(String host, String jobName, String userName, String password, File dest)
            throws IOException {

        if(!host.endsWith("/")) {
            host = host + "/";
        }

        JSONObject json = new JSONObject();
        json.put("url", host + jobName + "/api/json");
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
}
