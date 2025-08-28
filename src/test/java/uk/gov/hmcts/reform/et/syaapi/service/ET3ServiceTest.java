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
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.Et3Request;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.exception.ManageCaseRoleException;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.ET3FormService;
import uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.ET3_STATUS_SUBMITTED;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.MODIFICATION_TYPE_SUBMIT;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.RESPONSE_STATUS_COMPLETED;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.SUBMIT_ET3_FORM;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.UPDATE_ET3_FORM;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_RESPONDENT_SUM_TYPE_ITEM_VALID_CCD_ID;

@EqualsAndHashCode
@ExtendWith(MockitoExtension.class)
class ET3ServiceTest {

    @Mock
    CachedIdamService cachedIdamService;
    @Mock
    CoreCaseDataApi ccdApi;
    @Mock
    CaseService caseService;
    @Mock
    NotificationService notificationService;
    @Mock
    ET3FormService et3FormService;

    private ET3Service et3Service;

    @BeforeEach
    void setUp() {
        et3Service = new ET3Service(cachedIdamService,
                                    ccdApi,
                                    caseService,
                                    et3FormService,
                                    notificationService);
    }

    @ParameterizedTest
    @ValueSource(strings = {StringUtils.EMPTY,
        StringUtils.SPACE,
        TestConstants.TEST_CASE_SUBMISSION_REFERENCE1,
        TestConstants.TEST_CASE_SUBMISSION_REFERENCE2})
    void theFindCaseBySubmissionReference(String submissionReference) {
        CaseDetails caseDetails;
        if (StringUtils.isNotBlank(submissionReference)) {
            when(cachedIdamService.getAdminUserToken()).thenReturn(TestConstants.TEST_SERVICE_AUTH_TOKEN);
            when(cachedIdamService.getAuthorisationToken()).thenReturn(TestConstants.TEST_SERVICE_AUTH_TOKEN);
            if (TestConstants.TEST_CASE_SUBMISSION_REFERENCE1.equals(submissionReference)) {
                when(ccdApi.getCase(TestConstants.TEST_SERVICE_AUTH_TOKEN,
                                    TestConstants.TEST_SERVICE_AUTH_TOKEN,
                                    submissionReference))
                    .thenReturn(CaseDetails.builder()
                                    .caseTypeId(TestConstants.TEST_CASE_TYPE_ID_ENGLAND_WALES)
                                    .id(Long.parseLong(submissionReference))
                                    .build());
                caseDetails = et3Service.findCaseBySubmissionReference(TestConstants.TEST_CASE_SUBMISSION_REFERENCE1);
                assertThat(caseDetails).isNotNull();
                assertThat(caseDetails.getId()).isEqualTo(Long.parseLong(
                    TestConstants.TEST_CASE_SUBMISSION_REFERENCE1));
                assertThat(caseDetails.getCaseTypeId()).isEqualTo(TestConstants.TEST_CASE_TYPE_ID_ENGLAND_WALES);
            } else {
                when(ccdApi.getCase(eq(TestConstants.TEST_SERVICE_AUTH_TOKEN),
                                        eq(TestConstants.TEST_SERVICE_AUTH_TOKEN),
                                        anyString()))
                    .thenReturn(null);
                assertThat(assertThrows(RuntimeException.class, () ->
                    et3Service.findCaseBySubmissionReference(TestConstants.TEST_CASE_SUBMISSION_REFERENCE1))
                               .getMessage()).contains(TestConstants.TEST_ET3_SERVICE_EXCEPTION_CASE_DETAILS_NOT_FOUND);
            }
        } else {
            assertThat(assertThrows(RuntimeException.class, () ->
                et3Service.findCaseBySubmissionReference(submissionReference))
                           .getMessage())
                .isEqualTo(TestConstants.TEST_ET3_SERVICE_EXCEPTION_CASE_DETAILS_NOT_FOUND_EMPTY_PARAMETERS);
        }

    }

    @Test
    void theUpdateSubmittedCaseWithCaseDetailsForCaseAssignment() {
        CaseDetails caseDetails = new CaseTestData().getCaseDetailsWithCaseData();
        when(caseService.triggerEvent(TestConstants.TEST_SERVICE_AUTH_TOKEN,
                                      caseDetails.getId().toString(),
                                      UPDATE_ET3_FORM,
                                      caseDetails.getCaseTypeId(),
                                      caseDetails.getData())).thenReturn(caseDetails);
        assertDoesNotThrow(() -> et3Service
            .updateSubmittedCaseWithCaseDetailsForCaseAssignment(TestConstants.TEST_SERVICE_AUTH_TOKEN,
                                                                 caseDetails,
                                                                 UPDATE_ET3_FORM));
    }

    @Test
    void theGetAllUserCasesForET3() {
        List<CaseDetails> scotlandCaseDetailsList =
            new CaseTestData().getSearchResultRequestCaseDataListScotland().getCases();
        List<CaseDetails> englandWalesCaseDetailsList =
            new CaseTestData().getSearchResultRequestCaseDataListEngland().getCases();
        List<CaseDetails> allCaseDetails = new ArrayList<>();
        allCaseDetails.addAll(scotlandCaseDetailsList);
        allCaseDetails.addAll(englandWalesCaseDetailsList);
        when(cachedIdamService.getAuthorisationToken()).thenReturn(TestConstants.TEST_SERVICE_AUTH_TOKEN);
        UserInfo userinfo = new CaseTestData().getUserInfo();
        when(cachedIdamService.getUserInfo(TestConstants.TEST_SERVICE_AUTH_TOKEN)).thenReturn(userinfo);
        when(ccdApi.searchForCitizen(
            TestConstants.TEST_SERVICE_AUTH_TOKEN,
            TestConstants.TEST_SERVICE_AUTH_TOKEN,
            userinfo.getUid(),
            TestConstants.TEST_JURISDICTION_ID_EMPLOYMENT,
            TestConstants.TEST_CASE_TYPE_ID_ENGLAND_WALES,
            new HashMap<>())).thenReturn(englandWalesCaseDetailsList);
        when(ccdApi.searchForCitizen(
            TestConstants.TEST_SERVICE_AUTH_TOKEN,
            TestConstants.TEST_SERVICE_AUTH_TOKEN,
            userinfo.getUid(),
            TestConstants.TEST_JURISDICTION_ID_EMPLOYMENT,
            TestConstants.TEST_CASE_TYPE_ID_SCOTLAND,
            new HashMap<>())).thenReturn(scotlandCaseDetailsList);
        assertThat(et3Service.getAllUserCasesForET3(TestConstants.TEST_SERVICE_AUTH_TOKEN)).isEqualTo(allCaseDetails);
        when(cachedIdamService.getUserInfo(TestConstants.TEST_SERVICE_AUTH_TOKEN)).thenReturn(null);
        ManageCaseRoleException exception =
            assertThrows(ManageCaseRoleException.class,
                         () -> et3Service.getAllUserCasesForET3(TestConstants.TEST_SERVICE_AUTH_TOKEN));
        assertThat(exception.getMessage()).isEqualTo(TestConstants.TEST_ET3_SERVICE_EXCEPTION_UNABLE_TO_GET_USER_INFO);
    }

    @Test
    void theModifyEt3DataForUpdate() {
        Et3Request et3Request = new CaseTestData().getEt3Request();
        CaseDetails expectedCaseDetails = new CaseTestData().getCaseDetailsWithCaseData();
        CaseData expectedCaseData = EmployeeObjectMapper
            .convertCaseDataMapToCaseDataObject(expectedCaseDetails.getData());
        expectedCaseData.getRespondentCollection().getFirst().setValue(et3Request.getRespondent().getValue());
        expectedCaseData.getRespondentCollection().getFirst().setId(TEST_RESPONDENT_SUM_TYPE_ITEM_VALID_CCD_ID);
        expectedCaseDetails.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(expectedCaseData));
        StartEventResponse startEventResponse = new CaseTestData().getStartEventResponse();
        startEventResponse.setCaseDetails(new CaseTestData().getCaseDetails());
        when(caseService.startUpdate(TestConstants.TEST_SERVICE_AUTH_TOKEN,
                            et3Request.getCaseSubmissionReference(),
                                     et3Request.getCaseTypeId(),
                                     UPDATE_ET3_FORM)).thenReturn(startEventResponse);
        when(caseService.submitUpdate(eq(TestConstants.TEST_SERVICE_AUTH_TOKEN),
                                      eq(startEventResponse.getCaseDetails().getId().toString()),
                                      any(),
                                      eq(startEventResponse.getCaseDetails().getCaseTypeId())))
            .thenReturn(expectedCaseDetails);
        CaseDetails actualCaseDetails = et3Service.modifyEt3Data(TestConstants.TEST_SERVICE_AUTH_TOKEN, et3Request);
        CaseData caseDataFromService = EmployeeObjectMapper
            .convertCaseDataMapToCaseDataObject(actualCaseDetails.getData());
        RespondentSumType actualRespondent = caseDataFromService.getRespondentCollection().getFirst().getValue();
        RespondentSumType expectedRespondent = EmployeeObjectMapper
            .convertCaseDataMapToCaseDataObject(
                expectedCaseDetails.getData()).getRespondentCollection().getFirst().getValue();
        assertThat(actualRespondent).isEqualTo(expectedRespondent);
    }

    @Test
    void theModifyEt3DataForSubmit() {
        Et3Request et3Request = new CaseTestData().getEt3Request();
        et3Request.setRequestType(MODIFICATION_TYPE_SUBMIT);
        CaseDetails expectedCaseDetails = new CaseTestData().getCaseDetailsWithCaseData();
        CaseData expectedCaseData = EmployeeObjectMapper
            .convertCaseDataMapToCaseDataObject(expectedCaseDetails.getData());
        expectedCaseData.getRespondentCollection().getFirst().setValue(et3Request.getRespondent().getValue());
        expectedCaseData.getRespondentCollection().getFirst().getValue().setEt3Status(ET3_STATUS_SUBMITTED);
        expectedCaseData.getRespondentCollection().getFirst().getValue().setResponseStatus(RESPONSE_STATUS_COMPLETED);
        expectedCaseDetails.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(expectedCaseData));
        expectedCaseData.getRespondentCollection().getFirst().setValue(et3Request.getRespondent().getValue());
        expectedCaseData.getRespondentCollection().getFirst().setId(TEST_RESPONDENT_SUM_TYPE_ITEM_VALID_CCD_ID);
        expectedCaseDetails.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(expectedCaseData));
        StartEventResponse startEventResponse = new CaseTestData().getStartEventResponse();
        startEventResponse.setCaseDetails(new CaseTestData().getCaseDetails());
        when(caseService.startUpdate(TestConstants.TEST_SERVICE_AUTH_TOKEN,
                                     et3Request.getCaseSubmissionReference(),
                                     et3Request.getCaseTypeId(),
                                     SUBMIT_ET3_FORM)).thenReturn(startEventResponse);
        when(caseService.submitUpdate(eq(TestConstants.TEST_SERVICE_AUTH_TOKEN),
                                      eq(startEventResponse.getCaseDetails().getId().toString()),
                                      any(),
                                      eq(startEventResponse.getCaseDetails().getCaseTypeId())))
            .thenReturn(expectedCaseDetails);
        CaseDetails actualCaseDetails = et3Service.modifyEt3Data(TestConstants.TEST_SERVICE_AUTH_TOKEN, et3Request);
        CaseData caseDataFromService = EmployeeObjectMapper
            .convertCaseDataMapToCaseDataObject(actualCaseDetails.getData());
        RespondentSumType actualRespondent = caseDataFromService.getRespondentCollection().getFirst().getValue();
        RespondentSumType expectedRespondent = EmployeeObjectMapper
            .convertCaseDataMapToCaseDataObject(
                expectedCaseDetails.getData()).getRespondentCollection().getFirst().getValue();
        assertThat(actualRespondent).isEqualTo(expectedRespondent);
        verify(notificationService, times(1))
            .sendEt3ConfirmationEmail(anyString(), any(), anyString());
    }

    @Test
    void theFindCaseByEthosCaseReference() {
        CaseDetails englandWalesCaseDetails = CaseDetails.builder()
            .caseTypeId(
                TestConstants.TEST_CASE_TYPE_ID_ENGLAND_WALES)
            .id(Long.parseLong(
                TestConstants.TEST_CASE_SUBMISSION_REFERENCE1))
            .state(TestConstants.TEST_CASE_STATE_ACCEPTED)
            .build();
        CaseDetails scotlandCaseDetails = CaseDetails.builder()
            .caseTypeId(
                TestConstants.TEST_CASE_TYPE_ID_SCOTLAND)
            .id(Long.parseLong(
                TestConstants.TEST_CASE_SUBMISSION_REFERENCE1))
            .state(TestConstants.TEST_CASE_STATE_ACCEPTED)
            .build();
        when(cachedIdamService.getAdminUserToken()).thenReturn(TestConstants.TEST_SERVICE_AUTH_TOKEN);
        when(cachedIdamService.getAuthorisationToken()).thenReturn(TestConstants.TEST_SERVICE_AUTH_TOKEN);
        when(ccdApi.searchCases(TestConstants.TEST_SERVICE_AUTH_TOKEN,
                                TestConstants.TEST_SERVICE_AUTH_TOKEN,
                                TestConstants.TEST_CASE_TYPE_ID_ENGLAND_WALES,
                                TestConstants.EXPECTED_QUERY_BY_ETHOS_CASE_REFERENCE))
            .thenReturn(SearchResult.builder().cases(List.of(englandWalesCaseDetails)).total(1).build());
        CaseDetails caseDetails = et3Service.findCaseByEthosCaseReference(TestConstants.TEST_ETHOS_CASE_REFERENCE);
        assertThat(caseDetails).isEqualTo(englandWalesCaseDetails);
        when(ccdApi.searchCases(TestConstants.TEST_SERVICE_AUTH_TOKEN,
                                TestConstants.TEST_SERVICE_AUTH_TOKEN,
                                TestConstants.TEST_CASE_TYPE_ID_ENGLAND_WALES,
                                TestConstants.EXPECTED_QUERY_BY_ETHOS_CASE_REFERENCE))
            .thenReturn(SearchResult.builder().cases(new ArrayList<>()).build());
        when(ccdApi.searchCases(TestConstants.TEST_SERVICE_AUTH_TOKEN,
                                TestConstants.TEST_SERVICE_AUTH_TOKEN,
                                TestConstants.TEST_CASE_TYPE_ID_SCOTLAND,
                                TestConstants.EXPECTED_QUERY_BY_ETHOS_CASE_REFERENCE))
            .thenReturn(SearchResult.builder().cases(List.of(scotlandCaseDetails)).total(1).build());
        caseDetails = et3Service.findCaseByEthosCaseReference(TestConstants.TEST_ETHOS_CASE_REFERENCE);
        assertThat(caseDetails).isEqualTo(scotlandCaseDetails);
        when(ccdApi.searchCases(TestConstants.TEST_SERVICE_AUTH_TOKEN,
                                TestConstants.TEST_SERVICE_AUTH_TOKEN,
                                TestConstants.TEST_CASE_TYPE_ID_SCOTLAND,
                                TestConstants.EXPECTED_QUERY_BY_ETHOS_CASE_REFERENCE))
            .thenReturn(SearchResult.builder().cases(new ArrayList<>()).build());
        caseDetails = et3Service.findCaseByEthosCaseReference(TestConstants.TEST_ETHOS_CASE_REFERENCE);
        assertThat(caseDetails).isNull();
    }

    @Test
    void theFindCaseById() {
        CaseDetails englandWalesCaseDetails = CaseDetails.builder()
            .caseTypeId(
                TestConstants.TEST_CASE_TYPE_ID_ENGLAND_WALES)
            .id(Long.parseLong(
                TestConstants.TEST_CASE_SUBMISSION_REFERENCE1))
            .state(TestConstants.TEST_CASE_STATE_ACCEPTED)
            .build();
        CaseDetails scotlandCaseDetails = CaseDetails.builder()
            .caseTypeId(
                TestConstants.TEST_CASE_TYPE_ID_SCOTLAND)
            .id(Long.parseLong(
                TestConstants.TEST_CASE_SUBMISSION_REFERENCE1))
            .state(TestConstants.TEST_CASE_STATE_ACCEPTED)
            .build();
        when(cachedIdamService.getAdminUserToken()).thenReturn(TestConstants.TEST_SERVICE_AUTH_TOKEN);
        when(cachedIdamService.getAuthorisationToken()).thenReturn(TestConstants.TEST_SERVICE_AUTH_TOKEN);
        when(ccdApi.searchCases(TestConstants.TEST_SERVICE_AUTH_TOKEN,
                                TestConstants.TEST_SERVICE_AUTH_TOKEN,
                                TestConstants.TEST_CASE_TYPE_ID_ENGLAND_WALES,
                                TestConstants.EXPECTED_QUERY_BY_ID))
            .thenReturn(SearchResult.builder().cases(List.of(englandWalesCaseDetails)).total(1).build());
        CaseDetails caseDetails =
            et3Service.findCaseByIdAndAcceptedState(TestConstants.TEST_CASE_SUBMISSION_REFERENCE1);
        assertThat(caseDetails).isEqualTo(englandWalesCaseDetails);
        when(ccdApi.searchCases(TestConstants.TEST_SERVICE_AUTH_TOKEN,
                                TestConstants.TEST_SERVICE_AUTH_TOKEN,
                                TestConstants.TEST_CASE_TYPE_ID_ENGLAND_WALES,
                                TestConstants.EXPECTED_QUERY_BY_ID))
            .thenReturn(SearchResult.builder().cases(new ArrayList<>()).build());
        when(ccdApi.searchCases(TestConstants.TEST_SERVICE_AUTH_TOKEN,
                                TestConstants.TEST_SERVICE_AUTH_TOKEN,
                                TestConstants.TEST_CASE_TYPE_ID_SCOTLAND,
                                TestConstants.EXPECTED_QUERY_BY_ID))
            .thenReturn(SearchResult.builder().cases(List.of(scotlandCaseDetails)).total(1).build());
        caseDetails = et3Service.findCaseByIdAndAcceptedState(TestConstants.TEST_CASE_SUBMISSION_REFERENCE1);
        assertThat(caseDetails).isEqualTo(scotlandCaseDetails);
        when(ccdApi.searchCases(TestConstants.TEST_SERVICE_AUTH_TOKEN,
                                TestConstants.TEST_SERVICE_AUTH_TOKEN,
                                TestConstants.TEST_CASE_TYPE_ID_SCOTLAND,
                                TestConstants.EXPECTED_QUERY_BY_ID))
            .thenReturn(SearchResult.builder().cases(new ArrayList<>()).build());
        caseDetails = et3Service.findCaseByIdAndAcceptedState(TestConstants.TEST_CASE_SUBMISSION_REFERENCE1);
        assertThat(caseDetails).isNull();
    }
}
