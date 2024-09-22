package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_CASE_SUBMISSION_REFERENCE1;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_CASE_SUBMISSION_REFERENCE2;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_CASE_TYPE_ID_ENGLAND_WALES;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_ET3_SERVICE_EXCEPTION_CASE_DETAILS_NOT_FOUND;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_ET3_SERVICE_EXCEPTION_CASE_DETAILS_NOT_FOUND_EMPTY_PARAMETERS;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@EqualsAndHashCode
@ExtendWith(MockitoExtension.class)
class ET3ServiceTest {

    @Mock
    AdminUserService adminUserService;
    @Mock
    AuthTokenGenerator authTokenGenerator;
    @Mock
    CoreCaseDataApi ccdApi;
    @Mock
    CaseService caseService;

    private ET3Service et3Service;

    @BeforeEach
    void setUp() {
        et3Service = new ET3Service(adminUserService, authTokenGenerator, ccdApi, caseService);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        StringUtils.EMPTY, StringUtils.SPACE, TEST_CASE_SUBMISSION_REFERENCE1, TEST_CASE_SUBMISSION_REFERENCE2})
    void theFindCaseBySubmissionReference(String submissionReference) {
        CaseDetails caseDetails;
        if (StringUtils.isNotBlank(submissionReference)) {
            when(adminUserService.getAdminUserToken()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
            when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
            if (TEST_CASE_SUBMISSION_REFERENCE1.equals(submissionReference)) {
                when(ccdApi.getCase(TEST_SERVICE_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, submissionReference))
                    .thenReturn(CaseDetails.builder()
                                    .caseTypeId(TEST_CASE_TYPE_ID_ENGLAND_WALES)
                                    .id(Long.parseLong(submissionReference))
                                    .build());
                caseDetails = et3Service.findCaseBySubmissionReference(TEST_CASE_SUBMISSION_REFERENCE1);
                assertThat(caseDetails).isNotNull();
                assertThat(caseDetails.getId()).isEqualTo(Long.parseLong(TEST_CASE_SUBMISSION_REFERENCE1));
                assertThat(caseDetails.getCaseTypeId()).isEqualTo(TEST_CASE_TYPE_ID_ENGLAND_WALES);
            } else {
                when(ccdApi.getCase(eq(TEST_SERVICE_AUTH_TOKEN),
                                        eq(TEST_SERVICE_AUTH_TOKEN),
                                        anyString()))
                    .thenReturn(null);
                assertThat(assertThrows(RuntimeException.class, () ->
                    et3Service.findCaseBySubmissionReference(TEST_CASE_SUBMISSION_REFERENCE1))
                               .getMessage()).contains(TEST_ET3_SERVICE_EXCEPTION_CASE_DETAILS_NOT_FOUND);
            }
        } else {
            assertThat(assertThrows(RuntimeException.class, () ->
                et3Service.findCaseBySubmissionReference(submissionReference))
                           .getMessage()).isEqualTo(TEST_ET3_SERVICE_EXCEPTION_CASE_DETAILS_NOT_FOUND_EMPTY_PARAMETERS);
        }

    }

    @Test
    void theUpdateSubmittedCaseWithCaseDetails() {
        CaseDetails caseDetails = new CaseTestData().getCaseDetailsWithCaseData();
        when(caseService.triggerEvent(TEST_SERVICE_AUTH_TOKEN,
                                      caseDetails.getId().toString(),
                                      CaseEvent.UPDATE_CASE_SUBMITTED,
                                      caseDetails.getCaseTypeId(),
                                      caseDetails.getData())).thenReturn(caseDetails);
        assertDoesNotThrow(() -> et3Service.updateSubmittedCaseWithCaseDetails(TEST_SERVICE_AUTH_TOKEN,
                                                                               caseDetails));
    }
}
