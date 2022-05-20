package uk.gov.hmcts.reform.et.syaapi;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SyaApiApplication.class})
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BaseFunctionalTest {
    protected final String baseUrl = System.getenv("TEST_URL") == null ? "http://et-sya-api-aat.service.core-compute-aat.internal" : System.getenv("TEST_URL");
    protected String userToken;
    protected CloseableHttpClient client;
    protected IdamTestApiRequests idamTestApiRequests;

    @Value("${idam.url}")
    private String idamApiUrl;

    @BeforeAll
    public void setup() throws Exception {
        client = buildClient();
        idamTestApiRequests = new IdamTestApiRequests(client, idamApiUrl);
        CreateUser user = idamTestApiRequests.createUser(createRandomEmail());
        userToken = idamTestApiRequests.getAccessToken(user.getEmail());
    }

    private String createRandomEmail() {
        int randomNumber = (int) (Math.random() * 10_000_000);
        return "test" + randomNumber + "@hmcts.net";
    }

    private CloseableHttpClient buildClient()
        throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf =
            new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create()
            .setSSLSocketFactory(sslsf);
        return httpClientBuilder.build();
    }

}
