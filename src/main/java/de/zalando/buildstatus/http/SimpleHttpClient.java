package de.zalando.buildstatus.http;

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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import static org.apache.http.util.TextUtils.isEmpty;

public class SimpleHttpClient {

    private HttpClient defaultClient;
    private HttpClient insecureSslClient;

    public String sendGetWithBasicAuth(String url, String userName, String password,
            boolean acceptInsecureSslCert) {

        HttpClient client;
        try {
            client = buildClient(acceptInsecureSslCert);
        } catch (Exception e) {
            throw new RuntimeException("failed to init HTTP client", e);
        }

        try {
            return sendGetWithBasicAuth(url, userName, password, client);
        } catch (IOException e) {
            throw new RuntimeException("failed to send request to Jenkins API", e);
        }
    }

    private String sendGetWithBasicAuth(String url, String userName, String password, HttpClient client)
            throws IOException {

        HttpGet get = new HttpGet(url);
        addAuthHeader(userName, password, get);
        HttpResponse response = client.execute(get);

        int statusCode = response.getStatusLine().getStatusCode();
        if(statusCode != 200) {
            throw new RuntimeException("failed to access [" + url + "] status code [" + statusCode + "]");
        }

        return IOUtils.toString(response.getEntity().getContent());
    }

    private void addAuthHeader(String userName, String password, HttpGet request) {
        String basicAuthString = userName + ":" + password;
        if(!isEmpty(userName) && !isEmpty(password)) {
            request.addHeader("Authorization", "Basic " + Base64.encodeBase64String(basicAuthString.getBytes()));
        }
    }

    private HttpClient buildClient(boolean acceptInsecureSslCert) throws KeyStoreException, NoSuchAlgorithmException,
            KeyManagementException, IOException {

        if(!acceptInsecureSslCert) {
            if(defaultClient == null) {
                defaultClient = HttpClients.createDefault();
            }
            return defaultClient;
        }

        if(insecureSslClient == null) {
            initInsecureSslHttpClient();
        }

        return insecureSslClient;
    }

    private void initInsecureSslHttpClient() throws NoSuchAlgorithmException, KeyManagementException,
            KeyStoreException {

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (arg0, arg1) -> true).build();
        httpClientBuilder.setSslcontext(sslContext);

        HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
        RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory)
                .build();

        insecureSslClient = httpClientBuilder.build();
    }
}
