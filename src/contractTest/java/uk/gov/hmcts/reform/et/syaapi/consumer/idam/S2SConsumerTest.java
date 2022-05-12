package uk.gov.hmcts.reform.et.syaapi.consumer.idam;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
public class S2SConsumerTest {

    private static final String S2S_URL = "/lease";

    @Pact(provider="idam_api_s2s", consumer= "et-sya-api-service")
    RequestResponsePact executeServiceAuthApiGetToke(PactDslWithProvider builder) {

        Map<String, String> responseHeaders = Map.of(HttpHeaders.AUTHORIZATION, "someToken");

        return builder
            .given("a case exists")
            .uponReceiving("Provider receives a token request request from et-sya-api API")
            .path(S2S_URL)
            .method(GET.toString())
            .willRespondWith()
            .status(OK.value())
            .headers(responseHeaders)
            .body(createAuthResponse())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeServiceAuthApiGetToke")
    void shouldReceiveTokenAnd200(MockServer mockServer) {

        String responseBody = RestAssured
            .given()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            //.body(createRequestBody())
            .log().all(true)
            .when()
            .get(mockServer.getUrl() + S2S_URL)
            .then()
            .statusCode(200)
            .and()
            .extract()
            .asString();

        System.out.println(responseBody);
    }


    private PactDslJsonBody createAuthResponse() {
        return new PactDslJsonBody()
            .stringType("access_token", "some-long-value")
            .stringType("refresh_token", "another-long-value")
            .stringType("scope", "openid roles profile")
            .stringType("id_token", "some-value")
            .stringType("token_type", "Bearer")
            .stringType("expires_in", "12345");
    }


}
