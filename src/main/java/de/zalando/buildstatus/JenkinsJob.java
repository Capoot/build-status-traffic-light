package de.zalando.buildstatus;


import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.fluent.Request;
import org.json.JSONObject;

import java.io.IOException;

public class JenkinsJob implements Job {

    private final String serverUrl;
    private final String jobName;
    private final String authHeader;

    public JenkinsJob(String serverUrl, String jobName, String user, String password) {

        this.serverUrl = serverUrl;
        this.jobName = jobName;

        String basicAuthString = user + ":" + password;
        this.authHeader = Base64.encodeBase64String(basicAuthString.getBytes());
    }

    @Override
    public JobStatus queryStatus() {
        final String url = serverUrl + "/" + jobName + "/api/json";
        String jsonString = requestJobFromJsonApi(url);
        JSONObject json = new JSONObject(jsonString);
        String color = json.get("color").toString().toLowerCase();
        switch(color) {
            case "red" : return JobStatus.FAILED;
            case "yellow" : return JobStatus.UNSTABLE;
            case "blue" : return JobStatus.SUCCESS;
            case "red_anime" : return JobStatus.FAILED_ANIMATION;
            case "yellow_anime" : return JobStatus.UNSTABLE_ANIMATION;
            case "blue_anime" : return JobStatus.SUCCESS_ANIMATION;
        }
        throw new IllegalStateException("Failed to retrieve job status from jenkins url = [" + url + "]");
    }

    private String requestJobFromJsonApi(String url) {
        try {
            Request request = Request.Get(url);
            if (isSecured()) {
                request.addHeader("Authorization", authHeader);
            }
            return request.execute().returnContent().asString();
        } catch (IOException e) {
            throw new RuntimeException("failed to send request to Jenkins API", e);
        }
    }

    private boolean isSecured() {
        return authHeader != null;
    }
}
