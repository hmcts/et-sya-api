package uk.gov.hmcts.reform.et.syaapi.consumer;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.annotations.PactFolder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "ccdDataStoreAPI_Cases", port = "8890")
@PactFolder("pacts")
@SpringBootTest({
    "core_case_data.api.url : localhost:8890"
})
public class SpringBootContractBaseTest {
    public static final String AUTH_TOKEN = "Bearer someAuthorizationToken";
    public static final String SERVICE_AUTH_TOKEN = "Bearer someServiceAuthorizationToken";

    public static final String INITIATE_CASE_DRAFT = "INITIATE_CASE_DRAFT";
    public static final String UPDATE_CASE_DRAFT = "UPDATE_CASE_DRAFT";
    protected static final String USER_ID = "123456";
    protected static final Long CASE_ID = 1_593_694_526_480_033L;
    public static final String JURISDICTION = "jurisdictionId";
    public static final String CASE_TYPE = "caseType";
    public static final String PUBLIC = "PUBLIC";
    public static final String JURISDICTION_ID = "EMPLOYMENT";
    public static final String CASE_TYPE_ID = "ET_EnglandWales";
    public static final String EVENT_ID = "EVENT_ID";
    public static final int SLEEP_TIME = 2000;
    protected static final String ALPHABETIC_REGEX = "[/^[A-Za-z_]+$/]+";
    @Autowired
    protected CoreCaseDataApi coreCaseDataApi;
    public static final Map<String, String> RESPONSE_HEADERS = Map.of("Content-Type", "application/json");

    @BeforeEach
    public void prepareTest() throws Exception {
        Thread.sleep(SLEEP_TIME);
    }

    protected Map<String, Object> addCaseTypeJurdisticaton() {
        Map<String, Object> map = new ConcurrentHashMap<>();
        map.put(JURISDICTION, JURISDICTION_ID);
        map.put(CASE_TYPE, CASE_TYPE_ID);
        return map;
    }

    protected static DslPart buildStartEventResponseWithEmptyCaseDetails(String eventId) {
        return newJsonBody((o) -> {
            o.stringType("event_id", eventId)
                .stringType("token", null)
                .object("case_details", (cd) -> {
                    cd.numberType("id", CASE_ID);
                    cd.stringMatcher("jurisdiction", ALPHABETIC_REGEX, "EMPLOYMENT");
                    cd.stringType("callback_response_status", null);
                    cd.stringMatcher("case_type_id", ALPHABETIC_REGEX, "ET_EnglandWales");
                    cd.object("case_data", data -> {
                    });
                });
        }).build();
    }

    public static DslPart buildCaseDetailsDsl(Long caseId) {
        return newJsonBody((o) -> {
            o.numberType("id", caseId)
                .stringType("jurisdiction", "EMPLOYMENT")
                .stringType("state", "ADMISSION_TO_HMCTS")
                .stringValue("case_type_id", "ET_EnglandWales")
                .object("case_data", (dataMap) -> {
                    dataMap
                        .stringType("caseType", "Single")
                        .stringType("caseSource", "Manually Created");
                });
        }).build();
    }
}
