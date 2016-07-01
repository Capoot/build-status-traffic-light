package de.zalando.buildstatus.job;

import de.zalando.buildstatus.http.SimpleHttpClient;

import java.util.regex.Pattern;

public class GenericRestApiJob implements Job {

    private final String url;
    private final Pattern successPattern;
    private final Pattern unstablePattern;
    private final String userName;
    private final String password;
    private final boolean acceptSelfSignedSslCert;

    public GenericRestApiJob(String url, String userName, String password, String successRegex, String unstableRegex,
            boolean acceptSelfSignedSslCert) {

        this.url = url;
        this.userName = userName;
        this.password = password;
        this.acceptSelfSignedSslCert = acceptSelfSignedSslCert;

        successPattern = Pattern.compile(successRegex);
        unstablePattern = Pattern.compile(unstableRegex);
    }

    @Override
    public JobStatus queryStatus(SimpleHttpClient client) {

        String responseText = client.sendGetWithBasicAuth(url, userName, password, acceptSelfSignedSslCert);

        if(successPattern.matcher(responseText).matches()) {
            return JobStatus.SUCCESS;
        }

        if(unstablePattern.matcher(responseText).matches()) {
            return JobStatus.UNSTABLE;
        }

        return JobStatus.FAILED;
    }

    @Override
    public String getName() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getPrintableDetails() {
        throw new RuntimeException("not implemented");
    }
}
