package uk.gov.hmcts.reform.et.syaapi.utils;

import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfDecodedMultipartFile;
import uk.gov.hmcts.reform.et.syaapi.service.util.ServiceUtil;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"PMD.TooManyMethods"})
class ServiceUtilTest {

    @ParameterizedTest
    @MethodSource("retrieveCaseDataArgumentsForTheTestFindClaimantLanguage")
    void theFindClaimantLanguage(CaseData caseData, String expectedLanguage) {
        assertThat(ServiceUtil.findClaimantLanguage(caseData)).isEqualTo(expectedLanguage);
    }

    @ParameterizedTest
    @MethodSource("retrieveCaseDataUserInfoArgumentsForTestingFirstNames")
    void theFindClaimantFirstNameByCaseDataUserInfo(CaseData caseData, UserInfo userInfo, String expectedFirstNames) {
        assertThat(ServiceUtil.findClaimantFirstNameByCaseDataUserInfo(caseData, userInfo))
            .isEqualTo(expectedFirstNames);
    }

    @ParameterizedTest
    @MethodSource("retrieveCaseDataUserInfoArgumentsForTestingLastName")
    void theFindClaimantLastNameByCaseDataUserInfo(CaseData caseData, UserInfo userInfo, String expectedLastName) {
        assertThat(ServiceUtil.findClaimantLastNameByCaseDataUserInfo(caseData, userInfo))
            .isEqualTo(expectedLastName);
    }

    @ParameterizedTest
    @MethodSource("retrievePdfFileListForTestingHasPdfFileByGivenIndex")
    void theHasPdfFile(List<PdfDecodedMultipartFile> pdfFileList, int index, boolean expectedValue) {
        assertThat(ServiceUtil.hasPdfFile(pdfFileList, index)).isEqualTo(expectedValue);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("retrievePdfFileListForTestingPrepareUploadByGivenIndex")
    void thePrepareUpload(List<PdfDecodedMultipartFile> pdfFileList, int index, Object expectedValue) {
        try (MockedStatic<ServiceUtil> mockedServiceUtil = Mockito.mockStatic(ServiceUtil.class)) {
            mockedServiceUtil.when(() -> ServiceUtil.prepareUpload(
                    null, 0))
                .thenReturn(TestConstants.FILE_NOT_EXISTS);
            mockedServiceUtil.when(() -> ServiceUtil.prepareUpload(
                    TestConstants.EMPTY_PDF_DECODED_MULTIPART_FILE_LIST, 0))
                .thenReturn(TestConstants.FILE_NOT_EXISTS);
            mockedServiceUtil.when(() -> ServiceUtil.prepareUpload(
                    TestConstants.NULL_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST, 0))
                .thenReturn(TestConstants.FILE_NOT_EXISTS);
            mockedServiceUtil.when(() -> ServiceUtil.prepareUpload(
                    TestConstants.EMPTY_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST, 0))
                .thenReturn(TestConstants.FILE_NOT_EXISTS);
            mockedServiceUtil.when(() -> ServiceUtil.prepareUpload(
                TestConstants.NOT_EMPTY_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST, 0))
                .thenReturn(TestConstants.PREPARE_PDF_UPLOAD_JSON_OBJECT);
            assertThat(ServiceUtil.prepareUpload(pdfFileList, index)).isEqualTo(expectedValue);
        }
    }

    @ParameterizedTest
    @MethodSource("retrievePdfFileListForTestingFindPdfFileBySelectedLanguage")
    void theFindPdfFileBySelectedLanguage(List<PdfDecodedMultipartFile> pdfFileList, String selectedLanguage,
                                          byte[] expectedValue) {
        assertThat(ServiceUtil.findPdfFileBySelectedLanguage(pdfFileList, selectedLanguage)).isEqualTo(expectedValue);
    }

    private static Stream<Arguments> retrieveCaseDataArgumentsForTheTestFindClaimantLanguage() {
        return TestData.generateCaseDataArgumentsForTheTestFindClaimantLanguage();
    }

    private static Stream<Arguments> retrieveCaseDataUserInfoArgumentsForTestingFirstNames() {
        return TestData.generateCaseDataUserInfoArgumentsForTestingFirstNames();
    }

    private static Stream<Arguments> retrieveCaseDataUserInfoArgumentsForTestingLastName() {
        return TestData.generateCaseDataUserInfoArgumentsForTestingLastName();
    }

    private static Stream<Arguments> retrievePdfFileListForTestingHasPdfFileByGivenIndex() {
        return TestData.generatePdfFileListForTestingHasPdfFileByGivenIndex();
    }

    private static Stream<Arguments> retrievePdfFileListForTestingPrepareUploadByGivenIndex() {
        return TestData.generatePdfFileListForTestingPrepareUploadByGivenIndex();
    }

    private static Stream<Arguments> retrievePdfFileListForTestingFindPdfFileBySelectedLanguage() {
        return TestData.generatePdfFileListForTestingFindPdfFileBySelectedLanguage();
    }
}