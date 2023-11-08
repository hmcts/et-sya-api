package uk.gov.hmcts.reform.et.syaapi.consumer.test.idam;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactDirectory;
import io.restassured.RestAssured;
import org.assertj.core.api.Assertions;
import org.json.JSONObject;
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
@PactDirectory("pacts")
class JwksConsumerTest {
    private static final String JWKS_AUTH_URL = "/o/jwks";

    @Pact(provider = "idam_jwks_api", consumer = "et_sya_api_service")
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

        JSONObject response = new JSONObject(responseBody);
        Assertions.assertThat(response).isNotNull();

    }

    private PactDslJsonBody createAuthResponse() {
        return new PactDslJsonBody()
            .eachLike("keys")
                .stringType("kid", "KeyId1")
                .stringType("kty", "RSA")
                .stringType("e", "AQAB")
                .stringType("use", "Public Key Use1")
                .stringType("n", "someToken");
    }
}
