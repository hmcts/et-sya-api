package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignedUserRolesResponse;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_CREATOR;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.RespondentUtil.setRespondentIdamIdAndDefaultLinkStatuses;

class RespondentUtilTest {

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
                .contains(TestConstants.TEST_RESPONDENT_UTIL_EXCEPTION_CASE_DATA_NOT_FOUND);
            return;
        }
        List<?> respondentCollection =
            (ArrayList<?>) caseDetails.getData().get(TestConstants.TEST_RESPONDENT_COLLECTION_KEY);
        if (hasRespondentCollectionException(respondentCollection,
                                             caseDetails,
                                             respondentName,
                                             idamId,
                                             modificationType)) {
            return;
        }
        CaseData caseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(caseDetails.getData());
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
        caseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(caseDetails.getData());
        if (TestConstants.TEST_MODIFICATION_TYPE_ASSIGNMENT.equals(modificationType)) {
            assertThat(caseData.getRespondentCollection().getFirst().getValue().getIdamId()).isEqualTo(idamId);
        } else {
            assertThat(caseData.getRespondentCollection().getFirst().getValue().getIdamId())
                .isEqualTo(StringUtils.EMPTY);
        }
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getEt3CaseDetailsLinksStatuses()
                       .getPersonalDetails())
            .isEqualTo(TestConstants.TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_AVAILABLE_YET);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getEt3CaseDetailsLinksStatuses()
                       .getEt1ClaimForm()).isEqualTo(TestConstants.TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_VIEWED_YET);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getEt3CaseDetailsLinksStatuses()
                       .getRespondentResponse())
            .isEqualTo(TestConstants.TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_STARTED_YET);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getEt3CaseDetailsLinksStatuses()
                       .getHearingDetails()).isEqualTo(
                           TestConstants.TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_AVAILABLE_YET);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getEt3CaseDetailsLinksStatuses()
                       .getRespondentRequestsAndApplications())
            .isEqualTo(TestConstants.TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_AVAILABLE_YET);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getEt3CaseDetailsLinksStatuses()
                       .getClaimantApplications()).isEqualTo(
                           TestConstants.TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_AVAILABLE_YET);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getEt3CaseDetailsLinksStatuses()
                       .getContactTribunal()).isEqualTo(TestConstants.TEST_RESPONDENT_UTIL_LINK_STATUS_OPTIONAL);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getEt3CaseDetailsLinksStatuses()
                       .getTribunalOrders())
            .isEqualTo(TestConstants.TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_AVAILABLE_YET);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getEt3CaseDetailsLinksStatuses()
                       .getTribunalJudgements()).isEqualTo(
                           TestConstants.TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_AVAILABLE_YET);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getEt3CaseDetailsLinksStatuses()
                       .getDocuments()).isEqualTo(TestConstants.TEST_RESPONDENT_UTIL_LINK_STATUS_OPTIONAL);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getEt3HubLinksStatuses()
                       .getContactDetails()).isEqualTo(TestConstants.TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_STARTED_YET);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getEt3HubLinksStatuses()
                       .getEmployerDetails()).isEqualTo(TestConstants.TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_STARTED_YET);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getEt3HubLinksStatuses()
                       .getConciliationAndEmployeeDetails())
            .isEqualTo(TestConstants.TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_STARTED_YET);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getEt3HubLinksStatuses()
                       .getPayPensionBenefitDetails())
            .isEqualTo(TestConstants.TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_STARTED_YET);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getEt3HubLinksStatuses()
                       .getContestClaim()).isEqualTo(TestConstants.TEST_RESPONDENT_UTIL_LINK_STATUS_NOT_STARTED_YET);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getEt3HubLinksStatuses()
                       .getCheckYorAnswers())
            .isEqualTo(TestConstants.TEST_RESPONDENT_UTIL_LINK_STATUS_CANNOT_START_YET);
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
                .contains(TestConstants.TEST_RESPONDENT_UTIL_EXCEPTION_EMPTY_RESPONDENT_COLLECTION);
            return true;
        }

        if (ObjectUtils.isEmpty(respondentCollection.getFirst())
            || ObjectUtils.isEmpty(((LinkedHashMap<?, ?>) respondentCollection.getFirst())
                                       .get(TestConstants.TEST_HASHMAP_RESPONDENT_SUM_TYPE_ITEM_VALUE_KEY))
            || StringUtils.isBlank(respondentName)) {
            assertThat(assertThrows(RuntimeException.class, () ->
                setRespondentIdamIdAndDefaultLinkStatuses(caseDetails,
                                                          respondentName,
                                                          idamId,
                                                          modificationType)).getMessage())
                .contains(TestConstants.TEST_RESPONDENT_UTIL_EXCEPTION_RESPONDENT_NOT_FOUND_WITH_RESPONDENT_NAME);
            return true;
        }
        return false;
    }

    private static boolean hasRespondentNameException(CaseData caseData,
                                                      CaseDetails caseDetails,
                                                      String respondentName,
                                                      String idamId,
                                                      String modificationType) {
        if (!checkRespondentName(caseData.getRespondentCollection().getFirst().getValue(), respondentName)) {
            assertThat(assertThrows(RuntimeException.class, () ->
                setRespondentIdamIdAndDefaultLinkStatuses(caseDetails,
                                                          respondentName,
                                                          idamId,
                                                          modificationType)).getMessage())
                .contains(TestConstants.TEST_RESPONDENT_UTIL_EXCEPTION_RESPONDENT_NOT_FOUND_WITH_RESPONDENT_NAME);
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
                .contains(TestConstants.TEST_RESPONDENT_UTIL_EXCEPTION_INVALID_IDAM_ID);
            return true;
        }

        if (StringUtils.isNotBlank(caseData.getRespondentCollection().getFirst().getValue().getIdamId())
            && !idamId.equals(caseData.getRespondentCollection().getFirst().getValue().getIdamId())) {
            assertThat(assertThrows(RuntimeException.class, () ->
                setRespondentIdamIdAndDefaultLinkStatuses(caseDetails,
                                                          respondentName,
                                                          idamId,
                                                          modificationType)).getMessage())
                .contains(TestConstants.TEST_RESPONDENT_UTIL_EXCEPTION_IDAM_ID_ALREADY_EXISTS);
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
        caseDetailsWithEmptyRespondentCollection.getData()
            .put(TestConstants.TEST_RESPONDENT_COLLECTION_KEY, new ArrayList<>());

        CaseDetails caseDetailsWithEmptyRespondentSumTypeItem = new CaseTestData().getCaseDetailsWithCaseData();
        caseDetailsWithEmptyRespondentSumTypeItem.getData()
            .put(TestConstants.TEST_RESPONDENT_COLLECTION_KEY, new ArrayList<>());
        ((ArrayList<?>)caseDetailsWithEmptyRespondentSumTypeItem.getData()
            .get(TestConstants.TEST_RESPONDENT_COLLECTION_KEY)).add(null);

        CaseDetails caseDetailsWithEmptyRespondentSumType = new CaseTestData().getCaseDetailsWithCaseData();
        caseDetailsWithEmptyRespondentSumType.getData()
            .put(TestConstants.TEST_RESPONDENT_COLLECTION_KEY, new ArrayList<>());
        LinkedHashMap<String, Object> respondentSumTypeItemAsHashMap = new LinkedHashMap<>();
        respondentSumTypeItemAsHashMap.put(TestConstants.TEST_HASHMAP_RESPONDENT_SUM_TYPE_ITEM_VALUE_KEY, null);
        respondentSumTypeItemAsHashMap.put(TestConstants.TEST_HASHMAP_RESPONDENT_SUM_TYPE_ITEM_ID_KEY,
                                           TestConstants.TEST_HASHMAP_RESPONDENT_SUM_TYPE_ITEM_ID_VALUE);
        ((List)caseDetailsWithEmptyRespondentSumType.getData()
            .get(TestConstants.TEST_RESPONDENT_COLLECTION_KEY))
            .add(respondentSumTypeItemAsHashMap);

        CaseDetails caseDetails = new CaseTestData().getCaseDetailsWithCaseData();

        CaseDetails caseDetailsWithCorrectRespondentName = new CaseTestData().getCaseDetailsWithCaseData();
        CaseData caseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(caseDetails.getData());
        caseData.getRespondentCollection().getFirst().getValue().setRespondentName(TestConstants.TEST_RESPONDENT_NAME);
        caseDetailsWithCorrectRespondentName.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData));

        CaseDetails caseDetailsWithCorrectOrganisationName = new CaseTestData().getCaseDetailsWithCaseData();
        caseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(caseDetails.getData());
        caseData.getRespondentCollection().getFirst().getValue()
            .setRespondentOrganisation(TestConstants.TEST_RESPONDENT_NAME);
        caseDetailsWithCorrectOrganisationName.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData));

        caseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(caseDetails.getData());
        caseData.getRespondentCollection().getFirst().getValue()
            .setRespondentFirstName(TestConstants.TEST_RESPONDENT_NAME);
        caseData.getRespondentCollection().getFirst().getValue().setRespondentLastName(StringUtils.EMPTY);
        CaseDetails caseDetailsWithCorrectFirstName = new CaseTestData().getCaseDetailsWithCaseData();
        caseDetailsWithCorrectFirstName.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData));

        caseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(caseDetails.getData());
        caseData.getRespondentCollection().getFirst().getValue()
            .setRespondentLastName(TestConstants.TEST_RESPONDENT_NAME);
        caseData.getRespondentCollection().getFirst().getValue().setRespondentFirstName(StringUtils.EMPTY);
        CaseDetails caseDetailsWithCorrectLastName = new CaseTestData().getCaseDetailsWithCaseData();
        caseDetailsWithCorrectLastName.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData));

        caseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(caseDetails.getData());
        caseData.getRespondentCollection().getFirst().getValue().setRespondentFirstName("Respondent");
        caseData.getRespondentCollection().getFirst().getValue().setRespondentLastName("Name");
        CaseDetails caseDetailsWithCorrectFirstAndLastName = new CaseTestData().getCaseDetailsWithCaseData();
        caseDetailsWithCorrectFirstAndLastName.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData));

        caseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(caseDetails.getData());
        caseData.getRespondentCollection().getFirst().getValue().setRespondentName(TestConstants.TEST_RESPONDENT_NAME);
        caseData.getRespondentCollection().getFirst().getValue()
            .setRespondentName(TestConstants.TEST_RESPONDENT_IDAM_ID_2);
        CaseDetails caseDetailsWithDifferentIdamId = new CaseTestData().getCaseDetailsWithCaseData();
        caseDetailsWithCorrectRespondentName.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData));

        CaseDetails caseDetailsWithEmptyCaseData = CaseDetails.builder().data(new HashMap<>()).build();

        return Stream.of(Arguments.of(caseDetailsWithEmptyCaseData,
                                      TestConstants.TEST_RESPONDENT_NAME,
                                      TestConstants.TEST_RESPONDENT_IDAM_ID_1,
                                      TestConstants.TEST_MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(caseDetailsWithEmptyRespondentCollection,
                                      TestConstants.TEST_RESPONDENT_NAME,
                                      TestConstants.TEST_RESPONDENT_IDAM_ID_1,
                                      TestConstants.TEST_MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(caseDetailsWithEmptyRespondentSumTypeItem,
                                      TestConstants.TEST_RESPONDENT_NAME,
                                      TestConstants.TEST_RESPONDENT_IDAM_ID_1,
                                      TestConstants.TEST_MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(caseDetailsWithEmptyRespondentSumType,
                                      TestConstants.TEST_RESPONDENT_NAME,
                                      TestConstants.TEST_RESPONDENT_IDAM_ID_1,
                                      TestConstants.TEST_MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(caseDetails,
                                      StringUtils.EMPTY,
                                      TestConstants.TEST_RESPONDENT_IDAM_ID_1,
                                      TestConstants.TEST_MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(caseDetails,
                                      TestConstants.TEST_RESPONDENT_NAME,
                                      TestConstants.TEST_RESPONDENT_IDAM_ID_1,
                                      TestConstants.TEST_MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(caseDetailsWithCorrectRespondentName,
                                      TestConstants.TEST_RESPONDENT_NAME,
                                      TestConstants.TEST_RESPONDENT_IDAM_ID_1,
                                      TestConstants.TEST_MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(caseDetailsWithCorrectOrganisationName,
                                      TestConstants.TEST_RESPONDENT_NAME,
                                      TestConstants.TEST_RESPONDENT_IDAM_ID_1,
                                      TestConstants.TEST_MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(caseDetailsWithCorrectFirstName,
                                      TestConstants.TEST_RESPONDENT_NAME,
                                      TestConstants.TEST_RESPONDENT_IDAM_ID_1,
                                      TestConstants.TEST_MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(caseDetailsWithCorrectLastName,
                                      TestConstants.TEST_RESPONDENT_NAME,
                                      TestConstants.TEST_RESPONDENT_IDAM_ID_1,
                                      TestConstants.TEST_MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(caseDetailsWithCorrectFirstAndLastName,
                                      TestConstants.TEST_RESPONDENT_NAME,
                                      TestConstants.TEST_RESPONDENT_IDAM_ID_1,
                                      TestConstants.TEST_MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(caseDetailsWithDifferentIdamId,
                                      TestConstants.TEST_RESPONDENT_NAME,
                                      TestConstants.TEST_RESPONDENT_IDAM_ID_1,
                                      TestConstants.TEST_MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(caseDetailsWithCorrectRespondentName,
                                      TestConstants.TEST_RESPONDENT_NAME,
                                      StringUtils.EMPTY,
                                      TestConstants.TEST_MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(caseDetailsWithCorrectRespondentName,
                                      TestConstants.TEST_RESPONDENT_NAME,
                                      TestConstants.TEST_RESPONDENT_IDAM_ID_1,
                                      TestConstants.TEST_MODIFICATION_TYPE_REVOKE));

    }

    @ParameterizedTest
    @MethodSource("provideTheCheckIsUserCreatorTestData")
    void theCheckIsUserCreator(CaseAssignedUserRolesResponse caseAssignedUserRolesResponse) {
        if (ObjectUtils.isNotEmpty(caseAssignedUserRolesResponse)
            && CollectionUtils.isNotEmpty(caseAssignedUserRolesResponse.getCaseAssignedUserRoles())
            && CASE_USER_ROLE_CREATOR.equals(caseAssignedUserRolesResponse
                                                 .getCaseAssignedUserRoles().getFirst().getCaseRole())) {
            assertThat(RespondentUtil.checkIsUserCreator(caseAssignedUserRolesResponse)).isTrue();
            return;
        }
        assertThat(RespondentUtil.checkIsUserCreator(caseAssignedUserRolesResponse)).isFalse();
    }

    private static Stream<CaseAssignedUserRolesResponse> provideTheCheckIsUserCreatorTestData() {
        CaseAssignedUserRolesResponse emptyCaseAssignedUserRolesResponse =
            CaseAssignedUserRolesResponse.builder().build();
        CaseAssignedUserRolesResponse nullCaseAssignedUserRoles =
            CaseAssignedUserRolesResponse.builder().caseAssignedUserRoles(null).build();
        CaseAssignedUserRolesResponse emptyCaseAssignedUserRoles =
            CaseAssignedUserRolesResponse.builder().caseAssignedUserRoles(new ArrayList<>()).build();
        CaseAssignedUserRolesResponse creatorCaseAssignedUserRoles =
            CaseAssignedUserRolesResponse.builder().caseAssignedUserRoles(
                List.of(CaseAssignmentUserRole.builder()
                            .caseRole(TestConstants.TEST_CASE_USER_ROLE_CREATOR).build())).build();
        CaseAssignedUserRolesResponse defendantCaseAssignedUserRoles =
            CaseAssignedUserRolesResponse.builder().caseAssignedUserRoles(
                List.of(CaseAssignmentUserRole.builder()
                            .caseRole(TestConstants.TEST_CASE_USER_ROLE_DEFENDANT).build())).build();
        return Stream.of(null,
                         emptyCaseAssignedUserRolesResponse,
                         nullCaseAssignedUserRoles,
                         emptyCaseAssignedUserRoles,
                         creatorCaseAssignedUserRoles,
                         defendantCaseAssignedUserRoles);
    }
}
