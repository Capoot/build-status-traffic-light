package de.zalando.buildstatus.job;

import de.zalando.buildstatus.http.SimpleHttpClient;

import java.util.regex.Pattern;

public class TravisCiDotOrgJob implements Job {

    public static final String TYPE = "travis-ci.org";

    private static final Pattern SUCCESSFULL_BUILD_PATTERN = Pattern.compile(".*passing.*");

    private final String url;

    public TravisCiDotOrgJob(String jobName, String owner, String branch) {
        url = "https://api.travis-ci.org/" + owner + "/" + jobName + "?branch=" + branch;
    }

    @Override
    public JobStatus queryStatus(SimpleHttpClient client) {
        String response = client.sendGetWithBasicAuth(url, null, null, false);
        if(SUCCESSFULL_BUILD_PATTERN.matcher(response).matches()) {
            return JobStatus.SUCCESS;
        }
        return JobStatus.FAILED;
    }

    String getUrl() {
        return url;
    }
}
