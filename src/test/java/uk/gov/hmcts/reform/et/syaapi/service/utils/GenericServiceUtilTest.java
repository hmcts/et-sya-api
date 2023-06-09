package uk.gov.hmcts.reform.et.syaapi.service.utils;

import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfDecodedMultipartFile;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GenericServiceUtilTest {

    @ParameterizedTest
    @MethodSource(
        "uk.gov.hmcts.reform.et.syaapi.model.TestData#generateCaseDataArgumentsForTheTestFindClaimantLanguage")
    void theFindClaimantLanguage(CaseData caseData, String expectedLanguage) {
        assertThat(GenericServiceUtil.findClaimantLanguage(caseData)).isEqualTo(expectedLanguage);
    }

    @ParameterizedTest
    @MethodSource(
        "uk.gov.hmcts.reform.et.syaapi.model.CaseTestData#generateCaseDataUserInfoArgumentsForTestingFirstNames")
    void theFindClaimantFirstNameByCaseDataUserInfo(CaseData caseData, UserInfo userInfo, String expectedFirstNames) {
        assertThat(GenericServiceUtil.findClaimantFirstNameByCaseDataUserInfo(caseData, userInfo))
            .isEqualTo(expectedFirstNames);
    }

    @ParameterizedTest
    @MethodSource(
        "uk.gov.hmcts.reform.et.syaapi.model.CaseTestData#generateCaseDataUserInfoArgumentsForTestingLastName")
    void theFindClaimantLastNameByCaseDataUserInfo(CaseData caseData, UserInfo userInfo, String expectedLastName) {
        assertThat(GenericServiceUtil.findClaimantLastNameByCaseDataUserInfo(caseData, userInfo))
            .isEqualTo(expectedLastName);
    }

    @ParameterizedTest
    @MethodSource(
        "uk.gov.hmcts.reform.et.syaapi.model.TestData#generatePdfFileListForTestingHasPdfFileByGivenIndex")
    void theHasPdfFile(List<PdfDecodedMultipartFile> pdfFileList, int index, boolean expectedValue) {
        assertThat(GenericServiceUtil.hasPdfFile(pdfFileList, index)).isEqualTo(expectedValue);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource(
        "uk.gov.hmcts.reform.et.syaapi.model.TestData#generatePdfFileListForTestingPrepareUploadByGivenIndex")
    void thePrepareUpload(List<PdfDecodedMultipartFile> pdfFileList, int index, Object expectedValue) {
        try (MockedStatic<GenericServiceUtil> mockedServiceUtil = Mockito.mockStatic(GenericServiceUtil.class)) {
            mockedServiceUtil.when(() -> GenericServiceUtil.prepareUpload(
                    null, 0))
                .thenReturn(TestConstants.FILE_NOT_EXISTS);
            mockedServiceUtil.when(() -> GenericServiceUtil.prepareUpload(
                    TestConstants.EMPTY_PDF_DECODED_MULTIPART_FILE_LIST, 0))
                .thenReturn(TestConstants.FILE_NOT_EXISTS);
            mockedServiceUtil.when(() -> GenericServiceUtil.prepareUpload(
                    TestConstants.NULL_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST, 0))
                .thenReturn(TestConstants.FILE_NOT_EXISTS);
            mockedServiceUtil.when(() -> GenericServiceUtil.prepareUpload(
                    TestConstants.EMPTY_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST, 0))
                .thenReturn(TestConstants.FILE_NOT_EXISTS);
            mockedServiceUtil.when(() -> GenericServiceUtil.prepareUpload(
                TestConstants.NOT_EMPTY_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST, 0))
                .thenReturn(TestConstants.PREPARE_PDF_UPLOAD_JSON_OBJECT);
            assertThat(GenericServiceUtil.prepareUpload(pdfFileList, index)).isEqualTo(expectedValue);
        }
    }

    @ParameterizedTest
    @MethodSource(
        "uk.gov.hmcts.reform.et.syaapi.model.TestData#generatePdfFileListForTestingFindPdfFileBySelectedLanguage")
    void theFindPdfFileBySelectedLanguage(List<PdfDecodedMultipartFile> pdfFileList, String selectedLanguage,
                                          byte[] expectedValue) {
        assertThat(GenericServiceUtil.findPdfFileBySelectedLanguage(pdfFileList, selectedLanguage))
            .isEqualTo(expectedValue);
    }
}
