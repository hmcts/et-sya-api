package uk.gov.hmcts.reform.et.syaapi.consumer.idam;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.restassured.RestAssured;
import org.assertj.core.api.Assertions;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
public class JwksConsumerTest {
    private static final String JWKS_AUTH_URL = "/o/jwks";

    @Pact(provider="idam_jwks_api", consumer= "et-sya-api-service")
    RequestResponsePact executeServiceAuthApiGetToke(PactDslWithProvider builder) {

        Map<String, String> responseHeaders = Map.of(HttpHeaders.AUTHORIZATION, "Bearer UserAuthToken");

        return builder
            .given("a case exists")
            .uponReceiving("Provider receives a token request request from et-sya-api API")
            .path(JWKS_AUTH_URL)
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
            .log().all(true)
            .when()
            .get(mockServer.getUrl() + JWKS_AUTH_URL)
            .then()
            .statusCode(200)
            .and()
            .extract()
            .asString();

        System.out.println(responseBody);
        JSONObject response = new JSONObject(responseBody);
        Assertions.assertThat(response).isNotNull();
    }


    private PactDslJsonBody createAuthResponse() {
        return new PactDslJsonBody()
            .stringType("kid", "KeyId1")
            .stringType("kty", "RSA")
            .stringType("alg", "RSA256")
            .stringType("use", "Public Key Use1")
            .stringType("typ", "JWKS");


        /*.stringType("kid", "KeyId2")
            .stringType("kty", "RSA")
            .stringType("alg", "RSA256")
            .stringType("use", "Public Key Use2")
            .stringType("typ", "JWKS");*/


    }

}
