package uk.gov.hmcts.reform.et.syaapi;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BaseFunctionalTest {
    protected final String baseUrl = System.getenv("TEST_URL") != null ? System.getenv("TEST_URL") : "http://localhost:4550";
//    protected final String baseUrl = "http://localhost:4550";
    protected String userToken;
    protected CloseableHttpClient client;
    protected IdamTestApiRequests idamTestApiRequests;

    private CreateUser user;

    @Value("${idam.url}")
    private String idamApiUrl;

    @BeforeAll
    public void setup() throws Exception {
        client = buildClient();

        idamTestApiRequests = new IdamTestApiRequests(client, idamApiUrl);
        user = idamTestApiRequests.createUser(createRandomEmail());
        userToken = idamTestApiRequests.getAccessToken(user.getEmail());
//        userToken = "Bearer eyJraWQiOiIyMzQ1Njc4OSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJDQ0RfU3R1YiIsImlzcyI6Imh0dHA6XC9cL2ZyLWFtOjgwODBcL29wZW5hbVwvb2F1dGgyXC9obWN0cyIsInRva2VuTmFtZSI6ImFjY2Vzc190b2tlbiIsImV4cCI6MTY1MjkwNzU4MywiaWF0IjoxNjUyODkzMTgzfQ.BBm4bQw2i-9fr0EHE4Z18DQS_1-lpAn55ypHDMDtOc4Hacf07l20SPVGh54cInvl_B51bGcRW5zTvbfwdnjJKKyzZTys63_4WwZgu8JR4IE8BF8x4jMP7HJE8RIdaNTLNUbQhlJ_A25UAT1J0y7wb2ctrRSh3KzlyKjaBe5OBAhsHJ6F3hgedQrtEOR1yw9F8fvgrhRlAPz9cEChZ2DjCRvhNYN3Y5P6dDSkRwSHUZFGRftnzesJrM0MZO34U0HYAVn6yI_mODpJlUawslEr1SmRyju1VSQbQrhsrJqfa5zyjA6jW5wnebmE0z67XxHeSGudm7pJ18kiVrYWpjfaMA";
    }


    private String createRandomEmail() {
        int randomNumber = (int) (Math.random() * 10000000);
        String emailAddress = "test" + randomNumber + "@hmcts.net";
        return emailAddress;
    }

    private CloseableHttpClient buildClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf =
            new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create()
            .setSSLSocketFactory(sslsf);
        httpClientBuilder = httpClientBuilder.setProxy(new HttpHost("proxyout.reform.hmcts.net", 8080));
        return httpClientBuilder.build();
    }

}
