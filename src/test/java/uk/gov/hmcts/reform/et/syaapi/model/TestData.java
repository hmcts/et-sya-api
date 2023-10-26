package uk.gov.hmcts.reform.et.syaapi.model;

import lombok.Data;
import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.models.AdminDecisionNotificationStateUpdateRequest;
import uk.gov.hmcts.reform.et.syaapi.models.ChangeApplicationStatusRequest;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimantApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.models.RespondToApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.models.SendNotificationAddResponseRequest;
import uk.gov.hmcts.reform.et.syaapi.models.SendNotificationStateUpdateRequest;
import uk.gov.hmcts.reform.et.syaapi.models.SubmitStoredApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.models.TribunalResponseViewedRequest;
import uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants;
import uk.gov.hmcts.reform.et.syaapi.service.utils.ResourceLoader;
import uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants;
import uk.gov.hmcts.reform.et.syaapi.service.utils.data.TestDataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.MULTIPLE_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.NOT_EMPTY_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_ACAS;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_COMPANY_NAME;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TRUE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.WORK_ADDRESS_LINE_1;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.WORK_ADDRESS_LINE_2;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.WORK_ADDRESS_LINE_3;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.WORK_COUNTRY;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.WORK_COUNTY;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.WORK_POSTCODE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.WORK_POST_TOWN;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.YES;

@Data
public final class TestData {
    private final CaseDataContent submitCaseDataContent = ResourceLoader.fromString(
        "requests/submitCaseDataContent.json",
        CaseDataContent.class
    );

    private final StartEventResponse updateCaseEventResponse = ResourceLoader.fromString(
        "responses/updateCaseEventResponse.json",
        StartEventResponse.class
    );

    private final StartEventResponse startEventResponse = ResourceLoader.fromString(
        "responses/startEventResponse.json",
        StartEventResponse.class
    );

    private final StartEventResponse sendNotificationCollectionResponse = ResourceLoader.fromString(
        "responses/sendNotificationCollectionResponse.json",
        StartEventResponse.class
    );

    private final RespondToApplicationRequest respondToApplicationRequest = ResourceLoader.fromString(
        "requests/respondToApplication.json",
        RespondToApplicationRequest.class
    );

    private final CaseDetails caseDetailsWithData = ResourceLoader.fromString(
        "responses/caseDetailsWithCaseData.json",
        CaseDetails.class
    );

    private final CaseData caseData = ResourceLoader.fromString(
        "requests/caseData.json",
        CaseData.class
    );

    private final ChangeApplicationStatusRequest changeApplicationStatusRequest = ResourceLoader.fromString(
        "requests/viewAnApplication.json",
        ChangeApplicationStatusRequest.class
    );

    private final SubmitStoredApplicationRequest submitStoredApplicationRequest = ResourceLoader.fromString(
        "requests/submitStoredApplication.json",
        SubmitStoredApplicationRequest.class
    );

    private final ClaimantApplicationRequest claimantApplicationRequest = ResourceLoader.fromString(
        "requests/claimantTseRequest.json",
        ClaimantApplicationRequest.class
    );

    private final SendNotificationAddResponseRequest sendNotificationAddResponseRequest = ResourceLoader.fromString(
        "requests/SendNotificationAddResponseRequest.json",
        SendNotificationAddResponseRequest.class
    );
    private final SendNotificationStateUpdateRequest sendNotificationStateUpdateRequest = ResourceLoader.fromString(
        "requests/sendNotificationStateUpdateRequest.json",
        SendNotificationStateUpdateRequest.class
    );

    private final AdminDecisionNotificationStateUpdateRequest adminDecisionNotificationStateUpdateRequest =
        ResourceLoader.fromString(
            "requests/adminNotificationUpdateRequest.json",
            AdminDecisionNotificationStateUpdateRequest.class
        );

    private final TribunalResponseViewedRequest responseViewedRequest =
        ResourceLoader.fromString(
            "requests/tribunalResponseViewedRequest.json",
            TribunalResponseViewedRequest.class
        );

    public static Stream<Arguments> postcodeAddressArguments() {
        return Stream.of(
            Arguments.of("A1 1AA",
                         TestDataProvider.generateTestAddressByPostcodeCountry("A1      1AA",
                                                                               TestConstants.NULL_STRING)),
            Arguments.of("A2 2AA",
                         TestDataProvider.generateTestAddressByPostcodeCountry("A22AA",
                                                                               TestConstants.NULL_STRING)),
            Arguments.of("A3 3AA",
                         TestDataProvider.generateTestAddressByPostcodeCountry("A 3  3 A  A",
                                                                               TestConstants.NULL_STRING)),
            Arguments.of("NG4 4JF",
                         TestDataProvider.generateTestAddressByPostcodeCountry("NG44JF",
                                                                               TestConstants.NULL_STRING)),
            Arguments.of("NG5 5JF",
                         TestDataProvider.generateTestAddressByPostcodeCountry("NG5      5JF",
                                                                               TestConstants.NULL_STRING)),
            Arguments.of("NG6 6JF",
                         TestDataProvider.generateTestAddressByPostcodeCountry("N  G 6      6  J F",
                                                                               TestConstants.NULL_STRING)),
            Arguments.of("HU10 7NA",
                         TestDataProvider.generateTestAddressByPostcodeCountry("HU107NA",
                                                                               TestConstants.NULL_STRING)),
            Arguments.of("HU10 8NA",
                         TestDataProvider.generateTestAddressByPostcodeCountry("HU10      8NA",
                                                                               TestConstants.NULL_STRING)),
            Arguments.of("HU10 9NA",
                         TestDataProvider.generateTestAddressByPostcodeCountry("H U 1 0 9 N A",
                                                                               TestConstants.NULL_STRING)),
            Arguments.of("34730",
                         TestDataProvider.generateTestAddressByPostcodeCountry("34730",
                                                                               "Turkey")),
            Arguments.of("AB11 1AB",
                         TestDataProvider.generateTestAddressByPostcodeCountry("AB111AB",
                                                                               "United kingdom")),
            Arguments.of("AB12 1AB",
                         TestDataProvider.generateTestAddressByPostcodeCountry("AB121AB",
                                                                               TestConstants.NULL_STRING)),
            Arguments.of("AB13 1AB",
                         TestDataProvider.generateTestAddressByPostcodeCountry("AB131AB",
                                                                               TestConstants.EMPTY_STRING)),
            Arguments.of(TestConstants.EMPTY_STRING,
                         TestDataProvider.generateTestAddressByPostcodeCountry(TestConstants.EMPTY_STRING,
                                                                               TestConstants.NULL_STRING))
        );
    }

    public static Stream<Arguments> compensationArguments() {

        CaseData caseData1 = TestDataProvider.generateTestCaseDataByClaimantCompensation("Test Compensation",
                                                                                         "",
                                                                                         "");
        CaseData caseData2 = TestDataProvider.generateTestCaseDataByClaimantCompensation("Test Compensation",
                                                                                         "2000",
                                                                                         "");
        CaseData caseData3 = TestDataProvider.generateTestCaseDataByClaimantCompensation(null,
                                                                                         "2000",
                                                                                         "");
        CaseData caseData4 = TestDataProvider.generateTestCaseDataByClaimantCompensation("",
                                                                                         "",
                                                                                         ":");
        CaseData caseData5 = new CaseTestData().getCaseData();
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
        CaseData caseDataNullClaimantHearingPreferences = TestDataProvider
            .generateTestCaseDataByClaimantHearingPreferenceContactLanguage(TRUE, TestConstants.NULL_STRING);
        CaseData caseDataNullContactLanguage = TestDataProvider
            .generateTestCaseDataByClaimantHearingPreferenceContactLanguage(TestConstants.FALSE,
                                                                            TestConstants.NULL_STRING);
        CaseData caseDataEmptyContactLanguage = TestDataProvider
            .generateTestCaseDataByClaimantHearingPreferenceContactLanguage(TestConstants.FALSE,
                                                                            TestConstants.EMPTY_STRING);
        CaseData caseDataInvalidContactLanguage = TestDataProvider
            .generateTestCaseDataByClaimantHearingPreferenceContactLanguage(TestConstants.FALSE,
                                                                            TestConstants.INVALID_LANGUAGE);
        CaseData caseDataWelshContactLanguage = TestDataProvider
            .generateTestCaseDataByClaimantHearingPreferenceContactLanguage(TestConstants.FALSE,
                                                                            TestConstants.WELSH_LANGUAGE);
        CaseData caseDataEnglishContactLanguage = TestDataProvider
            .generateTestCaseDataByClaimantHearingPreferenceContactLanguage(TestConstants.FALSE,
                                                                            TestConstants.ENGLISH_LANGUAGE);

        return Stream.of(
            Arguments.of(caseDataNullClaimantHearingPreferences, TestConstants.ENGLISH_LANGUAGE),
            Arguments.of(caseDataNullContactLanguage, TestConstants.ENGLISH_LANGUAGE),
            Arguments.of(caseDataEmptyContactLanguage, TestConstants.ENGLISH_LANGUAGE),
            Arguments.of(caseDataInvalidContactLanguage, TestConstants.ENGLISH_LANGUAGE),
            Arguments.of(caseDataWelshContactLanguage, TestConstants.WELSH_LANGUAGE),
            Arguments.of(caseDataEnglishContactLanguage, TestConstants.ENGLISH_LANGUAGE)
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

    public static Stream<Arguments> generateCaseDataSamplesWithRespondentSumTypeItems() {
        Address respondentAddress = TestDataProvider.generateAddressByAddressFields(TestConstants.ADDRESS_LINE_1,
                                                                                    TestConstants.ADDRESS_LINE_2,
                                                                                    TestConstants.ADDRESS_LINE_3,
                                                                                    TestConstants.POST_TOWN,
                                                                                    TestConstants.COUNTY,
                                                                                    TestConstants.COUNTRY,
                                                                                    TestConstants.POSTCODE);
        ////// CASE DATA 1
        CaseData caseData1 = TestDataProvider.generateCaseDataForRespondent(TestConstants.STRING_NUMERIC_ONE, YES,
                                                                            TestConstants.NULL_ADDRESS);
        RespondentSumTypeItem respondentSumTypeItem = generateRespondentSumTypeItem(
            TestConstants.STRING_NUMERIC_ONE, TEST_COMPANY_NAME, respondentAddress, YES, TEST_ACAS,
            PdfMapperConstants.PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_UNFAIR_DISMISSAL);
        List<RespondentSumTypeItem> respondentCollection = new ArrayList<>();
        respondentCollection.add(respondentSumTypeItem);
        caseData1.setRespondentCollection(respondentCollection);
        ////// CASE DATA 2
        RespondentSumTypeItem respondentSumTypeItem2 = generateRespondentSumTypeItem(
            TestConstants.STRING_NUMERIC_TWO, TEST_COMPANY_NAME, respondentAddress, YES, TEST_ACAS,
            PdfMapperConstants.PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_ANOTHER_PERSON);
        RespondentSumTypeItem respondentSumTypeItem3 = generateRespondentSumTypeItem(
            TestConstants.STRING_NUMERIC_THREE, TEST_COMPANY_NAME, respondentAddress, TestConstants.NO, TEST_ACAS,
            PdfMapperConstants.PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_NO_POWER);
        RespondentSumTypeItem respondentSumTypeItem4 = generateRespondentSumTypeItem(
            TestConstants.STRING_NUMERIC_FOUR, TEST_COMPANY_NAME, respondentAddress, TestConstants.NO, TEST_ACAS,
            PdfMapperConstants.PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_UNFAIR_DISMISSAL);
        RespondentSumTypeItem respondentSumTypeItem5 = generateRespondentSumTypeItem(
            TestConstants.STRING_NUMERIC_FIVE, TEST_COMPANY_NAME, respondentAddress, TestConstants.NO, TEST_ACAS,
            PdfMapperConstants.PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_ANOTHER_PERSON);
        List<RespondentSumTypeItem> respondentCollection2 = new ArrayList<>();
        respondentCollection2.add(respondentSumTypeItem);
        respondentCollection2.add(respondentSumTypeItem2);
        respondentCollection2.add(respondentSumTypeItem3);
        respondentCollection2.add(respondentSumTypeItem4);
        respondentCollection2.add(respondentSumTypeItem5);
        Address workAddress = TestDataProvider.generateAddressByAddressFields(WORK_ADDRESS_LINE_1, WORK_ADDRESS_LINE_2,
                                                                              WORK_ADDRESS_LINE_3, WORK_POST_TOWN,
                                                                              WORK_COUNTY, WORK_COUNTRY, WORK_POSTCODE);
        CaseData caseData2 = TestDataProvider.generateCaseDataForRespondent(TestConstants.STRING_NUMERIC_TWO,
                                                                            TestConstants.NO,
                                                                            workAddress);
        caseData2.setRespondentCollection(respondentCollection2);
        ////// CASE DATA 3
        CaseData caseData3 = TestDataProvider.generateCaseDataForRespondent(TestConstants.STRING_NUMERIC_THREE,
                                                                            YES,
                                                                            TestConstants.NULL_ADDRESS);
        RespondentSumTypeItem respondentSumTypeItem6 = generateRespondentSumTypeItem(
            TestConstants.STRING_NUMERIC_TWO, TEST_COMPANY_NAME, respondentAddress, YES, TEST_ACAS,
            PdfMapperConstants.PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_EMPLOYER_ALREADY_IN_TOUCH);

        List<RespondentSumTypeItem> respondentCollection3 = new ArrayList<>();
        respondentCollection3.add(respondentSumTypeItem);
        respondentCollection3.add(respondentSumTypeItem6);
        caseData3.setRespondentCollection(respondentCollection3);

        ////// CASE DATA 4
        CaseData caseData4 = TestDataProvider.generateCaseDataForRespondent(TestConstants.STRING_NUMERIC_FOUR, YES,
                                                                            TestConstants.NULL_ADDRESS);

        return Stream.of(Arguments.of(caseData1),
                         Arguments.of(caseData2),
                         Arguments.of(caseData3),
                         Arguments.of(caseData4));
    }

    public static RespondentSumTypeItem generateRespondentSumTypeItem(String respondentSumTypeItemId,
                                                                      String name,
                                                                      Address respondentAddress,
                                                                      String respondentAcasQuestion,
                                                                      String acasNumber,
                                                                      String respondentAcasNo) {
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setId(respondentSumTypeItemId);
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName(name);
        respondentSumType.setRespondentAddress(respondentAddress);
        respondentSumType.setRespondentAcasQuestion(respondentAcasQuestion);
        respondentSumType.setRespondentAcas(acasNumber);
        respondentSumType.setRespondentAcasNo(respondentAcasNo);
        respondentSumTypeItem.setValue(respondentSumType);
        return respondentSumTypeItem;
    }

}
