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
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.service.utils.ResourceLoader;
import uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants;
import uk.gov.hmcts.reform.et.syaapi.service.utils.TestUtil;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.ADDRESS_LINE_1;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.ADDRESS_LINE_2;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.ADDRESS_LINE_3;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.BLANK_STRING;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CLAIMANT_IND_TYPE_EMPTY;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CLAIMANT_IND_TYPE_FILLED;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CLAIMANT_IND_TYPE_NULL;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CLAIM_DESCRIPTION;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.COUNTRY;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.COUNTY;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.ELIZABETH;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.EMAIL;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.FALSE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.FAX;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.FEMALE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.INVALID_LANGUAGE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.MALE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.MERCURY;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.MICHAEL;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.MISS;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.MR;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.MRS;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.MS;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.MULTIPLE_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.NOT_EMPTY_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.NULL_STRING;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.OTHER;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.OTHER_TITLE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.POST;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.POSTCODE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.POST_TOWN;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.PREFER_NOT_TO_SAY;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.REPRESENTATIVE_EMAIL;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.REPRESENTATIVE_MOBILE_NUMBER;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.REPRESENTATIVE_NAME;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.REPRESENTATIVE_ORGANISATION;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.REPRESENTATIVE_PHONE_NUMBER;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.REPRESENTATIVE_REFERENCE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.SWIFT;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TAYLOR;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_NAMES;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TRUE;

@Data
@SuppressWarnings({"PMD.TooManyFields", "PMD.TooManyMethods"})
public final class TestData {

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
        Et1CaseData et1CaseData = ResourceLoader.fromString("requests/caseData.json", Et1CaseData.class);
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
        return Stream.of(
            Arguments.of("A1 1AA", TestUtil.generateTestAddressByPostcodeCountry("A1      1AA", NULL_STRING)),
            Arguments.of("A2 2AA", TestUtil.generateTestAddressByPostcodeCountry("A22AA", NULL_STRING)),
            Arguments.of("A3 3AA", TestUtil.generateTestAddressByPostcodeCountry("A 3  3 A  A", NULL_STRING)),
            Arguments.of("NG4 4JF", TestUtil.generateTestAddressByPostcodeCountry("NG44JF", NULL_STRING)),
            Arguments.of("NG5 5JF", TestUtil.generateTestAddressByPostcodeCountry("NG5      5JF", NULL_STRING)),
            Arguments.of("NG6 6JF", TestUtil.generateTestAddressByPostcodeCountry("N  G 6      6  J F", NULL_STRING)),
            Arguments.of("HU10 7NA", TestUtil.generateTestAddressByPostcodeCountry("HU107NA", NULL_STRING)),
            Arguments.of("HU10 8NA", TestUtil.generateTestAddressByPostcodeCountry("HU10      8NA", NULL_STRING)),
            Arguments.of("HU10 9NA", TestUtil.generateTestAddressByPostcodeCountry("H U 1 0 9 N A", NULL_STRING)),
            Arguments.of("34730", TestUtil.generateTestAddressByPostcodeCountry("34730", "Turkey")),
            Arguments.of("AB11 1AB", TestUtil.generateTestAddressByPostcodeCountry("AB111AB", "United kingdom")),
            Arguments.of("AB12 1AB", TestUtil.generateTestAddressByPostcodeCountry("AB121AB", NULL_STRING)),
            Arguments.of("AB13 1AB", TestUtil.generateTestAddressByPostcodeCountry("AB131AB", EMPTY_STRING)),
            Arguments.of(EMPTY_STRING,
                         TestUtil.generateTestAddressByPostcodeCountry(EMPTY_STRING, NULL_STRING))
        );
    }

    public static Stream<Arguments> compensationArguments() {

        CaseData caseData1 = TestUtil.generateTestCaseDataByClaimantCompensation("Test Compensation",
                                                                                 "",
                                                                                 "");
        CaseData caseData2 = TestUtil.generateTestCaseDataByClaimantCompensation("Test Compensation",
                                                                                 "2000",
                                                                                 "");
        CaseData caseData3 = TestUtil.generateTestCaseDataByClaimantCompensation(null,
                                                                                 "2000",
                                                                                 "");
        CaseData caseData4 = TestUtil.generateTestCaseDataByClaimantCompensation("",
                                                                                 "",
                                                                                 ":");
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
        CaseData caseDataNullClaimantHearingPreferences = TestUtil
            .generateTestCaseDataByClaimantHearingPreferenceContactLanguage(TRUE, NULL_STRING);
        CaseData caseDataNullContactLanguage = TestUtil
            .generateTestCaseDataByClaimantHearingPreferenceContactLanguage(FALSE, NULL_STRING);
        CaseData caseDataEmptyContactLanguage = TestUtil
            .generateTestCaseDataByClaimantHearingPreferenceContactLanguage(FALSE, EMPTY_STRING);
        CaseData caseDataInvalidContactLanguage = TestUtil
            .generateTestCaseDataByClaimantHearingPreferenceContactLanguage(FALSE, INVALID_LANGUAGE);
        CaseData caseDataWelshContactLanguage = TestUtil
            .generateTestCaseDataByClaimantHearingPreferenceContactLanguage(FALSE, TestConstants.WELSH_LANGUAGE);
        CaseData caseDataEnglishContactLanguage = TestUtil
            .generateTestCaseDataByClaimantHearingPreferenceContactLanguage(FALSE, TestConstants.ENGLISH_LANGUAGE);

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

        CaseData caseDataNullClaimantIndType =
            TestUtil.generateTestCaseDataByFirstNames(CLAIMANT_IND_TYPE_NULL, EMPTY_STRING);
        CaseData caseDataEmptyClaimantIndType =
            TestUtil.generateTestCaseDataByFirstNames(CLAIMANT_IND_TYPE_EMPTY, EMPTY_STRING);
        CaseData caseDataNullClaimantFirstNames =
            TestUtil.generateTestCaseDataByFirstNames(CLAIMANT_IND_TYPE_FILLED, NULL_STRING);
        CaseData caseDataEmptyClaimantFirstNames =
            TestUtil.generateTestCaseDataByFirstNames(CLAIMANT_IND_TYPE_FILLED, EMPTY_STRING);
        CaseData caseDataNotEmptyClaimantFirstNames =
            TestUtil.generateTestCaseDataByFirstNames(CLAIMANT_IND_TYPE_FILLED, TEST_NAMES);

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
        caseDataNullClaimantFirstNames.getClaimantIndType().setClaimantLastName(NULL_STRING);

        CaseData caseDataEmptyClaimantFirstNames = new TestData().getCaseData();
        caseDataEmptyClaimantFirstNames.getClaimantIndType().setClaimantLastName(EMPTY_STRING);

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
        ClaimantIndType claimantIndTypeOtherTitleMaleNotNullDateOfBirth = TestUtil.generateClaimantIndType(
            OTHER, OTHER_TITLE, MICHAEL, MERCURY, "1979-05-08", MALE
        );

        ClaimantIndType claimantIndTypeTitleMrMaleNotNullDateOfBirth = TestUtil.generateClaimantIndType(
            MR, NULL_STRING, MICHAEL, MERCURY, "1980-06-09", MALE
        );


        ClaimantIndType claimantIndTypeTitleMsFemaleNotNullDateOfBirth = TestUtil.generateClaimantIndType(
            MS, NULL_STRING, ELIZABETH, TAYLOR, "1981-07-10", FEMALE
        );

        ClaimantIndType claimantIndTypeTitleMrsFemaleNotNullDateOfBirth = TestUtil.generateClaimantIndType(
            MRS, NULL_STRING, TAYLOR, SWIFT, "1982-08-11", FEMALE
        );

        ClaimantIndType claimantIndTypeTitleMissFemaleNotNullDateOfBirth = TestUtil.generateClaimantIndType(
            MISS, NULL_STRING, TAYLOR, SWIFT, "1983-09-12", FEMALE
        );

        ClaimantIndType claimantIndTypeOtherTitleMaleNullDateOfBirth = TestUtil.generateClaimantIndType(
            OTHER, OTHER_TITLE, MICHAEL, MERCURY, NULL_STRING, MALE
        );

        ClaimantIndType claimantIndTypeOtherTitleMaleEmptyDateOfBirth = TestUtil.generateClaimantIndType(
            OTHER, OTHER_TITLE, MICHAEL, MERCURY, EMPTY_STRING, MALE
        );
        ClaimantIndType claimantIndTypeOtherTitleMaleBlankDateOfBirth = TestUtil.generateClaimantIndType(
            OTHER, OTHER_TITLE, MICHAEL, MERCURY, BLANK_STRING, MALE
        );


        ClaimantIndType claimantIndTypeOtherTitlePreferNotToSay = TestUtil.generateClaimantIndType(
            OTHER, OTHER_TITLE, MICHAEL, MERCURY, BLANK_STRING, PREFER_NOT_TO_SAY
        );

        return Stream.of(
            Arguments.of(claimantIndTypeOtherTitleMaleNotNullDateOfBirth, "08", "05", "1979"),
            Arguments.of(claimantIndTypeTitleMrMaleNotNullDateOfBirth, "09", "06", "1980"),
            Arguments.of(claimantIndTypeTitleMsFemaleNotNullDateOfBirth, "10", "07", "1981"),
            Arguments.of(claimantIndTypeTitleMrsFemaleNotNullDateOfBirth, "11", "08", "1982"),
            Arguments.of(claimantIndTypeTitleMissFemaleNotNullDateOfBirth, "12", "09", "1983"),
            Arguments.of(claimantIndTypeOtherTitleMaleNullDateOfBirth, "", "", ""),
            Arguments.of(claimantIndTypeOtherTitleMaleEmptyDateOfBirth, "", "", ""),
            Arguments.of(claimantIndTypeOtherTitleMaleBlankDateOfBirth, "", "", ""),
            Arguments.of(claimantIndTypeOtherTitlePreferNotToSay, "", "", "")
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
        Address claimantAddressUK = TestUtil.generateAddressByAddressFields(ADDRESS_LINE_1, ADDRESS_LINE_2,
                                                                            ADDRESS_LINE_3, POST_TOWN, COUNTY,
                                                                            COUNTRY, POSTCODE);
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
        claimantRequestClaimDescriptionEmpty.setClaimDescription(EMPTY_STRING);
        ClaimantRequestType claimantRequestClaimDescriptionBlank = new ClaimantRequestType();
        claimantRequestClaimDescriptionBlank.setClaimDescription(BLANK_STRING);
        ClaimantRequestType claimantRequestClaimDescriptionFilled = new ClaimantRequestType();
        claimantRequestClaimDescriptionFilled.setClaimDescription(CLAIM_DESCRIPTION);
        ClaimantRequestType claimantRequestEmpty = new ClaimantRequestType();

        return Stream.of(
            Arguments.of(null, null),
            Arguments.of(claimantRequestEmpty, null),
            Arguments.of(claimantRequestClaimDescriptionNull, null),
            Arguments.of(claimantRequestClaimDescriptionEmpty, null),
            Arguments.of(claimantRequestClaimDescriptionBlank, null),
            Arguments.of(claimantRequestClaimDescriptionFilled, CLAIM_DESCRIPTION)
        );
    }

    public static Stream<Arguments> generateRepresentativeClaimantTypes() {
        Address representativeAddress = TestUtil.generateAddressByAddressFields(ADDRESS_LINE_1, ADDRESS_LINE_2,
                                                                                ADDRESS_LINE_3, POST_TOWN, COUNTY,
                                                                                COUNTRY, POSTCODE);
        RepresentedTypeC representativeClaimantTypeAllFilled = TestUtil.generateRepresentativeClaimantType(
            representativeAddress, REPRESENTATIVE_NAME, REPRESENTATIVE_REFERENCE, REPRESENTATIVE_ORGANISATION,
            REPRESENTATIVE_PHONE_NUMBER, REPRESENTATIVE_MOBILE_NUMBER, REPRESENTATIVE_EMAIL, EMAIL
        );
        RepresentedTypeC representativeClaimantTypeAddressNull = TestUtil.generateRepresentativeClaimantType(
            null, REPRESENTATIVE_NAME, REPRESENTATIVE_REFERENCE, REPRESENTATIVE_ORGANISATION,
            REPRESENTATIVE_PHONE_NUMBER, REPRESENTATIVE_MOBILE_NUMBER, REPRESENTATIVE_EMAIL, EMAIL
        );
        RepresentedTypeC representativeClaimantPreferenceNull = TestUtil.generateRepresentativeClaimantType(
            representativeAddress, REPRESENTATIVE_NAME, REPRESENTATIVE_REFERENCE, REPRESENTATIVE_ORGANISATION,
            REPRESENTATIVE_PHONE_NUMBER, REPRESENTATIVE_MOBILE_NUMBER, REPRESENTATIVE_EMAIL, NULL_STRING
        );
        RepresentedTypeC representativeClaimantPreferenceEmpty = TestUtil.generateRepresentativeClaimantType(
            representativeAddress, REPRESENTATIVE_NAME, REPRESENTATIVE_REFERENCE, REPRESENTATIVE_ORGANISATION,
            REPRESENTATIVE_PHONE_NUMBER, REPRESENTATIVE_MOBILE_NUMBER, REPRESENTATIVE_EMAIL, EMPTY_STRING
        );
        RepresentedTypeC representativeClaimantPreferenceBlank = TestUtil.generateRepresentativeClaimantType(
            representativeAddress, REPRESENTATIVE_NAME, REPRESENTATIVE_REFERENCE, REPRESENTATIVE_ORGANISATION,
            REPRESENTATIVE_PHONE_NUMBER, REPRESENTATIVE_MOBILE_NUMBER, REPRESENTATIVE_EMAIL, BLANK_STRING
        );
        RepresentedTypeC representativeClaimantTypePreferenceFax = TestUtil.generateRepresentativeClaimantType(
            representativeAddress, REPRESENTATIVE_NAME, REPRESENTATIVE_REFERENCE, REPRESENTATIVE_ORGANISATION,
            REPRESENTATIVE_PHONE_NUMBER, REPRESENTATIVE_MOBILE_NUMBER, REPRESENTATIVE_EMAIL, FAX
        );
        RepresentedTypeC representativeClaimantTypePreferencePost = TestUtil.generateRepresentativeClaimantType(
            representativeAddress, REPRESENTATIVE_NAME, REPRESENTATIVE_REFERENCE, REPRESENTATIVE_ORGANISATION,
            REPRESENTATIVE_PHONE_NUMBER, REPRESENTATIVE_MOBILE_NUMBER, REPRESENTATIVE_EMAIL, POST
        );

        return Stream.of(
            Arguments.of(representativeClaimantTypeAllFilled),
            Arguments.of(representativeClaimantTypeAddressNull),
            Arguments.of(representativeClaimantPreferenceNull),
            Arguments.of(representativeClaimantPreferenceEmpty),
            Arguments.of(representativeClaimantPreferenceBlank),
            Arguments.of(representativeClaimantTypePreferenceFax),
            Arguments.of(representativeClaimantTypePreferencePost)
        );
    }
}
