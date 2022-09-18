package uk.gov.hmcts.reform.et.syaapi.model;

import lombok.Data;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.Et1CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceLoader;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceUtil;
import uk.gov.service.notify.SendEmailResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.INITIATE_CASE_DRAFT;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.UPDATE_CASE_DRAFT;


@Data
public final class TestData {

    private final Et1CaseData et1CaseData = ResourceLoader.fromString(
        "requests/caseData.json",
        Et1CaseData.class
    );
    private final CaseData caseData = ResourceLoader.fromString(
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
    private final CaseDataContent submitCaseDataContent = ResourceLoader.fromString(
        "requests/submitCaseDataContent.json",
        CaseDataContent.class
    );
    private final List<DocumentTypeItem> uploadDocumentResponse = ResourceLoader.fromStringToList(
        "responses/documentTypeItemList.json",
        DocumentTypeItem.class
    );


    public Map<String, Object> getCaseRequestCaseDataMap() {
        Map<String, Object> requestCaseData = new ConcurrentHashMap<>();
        requestCaseData.put("typeOfClaim", et1CaseData.getTypeOfClaim());
        requestCaseData.put("caseType", et1CaseData.getEcmCaseType());
        requestCaseData.put("caseSource", et1CaseData.getCaseSource());
        requestCaseData.put("claimantRepresentedQuestion", et1CaseData.getClaimantRepresentedQuestion());
        requestCaseData.put("jurCodesCollection", et1CaseData.getJurCodesCollection());
        requestCaseData.put("claimantIndType", et1CaseData.getClaimantIndType());
        requestCaseData.put("claimantType", et1CaseData.getClaimantType());
        requestCaseData.put("representativeClaimantType", et1CaseData.getRepresentativeClaimantType());
        requestCaseData.put("claimantOtherType", et1CaseData.getClaimantOtherType());
        requestCaseData.put("respondentCollection", et1CaseData.getRespondentCollection());
        requestCaseData.put("claimantWorkAddress", et1CaseData.getClaimantWorkAddress());
        requestCaseData.put("caseNotes", et1CaseData.getCaseNotes());
        requestCaseData.put("managingOffice", et1CaseData.getManagingOffice());
        requestCaseData.put("newEmploymentType", et1CaseData.getNewEmploymentType());
        requestCaseData.put("claimantRequests", et1CaseData.getClaimantRequests());
        requestCaseData.put("claimantHearingPreference", et1CaseData.getClaimantHearingPreference());
        requestCaseData.put("claimantTaskListChecks", et1CaseData.getClaimantTaskListChecks());
        return requestCaseData;
    }

    public CaseRequest getCaseRequest() {
        return CaseRequest.builder()
            .postCode(caseData.getClaimantType().getClaimantAddressUK().getPostCode())
            .caseId(CASE_ID)
            .caseTypeId(caseData.getEcmCaseType())
            .caseData(getCaseRequestCaseDataMap())
            .build();
    }

    public CaseDataContent getUpdateCaseDataContent() {
        return CaseDataContent.builder()
            .event(Event.builder().id(UPDATE_CASE_DRAFT).build())
            .eventToken(getStartEventResponse().getToken())
            .data(getCaseRequestCaseDataMap())
            .build();
    }

    public CaseDataContent getTestDraftCaseDataContent() {
        return CaseDataContent.builder()
            .event(Event.builder().id(INITIATE_CASE_DRAFT).build())
            .eventToken(getStartEventResponse().getToken())
            .data(getEt1CaseData())
            .build();
    }

    public SendEmailResponse getSendEmailResponse() throws IOException {
        String sendEmailResponseStringVal = ResourceUtil.resourceAsString(
            "responses/caseDocumentUpload.json"
        );
        return new SendEmailResponse(sendEmailResponseStringVal);
    }
}
