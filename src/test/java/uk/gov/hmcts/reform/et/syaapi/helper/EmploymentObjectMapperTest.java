package uk.gov.hmcts.reform.et.syaapi.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.Et1CaseData;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.et.syaapi.helper.TestModelCreator.TYPE_OF_CLAIM_BREACH_OF_CONTRACT;
import static uk.gov.hmcts.reform.et.syaapi.helper.TestModelCreator.TYPE_OF_CLAIM_DISCRIMINATION;
import static uk.gov.hmcts.reform.et.syaapi.helper.TestModelCreator.TYPE_OF_CLAIM_PAY_RELATED_CLAIM;
import static uk.gov.hmcts.reform.et.syaapi.helper.TestModelCreator.TYPE_OF_CLAIM_UNFAIR_DISMISSAL;
import static uk.gov.hmcts.reform.et.syaapi.helper.TestModelCreator.TYPE_OF_CLAIM_WHISTLE_BLOWING;

@Import(EmployeeObjectMapper.class)
class EmploymentObjectMapperTest {
    @Mock
    private EmployeeObjectMapper employmentObjectMapper;

    @BeforeEach
    void setUp() {
        employmentObjectMapper = new EmployeeObjectMapper();
    }



    @Test
    void shouldGetEmployeeObjetMapper() {
        Et1CaseData et1CaseData = employmentObjectMapper.getEmploymentCaseData("{\"caseNotes\": \"TEST\"}");
        assertThat("TEST".equalsIgnoreCase(et1CaseData.getCaseNotes())).isTrue();
    }

    @Test
    void shouldGetNullData() {
        Et1CaseData et1CaseData = employmentObjectMapper.getEmploymentCaseData("\"caseType\": \"Single\"");
        assertThat(et1CaseData).isNull();
    }

    @Test
    void shouldGetCaseData() {
        Map<String, Object> requestCaseData = TestModelCreator.createRequestCaseData();
        CaseData caseData = employmentObjectMapper.getCaseData(requestCaseData);

        assertThat(caseData.getTypeOfClaim().get(0)).isEqualTo(TYPE_OF_CLAIM_DISCRIMINATION);
        assertThat(caseData.getTypeOfClaim().get(1)).isEqualTo(TYPE_OF_CLAIM_BREACH_OF_CONTRACT);
        assertThat(caseData.getTypeOfClaim().get(2)).isEqualTo(TYPE_OF_CLAIM_PAY_RELATED_CLAIM);
        assertThat(caseData.getTypeOfClaim().get(3)).isEqualTo(TYPE_OF_CLAIM_UNFAIR_DISMISSAL);
        assertThat(caseData.getTypeOfClaim().get(4)).isEqualTo(TYPE_OF_CLAIM_WHISTLE_BLOWING);
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
