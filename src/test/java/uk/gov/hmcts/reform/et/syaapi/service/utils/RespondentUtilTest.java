package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.exception.ET3Exception;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.EXCEPTION_RESPONDENT_COLLECTION_IS_EMPTY;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.EXCEPTION_RESPONDENT_NOT_FOUND;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.RespondentUtil.findRespondentSumTypeItemByIdamId;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.RespondentUtil.setRespondentIdamIdAndDefaultLinkStatuses;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_HASHMAP_RESPONDENT_SUM_TYPE_ITEM_ID_KEY;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_HASHMAP_RESPONDENT_SUM_TYPE_ITEM_ID_VALUE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_HASHMAP_RESPONDENT_SUM_TYPE_ITEM_VALUE_KEY;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_MODIFICATION_TYPE_ASSIGNMENT;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_MODIFICATION_TYPE_REVOKE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_RESPONDENT_COLLECTION_KEY;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_RESPONDENT_IDAM_ID_1;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_RESPONDENT_IDAM_ID_2;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_RESPONDENT_NAME;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_RESPONDENT_UTIL_EXCEPTION_CASE_DATA_NOT_FOUND;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_RESPONDENT_UTIL_EXCEPTION_EMPTY_RESPONDENT_COLLECTION;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_RESPONDENT_UTIL_EXCEPTION_IDAM_ID_ALREADY_EXISTS;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_RESPONDENT_UTIL_EXCEPTION_INVALID_IDAM_ID;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_RESPONDENT_UTIL_EXCEPTION_RESPONDENT_NOT_FOUND_WITH_RESPONDENT_NAME;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_RESPONDENT_UTIL_LINK_STATUS_CANNOT_START_YET;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_AVAILABLE_YET;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_STARTED_YET;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_VIEWED_YET;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_RESPONDENT_UTIL_LINK_STATUS_OPTIONAL;

class RespondentUtilTest {

    private static final String TEST_VALID_IDAM_ID = "1234567890";
    private static final String TEST_INVALID_IDAM_ID = "12345678901234567890";
    private static final String EXCEPTION_PREFIX = "java.lang.Exception: ";

    @ParameterizedTest
    @MethodSource("provideTheSetRespondentIdamIdAndDefaultLinkStatusesTestData")
    void theSetRespondentIdamIdAndDefaultLinkStatuses(CaseDetails caseDetails,
                                                      String respondentName,
                                                      String idamId,
                                                      String modificationType) {
        if (MapUtils.isEmpty(caseDetails.getData())) {
            assertThat(assertThrows(RuntimeException.class, () ->
                setRespondentIdamIdAndDefaultLinkStatuses(caseDetails,
                                                          respondentName,
                                                          idamId,
                                                          modificationType)).getMessage())
                .contains(TEST_RESPONDENT_UTIL_EXCEPTION_CASE_DATA_NOT_FOUND);
            return;
        }
        List<?> respondentCollection =
            (ArrayList<?>) caseDetails.getData().get(TEST_RESPONDENT_COLLECTION_KEY);
        if (hasRespondentCollectionException(respondentCollection,
                                             caseDetails,
                                             respondentName,
                                             idamId,
                                             modificationType)) {
            return;
        }
        CaseData caseData = EmployeeObjectMapper.mapRequestCaseDataToCaseData(caseDetails.getData());
        if (hasRespondentNameException(caseData, caseDetails, respondentName, idamId, modificationType)) {
            return;
        }
        if (hasIdamIdException(caseDetails, respondentName, idamId, caseData, modificationType)) {
            return;
        }
        setRespondentIdamIdAndDefaultLinkStatuses(caseDetails,
                                                  respondentName,
                                                  idamId,
                                                  modificationType);
        caseData = EmployeeObjectMapper.mapRequestCaseDataToCaseData(caseDetails.getData());
        if (TEST_MODIFICATION_TYPE_ASSIGNMENT.equals(modificationType)) {
            assertThat(caseData.getRespondentCollection().get(0).getValue().getIdamId()).isEqualTo(idamId);
        } else {
            assertThat(caseData.getRespondentCollection().get(0).getValue().getIdamId()).isEqualTo(StringUtils.EMPTY);
        }
        assertThat(caseData.getRespondentCollection().get(0).getValue().getEt3CaseDetailsLinksStatuses()
                       .getPersonalDetails()).isEqualTo(TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_AVAILABLE_YET);
        assertThat(caseData.getRespondentCollection().get(0).getValue().getEt3CaseDetailsLinksStatuses()
                       .getEt1ClaimForm()).isEqualTo(TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_VIEWED_YET);
        assertThat(caseData.getRespondentCollection().get(0).getValue().getEt3CaseDetailsLinksStatuses()
                       .getRespondentResponse()).isEqualTo(TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_STARTED_YET);
        assertThat(caseData.getRespondentCollection().get(0).getValue().getEt3CaseDetailsLinksStatuses()
                       .getHearingDetails()).isEqualTo(TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_AVAILABLE_YET);
        assertThat(caseData.getRespondentCollection().get(0).getValue().getEt3CaseDetailsLinksStatuses()
                       .getRespondentRequestsAndApplications())
            .isEqualTo(TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_AVAILABLE_YET);
        assertThat(caseData.getRespondentCollection().get(0).getValue().getEt3CaseDetailsLinksStatuses()
                       .getClaimantApplications()).isEqualTo(TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_AVAILABLE_YET);
        assertThat(caseData.getRespondentCollection().get(0).getValue().getEt3CaseDetailsLinksStatuses()
                       .getContactTribunal()).isEqualTo(TEST_RESPONDENT_UTIL_LINK_STATUS_OPTIONAL);
        assertThat(caseData.getRespondentCollection().get(0).getValue().getEt3CaseDetailsLinksStatuses()
                       .getTribunalOrders()).isEqualTo(TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_AVAILABLE_YET);
        assertThat(caseData.getRespondentCollection().get(0).getValue().getEt3CaseDetailsLinksStatuses()
                       .getTribunalJudgements()).isEqualTo(TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_AVAILABLE_YET);
        assertThat(caseData.getRespondentCollection().get(0).getValue().getEt3CaseDetailsLinksStatuses()
                       .getDocuments()).isEqualTo(TEST_RESPONDENT_UTIL_LINK_STATUS_OPTIONAL);
        assertThat(caseData.getRespondentCollection().get(0).getValue().getEt3HubLinksStatuses()
                       .getContactDetails()).isEqualTo(TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_STARTED_YET);
        assertThat(caseData.getRespondentCollection().get(0).getValue().getEt3HubLinksStatuses()
                       .getEmployerDetails()).isEqualTo(TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_STARTED_YET);
        assertThat(caseData.getRespondentCollection().get(0).getValue().getEt3HubLinksStatuses()
                       .getConciliationAndEmployeeDetails())
            .isEqualTo(TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_STARTED_YET);
        assertThat(caseData.getRespondentCollection().get(0).getValue().getEt3HubLinksStatuses()
                       .getPayPensionBenefitDetails()).isEqualTo(TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_STARTED_YET);
        assertThat(caseData.getRespondentCollection().get(0).getValue().getEt3HubLinksStatuses()
                       .getContestClaim()).isEqualTo(TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_STARTED_YET);
        assertThat(caseData.getRespondentCollection().get(0).getValue().getEt3HubLinksStatuses().getCheckYorAnswers())
            .isEqualTo(TEST_RESPONDENT_UTIL_LINK_STATUS_CANNOT_START_YET);
    }

    private static boolean hasRespondentCollectionException(List<?> respondentCollection,
                                                            CaseDetails caseDetails,
                                                            String respondentName,
                                                            String idamId,
                                                            String modificationType) {
        if (CollectionUtils.isEmpty(respondentCollection)) {
            assertThat(assertThrows(RuntimeException.class, () ->
                setRespondentIdamIdAndDefaultLinkStatuses(caseDetails,
                                                          respondentName,
                                                          idamId,
                                                          modificationType)).getMessage())
                .contains(TEST_RESPONDENT_UTIL_EXCEPTION_EMPTY_RESPONDENT_COLLECTION);
            return true;
        }

        if (ObjectUtils.isEmpty(respondentCollection.get(0))
            || ObjectUtils.isEmpty(((LinkedHashMap<?, ?>) respondentCollection.get(0))
                                       .get(TEST_HASHMAP_RESPONDENT_SUM_TYPE_ITEM_VALUE_KEY))
            || StringUtils.isBlank(respondentName)) {
            assertThat(assertThrows(RuntimeException.class, () ->
                setRespondentIdamIdAndDefaultLinkStatuses(caseDetails,
                                                          respondentName,
                                                          idamId,
                                                          modificationType)).getMessage())
                .contains(TEST_RESPONDENT_UTIL_EXCEPTION_RESPONDENT_NOT_FOUND_WITH_RESPONDENT_NAME);
            return true;
        }
        return false;
    }

    private static boolean hasRespondentNameException(CaseData caseData,
                                                      CaseDetails caseDetails,
                                                      String respondentName,
                                                      String idamId,
                                                      String modificationType) {
        if (!checkRespondentName(caseData.getRespondentCollection().get(0).getValue(), respondentName)) {
            assertThat(assertThrows(RuntimeException.class, () ->
                setRespondentIdamIdAndDefaultLinkStatuses(caseDetails,
                                                          respondentName,
                                                          idamId,
                                                          modificationType)).getMessage())
                .contains(TEST_RESPONDENT_UTIL_EXCEPTION_RESPONDENT_NOT_FOUND_WITH_RESPONDENT_NAME);
            return true;
        }
        return false;
    }

    private static boolean hasIdamIdException(CaseDetails caseDetails,
                                              String respondentName,
                                              String idamId,
                                              CaseData caseData,
                                              String modificationType) {
        if (StringUtils.isBlank(idamId)) {
            assertThat(assertThrows(RuntimeException.class, () ->
                setRespondentIdamIdAndDefaultLinkStatuses(caseDetails,
                                                          respondentName,
                                                          idamId,
                                                          modificationType)).getMessage())
                .contains(TEST_RESPONDENT_UTIL_EXCEPTION_INVALID_IDAM_ID);
            return true;
        }

        if (StringUtils.isNotBlank(caseData.getRespondentCollection().get(0).getValue().getIdamId())
            && !idamId.equals(caseData.getRespondentCollection().get(0).getValue().getIdamId())) {
            assertThat(assertThrows(RuntimeException.class, () ->
                setRespondentIdamIdAndDefaultLinkStatuses(caseDetails,
                                                          respondentName,
                                                          idamId,
                                                          modificationType)).getMessage())
                .contains(TEST_RESPONDENT_UTIL_EXCEPTION_IDAM_ID_ALREADY_EXISTS);
            return true;
        }
        return false;
    }

    private static boolean checkRespondentName(RespondentSumType respondentSumType, String respondentName) {
        if (respondentName.equals(respondentSumType.getRespondentName())
            || respondentName.equals(respondentSumType.getRespondentOrganisation())) {
            return true;
        }
        return respondentName.equals(generateRespondentNameByRespondentFirstNameAndLastName(
            respondentSumType.getRespondentFirstName(), respondentSumType.getRespondentLastName()));
    }

    private static String generateRespondentNameByRespondentFirstNameAndLastName(String respondentFirstName,
                                                                                 String respondentLastName) {
        if (StringUtils.isNotBlank(respondentFirstName) && StringUtils.isNotBlank(respondentLastName)) {
            return respondentFirstName + StringUtils.SPACE + respondentLastName;
        } else if (StringUtils.isNotBlank(respondentFirstName)) {
            return respondentFirstName;
        } else if (StringUtils.isNotBlank(respondentLastName)) {
            return respondentLastName;
        }
        return StringUtils.EMPTY;
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    private static Stream<Arguments> provideTheSetRespondentIdamIdAndDefaultLinkStatusesTestData() {
        CaseDetails caseDetailsWithEmptyRespondentCollection = new CaseTestData().getCaseDetailsWithCaseData();
        caseDetailsWithEmptyRespondentCollection.getData().put(TEST_RESPONDENT_COLLECTION_KEY, new ArrayList<>());

        CaseDetails caseDetailsWithEmptyRespondentSumTypeItem = new CaseTestData().getCaseDetailsWithCaseData();
        caseDetailsWithEmptyRespondentSumTypeItem.getData().put(TEST_RESPONDENT_COLLECTION_KEY, new ArrayList<>());
        ((ArrayList<?>)caseDetailsWithEmptyRespondentSumTypeItem.getData()
            .get(TEST_RESPONDENT_COLLECTION_KEY)).add(null);

        CaseDetails caseDetailsWithEmptyRespondentSumType = new CaseTestData().getCaseDetailsWithCaseData();
        caseDetailsWithEmptyRespondentSumType.getData().put(TEST_RESPONDENT_COLLECTION_KEY, new ArrayList<>());
        LinkedHashMap<String, Object> respondentSumTypeItemAsHashMap = new LinkedHashMap<>();
        respondentSumTypeItemAsHashMap.put(TEST_HASHMAP_RESPONDENT_SUM_TYPE_ITEM_VALUE_KEY, null);
        respondentSumTypeItemAsHashMap.put(TEST_HASHMAP_RESPONDENT_SUM_TYPE_ITEM_ID_KEY,
                                           TEST_HASHMAP_RESPONDENT_SUM_TYPE_ITEM_ID_VALUE);
        ((List)caseDetailsWithEmptyRespondentSumType.getData()
            .get(TEST_RESPONDENT_COLLECTION_KEY))
            .add(respondentSumTypeItemAsHashMap);

        CaseDetails caseDetails = new CaseTestData().getCaseDetailsWithCaseData();

        CaseDetails caseDetailsWithCorrectRespondentName = new CaseTestData().getCaseDetailsWithCaseData();
        CaseData caseData = EmployeeObjectMapper.mapRequestCaseDataToCaseData(caseDetails.getData());
        caseData.getRespondentCollection().get(0).getValue().setRespondentName(TEST_RESPONDENT_NAME);
        caseDetailsWithCorrectRespondentName.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData));

        CaseDetails caseDetailsWithCorrectOrganisationName = new CaseTestData().getCaseDetailsWithCaseData();
        caseData = EmployeeObjectMapper.mapRequestCaseDataToCaseData(caseDetails.getData());
        caseData.getRespondentCollection().get(0).getValue().setRespondentOrganisation(TEST_RESPONDENT_NAME);
        caseDetailsWithCorrectOrganisationName.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData));

        caseData = EmployeeObjectMapper.mapRequestCaseDataToCaseData(caseDetails.getData());
        caseData.getRespondentCollection().get(0).getValue().setRespondentFirstName(TEST_RESPONDENT_NAME);
        caseData.getRespondentCollection().get(0).getValue().setRespondentLastName(StringUtils.EMPTY);
        CaseDetails caseDetailsWithCorrectFirstName = new CaseTestData().getCaseDetailsWithCaseData();
        caseDetailsWithCorrectFirstName.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData));

        caseData = EmployeeObjectMapper.mapRequestCaseDataToCaseData(caseDetails.getData());
        caseData.getRespondentCollection().get(0).getValue().setRespondentLastName(TEST_RESPONDENT_NAME);
        caseData.getRespondentCollection().get(0).getValue().setRespondentFirstName(StringUtils.EMPTY);
        CaseDetails caseDetailsWithCorrectLastName = new CaseTestData().getCaseDetailsWithCaseData();
        caseDetailsWithCorrectLastName.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData));

        caseData = EmployeeObjectMapper.mapRequestCaseDataToCaseData(caseDetails.getData());
        caseData.getRespondentCollection().get(0).getValue().setRespondentFirstName("Respondent");
        caseData.getRespondentCollection().get(0).getValue().setRespondentLastName("Name");
        CaseDetails caseDetailsWithCorrectFirstAndLastName = new CaseTestData().getCaseDetailsWithCaseData();
        caseDetailsWithCorrectFirstAndLastName.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData));

        caseData = EmployeeObjectMapper.mapRequestCaseDataToCaseData(caseDetails.getData());
        caseData.getRespondentCollection().get(0).getValue().setRespondentName(TEST_RESPONDENT_NAME);
        caseData.getRespondentCollection().get(0).getValue().setRespondentName(TEST_RESPONDENT_IDAM_ID_2);
        CaseDetails caseDetailsWithDifferentIdamId = new CaseTestData().getCaseDetailsWithCaseData();
        caseDetailsWithCorrectRespondentName.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData));

        CaseDetails caseDetailsWithEmptyCaseData = CaseDetails.builder().data(new HashMap<>()).build();

        return Stream.of(Arguments.of(caseDetailsWithEmptyCaseData,
                                      TEST_RESPONDENT_NAME,
                                      TEST_RESPONDENT_IDAM_ID_1,
                                      TEST_MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(caseDetailsWithEmptyRespondentCollection,
                                      TEST_RESPONDENT_NAME,
                                      TEST_RESPONDENT_IDAM_ID_1,
                                      TEST_MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(caseDetailsWithEmptyRespondentSumTypeItem,
                                      TEST_RESPONDENT_NAME,
                                      TEST_RESPONDENT_IDAM_ID_1,
                                      TEST_MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(caseDetailsWithEmptyRespondentSumType,
                                      TEST_RESPONDENT_NAME,
                                      TEST_RESPONDENT_IDAM_ID_1,
                                      TEST_MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(caseDetails,
                                      StringUtils.EMPTY,
                                      TEST_RESPONDENT_IDAM_ID_1,
                                      TEST_MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(caseDetails,
                                      TEST_RESPONDENT_NAME,
                                      TEST_RESPONDENT_IDAM_ID_1,
                                      TEST_MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(caseDetailsWithCorrectRespondentName,
                                      TEST_RESPONDENT_NAME,
                                      TEST_RESPONDENT_IDAM_ID_1,
                                      TEST_MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(caseDetailsWithCorrectOrganisationName,
                                      TEST_RESPONDENT_NAME,
                                      TEST_RESPONDENT_IDAM_ID_1,
                                      TEST_MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(caseDetailsWithCorrectFirstName,
                                      TEST_RESPONDENT_NAME,
                                      TEST_RESPONDENT_IDAM_ID_1,
                                      TEST_MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(caseDetailsWithCorrectLastName,
                                      TEST_RESPONDENT_NAME,
                                      TEST_RESPONDENT_IDAM_ID_1,
                                      TEST_MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(caseDetailsWithCorrectFirstAndLastName,
                                      TEST_RESPONDENT_NAME,
                                      TEST_RESPONDENT_IDAM_ID_1,
                                      TEST_MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(caseDetailsWithDifferentIdamId,
                                      TEST_RESPONDENT_NAME,
                                      TEST_RESPONDENT_IDAM_ID_1,
                                      TEST_MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(caseDetailsWithCorrectRespondentName,
                                      TEST_RESPONDENT_NAME,
                                      StringUtils.EMPTY,
                                      TEST_MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(caseDetailsWithCorrectRespondentName,
                                      TEST_RESPONDENT_NAME,
                                      TEST_RESPONDENT_IDAM_ID_1,
                                      TEST_MODIFICATION_TYPE_REVOKE));

    }

    @ParameterizedTest
    @MethodSource("provideFindRespondentSumTypeItemByIdamIdTestData")
    void theFindRespondentSumTypeItemByIdamId(CaseData caseData, String idamId) {
        if (CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            assertThat(assertThrows(ET3Exception.class, () ->
                findRespondentSumTypeItemByIdamId(caseData, idamId))
                           .getMessage()).isEqualTo(EXCEPTION_PREFIX + EXCEPTION_RESPONDENT_COLLECTION_IS_EMPTY);
            return;
        }
        if (TEST_INVALID_IDAM_ID.equals(idamId)) {
            assertThat(assertThrows(ET3Exception.class, () ->
                findRespondentSumTypeItemByIdamId(caseData, idamId))
                           .getMessage()).isEqualTo(EXCEPTION_PREFIX + EXCEPTION_RESPONDENT_NOT_FOUND);
            return;
        }
        assertThat(findRespondentSumTypeItemByIdamId(caseData, idamId))
            .isEqualTo(caseData.getRespondentCollection().get(0));
    }

    private static Stream<Arguments> provideFindRespondentSumTypeItemByIdamIdTestData() {
        CaseData validCaseData = new CaseTestData().getCaseData();
        CaseData caseDataEmptyRespondentCollection = new CaseTestData().getCaseData();
        caseDataEmptyRespondentCollection.setRespondentCollection(null);
        return Stream.of(Arguments.of(validCaseData, TEST_VALID_IDAM_ID),
                         Arguments.of(validCaseData, TEST_INVALID_IDAM_ID),
                         Arguments.of(caseDataEmptyRespondentCollection, TEST_VALID_IDAM_ID));
    }

}
