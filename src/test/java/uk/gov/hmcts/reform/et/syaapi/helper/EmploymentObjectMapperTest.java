package uk.gov.hmcts.reform.et.syaapi.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.Et1CaseData;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TYPE_OF_CLAIM_BREACH_OF_CONTRACT;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TYPE_OF_CLAIM_DISCRIMINATION;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TYPE_OF_CLAIM_PAY_RELATED_CLAIM;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TYPE_OF_CLAIM_UNFAIR_DISMISSAL;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TYPE_OF_CLAIM_WHISTLE_BLOWING;

@Import(EmployeeObjectMapper.class)
class EmploymentObjectMapperTest {
    @Mock
    private EmployeeObjectMapper employmentObjectMapper;
    private CaseTestData caseTestData;

    @BeforeEach
    void beforeEach() {
        employmentObjectMapper = new EmployeeObjectMapper();
        caseTestData = new CaseTestData();
    }

    @Test
    void shouldGetEmployeeObjetMapper() {
        Et1CaseData et1CaseData = employmentObjectMapper.getEmploymentCaseData("{\"caseNotes\": \"TEST\"}");
        assertThat(et1CaseData.getCaseNotes()).isEqualToIgnoringCase("TEST");
    }

    @Test
    void shouldGetNullData() {
        Et1CaseData et1CaseData = employmentObjectMapper.getEmploymentCaseData("\"caseType\": \"Single\"");
        assertThat(et1CaseData).isNull();
    }

    @Test
    void shouldMapCaseRequestToCaseData() {
        Map<String, Object> requestCaseData = caseTestData.getCaseRequestCaseDataMap();
        CaseData caseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(requestCaseData);
        assertThat(caseData.getTypesOfClaim().get(0)).isEqualTo(TYPE_OF_CLAIM_DISCRIMINATION);
        assertThat(caseData.getTypesOfClaim().get(1)).isEqualTo(TYPE_OF_CLAIM_BREACH_OF_CONTRACT);
        assertThat(caseData.getTypesOfClaim().get(2)).isEqualTo(TYPE_OF_CLAIM_PAY_RELATED_CLAIM);
        assertThat(caseData.getTypesOfClaim().get(3)).isEqualTo(TYPE_OF_CLAIM_UNFAIR_DISMISSAL);
        assertThat(caseData.getTypesOfClaim().get(4)).isEqualTo(TYPE_OF_CLAIM_WHISTLE_BLOWING);
        assertThat(caseData.getEcmCaseType()).isEqualTo(requestCaseData.get("caseType"));
        assertThat(caseData.getCaseSource()).isEqualTo(requestCaseData.get("caseSource"));
        assertThat(caseData.getClaimantRepresentedQuestion()).isEqualTo(
            requestCaseData.get("claimantRepresentedQuestion"));
        assertThat(caseData.getJurCodesCollection()).isEqualTo(requestCaseData.get("jurCodesCollection"));
        assertThat(caseData.getClaimantIndType()).isEqualTo(requestCaseData.get("claimantIndType"));
        assertThat(caseData.getClaimantType()).isEqualTo(requestCaseData.get("claimantType"));
        assertThat(caseData.getRepresentativeClaimantType()).isEqualTo(
            requestCaseData.get("representativeClaimantType"));
        assertThat(caseData.getClaimantOtherType()).isEqualTo(requestCaseData.get("claimantOtherType"));
        assertThat(caseData.getRespondentCollection()).isEqualTo(requestCaseData.get("respondentCollection"));
        assertThat(caseData.getClaimantWorkAddress()).isEqualTo(requestCaseData.get("claimantWorkAddress"));
        assertThat(caseData.getCaseNotes()).isEqualTo(requestCaseData.get("caseNotes"));
        assertThat(caseData.getManagingOffice()).isEqualTo(requestCaseData.get("managingOffice"));
        assertThat(caseData.getNewEmploymentType()).isEqualTo(requestCaseData.get("newEmploymentType"));
        assertThat(caseData.getClaimantRequests()).isEqualTo(requestCaseData.get("claimantRequests"));
        assertThat(caseData.getClaimantHearingPreference()).isEqualTo(
            requestCaseData.get("claimantHearingPreference"));
        assertThat(caseData.getClaimantTaskListChecks()).isEqualTo(requestCaseData.get("claimantTaskListChecks"));
    }
}
