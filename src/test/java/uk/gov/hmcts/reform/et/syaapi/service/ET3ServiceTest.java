package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ENGLAND_CASE_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@EqualsAndHashCode
@ExtendWith(MockitoExtension.class)
class ET3ServiceTest {

    private static final String CASE_SUBMISSION_REFERENCE = "1234567890";

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

    @Test
    void theFindCaseBySubmissionReferenceCaseTypeId() {
        when(adminUserService.getAdminUserToken()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdApi.searchCases(eq(TEST_SERVICE_AUTH_TOKEN),
                                eq(TEST_SERVICE_AUTH_TOKEN),
                                eq(ENGLAND_CASE_TYPE),
                                anyString()))
            .thenReturn(SearchResult.builder().cases(List.of(CaseDetails.builder()
                                                                 .caseTypeId(ENGLAND_CASE_TYPE)
                                                                 .id(Long.parseLong(CASE_SUBMISSION_REFERENCE))
                                                                 .build())).total(1).build());
        CaseDetails caseDetails = et3Service.findCaseBySubmissionReferenceCaseTypeId(CASE_SUBMISSION_REFERENCE,
                                                                                     ENGLAND_CASE_TYPE);
        assertThat(caseDetails).isNotNull();
        assertThat(caseDetails.getId()).isEqualTo(Long.parseLong(CASE_SUBMISSION_REFERENCE));
        assertThat(caseDetails.getCaseTypeId()).isEqualTo(ENGLAND_CASE_TYPE);
    }

}
