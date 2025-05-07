/**
 * 
 */
package com.github.leeyazhou.httpclient5;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author leeyazhou
 */
@SpringBootApplication
public class TlsTest implements CommandLineRunner {

  public static void main(String[] args) throws Exception {
    SpringApplication.run(TlsTest.class, args);
  }

  public void run(String... args) throws Exception {
    try (CloseableHttpClient httpclient = createHttpClient()) {
      ClassicHttpRequest httpGet = ClassicRequestBuilder.get("https://httpbin.org/get").build();
      // The underlying HTTP connection is still held by the response object
      // to allow the response content to be streamed directly from the network
      // socket.
      // In order to ensure correct deallocation of system resources
      // the user MUST call CloseableHttpResponse#close() from a finally clause.
      // Please note that if response content is not fully consumed the underlying
      // connection cannot be safely re-used and will be shut down and discarded
      // by the connection manager.
      httpclient.execute(httpGet, response -> {
        System.out.println(response.getCode() + " " + response.getReasonPhrase());
        final HttpEntity entity1 = response.getEntity();
        // do something useful with the response body
        // and ensure it is fully consumed
        EntityUtils.consume(entity1);
        return null;
      });

      ClassicHttpRequest httpPost = ClassicRequestBuilder.post("http://httpbin.org/post")
          .setEntity(new UrlEncodedFormEntity(
              Arrays.asList(new BasicNameValuePair("username", "vip"), new BasicNameValuePair("password", "secret"))))
          .build();
      httpclient.execute(httpPost, response -> {
        System.out.println(response.getCode() + " " + response.getReasonPhrase());
        final HttpEntity entity2 = response.getEntity();
        // do something useful with the response body
        // and ensure it is fully consumed
        EntityUtils.consume(entity2);
        return null;
      });
    }
  }

  public static CloseableHttpClient createHttpClient() throws Exception {
    SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(null, new TrustStrategy() {
      public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        return true;
      }
    }).build();
    TlsSocketStrategy tlsSocketStrategy = new DefaultClientTlsStrategy(sslContext);
    PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder =
        PoolingHttpClientConnectionManagerBuilder.create();
    connectionManagerBuilder.setTlsSocketStrategy(tlsSocketStrategy);

    connectionManagerBuilder.setMaxConnTotal(128);
    connectionManagerBuilder.setMaxConnPerRoute(128);

    HttpClientBuilder builder = HttpClientBuilder.create();
    PoolingHttpClientConnectionManager connectionManager = connectionManagerBuilder.build();
    builder.setConnectionManager(connectionManager);

    return builder.build();
  }


}
