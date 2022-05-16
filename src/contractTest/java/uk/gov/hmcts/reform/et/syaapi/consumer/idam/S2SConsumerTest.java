package uk.gov.hmcts.reform.et.syaapi.consumer.idam;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import io.restassured.RestAssured;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@PactFolder("pacts")
class S2SConsumerTest {
    private static final String S2S_URL = "/lease";

    @Pact(provider = "s2-auth-api", consumer = "et_sya_api_service")
    RequestResponsePact executeServiceAuthApiGetToken(PactDslWithProvider builder) {

        Map<String, String> responseHeaders = Map.of(HttpHeaders.AUTHORIZATION, "someToken");

        return builder
            .given("a secret exists")
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
    @PactTestFor(pactMethod = "executeServiceAuthApiGetToken")
    void shouldReceiveTokenAnd200(MockServer mockServer) {

        String responseBody = RestAssured
            .given()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body(createRequestBody())
            .log().all(true)
            .when()
            .get(mockServer.getUrl() + S2S_URL)
            .then()
            .statusCode(200)
            .and()
            .extract()
            .asString();

        JSONObject response = new JSONObject(responseBody);
        assertThat(response).isNotNull();
        assertThat(response.getString("token")).isNotBlank();
    }


    private PactDslJsonBody createAuthResponse() {
        return new PactDslJsonBody()
            .stringType("token","someMicroServiceToken");
    }

    private static String createRequestBody() {
        return new StringBuffer().append("{\"microservice\": \"microServiceName\"))")
            .append(" \"oneTimePassword\": \"987651\",")
            .append(" }").toString();
    }

}
