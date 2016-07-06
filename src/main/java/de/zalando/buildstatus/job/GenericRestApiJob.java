package de.zalando.buildstatus.job;

import de.zalando.buildstatus.http.SimpleHttpClient;

import java.util.regex.Pattern;

public class GenericRestApiJob implements Job {

    public static final String TYPE = "generic-rest-api";

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
        if(unstableRegex != null && !unstableRegex.isEmpty()) {
            unstablePattern = Pattern.compile(unstableRegex);
        } else {
            unstablePattern = null;
        }
    }

    @Override
    public JobStatus queryStatus(SimpleHttpClient client) {

        String responseText = client.sendGetWithBasicAuth(url, userName, password, acceptSelfSignedSslCert);

        if(successPattern.matcher(responseText).matches()) {
            return JobStatus.SUCCESS;
        }

        if(unstablePattern != null && unstablePattern.matcher(responseText).matches()) {
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

    public String getUrl() {
        return url;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isAcceptSelfSignedSslCert() {
        return acceptSelfSignedSslCert;
    }

    public String getPassword() {
        return password;
    }

    public String getSuccessRegex() {
        return successPattern.toString();
    }

    public String getUnstableRegex() {
        if(unstablePattern == null) {
            return null;
        }
        return unstablePattern.toString();
    }
}
