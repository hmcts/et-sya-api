package uk.gov.hmcts.reform.et.syaapi.model;

import lombok.Data;
import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.Et1CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantRequestType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.service.utils.ResourceLoader;
import uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.FEMALE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.MALE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.MERCURY;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.MICHAEL;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.MULTIPLE_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.NOT_EMPTY_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.OTHER;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.OTHER_TITLE;

@Data
@SuppressWarnings({"PMD.TooManyFields", "PMD.TooManyMethods"})
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
            .event(Event.builder().id(TestConstants.UPDATE_CASE_DRAFT).build())
            .eventToken(getStartEventResponse().getToken())
            .data(getCaseRequestCaseDataMap())
            .build();
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

        Address address14 = new TestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address14.setPostCode("");

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
            Arguments.of("AB13 1AB", address13),
            Arguments.of("", address14)
        );
    }

    public static Stream<Arguments> compensationArguments() {

        CaseData caseData1 = new TestData().getCaseData();
        caseData1.getClaimantRequests().setClaimantCompensationText("Test Compensation");
        caseData1.getClaimantRequests().setClaimantCompensationAmount("");
        caseData1.getClaimantRequests().setClaimantTribunalRecommendation("");

        CaseData caseData2 = new TestData().getCaseData();
        caseData2.getClaimantRequests().setClaimantCompensationText("Test Compensation");
        caseData2.getClaimantRequests().setClaimantCompensationAmount("2000");
        caseData2.getClaimantRequests().setClaimantTribunalRecommendation("");

        CaseData caseData3 = new TestData().getCaseData();
        caseData3.getClaimantRequests().setClaimantCompensationText(null);
        caseData3.getClaimantRequests().setClaimantCompensationAmount("2000");
        caseData3.getClaimantRequests().setClaimantTribunalRecommendation("");

        CaseData caseData4 = new TestData().getCaseData();
        caseData4.getClaimantRequests().setClaimantCompensationAmount("");
        caseData4.getClaimantRequests().setClaimantTribunalRecommendation("");
        caseData4.getClaimantRequests().setClaimantCompensationText(":");

        CaseData caseData5 = new TestData().getCaseData();
        caseData5.setClaimantRequests(null);

        return Stream.of(
            Arguments.of(null, ""),

            Arguments.of(caseData1, "Compensation:\"Test Compensation\"" + System.lineSeparator()
                + System.lineSeparator()),
            Arguments.of(caseData2, "Compensation:\"Test Compensation\nAmount requested: £2000\""
                + System.lineSeparator() + System.lineSeparator()),
            Arguments.of(caseData3, "Compensation:\"Amount requested: £2000\"" + System.lineSeparator()
                + System.lineSeparator()),
            Arguments.of(caseData4, ""),
            Arguments.of(caseData5, "")
        );

    }

    public static Stream<Arguments> generateSubmitCaseConfirmationEmailPdfFilesArguments() {
        return Stream.of(
            Arguments.of(null, TestConstants.EMPTY_RESPONSE),
            Arguments.of(TestConstants.EMPTY_PDF_DECODED_MULTIPART_FILE_LIST, TestConstants.EMPTY_RESPONSE),
            Arguments.of(TestConstants.NULL_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST, TestConstants.EMPTY_RESPONSE),
            Arguments.of(TestConstants.EMPTY_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST, TestConstants.EMPTY_RESPONSE),
            Arguments.of(NOT_EMPTY_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST,
                         TestConstants.TEST_SUBMIT_CASE_PDF_FILE_RESPONSE)
        );
    }

    public static Stream<Arguments> generateCaseDataArgumentsForTheTestFindClaimantLanguage() {
        CaseData caseDataNullClaimantHearingPreferences = new TestData().getCaseData();
        caseDataNullClaimantHearingPreferences.setClaimantHearingPreference(null);

        CaseData caseDataNullContactLanguage = new TestData().getCaseData();
        caseDataNullContactLanguage.getClaimantHearingPreference().setContactLanguage(null);

        CaseData caseDataEmptyContactLanguage = new TestData().getCaseData();
        caseDataEmptyContactLanguage.getClaimantHearingPreference().setContactLanguage("");

        CaseData caseDataInvalidContactLanguage = new TestData().getCaseData();
        caseDataInvalidContactLanguage.getClaimantHearingPreference().setContactLanguage("invalid language");

        CaseData caseDataWelshContactLanguage = new TestData().getCaseData();
        caseDataWelshContactLanguage.getClaimantHearingPreference().setContactLanguage(TestConstants.WELSH_LANGUAGE);

        CaseData caseDataEnglishContactLanguage = new TestData().getCaseData();
        caseDataEnglishContactLanguage.getClaimantHearingPreference()
            .setContactLanguage(TestConstants.ENGLISH_LANGUAGE);

        return Stream.of(
            Arguments.of(caseDataNullClaimantHearingPreferences, TestConstants.ENGLISH_LANGUAGE),
            Arguments.of(caseDataNullContactLanguage, TestConstants.ENGLISH_LANGUAGE),
            Arguments.of(caseDataEmptyContactLanguage, TestConstants.ENGLISH_LANGUAGE),
            Arguments.of(caseDataInvalidContactLanguage, TestConstants.ENGLISH_LANGUAGE),
            Arguments.of(caseDataWelshContactLanguage, TestConstants.WELSH_LANGUAGE),
            Arguments.of(caseDataEnglishContactLanguage, TestConstants.ENGLISH_LANGUAGE)
        );
    }

    public static Stream<Arguments> generateCaseDataUserInfoArgumentsForTestingFirstNames() {

        CaseData caseDataNullClaimantIndType = new TestData().getCaseData();
        caseDataNullClaimantIndType.setClaimantIndType(null);

        CaseData caseDataEmptyClaimantIndType = new TestData().getCaseData();
        caseDataEmptyClaimantIndType.setClaimantIndType(new ClaimantIndType());

        CaseData caseDataNullClaimantFirstNames = new TestData().getCaseData();
        caseDataNullClaimantFirstNames.getClaimantIndType().setClaimantFirstNames(null);

        CaseData caseDataEmptyClaimantFirstNames = new TestData().getCaseData();
        caseDataEmptyClaimantFirstNames.getClaimantIndType().setClaimantFirstNames("");

        CaseData caseDataNotEmptyClaimantFirstNames = new TestData().getCaseData();

        TestData tmpTestData = new TestData();

        return Stream.of(
            Arguments.of(caseDataNullClaimantIndType, tmpTestData.getUserInfo(),
                         tmpTestData.getUserInfo().getGivenName()),
            Arguments.of(caseDataEmptyClaimantIndType, tmpTestData.getUserInfo(),
                         tmpTestData.getUserInfo().getGivenName()),
            Arguments.of(caseDataNullClaimantFirstNames, tmpTestData.getUserInfo(),
                         tmpTestData.getUserInfo().getGivenName()),
            Arguments.of(caseDataEmptyClaimantFirstNames, tmpTestData.getUserInfo(),
                         tmpTestData.getUserInfo().getGivenName()),
            Arguments.of(caseDataNotEmptyClaimantFirstNames, tmpTestData.getUserInfo(),
                         tmpTestData.getCaseData().getClaimantIndType().getClaimantFirstNames())
        );
    }

    public static Stream<Arguments> generateCaseDataUserInfoArgumentsForTestingLastName() {

        CaseData caseDataNullClaimantIndType = new TestData().getCaseData();
        caseDataNullClaimantIndType.setClaimantIndType(null);

        CaseData caseDataEmptyClaimantIndType = new TestData().getCaseData();
        caseDataEmptyClaimantIndType.setClaimantIndType(new ClaimantIndType());

        CaseData caseDataNullClaimantFirstNames = new TestData().getCaseData();
        caseDataNullClaimantFirstNames.getClaimantIndType().setClaimantLastName(null);

        CaseData caseDataEmptyClaimantFirstNames = new TestData().getCaseData();
        caseDataEmptyClaimantFirstNames.getClaimantIndType().setClaimantLastName("");

        CaseData caseDataNotEmptyClaimantFirstNames = new TestData().getCaseData();

        TestData tmpTestData = new TestData();

        return Stream.of(
            Arguments.of(caseDataNullClaimantIndType, tmpTestData.getUserInfo(),
                         tmpTestData.getUserInfo().getFamilyName()),
            Arguments.of(caseDataEmptyClaimantIndType, tmpTestData.getUserInfo(),
                         tmpTestData.getUserInfo().getFamilyName()),
            Arguments.of(caseDataNullClaimantFirstNames, tmpTestData.getUserInfo(),
                         tmpTestData.getUserInfo().getFamilyName()),
            Arguments.of(caseDataEmptyClaimantFirstNames, tmpTestData.getUserInfo(),
                         tmpTestData.getUserInfo().getFamilyName()),
            Arguments.of(caseDataNotEmptyClaimantFirstNames, tmpTestData.getUserInfo(),
                         tmpTestData.getCaseData().getClaimantIndType().getClaimantLastName())
        );
    }

    public static Stream<Arguments> generatePdfFileListForTestingHasPdfFileByGivenIndex() {
        return Stream.of(
            Arguments.of(null, 0, false),
            Arguments.of(TestConstants.EMPTY_PDF_DECODED_MULTIPART_FILE_LIST, 0, false),
            Arguments.of(TestConstants.NULL_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST, 0, false),
            Arguments.of(TestConstants.EMPTY_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST, 0, false),
            Arguments.of(NOT_EMPTY_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST, 0, true)
        );
    }

    public static Stream<Arguments> generatePdfFileListForTestingPrepareUploadByGivenIndex() {
        return Stream.of(
            Arguments.of(null, 0, TestConstants.FILE_NOT_EXISTS),
            Arguments.of(TestConstants.EMPTY_PDF_DECODED_MULTIPART_FILE_LIST, 0,
                         TestConstants.FILE_NOT_EXISTS),
            Arguments.of(TestConstants.NULL_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST, 0,
                         TestConstants.FILE_NOT_EXISTS),
            Arguments.of(TestConstants.EMPTY_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST, 0,
                         TestConstants.FILE_NOT_EXISTS),
            Arguments.of(NOT_EMPTY_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST, 0,
                         TestConstants.PREPARE_PDF_UPLOAD_JSON_OBJECT)
        );
    }

    public static Stream<Arguments> generatePdfFileListForTestingFindPdfFileBySelectedLanguage() {
        return Stream.of(
            Arguments.of(null, TestConstants.WELSH_LANGUAGE, TestConstants.EMPTY_BYTE_ARRAY),
            Arguments.of(null, TestConstants.ENGLISH_LANGUAGE, TestConstants.EMPTY_BYTE_ARRAY),
            Arguments.of(TestConstants.EMPTY_PDF_DECODED_MULTIPART_FILE_LIST, TestConstants.WELSH_LANGUAGE,
                         TestConstants.EMPTY_BYTE_ARRAY),
            Arguments.of(TestConstants.EMPTY_PDF_DECODED_MULTIPART_FILE_LIST, TestConstants.ENGLISH_LANGUAGE,
                         TestConstants.EMPTY_BYTE_ARRAY),
            Arguments.of(TestConstants.NULL_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST,
                         TestConstants.WELSH_LANGUAGE, TestConstants.EMPTY_BYTE_ARRAY),
            Arguments.of(TestConstants.NULL_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST,
                         TestConstants.ENGLISH_LANGUAGE, TestConstants.EMPTY_BYTE_ARRAY),
            Arguments.of(TestConstants.EMPTY_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST,
                         TestConstants.WELSH_LANGUAGE, TestConstants.EMPTY_BYTE_ARRAY),
            Arguments.of(TestConstants.EMPTY_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST,
                         TestConstants.ENGLISH_LANGUAGE, TestConstants.EMPTY_BYTE_ARRAY),
            Arguments.of(NOT_EMPTY_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST,
                         TestConstants.WELSH_LANGUAGE, TestConstants.EMPTY_BYTE_ARRAY),
            Arguments.of(NOT_EMPTY_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST,
                         TestConstants.ENGLISH_LANGUAGE,
                         NOT_EMPTY_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST.get(0).getBytes()),
            Arguments.of(MULTIPLE_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST,
                         TestConstants.WELSH_LANGUAGE,
                         MULTIPLE_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST.get(1).getBytes()),
            Arguments.of(MULTIPLE_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST,
                         TestConstants.ENGLISH_LANGUAGE,
                         MULTIPLE_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST.get(0).getBytes())
        );
    }


    public static Stream<Arguments> generateSendDocUploadErrorEmailPdfFilesArguments() {
        return Stream.of(
            Arguments.of(null, null, null),
            Arguments.of(TestConstants.NULL_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST,
                         TestConstants.NULL_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST,
                         TestConstants.NULL_UPLOADED_DOCUMENT_TYPE_FILE),
            Arguments.of(TestConstants.EMPTY_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST,
                         TestConstants.EMPTY_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST,
                         TestConstants.EMPTY_UPLOADED_DOCUMENT_TYPE_FILE),
            Arguments.of(TestConstants.EMPTY_PDF_DECODED_MULTIPART_FILE_LIST,
                         TestConstants.EMPTY_PDF_DECODED_MULTIPART_FILE_LIST,
                         TestConstants.EMPTY_UPLOADED_DOCUMENT_TYPE),
            Arguments.of(NOT_EMPTY_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST,
                         NOT_EMPTY_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST,
                         TestConstants.NOT_EMPTY_UPLOADED_DOCUMENT_TYPE_FILE)
        );
    }

    public SearchResult requestCaseDataListSearchResult() {
        SearchResult searchResult = SearchResult.builder().build();
        searchResult.setCases(getRequestCaseDataList());
        searchResult.setTotal(2);
        return searchResult;
    }

    public SearchResult getSearchResultRequestCaseDataListScotland() {
        SearchResult searchResult = SearchResult.builder().build();
        searchResult.setCases(getRequestCaseDataListScotland());
        searchResult.setTotal(2);
        return searchResult;
    }

    public SearchResult getSearchResultRequestCaseDataListEngland() {
        SearchResult searchResult = SearchResult.builder().build();
        searchResult.setCases(getRequestCaseDataListEngland());
        searchResult.setTotal(1);
        return searchResult;
    }

    public static Stream<Arguments> generateClaimantIndTypeArguments() {
        ClaimantIndType claimantIndTypeOtherTitleMaleNotNullDateOfBirth = new ClaimantIndType();
        claimantIndTypeOtherTitleMaleNotNullDateOfBirth.setClaimantPreferredTitle(OTHER);
        claimantIndTypeOtherTitleMaleNotNullDateOfBirth.setClaimantTitleOther(OTHER_TITLE);
        claimantIndTypeOtherTitleMaleNotNullDateOfBirth.setClaimantFirstNames(MICHAEL);
        claimantIndTypeOtherTitleMaleNotNullDateOfBirth.setClaimantLastName(MERCURY);
        claimantIndTypeOtherTitleMaleNotNullDateOfBirth.setClaimantDateOfBirth("1979-05-08");
        claimantIndTypeOtherTitleMaleNotNullDateOfBirth.setClaimantSex(MALE);

        ClaimantIndType claimantIndTypeTitleMrMaleNotNullDateOfBirth = new ClaimantIndType();
        claimantIndTypeTitleMrMaleNotNullDateOfBirth.setClaimantPreferredTitle("Mr");
        claimantIndTypeTitleMrMaleNotNullDateOfBirth.setClaimantFirstNames(MICHAEL);
        claimantIndTypeTitleMrMaleNotNullDateOfBirth.setClaimantLastName(MERCURY);
        claimantIndTypeTitleMrMaleNotNullDateOfBirth.setClaimantDateOfBirth("1980-06-09");
        claimantIndTypeTitleMrMaleNotNullDateOfBirth.setClaimantSex(MALE);

        ClaimantIndType claimantIndTypeTitleMsFemaleNotNullDateOfBirth = new ClaimantIndType();
        claimantIndTypeTitleMsFemaleNotNullDateOfBirth.setClaimantPreferredTitle("Ms");
        claimantIndTypeTitleMsFemaleNotNullDateOfBirth.setClaimantFirstNames("Elizabeth");
        claimantIndTypeTitleMsFemaleNotNullDateOfBirth.setClaimantLastName("Taylor");
        claimantIndTypeTitleMsFemaleNotNullDateOfBirth.setClaimantDateOfBirth("1981-07-10");
        claimantIndTypeTitleMsFemaleNotNullDateOfBirth.setClaimantSex(FEMALE);

        ClaimantIndType claimantIndTypeTitleMrsFemaleNotNullDateOfBirth = new ClaimantIndType();
        claimantIndTypeTitleMrsFemaleNotNullDateOfBirth.setClaimantPreferredTitle("Mrs");
        claimantIndTypeTitleMrsFemaleNotNullDateOfBirth.setClaimantFirstNames("Taylor");
        claimantIndTypeTitleMrsFemaleNotNullDateOfBirth.setClaimantLastName("Swift");
        claimantIndTypeTitleMrsFemaleNotNullDateOfBirth.setClaimantDateOfBirth("1982-08-11");
        claimantIndTypeTitleMrsFemaleNotNullDateOfBirth.setClaimantSex(FEMALE);

        ClaimantIndType claimantIndTypeTitleMissFemaleNotNullDateOfBirth = new ClaimantIndType();
        claimantIndTypeTitleMissFemaleNotNullDateOfBirth.setClaimantPreferredTitle("Miss");
        claimantIndTypeTitleMissFemaleNotNullDateOfBirth.setClaimantFirstNames("Taylor");
        claimantIndTypeTitleMissFemaleNotNullDateOfBirth.setClaimantLastName("Swift");
        claimantIndTypeTitleMissFemaleNotNullDateOfBirth.setClaimantDateOfBirth("1983-09-12");
        claimantIndTypeTitleMissFemaleNotNullDateOfBirth.setClaimantSex(FEMALE);

        ClaimantIndType claimantIndTypeOtherTitleMaleNullDateOfBirth = new ClaimantIndType();
        claimantIndTypeOtherTitleMaleNullDateOfBirth.setClaimantPreferredTitle(OTHER);
        claimantIndTypeOtherTitleMaleNullDateOfBirth.setClaimantTitleOther(OTHER_TITLE);
        claimantIndTypeOtherTitleMaleNullDateOfBirth.setClaimantFirstNames(MICHAEL);
        claimantIndTypeOtherTitleMaleNullDateOfBirth.setClaimantLastName(MERCURY);
        claimantIndTypeOtherTitleMaleNullDateOfBirth.setClaimantDateOfBirth(null);
        claimantIndTypeOtherTitleMaleNullDateOfBirth.setClaimantSex(MALE);

        ClaimantIndType claimantIndTypeOtherTitleMaleEmptyDateOfBirth = new ClaimantIndType();
        claimantIndTypeOtherTitleMaleEmptyDateOfBirth.setClaimantPreferredTitle(OTHER);
        claimantIndTypeOtherTitleMaleEmptyDateOfBirth.setClaimantTitleOther(OTHER_TITLE);
        claimantIndTypeOtherTitleMaleEmptyDateOfBirth.setClaimantFirstNames(MICHAEL);
        claimantIndTypeOtherTitleMaleEmptyDateOfBirth.setClaimantLastName(MERCURY);
        claimantIndTypeOtherTitleMaleEmptyDateOfBirth.setClaimantDateOfBirth(" ");
        claimantIndTypeOtherTitleMaleEmptyDateOfBirth.setClaimantSex(MALE);

        ClaimantIndType claimantIndTypeOtherTitlePreferNotToSayEmptyDateOfBirth = new ClaimantIndType();
        claimantIndTypeOtherTitlePreferNotToSayEmptyDateOfBirth.setClaimantPreferredTitle(OTHER);
        claimantIndTypeOtherTitlePreferNotToSayEmptyDateOfBirth.setClaimantTitleOther(OTHER_TITLE);
        claimantIndTypeOtherTitlePreferNotToSayEmptyDateOfBirth.setClaimantFirstNames(MICHAEL);
        claimantIndTypeOtherTitlePreferNotToSayEmptyDateOfBirth.setClaimantLastName(MERCURY);
        claimantIndTypeOtherTitlePreferNotToSayEmptyDateOfBirth.setClaimantDateOfBirth("1984-10-13");
        claimantIndTypeOtherTitlePreferNotToSayEmptyDateOfBirth.setClaimantSex("Prefer not to say");

        return Stream.of(
            Arguments.of(claimantIndTypeOtherTitleMaleNotNullDateOfBirth, "08", "05", "1979"),
            Arguments.of(claimantIndTypeTitleMrMaleNotNullDateOfBirth, "09", "06", "1980"),
            Arguments.of(claimantIndTypeTitleMsFemaleNotNullDateOfBirth, "10", "07", "1981"),
            Arguments.of(claimantIndTypeTitleMrsFemaleNotNullDateOfBirth, "11", "08", "1982"),
            Arguments.of(claimantIndTypeTitleMissFemaleNotNullDateOfBirth, "12", "09", "1983"),
            Arguments.of(claimantIndTypeOtherTitleMaleNullDateOfBirth, "", "", ""),
            Arguments.of(claimantIndTypeOtherTitlePreferNotToSayEmptyDateOfBirth, "13", "10", "1984")
        );
    }

    public static Stream<Arguments> generateClaimantTypeArguments() {

        ClaimantType claimantTypePhoneNumber = new ClaimantType();
        claimantTypePhoneNumber.setClaimantPhoneNumber("07444444444");

        ClaimantType claimantTypeMobileNumber = new ClaimantType();
        claimantTypeMobileNumber.setClaimantPhoneNumber("07444444555");

        ClaimantType claimantTypeEmail = new ClaimantType();
        claimantTypeEmail.setClaimantEmailAddress("mehmet@tdmehmet.com");

        ClaimantType claimantTypeContactPreferenceEmail = new ClaimantType();
        claimantTypeContactPreferenceEmail.setClaimantContactPreference("Email");

        ClaimantType claimantTypeContactPreferencePost = new ClaimantType();
        claimantTypeContactPreferencePost.setClaimantContactPreference("Post");

        Address claimantAddressUK = new Address();
        claimantAddressUK.setAddressLine1("AddressLine1");
        claimantAddressUK.setAddressLine2("AddressLine2");
        claimantAddressUK.setAddressLine3("AddressLine3");
        claimantAddressUK.setPostTown("PostTown");
        claimantAddressUK.setCounty("County");
        claimantAddressUK.setCountry("Country");
        claimantAddressUK.setPostCode("SW1A 1AA");
        ClaimantType claimantTypeAddressUK = new ClaimantType();
        claimantTypeAddressUK.setClaimantAddressUK(claimantAddressUK);

        ClaimantType claimantTypeAll = new ClaimantType();
        claimantTypeAll.setClaimantPhoneNumber("07444444444");
        claimantTypeAll.setClaimantPhoneNumber("07444444555");
        claimantTypeAll.setClaimantEmailAddress("mehmet@tdmehmet.com");
        claimantTypeAll.setClaimantContactPreference("Email");
        claimantTypeAll.setClaimantContactPreference("Post");
        claimantTypeAll.setClaimantAddressUK(claimantAddressUK);

        ClaimantType claimantTypeBlank = new ClaimantType();

        return Stream.of(
            Arguments.of(claimantTypeBlank),
            Arguments.of(claimantTypePhoneNumber),
            Arguments.of(claimantTypeMobileNumber),
            Arguments.of(claimantTypeEmail),
            Arguments.of(claimantTypeContactPreferenceEmail),
            Arguments.of(claimantTypeContactPreferencePost),
            Arguments.of(claimantTypeAddressUK),
            Arguments.of(claimantTypeAll)
        );
    }

    public static Stream<Arguments> generateClaimantRequests() {

        ClaimantRequestType claimantRequestClaimDescriptionNull = new ClaimantRequestType();
        claimantRequestClaimDescriptionNull.setClaimDescription(null);

        ClaimantRequestType claimantRequestClaimDescriptionEmpty = new ClaimantRequestType();
        claimantRequestClaimDescriptionEmpty.setClaimDescription("");

        ClaimantRequestType claimantRequestClaimDescriptionBlank = new ClaimantRequestType();
        claimantRequestClaimDescriptionBlank.setClaimDescription("   ");

        ClaimantRequestType claimantRequestClaimDescriptionFilled = new ClaimantRequestType();
        claimantRequestClaimDescriptionFilled.setClaimDescription("Test Claim Description");

        ClaimantRequestType claimantRequestEmpty = new ClaimantRequestType();

        return Stream.of(
            Arguments.of(null, null),
            Arguments.of(claimantRequestEmpty, null),
            Arguments.of(claimantRequestClaimDescriptionNull, null),
            Arguments.of(claimantRequestClaimDescriptionEmpty, null),
            Arguments.of(claimantRequestClaimDescriptionBlank, null),
            Arguments.of(claimantRequestClaimDescriptionFilled, "Test Claim Description")
        );
    }
}
