package uk.gov.hmcts.reform.et.syaapi.consumer;

import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.annotations.PactFolder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "ccd_data_store_get_case_by_id", port = "8890")
@PactFolder("pacts")
@SpringBootTest({
    "core_case_data.api.url : localhost:8890"
})
public class SpringBootContractBaseTest {
    public static final String AUTHORIZATION_BEARER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";
    public static final String SERVICE_BEARER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92V";
    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    public static final String AUTH_TOKEN = "Bearer someAuthorizationToken";
    public static final String SERVICE_AUTH_TOKEN = "Bearer someServiceAuthorizationToken";

    public HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(SERVICE_AUTHORIZATION, SERVICE_BEARER_TOKEN);
        headers.add(AUTHORIZATION, AUTHORIZATION_BEARER_TOKEN);
        return headers;
    }
}
