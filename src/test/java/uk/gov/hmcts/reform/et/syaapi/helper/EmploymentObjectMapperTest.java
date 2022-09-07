package uk.gov.hmcts.reform.et.syaapi.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.Et1CaseData;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Import(EmployeeObjectMapper.class)
class EmploymentObjectMapperTest {
    @Mock
    private EmployeeObjectMapper employmentObjectMapper;

    private Map<String, Object> requestCaseData;

    @BeforeEach
    void setUp() {
        employmentObjectMapper = new EmployeeObjectMapper();

        requestCaseData = new HashMap<>();
        requestCaseData.put("typeOfClaim", TestModelCreator.getTypeOfClaimArray());
        requestCaseData.put("caseType", TestModelCreator.CASE_TYPE_SINGLE);
        requestCaseData.put("caseSource", TestModelCreator.CASE_SOURCE_INTERNET);
        requestCaseData.put("claimantRepresentedQuestion", TestModelCreator.YES_OR_NO_YES);
        requestCaseData.put("jurCodesCollection", TestModelCreator.getJureCodesTypeItemList());
        requestCaseData.put("claimantIndType", TestModelCreator.getClaimantIndType());
        requestCaseData.put("claimantType", TestModelCreator.getClaimantType());
        requestCaseData.put("representativeClaimantType", TestModelCreator.getRepresentativeClaimantTypeType());
        requestCaseData.put("claimantOtherType", TestModelCreator.getClaimantOtherType());
        requestCaseData.put("respondentCollection", TestModelCreator.getRespondentCollection());
        requestCaseData.put("claimantWorkAddress", TestModelCreator.getClaimantWorkAddress());
        requestCaseData.put("caseNotes", TestModelCreator.CASE_NOTES);
        requestCaseData.put("managingOffice", TestModelCreator.MANAGING_OFFICE);
        requestCaseData.put("newEmploymentType", TestModelCreator.getNewEmploymentType());
        requestCaseData.put("claimantRequests", TestModelCreator.getClaimantRequests());
        requestCaseData.put("claimantHearingPreference", TestModelCreator.getClaimantHearingPreference());
        requestCaseData.put("claimantTaskListChecks", TestModelCreator.getClaimantTaskListChecks());
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
        CaseData caseData = employmentObjectMapper.getCaseData(requestCaseData);

        assertThat(caseData.getTypeOfClaim().get(0)).isEqualTo(TestModelCreator.getTypeOfClaimArray()[0]);
        assertThat(caseData.getTypeOfClaim().get(1)).isEqualTo(TestModelCreator.getTypeOfClaimArray()[1]);
        assertThat(caseData.getTypeOfClaim().get(2)).isEqualTo(TestModelCreator.getTypeOfClaimArray()[2]);
        assertThat(caseData.getTypeOfClaim().get(3)).isEqualTo(TestModelCreator.getTypeOfClaimArray()[3]);
        assertThat(caseData.getTypeOfClaim().get(4)).isEqualTo(TestModelCreator.getTypeOfClaimArray()[4]);
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
