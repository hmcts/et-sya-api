package uk.gov.hmcts.reform.et.syaapi.service.utils.data;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.reform.et.syaapi.models.CaseDocument;
import uk.gov.hmcts.reform.et.syaapi.service.utils.ResourceLoader;

import java.util.Map;

import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_CASE_DATA_JSON_FILE;

public final class TestDataProvider {

    private TestDataProvider() {
        // Utility classes should not have a public or default constructor.
    }

    public static CaseData generateTestCaseDataByClaimantHearingPreferenceContactLanguage(
        boolean isClaimantHearingPreferenceEmpty,
        String contactLanguage) {
        CaseData caseData = ResourceLoader.fromString(TEST_CASE_DATA_JSON_FILE, CaseData.class);
        if (isClaimantHearingPreferenceEmpty) {
            caseData.setClaimantHearingPreference(null);
        } else {
            caseData.getClaimantHearingPreference().setContactLanguage(contactLanguage);
        }
        return caseData;
    }

    public static CaseData generateTestCaseDataByFirstNames(
        String claimantIndTypeEmptyOrNull,
        String firstNames) {
        CaseData caseData = ResourceLoader.fromString(TEST_CASE_DATA_JSON_FILE, CaseData.class);
        if ("null".equals(claimantIndTypeEmptyOrNull)) {
            caseData.setClaimantIndType(null);
        } else if ("empty".equals(claimantIndTypeEmptyOrNull)) {
            caseData.setClaimantIndType(new ClaimantIndType());
        } else {
            caseData.getClaimantIndType().setClaimantFirstNames(firstNames);
        }
        return caseData;
    }

    public static ResponseEntity<CaseDocument> getDocumentDetailsFromCdam() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<>(
            CaseDocument.builder()
                .size("size").mimeType("mimeType").hashToken("token").createdOn("createdOn").createdBy("createdBy")
                .lastModifiedBy("lastModifiedBy").modifiedOn("modifiedOn").ttl("ttl")
                .metadata(Map.of("test", "test"))
                .originalDocumentName("docName.txt").classification("PUBLIC")
                .links(Map.of("self", Map.of("href", "TestURL.com"))).build(),
            headers,
            HttpStatus.OK
        );
    }
}
