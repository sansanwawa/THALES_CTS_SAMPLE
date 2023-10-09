package org.thales.tokenization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.thales.tokenization.entities.request.DeTokenizeRequest;
import org.thales.tokenization.entities.request.TokenizeRequest;
import org.thales.tokenization.entities.TokenizeWrapper;
import org.thales.tokenization.entities.response.DeTokenizeResponse;
import org.thales.tokenization.entities.response.TokenizeResponse;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;

/**
 * Author :
 * sandy.haryono@thalesgroup.com
 */
public class CTSPoster {

    private static Logger logger = LogManager.getLogger(CTSPoster.class);

    private static String ctsTokenizeUrl;

    private static String ctsDeTokenizeUrl;


    private static int connection;


    public static void tokenizeInit(String ctsTokenizeUrl) {
        CTSPoster.ctsTokenizeUrl = ctsTokenizeUrl;
    }

    public static void deTokenizeInit(String ctsDeTokenizeUrl) {
        CTSPoster.ctsDeTokenizeUrl = ctsDeTokenizeUrl;
    }

    public static void setConnection(int connection) {
        CTSPoster.connection = connection;
    }


    /**
     * Layered socket factory for TLS/SSL connections.
     * SSLSocketFactory can be used to validate the identity of the HTTPS server against a list of
     * trusted certificates and to authenticate to the HTTPS server using a private key.
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    private static SSLConnectionSocketFactory getSSLConnectionFactoryRegistry() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        return new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
    }


    /**
     *
     * A connection manager for a single connection.
     * This connection manager maintains only one active connection.
     * Even though this class is fully thread-safe it ought to be used by one execution thread only,
     * as only one thread a time can lease the connection at a time.
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    private static BasicHttpClientConnectionManager getBasicHTTPClientConnectionManager() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        Registry<ConnectionSocketFactory> socketFactoryRegistry =
                RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("https", getSSLConnectionFactoryRegistry())
                        .register("http", new PlainConnectionSocketFactory())
                        .build();

        BasicHttpClientConnectionManager connectionManager =
                new BasicHttpClientConnectionManager(socketFactoryRegistry);


        return connectionManager;
    }


    /**
     * ClientConnectionPoolManager maintains a pool of HttpClientConnections
     * and is able to service connection requests from multiple execution threads
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    private static PoolingHttpClientConnectionManager getPoolingHTTPClientConnectionManager() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        Registry<ConnectionSocketFactory> socketFactoryRegistry =
                RegistryBuilder.<ConnectionSocketFactory> create()
                        .register("https", getSSLConnectionFactoryRegistry())
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .build();


        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        connManager.setMaxTotal(CTSPoster.connection);
        connManager.setDefaultMaxPerRoute(CTSPoster.connection);
        return connManager;
    }


    /**
     * Multiple TokenizeRequests in one single request to CTS server,
     * it will deliver back an array result of TokenizeResponse[]
     * @param username
     * @param password
     * @param request
     * @return TokenizeResponse[]
     * @throws Exception
     */
    public static TokenizeResponse[] tokenizeMultiple(String username, String password, ArrayList<TokenizeRequest> request) throws Exception {

        //neeed to validate the URL here!!!!!!
        if (username == null || password == null) {
            String errorMsg = "Please check parameters (username & password) can not be empty!";
            logger.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }


        ObjectMapper mapper = new ObjectMapper();
        String JSON_STRING = null;
        try {
            JSON_STRING = mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            logger.error(e);
            e.printStackTrace();
        }


        CloseableHttpClient client = null;
        try {

            client = HttpClients.custom().setSSLSocketFactory(getSSLConnectionFactoryRegistry())
                    .setConnectionManager(getBasicHTTPClientConnectionManager()).build();

            HttpPost httpPost = new HttpPost(CTSPoster.ctsTokenizeUrl);
            String encoding = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setEntity(new StringEntity(JSON_STRING, ContentType.APPLICATION_JSON));
            HttpEntity h = httpPost.getEntity();
            CloseableHttpResponse response = client.execute(httpPost);

            String requestContent = EntityUtils.toString(h);
            logger.info("----------------- Tokenize ---------------------");
            logger.info("url = [{}] username = [{}] password = [{}]", CTSPoster.ctsTokenizeUrl, username, password); //for debug purpose only
            logger.info("request  = " + requestContent);
            String responseString = new BasicResponseHandler().handleResponse(response);
            logger.info("response  = " + responseString);

            ObjectMapper mpr = new ObjectMapper();
            TokenizeResponse[] result = mpr.readValue(responseString, TokenizeResponse[].class);
            return result;

        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException | IOException e) {
            logger.error(e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }


    }


    /**
     * Multiple DeTokenizeRequest in one single request to CTS server,
     * it will deliver back an array result of DeTokenizeResponse[]
     *
     * @param username
     * @param password
     * @param request
     * @return DeTokenizeResponse[]
     */
    public static DeTokenizeResponse[] deTokenizeMultiple(String username, String password, ArrayList<DeTokenizeRequest> request) {

        //neeed to validate the URL here!!!!!!
        if (username == null || password == null) {
            String errorMsg = "Please check parameters (tokenTemplate,tokenGroup, username & password)!";
            logger.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }


        ObjectMapper mapper = new ObjectMapper();
        String JSON_STRING = null;
        try {
            JSON_STRING = mapper.writeValueAsString(request);
        } catch (IOException e) {
            logger.error(e);
            e.printStackTrace();
        }


        CloseableHttpClient client = null;
        try {

            client = HttpClients.custom().setSSLSocketFactory(getSSLConnectionFactoryRegistry())
                    .setConnectionManager(getBasicHTTPClientConnectionManager()).build();

            HttpPost httpPost = new HttpPost(CTSPoster.ctsDeTokenizeUrl);
            String encoding = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
            httpPost.setHeader("Content-type", "application/json");

            httpPost.setEntity(new StringEntity(JSON_STRING, ContentType.APPLICATION_JSON));

            HttpEntity h = httpPost.getEntity();

            CloseableHttpResponse response = client.execute(httpPost);


            String requestContent = EntityUtils.toString(h);
            logger.info("----------------- DeTokenize ---------------------");
            logger.info("url = [{}] username = [{}] password = [{}]", CTSPoster.ctsDeTokenizeUrl, username, password); //for debug purpose only
            logger.info("request  = " + requestContent);
            String responseString = new BasicResponseHandler().handleResponse(response);
            logger.info("response  = " + responseString);

            ObjectMapper mpr = new ObjectMapper();
            DeTokenizeResponse[] result = mpr.readValue(responseString, DeTokenizeResponse[].class);
            return result;

        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException | IOException e) {
            logger.error(e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }


    /**
     * A single Tokenize request to server and it will deliver back a single TokenizeResponse
     * @param wrapper
     * @param data
     * @return TokenizeResponse
     */
    public static TokenizeResponse tokenize(TokenizeWrapper wrapper, String data) {

        String username = wrapper.getHeaderAuth().getUsername();
        String password = wrapper.getHeaderAuth().getPassword();
        String tokenGroup = wrapper.getHeaderAuth().getTokengroup();
        String tokenTemplate = wrapper.getHeaderAuth().getTokentemplate();
        //neeed to validate the URL here!!!!!!


        if (username == null || password == null || tokenGroup == null || tokenTemplate == null) {
            String errorMsg = "Please check parameters (tokenTemplate,tokenGroup, username & password)!";
            logger.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        TokenizeRequest request = new TokenizeRequest(tokenGroup, tokenTemplate, data);

        ObjectMapper mapper = new ObjectMapper();
        String JSON_STRING = null;
        try {
            JSON_STRING = mapper.writeValueAsString(request);
        } catch (IOException e) {
            logger.error(e);
            e.printStackTrace();
        }


        CloseableHttpClient client = null;
        try {

            client = HttpClients.custom().setSSLSocketFactory(getSSLConnectionFactoryRegistry())
                    .setConnectionManager(getBasicHTTPClientConnectionManager()).build();

            HttpPost httpPost = new HttpPost(CTSPoster.ctsTokenizeUrl);
            String encoding = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
            httpPost.setHeader("Content-type", "application/json");

            httpPost.setEntity(new StringEntity(JSON_STRING, ContentType.APPLICATION_JSON));

            HttpEntity h = httpPost.getEntity();

            CloseableHttpResponse response = client.execute(httpPost);


            String requestContent = EntityUtils.toString(h);
            logger.info("----------------- Tokenize ---------------------");
            logger.info("url = [{}] username = [{}] password = [{}]", CTSPoster.ctsTokenizeUrl, username, password); //for debug purpose only
            logger.info("request  = " + requestContent);
            String responseString = new BasicResponseHandler().handleResponse(response);
            logger.info("response  = " + responseString);

            ObjectMapper mpr = new ObjectMapper();
            TokenizeResponse result = mpr.readValue(responseString, TokenizeResponse.class);
            return result;

        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException | IOException e) {
            logger.error(e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }


    }


    /**
     * A single DeTokenize request to server and it will deliver back a single DeTokenizeResponse
     * @param wrapper
     * @param token
     * @return DeTokenizeResponse
     */
    public static DeTokenizeResponse deTokenize(TokenizeWrapper wrapper, String token) {
        String username = wrapper.getHeaderAuth().getUsername();
        String password = wrapper.getHeaderAuth().getPassword();
        String tokenGroup = wrapper.getHeaderAuth().getTokengroup();
        String tokenTemplate = wrapper.getHeaderAuth().getTokentemplate();


        if (username == null || password == null || tokenGroup == null || tokenTemplate == null) {
            String errorMsg = "Please check parameters (tokenTemplate,tokenGroup, username & password)!";
            logger.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        DeTokenizeRequest request = new DeTokenizeRequest(tokenGroup, tokenTemplate, token);

        ObjectMapper mapper = new ObjectMapper();
        String JSON_STRING = null;
        try {
            JSON_STRING = mapper.writeValueAsString(request);
        } catch (IOException e) {
            logger.error(e);
            e.printStackTrace();
        }


        CloseableHttpClient client = null;
        try {
            client = HttpClients.custom().setSSLSocketFactory(getSSLConnectionFactoryRegistry())
                    .setConnectionManager(getBasicHTTPClientConnectionManager()).build();

            HttpPost httpPost = new HttpPost(CTSPoster.ctsDeTokenizeUrl);
            String encoding = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
            httpPost.setHeader("Content-type", "application/json");

            httpPost.setEntity(new StringEntity(JSON_STRING, ContentType.APPLICATION_JSON));
            HttpEntity h = httpPost.getEntity();
            CloseableHttpResponse response = client.execute(httpPost);

            String requestContent = EntityUtils.toString(h);
            logger.info("----------------- DeTokenize ---------------------");
            logger.info("request  = " + requestContent);
            String responseString = new BasicResponseHandler().handleResponse(response);
            logger.info("response  = " + responseString);

            ObjectMapper mpr = new ObjectMapper();
            DeTokenizeResponse result = mpr.readValue(responseString, DeTokenizeResponse.class);
            return result;

        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException | IOException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }


    }


}
