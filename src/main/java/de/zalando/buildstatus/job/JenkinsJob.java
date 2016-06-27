package de.zalando.buildstatus.job;


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.json.JSONObject;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import static org.apache.http.util.TextUtils.isEmpty;

public class JenkinsJob implements Job {

    public static final String TYPE = "jenkins";

    private final String authHeader;
    private final String url;
    private final String name;
    private final String host;
    private final boolean acceptInsecureSslCert;

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

        String basicAuthString = user + ":" + password;
        if(!isEmpty(user) && !isEmpty(password)) {
            authHeader = "Basic " + Base64.encodeBase64String(basicAuthString.getBytes());
        } else {
            authHeader = null;
        }
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

        HttpClient client;
        try {
            client = buildClient();
        } catch (Exception e) {
            throw new RuntimeException("failed to init HTTP client", e);
        }

        try {
            HttpGet get = new HttpGet(url);
            if (isSecured()) {
                get.addHeader("Authorization", authHeader);
            }
            HttpResponse response = client.execute(get);
            int statusCode = response.getStatusLine().getStatusCode();
            if(statusCode != 200) {
                throw new RuntimeException("failed to access [" + url + "] status code [" + statusCode + "]");
            }
            return IOUtils.toString(response.getEntity().getContent());
        } catch (IOException e) {
            throw new RuntimeException("failed to send request to Jenkins API", e);
        }
    }

    private HttpClient buildClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException,
            IOException {

        if(!acceptInsecureSslCert) {
            return HttpClients.createDefault();
        }

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (arg0, arg1) -> true).build();
        httpClientBuilder.setSslcontext(sslContext);

        HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
        RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory)
                .build();

        return httpClientBuilder.build();
    }


    private boolean isSecured() {
        return authHeader != null;
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
