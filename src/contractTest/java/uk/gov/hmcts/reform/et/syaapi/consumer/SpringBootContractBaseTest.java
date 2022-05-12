package uk.gov.hmcts.reform.et.syaapi.consumer;

import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.annotations.PactFolder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "ccd_data_store_cases_api", port = "8890")
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

    public static final String INITIATE_CASE_DRAFT = "INITIATE_CASE_DRAFT";
    public static final String UPDATE_CASE_DRAFT = "UPDATE_CASE_DRAFT";
    protected static final String USER_ID = "123456";
    protected static final Long CASE_ID = 1593694526480033L;

    public static final String JURISDICTION = "jurisdictionId";
    public static final String CASE_TYPE = "caseType";

    public static final int SLEEP_TIME = 2000;

    protected static final String ALPHABETIC_REGEX = "[/^[A-Za-z_]+$/]+";
    @Autowired
    protected CoreCaseDataApi coreCaseDataApi;

    public HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(SERVICE_AUTHORIZATION, SERVICE_BEARER_TOKEN);
        headers.add(AUTHORIZATION, AUTHORIZATION_BEARER_TOKEN);
        return headers;
    }

    @BeforeEach
    public void prepareTest() throws Exception {
        Thread.sleep(SLEEP_TIME);
    }

    protected Map<String, Object> setUpStateMapForProviderWithoutCaseData() {
        Map<String, Object> map = new HashMap<>();
        map.put(JURISDICTION, "EMPLOYMENT");
        map.put(CASE_TYPE, "ET_EnglandWales");
        return map;
    }
}
