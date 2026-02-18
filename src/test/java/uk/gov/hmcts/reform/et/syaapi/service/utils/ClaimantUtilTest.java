package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.HubLinksStatuses;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.MODIFICATION_TYPE_ASSIGNMENT;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.MODIFICATION_TYPE_REVOKE;

class ClaimantUtilTest {

    private static final String TEST_USER_ID = "test-user-id-12345";
    private static final String DIFFERENT_USER_ID = "different-user-id-67890";
    private static final Long TEST_CASE_ID = 1646225213651590L;

    private CaseData caseData;
    private CaseDetails caseDetails;

    @BeforeEach
    void setUp() {
        caseData = new CaseData();
        caseDetails = createCaseDetailsWithCaseData(caseData);
    }

    // ==================== isClaimantNonSystemUser tests ====================

    @Test
    void isClaimantNonSystemUserTest_BothNull() {
        caseData.setEt1OnlineSubmission(null);
        caseData.setHubLinksStatuses(null);
        assertTrue(ClaimantUtil.isClaimantNonSystemUser(caseData));
    }

    @Test
    void isClaimantNonSystemUserTest_SubmissionYes() {
        caseData.setEt1OnlineSubmission("Yes");
        caseData.setHubLinksStatuses(null);
        assertFalse(ClaimantUtil.isClaimantNonSystemUser(caseData));
    }

    @Test
    void isClaimantNonSystemUserTest_HubLinksStatuses() {
        caseData.setEt1OnlineSubmission(null);
        caseData.setHubLinksStatuses(new HubLinksStatuses());
        assertFalse(ClaimantUtil.isClaimantNonSystemUser(caseData));
    }

    @Test
    void isClaimantNonSystemUserTest_AllYes() {
        caseData.setEt1OnlineSubmission(YES);
        caseData.setHubLinksStatuses(new HubLinksStatuses());
        caseData.setMigratedFromEcm(YES);
        assertTrue(ClaimantUtil.isClaimantNonSystemUser(caseData));
    }

    // ==================== setClaimantIdamId tests ====================

    @Test
    void setClaimantIdamId_Assignment_SetsClaimantIdAndHubLinksStatuses() {
        caseDetails = createCaseDetailsWithCaseData(caseData);

        boolean result = ClaimantUtil.setClaimantIdamId(
            caseDetails, TEST_USER_ID, MODIFICATION_TYPE_ASSIGNMENT);

        assertFalse(result);
        CaseData updatedCaseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(caseDetails.getData());
        assertThat(updatedCaseData.getClaimantId()).isEqualTo(TEST_USER_ID);
    }

    @Test
    void setClaimantIdamId_Assignment_AlreadyAssignedSameUser_ReturnsTrue() {
        caseData.setClaimantId(TEST_USER_ID);
        caseDetails = createCaseDetailsWithCaseData(caseData);

        boolean result = ClaimantUtil.setClaimantIdamId(
            caseDetails, TEST_USER_ID, MODIFICATION_TYPE_ASSIGNMENT);

        assertTrue(result);
    }

    @Test
    void setClaimantIdamId_Assignment_AlreadyAssignedDifferentUser_ThrowsException() {
        caseData.setClaimantId(DIFFERENT_USER_ID);
        caseDetails = createCaseDetailsWithCaseData(caseData);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            ClaimantUtil.setClaimantIdamId(
                caseDetails, TEST_USER_ID, MODIFICATION_TYPE_ASSIGNMENT));

        assertThat(exception.getMessage()).contains("Claimant IDAM ID already exists");
    }

    @Test
    void setClaimantIdamId_Assignment_DoesNotOverwriteExistingHubLinksStatuses() {
        HubLinksStatuses existingStatuses = new HubLinksStatuses();
        existingStatuses.setPersonalDetails("completed");
        caseData.setHubLinksStatuses(existingStatuses);
        caseDetails = createCaseDetailsWithCaseData(caseData);

        ClaimantUtil.setClaimantIdamId(
            caseDetails, TEST_USER_ID, MODIFICATION_TYPE_ASSIGNMENT);

        CaseData updatedCaseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(caseDetails.getData());
        assertThat(updatedCaseData.getHubLinksStatuses().getPersonalDetails()).isEqualTo("completed");
    }

    @Test
    void setClaimantIdamId_Revoke_ClearsClaimantId() {
        caseData.setClaimantId(TEST_USER_ID);
        caseDetails = createCaseDetailsWithCaseData(caseData);

        boolean result = ClaimantUtil.setClaimantIdamId(
            caseDetails, TEST_USER_ID, MODIFICATION_TYPE_REVOKE);

        assertFalse(result);
        CaseData updatedCaseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(caseDetails.getData());
        assertThat(updatedCaseData.getClaimantId()).isEmpty();
    }

    @Test
    void setClaimantIdamId_EmptyCaseData_ThrowsException() {
        CaseDetails emptyCaseDetails = CaseDetails.builder().id(TEST_CASE_ID).data(null).build();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            ClaimantUtil.setClaimantIdamId(
                emptyCaseDetails, TEST_USER_ID, MODIFICATION_TYPE_ASSIGNMENT));

        assertThat(exception.getMessage()).contains("does not have case data");
    }

    @Test
    void testSerializationOfGenericTseApplicationType() {
        GenericTseApplicationType respondentApp = GenericTseApplicationType.builder()
            .applicant(RESPONDENT_TITLE)
            .type("Amend response")
            .build();
        GenericTseApplicationTypeItem appItem = GenericTseApplicationTypeItem.builder()
            .id("1")
            .value(respondentApp)
            .build();
        caseData.setGenericTseApplicationCollection(List.of(appItem));

        Map<String, Object> map = EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData);
        CaseData deserialized = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(map);

        assertThat(deserialized.getGenericTseApplicationCollection()).isNotEmpty();
        assertThat(deserialized.getGenericTseApplicationCollection()
                       .getFirst().getValue().getApplicant()).isEqualTo(RESPONDENT_TITLE);
    }

    // ==================== Helper methods ====================

    private CaseDetails createCaseDetailsWithCaseData(CaseData caseData) {
        Map<String, Object> caseDataMap = EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData);
        if (caseDataMap == null) {
            caseDataMap = new HashMap<>();
        }
        return CaseDetails.builder()
            .id(TEST_CASE_ID)
            .data(caseDataMap)
            .build();
    }
}
