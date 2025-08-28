package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.Et3Request;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.et3links.ET3HubLinksStatuses;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants;
import uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants;
import uk.gov.hmcts.reform.et.syaapi.exception.ET3Exception;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.MODIFICATION_TYPE_ASSIGNMENT;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.MODIFICATION_TYPE_SUBMIT;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.MODIFICATION_TYPE_UPDATE;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.EXCEPTION_AUTHORISATION_TOKEN_BLANK;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.EXCEPTION_ET3_CASE_TYPE_BLANK;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.EXCEPTION_ET3_REQUEST_EMPTY;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.EXCEPTION_ET3_REQUEST_TYPE_BLANK;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.EXCEPTION_ET3_RESPONDENT_EMPTY;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.EXCEPTION_ET3_RESPONDENT_IDAM_ID_IS_BLANK;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.EXCEPTION_ET3_SUBMISSION_REFERENCE_BLANK;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.EXCEPTION_RESPONDENT_COLLECTION_IS_EMPTY;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.EXCEPTION_RESPONDENT_NOT_FOUND;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.SUBMIT_ET3_FORM;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.UPDATE_ET3_FORM;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.ResponseUtil.findSelectedRespondentByRespondentSumTypeItem;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

class ResponseUtilTest {

    private static final String EXCEPTION_PREFIX = "java.lang.Exception: ";
    private static final String TEST_VALID_IDAM_ID = "12345";
    private static final String TEST_INVALID_IDAM_ID = "12345678901234567890";
    private static final String TEST_INVALID_RESPONDENT_CCD_ID = "0";
    private static final String TEST_VALID_RESPONDENT_CCD_ID = "123456789";

    @ParameterizedTest
    @MethodSource("provideCheckModifyEt3DataParametersTestData")
    void theCheckModifyEt3DataParameters(String authorisation, Et3Request et3Request) {
        if (StringUtils.isBlank(authorisation)) {
            assertThat(assertThrows(ET3Exception.class, () ->
                ResponseUtil.checkModifyEt3DataParameters(authorisation, et3Request))
                           .getMessage()).isEqualTo(EXCEPTION_PREFIX + EXCEPTION_AUTHORISATION_TOKEN_BLANK);
            return;
        }
        if (ObjectUtils.isEmpty(et3Request)) {
            assertThat(assertThrows(ET3Exception.class, () ->
                ResponseUtil.checkModifyEt3DataParameters(authorisation, et3Request))
                           .getMessage()).isEqualTo(EXCEPTION_PREFIX + EXCEPTION_ET3_REQUEST_EMPTY);
            return;
        }
        if (StringUtils.isBlank(et3Request.getCaseSubmissionReference())) {
            assertThat(assertThrows(ET3Exception.class, () ->
                ResponseUtil.checkModifyEt3DataParameters(authorisation, et3Request))
                           .getMessage()).isEqualTo(
                               EXCEPTION_PREFIX + EXCEPTION_ET3_SUBMISSION_REFERENCE_BLANK);
            return;
        }
        if (StringUtils.isBlank(et3Request.getCaseTypeId())) {
            assertThat(assertThrows(ET3Exception.class, () ->
                ResponseUtil.checkModifyEt3DataParameters(authorisation, et3Request))
                           .getMessage()).isEqualTo(EXCEPTION_PREFIX + EXCEPTION_ET3_CASE_TYPE_BLANK);
            return;
        }
        if (StringUtils.isBlank(et3Request.getRequestType())) {
            assertThat(assertThrows(ET3Exception.class, () ->
                ResponseUtil.checkModifyEt3DataParameters(authorisation, et3Request))
                           .getMessage()).isEqualTo(EXCEPTION_PREFIX + EXCEPTION_ET3_REQUEST_TYPE_BLANK);
            return;
        }
        if (ObjectUtils.isEmpty(et3Request.getRespondent())) {
            assertThat(assertThrows(ET3Exception.class, () ->
                ResponseUtil.checkModifyEt3DataParameters(authorisation, et3Request))
                           .getMessage()).isEqualTo(EXCEPTION_PREFIX + EXCEPTION_ET3_RESPONDENT_EMPTY);
            return;
        }
        if (StringUtils.isBlank(et3Request.getRespondent().getValue().getIdamId())) {
            assertThat(assertThrows(ET3Exception.class, () ->
                ResponseUtil.checkModifyEt3DataParameters(authorisation, et3Request))
                           .getMessage()).isEqualTo(EXCEPTION_PREFIX + EXCEPTION_ET3_RESPONDENT_IDAM_ID_IS_BLANK);
            return;
        }
        assertDoesNotThrow(() -> ResponseUtil.checkModifyEt3DataParameters(authorisation, et3Request));
    }

    private static Stream<Arguments> provideCheckModifyEt3DataParametersTestData() {
        Et3Request et3RequestBlankSubmissionReference = new CaseTestData().getEt3Request();
        et3RequestBlankSubmissionReference.setCaseSubmissionReference(StringUtils.SPACE);
        Et3Request et3RequestBlankCaseTypeId = new CaseTestData().getEt3Request();
        et3RequestBlankCaseTypeId.setCaseTypeId(StringUtils.SPACE);
        Et3Request et3RequestBlankRequestType = new CaseTestData().getEt3Request();
        et3RequestBlankRequestType.setRequestType(StringUtils.SPACE);
        Et3Request et3RequestNullRespondent = new CaseTestData().getEt3Request();
        et3RequestNullRespondent.setRespondent(null);
        Et3Request et3RequestBlankIdamId = new CaseTestData().getEt3Request();
        et3RequestBlankIdamId.getRespondent().getValue().setIdamId(StringUtils.SPACE);
        Et3Request validEt3Request = new CaseTestData().getEt3Request();
        return Stream.of(Arguments.of(null, validEt3Request),
                         Arguments.of(StringUtils.EMPTY, validEt3Request),
                         Arguments.of(StringUtils.SPACE, validEt3Request),
                         Arguments.of(TEST_SERVICE_AUTH_TOKEN, et3RequestBlankSubmissionReference),
                         Arguments.of(TEST_SERVICE_AUTH_TOKEN, et3RequestBlankCaseTypeId),
                         Arguments.of(TEST_SERVICE_AUTH_TOKEN, et3RequestBlankRequestType),
                         Arguments.of(TEST_SERVICE_AUTH_TOKEN, et3RequestNullRespondent),
                         Arguments.of(TEST_SERVICE_AUTH_TOKEN, et3RequestBlankIdamId));
    }

    @ParameterizedTest
    @MethodSource("provideFindSelectedRespondentByRespondentSumTypeItem")
    void theFindSelectedRespondentByRespondentSumTypeItem(CaseData caseData,
                                                          RespondentSumTypeItem respondentSumTypeItem) {
        if (CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            assertThat(assertThrows(ET3Exception.class, () ->
                findSelectedRespondentByRespondentSumTypeItem(caseData, respondentSumTypeItem))
                           .getMessage()).isEqualTo(EXCEPTION_PREFIX + EXCEPTION_RESPONDENT_COLLECTION_IS_EMPTY);
            return;
        }
        if (TEST_INVALID_IDAM_ID.equals(respondentSumTypeItem.getValue().getIdamId())) {
            assertThat(assertThrows(ET3Exception.class, () ->
                findSelectedRespondentByRespondentSumTypeItem(caseData, respondentSumTypeItem))
                           .getMessage()).isEqualTo(EXCEPTION_PREFIX + EXCEPTION_RESPONDENT_NOT_FOUND);
            return;
        }
        assertThat(findSelectedRespondentByRespondentSumTypeItem(caseData, respondentSumTypeItem))
            .isEqualTo(caseData.getRespondentCollection().get(0));
    }

    private static Stream<Arguments> provideFindSelectedRespondentByRespondentSumTypeItem() {
        RespondentSumTypeItem respondentSumTypeItemValid = new CaseTestData().getEt3Request().getRespondent();
        respondentSumTypeItemValid.setId(TEST_VALID_RESPONDENT_CCD_ID);
        respondentSumTypeItemValid.getValue().setIdamId(TEST_VALID_IDAM_ID);

        RespondentSumTypeItem respondentSumTypeItemInValidIdamId = new CaseTestData().getEt3Request().getRespondent();
        respondentSumTypeItemInValidIdamId.setId(TEST_VALID_RESPONDENT_CCD_ID);
        respondentSumTypeItemInValidIdamId.getValue().setIdamId(TEST_INVALID_IDAM_ID);

        RespondentSumTypeItem respondentSumTypeItemInvalidCcdId = new CaseTestData().getEt3Request().getRespondent();
        respondentSumTypeItemInvalidCcdId.setId(TEST_INVALID_RESPONDENT_CCD_ID);
        respondentSumTypeItemInvalidCcdId.getValue().setIdamId(TEST_VALID_IDAM_ID);

        CaseData caseDataValid = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(
            new CaseTestData().getCaseDetailsWithCaseData().getData());
        caseDataValid.getRespondentCollection().get(0).setValue(respondentSumTypeItemValid.getValue());
        caseDataValid.getRespondentCollection().get(0).setId(respondentSumTypeItemInValidIdamId.getId());
        CaseData caseDataEmptyRespondentCollection = new CaseTestData().getCaseData();
        caseDataEmptyRespondentCollection.setRespondentCollection(null);
        return Stream.of(Arguments.of(
            caseDataEmptyRespondentCollection, respondentSumTypeItemValid,
            caseDataValid, respondentSumTypeItemInValidIdamId,
            caseDataValid, respondentSumTypeItemValid,
            caseDataValid, respondentSumTypeItemInvalidCcdId));
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {MODIFICATION_TYPE_SUBMIT, MODIFICATION_TYPE_ASSIGNMENT, MODIFICATION_TYPE_UPDATE})
    void theFindCaseEventByRequestType(String requestType) {
        if (MODIFICATION_TYPE_SUBMIT.equals(requestType)) {
            assertThat(ResponseUtil.findCaseEventByRequestType(requestType)).isEqualTo(SUBMIT_ET3_FORM);
            return;
        }
        assertThat(ResponseUtil.findCaseEventByRequestType(requestType)).isEqualTo(UPDATE_ET3_FORM);
    }

    @Test
    void theGetCaseDetailsByStartEventResponse() {
        StartEventResponse startEventResponse = new CaseTestData().getStartEventResponse();
        assertThat(ResponseUtil.getCaseDetailsByStartEventResponse(startEventResponse))
            .isEqualTo(startEventResponse.getCaseDetails());
        assertThrows(ET3Exception.class, () -> ResponseUtil.getCaseDetailsByStartEventResponse(null));
        assertThrows(ET3Exception.class, () -> ResponseUtil.getCaseDetailsByStartEventResponse(
            StartEventResponse.builder().build()));
    }

    @ParameterizedTest
    @MethodSource("provideSetET3SubmitValuesTestData")
    void setET3SubmitValuesUpdatesFieldsCorrectly(RespondentSumType respondentSumType,
                                                  String expectedResponseReceivedCount) {
        ResponseUtil.setET3SubmitValues(respondentSumType);

        assertThat(respondentSumType.getEt3Status()).isEqualTo(ManageCaseRoleConstants.RESPONSE_STATUS_COMPLETED);
        assertThat(respondentSumType.getEt3HubLinksStatuses().getCheckYorAnswers())
            .isEqualTo(ManageCaseRoleConstants.RESPONSE_STATUS_COMPLETED);
        assertThat(respondentSumType.getResponseReceivedDate()).isEqualTo(LocalDate.now().toString());
        assertThat(respondentSumType.getResponseReceivedCount()).isEqualTo(expectedResponseReceivedCount);
    }

    private static Stream<Arguments> provideSetET3SubmitValuesTestData() {
        RespondentSumType respondentWithNullCount = new RespondentSumType();
        respondentWithNullCount.setEt3HubLinksStatuses(new ET3HubLinksStatuses());
        respondentWithNullCount.setResponseReceived(EtSyaConstants.YES);
        respondentWithNullCount.setResponseReceivedCount(null);

        RespondentSumType respondentWithExistingCount = new RespondentSumType();
        respondentWithExistingCount.setEt3HubLinksStatuses(new ET3HubLinksStatuses());
        respondentWithExistingCount.setResponseReceivedCount("2");

        return Stream.of(
            Arguments.of(respondentWithNullCount, "1"),
            Arguments.of(respondentWithExistingCount, "3")
        );
    }
}
