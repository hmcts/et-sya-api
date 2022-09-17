package uk.gov.hmcts.reform.et.syaapi.model;

import lombok.Data;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.Et1CaseData;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceLoader;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.CASE_ID;

@Data
public final class TestData {

    private final Et1CaseData testEt1CaseData = ResourceLoader.fromString(
        "requests/caseData.json",
        Et1CaseData.class
    );
    private final CaseData testCaseData = ResourceLoader.fromString(
        "requests/caseData.json",
        CaseData.class
    );
    private final CaseDetails expectedDetails = ResourceLoader.fromString(
        "responses/caseDetails.json",
        CaseDetails.class
    );
    private final List<CaseDetails> requestCaseDataListEngland = ResourceLoader.fromStringToList(
        "responses/caseDetailsEngland.json",
        CaseDetails.class
    );
    private final List<CaseDetails> requestCaseDataListScotland = ResourceLoader.fromStringToList(
        "responses/caseDetailsScotland.json",
        CaseDetails.class
    );
    private final List<CaseDetails> expectedCaseDataListCombined = ResourceLoader.fromStringToList(
        "responses/caseDetailsCombined.json",
        CaseDetails.class
    );
    private final StartEventResponse startEventResponse = ResourceLoader.fromString(
        "responses/startEventResponse.json",
        StartEventResponse.class
    );
    private final List<CaseDetails> requestCaseDataList = ResourceLoader.fromStringToList(
        "responses/caseDetailsList.json",
        CaseDetails.class
    );

    public Map<String, Object> getTestCaseRequestCaseDataMap() {
        Map<String, Object> requestCaseData = new ConcurrentHashMap<>();
        requestCaseData.put("typeOfClaim", testEt1CaseData.getTypeOfClaim());
        requestCaseData.put("caseType", testEt1CaseData.getEcmCaseType());
        requestCaseData.put("caseSource", testEt1CaseData.getCaseSource());
        requestCaseData.put("claimantRepresentedQuestion", testEt1CaseData.getClaimantRepresentedQuestion());
        requestCaseData.put("jurCodesCollection", testEt1CaseData.getJurCodesCollection());
        requestCaseData.put("claimantIndType", testEt1CaseData.getClaimantIndType());
        requestCaseData.put("claimantType", testEt1CaseData.getClaimantType());
        requestCaseData.put("representativeClaimantType", testEt1CaseData.getRepresentativeClaimantType());
        requestCaseData.put("claimantOtherType", testEt1CaseData.getClaimantOtherType());
        requestCaseData.put("respondentCollection", testEt1CaseData.getRespondentCollection());
        requestCaseData.put("claimantWorkAddress", testEt1CaseData.getClaimantWorkAddress());
        requestCaseData.put("caseNotes", testEt1CaseData.getCaseNotes());
        requestCaseData.put("managingOffice", testEt1CaseData.getManagingOffice());
        requestCaseData.put("newEmploymentType", testEt1CaseData.getNewEmploymentType());
        requestCaseData.put("claimantRequests", testEt1CaseData.getClaimantRequests());
        requestCaseData.put("claimantHearingPreference", testEt1CaseData.getClaimantHearingPreference());
        requestCaseData.put("claimantTaskListChecks", testEt1CaseData.getClaimantTaskListChecks());
        return requestCaseData;
    }

    public CaseRequest getTestCaseRequest() {
        return CaseRequest.builder()
            .postCode(testCaseData.getClaimantType().getClaimantAddressUK().getPostCode())
            .caseId(CASE_ID)
            .caseTypeId(testCaseData.getEcmCaseType())
            .caseData(getTestCaseRequestCaseDataMap())
            .build();
    }
}
