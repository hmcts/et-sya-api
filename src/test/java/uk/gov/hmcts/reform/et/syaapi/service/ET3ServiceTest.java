package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
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

    private ET3Service et3Service;

    @BeforeEach
    void setUp() {
        et3Service = new ET3Service(adminUserService, authTokenGenerator, ccdApi);
    }

    @ParameterizedTest
    @MethodSource("generateTheFindCaseBySubmissionReferenceCaseTypeIdTestData")
    void theFindCaseBySubmissionReferenceCaseTypeId(String submissionReference, String caseTypeId) {
        CaseDetails caseDetails;
        if (StringUtils.isNotBlank(submissionReference) && StringUtils.isNotBlank(caseTypeId)) {
            when(adminUserService.getAdminUserToken()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
            when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
            if (TEST_CASE_SUBMISSION_REFERENCE1.equals(submissionReference)) {
                when(ccdApi.searchCases(eq(TEST_SERVICE_AUTH_TOKEN),
                                        eq(TEST_SERVICE_AUTH_TOKEN),
                                        eq(caseTypeId),
                                        anyString()))
                    .thenReturn(SearchResult.builder().cases(List.of(CaseDetails.builder()
                                                                         .caseTypeId(caseTypeId)
                                                                         .id(Long.parseLong(submissionReference))
                                                                         .build())).total(1).build());
                caseDetails = et3Service.findCaseBySubmissionReferenceCaseTypeId(TEST_CASE_SUBMISSION_REFERENCE1,
                                                                                 caseTypeId);
                assertThat(caseDetails).isNotNull();
                assertThat(caseDetails.getId()).isEqualTo(Long.parseLong(TEST_CASE_SUBMISSION_REFERENCE1));
                assertThat(caseDetails.getCaseTypeId()).isEqualTo(TEST_CASE_TYPE_ID_ENGLAND_WALES);
            } else {
                when(ccdApi.searchCases(eq(TEST_SERVICE_AUTH_TOKEN),
                                        eq(TEST_SERVICE_AUTH_TOKEN),
                                        eq(caseTypeId),
                                        anyString()))
                    .thenReturn(SearchResult.builder().cases(new ArrayList<>()).total(1).build());
                assertThat(assertThrows(RuntimeException.class, () ->
                    et3Service.findCaseBySubmissionReferenceCaseTypeId(TEST_CASE_SUBMISSION_REFERENCE1,caseTypeId))
                               .getMessage()).contains(TEST_ET3_SERVICE_EXCEPTION_CASE_DETAILS_NOT_FOUND);
            }
        } else {
            assertThat(assertThrows(RuntimeException.class, () ->
                et3Service.findCaseBySubmissionReferenceCaseTypeId(submissionReference, caseTypeId))
                           .getMessage()).isEqualTo(TEST_ET3_SERVICE_EXCEPTION_CASE_DETAILS_NOT_FOUND_EMPTY_PARAMETERS);
        }

    }

    private static Stream<Arguments> generateTheFindCaseBySubmissionReferenceCaseTypeIdTestData() {
        return Stream.of(Arguments.of(null, null),
                         Arguments.of(StringUtils.EMPTY, StringUtils.EMPTY),
                         Arguments.of(StringUtils.SPACE, StringUtils.SPACE),
                         Arguments.of(TEST_CASE_SUBMISSION_REFERENCE1, StringUtils.SPACE),
                         Arguments.of(StringUtils.SPACE, TEST_CASE_TYPE_ID_ENGLAND_WALES),
                         Arguments.of(TEST_CASE_SUBMISSION_REFERENCE1, TEST_CASE_TYPE_ID_ENGLAND_WALES),
                         Arguments.of(TEST_CASE_SUBMISSION_REFERENCE2, TEST_CASE_TYPE_ID_ENGLAND_WALES));
    }

}
