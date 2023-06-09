package uk.gov.hmcts.reform.et.syaapi.model;

import lombok.Data;
import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.et.syaapi.service.utils.ResourceLoader;
import uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants;
import uk.gov.hmcts.reform.et.syaapi.service.utils.TestUtil;

import java.util.stream.Stream;

import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.MULTIPLE_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.NOT_EMPTY_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TRUE;

@Data
public final class TestData {
    private final CaseDataContent submitCaseDataContent = ResourceLoader.fromString(
        "requests/submitCaseDataContent.json",
        CaseDataContent.class
    );

    public static Stream<Arguments> postcodeAddressArguments() {
        return Stream.of(
            Arguments.of("A1 1AA", TestUtil.generateTestAddressByPostcodeCountry("A1      1AA",
                                                                                 TestConstants.NULL_STRING)),
            Arguments.of("A2 2AA", TestUtil.generateTestAddressByPostcodeCountry("A22AA",
                                                                                 TestConstants.NULL_STRING)),
            Arguments.of("A3 3AA", TestUtil.generateTestAddressByPostcodeCountry("A 3  3 A  A",
                                                                                 TestConstants.NULL_STRING)),
            Arguments.of("NG4 4JF", TestUtil.generateTestAddressByPostcodeCountry("NG44JF",
                                                                                  TestConstants.NULL_STRING)),
            Arguments.of("NG5 5JF", TestUtil.generateTestAddressByPostcodeCountry("NG5      5JF",
                                                                                  TestConstants.NULL_STRING)),
            Arguments.of("NG6 6JF", TestUtil.generateTestAddressByPostcodeCountry("N  G 6      6  J F",
                                                                                  TestConstants.NULL_STRING)),
            Arguments.of("HU10 7NA", TestUtil.generateTestAddressByPostcodeCountry("HU107NA",
                                                                                   TestConstants.NULL_STRING)),
            Arguments.of("HU10 8NA", TestUtil.generateTestAddressByPostcodeCountry("HU10      8NA",
                                                                                   TestConstants.NULL_STRING)),
            Arguments.of("HU10 9NA", TestUtil.generateTestAddressByPostcodeCountry("H U 1 0 9 N A",
                                                                                   TestConstants.NULL_STRING)),
            Arguments.of("34730", TestUtil.generateTestAddressByPostcodeCountry("34730", "Turkey")),
            Arguments.of("AB11 1AB", TestUtil.generateTestAddressByPostcodeCountry("AB111AB", "United kingdom")),
            Arguments.of("AB12 1AB", TestUtil.generateTestAddressByPostcodeCountry("AB121AB",
                                                                                   TestConstants.NULL_STRING)),
            Arguments.of("AB13 1AB", TestUtil.generateTestAddressByPostcodeCountry("AB131AB",
                                                                                   TestConstants.EMPTY_STRING)),
            Arguments.of(TestConstants.EMPTY_STRING,
                         TestUtil.generateTestAddressByPostcodeCountry(TestConstants.EMPTY_STRING,
                                                                       TestConstants.NULL_STRING))
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
        CaseData caseDataNullClaimantHearingPreferences = TestUtil
            .generateTestCaseDataByClaimantHearingPreferenceContactLanguage(TRUE, TestConstants.NULL_STRING);
        CaseData caseDataNullContactLanguage = TestUtil
            .generateTestCaseDataByClaimantHearingPreferenceContactLanguage(TestConstants.FALSE,
                                                                            TestConstants.NULL_STRING);
        CaseData caseDataEmptyContactLanguage = TestUtil
            .generateTestCaseDataByClaimantHearingPreferenceContactLanguage(TestConstants.FALSE,
                                                                            TestConstants.EMPTY_STRING);
        CaseData caseDataInvalidContactLanguage = TestUtil
            .generateTestCaseDataByClaimantHearingPreferenceContactLanguage(TestConstants.FALSE,
                                                                            TestConstants.INVALID_LANGUAGE);
        CaseData caseDataWelshContactLanguage = TestUtil
            .generateTestCaseDataByClaimantHearingPreferenceContactLanguage(TestConstants.FALSE,
                                                                            TestConstants.WELSH_LANGUAGE);
        CaseData caseDataEnglishContactLanguage = TestUtil
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
}
