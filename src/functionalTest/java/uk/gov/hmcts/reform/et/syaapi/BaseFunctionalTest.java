package uk.gov.hmcts.reform.et.syaapi;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import static org.apache.http.conn.ssl.SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SyaApiApplication.class})
@Slf4j
public class BaseFunctionalTest {
    protected final String baseUrl = System.getenv("TEST_URL") != null ? System.getenv("TEST_URL") : "http://localhost:4550";
    protected String userToken;
    protected CloseableHttpClient client;
    protected IdamTestApiRequests idamTestApiRequests;

    private final IdamClient idamClient;
    private CreateUser user;

    @Value("${idam.url}")
    private String idamApiUrl;


    public BaseFunctionalTest(IdamClient idamClient) {
        this.idamClient = idamClient;
    }

    @Before
    public void setup() throws Exception {
        client = buildClient();

        idamTestApiRequests = new IdamTestApiRequests(client, idamApiUrl);
        user = idamTestApiRequests.createUser(createRandomEmail());

        userToken = getUserToken(user.getEmail(), user.getPassword());
    }


    private String createRandomEmail() {
        int randomNumber = (int) (Math.random() * 10000000);
        String emailAddress = "test" + randomNumber + "@hmcts.net";
        log.info("emailAddress " + emailAddress);
        return emailAddress;
    }

    private String getUserToken(String idamOauth2UserEmail, String idamOauth2UserPassword) {
        log.info("Getting new idam token");
        return idamClient.getAccessToken(idamOauth2UserEmail,  idamOauth2UserPassword);
    }

    private CloseableHttpClient buildClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf =
            new SSLConnectionSocketFactory(builder.build(), ALLOW_ALL_HOSTNAME_VERIFIER);

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create()
            .setSSLSocketFactory(sslsf);
        httpClientBuilder = httpClientBuilder.setProxy(new HttpHost("proxyout.reform.hmcts.net", 8080));
        return httpClientBuilder.build();
    }

}
