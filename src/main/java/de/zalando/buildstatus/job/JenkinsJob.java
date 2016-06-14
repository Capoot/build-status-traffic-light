package de.zalando.buildstatus.job;


import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.fluent.Request;
import org.json.JSONObject;

import java.io.IOException;

public class JenkinsJob implements Job {

    private final String authHeader;
    private final String url;
    private final String name;
    private final String host;

    public JenkinsJob(String host, String jobName, String user, String password) {

        if(!host.endsWith("/")) {
            host = host + "/";
        }

        this.host = host;
        url = host + jobName + "/api/json";
        name = jobName;

        String basicAuthString = user + ":" + password;
        this.authHeader = Base64.encodeBase64String(basicAuthString.getBytes());
    }


    @Override
    public JobStatus queryStatus() {
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

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDetails() {
        return name + "\t(jenkins)\t" + url;
    }

    public String getUrl() {
        return url;
    }

    public String getHost() {
        return host;
    }
}
