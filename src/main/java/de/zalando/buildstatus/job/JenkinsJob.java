package de.zalando.buildstatus.job;


import de.zalando.buildstatus.http.SimpleHttpClient;
import org.json.JSONObject;

import static org.apache.http.util.TextUtils.isEmpty;

public class JenkinsJob implements Job {

    public static final String TYPE = "jenkins";

    private final String url;
    private final String name;
    private final String host;
    private final boolean acceptInsecureSslCert;
    private final String userName;
    private final String password;

    public JenkinsJob(String host, String jobName, String user, String password, boolean acceptInsecureSslCert) {

        if(isEmpty(host) || isEmpty(jobName)) {
            throw new IllegalArgumentException("host + jobName args must not be empty");
        }

        if(!host.startsWith("http://") && !host.startsWith("https://")) {
            host = "http://" + host;
        }
        this.host = host;
        if(!host.endsWith("/")) {
            host = host + "/";
        }

        url = host + "job/" + jobName + "/api/json";
        name = jobName;
        this.acceptInsecureSslCert = acceptInsecureSslCert;
        this.userName = user;
        this.password = password;
    }

    @Override
    public JobStatus queryStatus(SimpleHttpClient client) {
        String jsonString = client.sendGetWithBasicAuth(url, userName, password, acceptInsecureSslCert);
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

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPrintableDetails() {
        return name + "\t(jenkins)\t" + url;
    }

    public String getUrl() {
        return url;
    }

    public String getHost() {
        return host;
    }

    public boolean isAcceptInsecureSslCert() {
        return acceptInsecureSslCert;
    }
}
