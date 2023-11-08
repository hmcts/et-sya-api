package uk.gov.hmcts.reform.et.syaapi.consumer.test.idam;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
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

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@PactDirectory("pacts")
class IdamConsumerTest {
    private static final String IDAM_USER_DETAILS = "/details";

    @Pact(provider = "idam_user_details", consumer = "et_sya_api_service")
    RequestResponsePact executeIdamUserDetailApi(PactDslWithProvider builder) {
        Map<String, String> responseHeaders = Map.of(HttpHeaders.AUTHORIZATION, "Bearer UserAuthToken");

        return builder
            .given("a user exists")
            .uponReceiving("Provider receives a token request and send user details to the API")
            .path(IDAM_USER_DETAILS)
            .method(GET.toString())
            .willRespondWith()
            .status(OK.value())
            .headers(responseHeaders)
            .body(createAuthResponse())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeIdamUserDetailApi")
    void shouldReceiveUserDetails(MockServer mockServer) {

        String responseBody = RestAssured
            .given()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .log().all(true)
            .when()
            .get(mockServer.getUrl() + IDAM_USER_DETAILS)
            .then()
            .statusCode(200)
            .and()
            .extract()
            .asString();

        JSONObject response = new JSONObject(responseBody);
        Assertions.assertThat(response).isNotNull();
    }

    private DslPart createAuthResponse() {
        return newJsonBody(o -> o.stringType("id",
                                         "123432")
            .stringType("forename", "Joe")
            .stringType("surname", "Bloggs")
            .stringType("email", "joe.bloggs@hmcts.net")
            .booleanType("active", true)
            .array("roles", role -> role.stringType("caseworker").stringType("citizen"))).build();
    }
}

