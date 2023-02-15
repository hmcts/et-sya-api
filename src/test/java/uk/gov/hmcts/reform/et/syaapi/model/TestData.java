package uk.gov.hmcts.reform.et.syaapi.model;

import lombok.Data;
import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.Et1CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceLoader;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceUtil;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.service.notify.SendEmailResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.UPDATE_CASE_DRAFT;

@Data
@SuppressWarnings("PMD.TooManyFields")
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
    private final CaseRequest caseRequest = ResourceLoader.fromString(
        "requests/caseRequest.json",
        CaseRequest.class
    );
    private final CaseRequest caseDataWithClaimTypes = ResourceLoader.fromString(
        "requests/caseDataWithClaimTypes.json",
        CaseRequest.class
    );
    private final CaseRequest caseRequestWithoutManagingAddress = ResourceLoader.fromString(
        "requests/caseDataWithoutManagingAddress.json",
        CaseRequest.class
    );
    private final CaseRequest emptyCaseRequest = ResourceLoader.fromString(
        "requests/noManagingOfficeAndRespondentsAddressCaseRequest.json",
        CaseRequest.class
    );

    private final CaseRequest englandWalesRequest = ResourceLoader.fromString(
        "requests/caseRequestEnglandWales.json",
        CaseRequest.class
    );

    private final UserInfo userInfo = ResourceLoader.fromString(
        "responses/userInfo.json",
        UserInfo.class
    );

    private final List<CaseDetails> requestCaseDataListEnglandAcas = ResourceLoader.fromStringToList(
        "responses/caseDetailsEnglandAcasDocs.json",
        CaseDetails.class
    );

    private final ClaimantTse claimantApplication = ResourceLoader.fromString(
        "responses/claimantTse.json",
        ClaimantTse.class
    );

    public Map<String, Object> getCaseRequestCaseDataMap() {
        Map<String, Object> requestCaseData = new ConcurrentHashMap<>();
        requestCaseData.put("typesOfClaim", et1CaseData.getTypesOfClaim());
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

    public CaseDataContent getUpdateCaseDataContent() {
        return CaseDataContent.builder()
            .event(Event.builder().id(UPDATE_CASE_DRAFT).build())
            .eventToken(getStartEventResponse().getToken())
            .data(getCaseRequestCaseDataMap())
            .build();
    }

    public SendEmailResponse getSendEmailResponse() throws IOException {
        String sendEmailResponseStringVal = ResourceUtil.resourceAsString(
            "responses/sendEmailResponse.json"
        );
        return new SendEmailResponse(sendEmailResponseStringVal);
    }

    public static Stream<Arguments> postcodeAddressArguments() {

        Address address1 = new TestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address1.setPostCode("A1      1AA");

        Address address2 = new TestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address2.setPostCode("A22AA");

        Address address3 = new TestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address3.setPostCode("A 3  3 A  A");

        Address address4 = new TestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address4.setPostCode("NG44JF");

        Address address5 = new TestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address5.setPostCode("NG5      5JF");

        Address address6 = new TestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address6.setPostCode("N  G 6      6  J F");

        Address address7 = new TestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address7.setPostCode("HU107NA");

        Address address8 = new TestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address8.setPostCode("HU10      8NA");

        Address address9 = new TestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address9.setPostCode("H U 1 0 9 N A");

        Address address10 = new TestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address10.setCountry("Turkey");
        address10.setPostCode("34730");

        Address address11 = new TestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address11.setCountry("United kingdom");
        address11.setPostCode("AB111AB");

        Address address12 = new TestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address12.setCountry(null);
        address12.setPostCode("AB121AB");

        Address address13 = new TestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address13.setCountry("");
        address13.setPostCode("AB131AB");

        return Stream.of(
            Arguments.of("A1 1AA", address1),
            Arguments.of("A2 2AA", address2),
            Arguments.of("A3 3AA", address3),
            Arguments.of("NG4 4JF", address4),
            Arguments.of("NG5 5JF", address5),
            Arguments.of("NG6 6JF", address6),
            Arguments.of("HU10 7NA", address7),
            Arguments.of("HU10 8NA", address8),
            Arguments.of("HU10 9NA", address9),
            Arguments.of("34730", address10),
            Arguments.of("AB11 1AB", address11),
            Arguments.of("AB12 1AB", address12),
            Arguments.of("AB13 1AB", address13)
        );

    }
}
