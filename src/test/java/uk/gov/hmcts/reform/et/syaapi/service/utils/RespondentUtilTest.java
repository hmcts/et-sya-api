package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignedUserRolesResponse;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.exception.ManageCaseRoleException;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_CREATOR;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_RESPONDENT_NOT_FOUND_WITH_INDEX;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.RespondentUtil.setRespondentIdamIdAndDefaultLinkStatuses;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CASE_ID;

class RespondentUtilTest {

    private static final String EXCEPTION_INVALID_RESPONDENT_INDEX =
        "java.lang.Exception: Respondent index, %s is not valid for the case with id, %s";
    private static final String STRING_ZERO = "0";
    private static final String STRING_NINE = "9";
    private static final String STRING_MINUS_ONE = "-1";
    private static final String INVALID_INTEGER = "abc";
    private static final String EXCEPTION_INVALID_RESPONDENT_INDEX_WITH_CASE_ID =
        "java.lang.Exception: Respondent does not exist for case: %s";
    private static final String EXCEPTION_RESPONDENT_REPRESENTATIVE_NOT_FOUND =
        "java.lang.Exception: Respondent representative not found for case: %s";

    @ParameterizedTest
    @MethodSource("provideTheSetRespondentIdamIdAndDefaultLinkStatusesTestData")
    void theSetRespondentIdamIdAndDefaultLinkStatuses(CaseDetails caseDetails,
                                                      String respondentName,
                                                      String idamId,
                                                      String modificationType) {
        UserInfo userInfo = UserInfo.builder()
            .uid("123456789012345678901234567890")
            .givenName("First")
            .familyName("Last")
            .sub("test@email.com")
            .build();
        if (MapUtils.isEmpty(caseDetails.getData())) {
            assertThat(assertThrows(RuntimeException.class, () ->
                setRespondentIdamIdAndDefaultLinkStatuses(caseDetails,
                                                          respondentName,
                                                          idamId,
                                                          modificationType,
                                                          userInfo)).getMessage())
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
        boolean alreadyAssigned = hasIdamIdAlreadyAssigned(caseData, idamId, modificationType);
        if (hasIdamIdException(caseDetails, respondentName, idamId, caseData, modificationType, alreadyAssigned)) {
            return;
        }
        boolean result = setRespondentIdamIdAndDefaultLinkStatuses(caseDetails,
                                                                   respondentName,
                                                                   idamId,
                                                                   modificationType,
                                                                   userInfo);
        caseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(caseDetails.getData());
        if (alreadyAssigned) {
            assertThat(result).isTrue();
            // IDAM ID should remain unchanged when already assigned
            assertThat(caseData.getRespondentCollection().getFirst().getValue().getIdamId()).isEqualTo(idamId);
        } else if (TestConstants.TEST_MODIFICATION_TYPE_ASSIGNMENT.equals(modificationType)) {
            assertThat(result).isFalse();
            assertThat(caseData.getRespondentCollection().getFirst().getValue().getIdamId()).isEqualTo(idamId);
            assertThat(caseData.getRespondentCollection().getFirst().getValue().getResponseRespondentEmail())
                .isEqualTo("test@email.com");
        } else {
            assertThat(result).isFalse();
            assertThat(caseData.getRespondentCollection().getFirst().getValue().getIdamId())
                .isEqualTo(StringUtils.EMPTY);
        }
    }

    private static boolean hasRespondentCollectionException(List<?> respondentCollection,
                                                            CaseDetails caseDetails,
                                                            String respondentName,
                                                            String idamId,
                                                            String modificationType) {
        UserInfo userInfo = new CaseTestData().getUserInfo();
        if (CollectionUtils.isEmpty(respondentCollection)) {
            assertThat(assertThrows(RuntimeException.class, () ->
                setRespondentIdamIdAndDefaultLinkStatuses(caseDetails,
                                                          respondentName,
                                                          idamId,
                                                          modificationType,
                                                          userInfo)).getMessage())
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
                                                          modificationType,
                                                          userInfo)).getMessage())
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
        UserInfo userInfo = new CaseTestData().getUserInfo();
        if (!checkRespondentName(caseData.getRespondentCollection().getFirst().getValue(), respondentName)) {
            assertThat(assertThrows(RuntimeException.class, () ->
                setRespondentIdamIdAndDefaultLinkStatuses(caseDetails,
                                                          respondentName,
                                                          idamId,
                                                          modificationType,
                                                          userInfo)).getMessage())
                .contains(TestConstants.TEST_RESPONDENT_UTIL_EXCEPTION_RESPONDENT_NOT_FOUND_WITH_RESPONDENT_NAME);
            return true;
        }
        return false;
    }

    private static boolean hasIdamIdAlreadyAssigned(CaseData caseData,
                                                    String idamId,
                                                    String modificationType) {
        return TestConstants.TEST_MODIFICATION_TYPE_ASSIGNMENT.equals(modificationType)
            && StringUtils.isNotBlank(caseData.getRespondentCollection().getFirst().getValue().getIdamId())
            && idamId.equals(caseData.getRespondentCollection().getFirst().getValue().getIdamId());
    }

    private static boolean hasIdamIdException(CaseDetails caseDetails,
                                              String respondentName,
                                              String idamId,
                                              CaseData caseData,
                                              String modificationType,
                                              boolean alreadyAssigned) {
        UserInfo userInfo = new CaseTestData().getUserInfo();
        if (StringUtils.isBlank(idamId)) {
            assertThat(assertThrows(RuntimeException.class, () ->
                setRespondentIdamIdAndDefaultLinkStatuses(caseDetails,
                                                          respondentName,
                                                          idamId,
                                                          modificationType,
                                                          userInfo)).getMessage())
                .contains(TestConstants.TEST_RESPONDENT_UTIL_EXCEPTION_INVALID_IDAM_ID);
            return true;
        }

        // Only throw exception if IDAM ID is different (not the same user)
        if (!alreadyAssigned
            && StringUtils.isNotBlank(caseData.getRespondentCollection().getFirst().getValue().getIdamId())
            && !idamId.equals(caseData.getRespondentCollection().getFirst().getValue().getIdamId())) {
            assertThat(assertThrows(RuntimeException.class, () ->
                setRespondentIdamIdAndDefaultLinkStatuses(caseDetails,
                                                          respondentName,
                                                          idamId,
                                                          modificationType,
                                                          userInfo)).getMessage())
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

    @Test
    void theFindRespondentSumTypeItemByIndex() {
        // Test no respondents
        assertThrows(
            ManageCaseRoleException.class, () ->
                RespondentUtil.findRespondentSumTypeItemByIndex(null,
                                                                STRING_MINUS_ONE,
                                                                CASE_ID));
        // Setup valid list
        List<RespondentSumTypeItem> validList = new ArrayList<>();
        RespondentSumType validType0 = new RespondentSumType(); // assume default is non-empty
        RespondentSumType validType1 = new RespondentSumType();
        RespondentSumTypeItem validTypeItem0 = new RespondentSumTypeItem();
        validTypeItem0.setValue(validType0);
        RespondentSumTypeItem validTypeItem1 = new RespondentSumTypeItem();
        validTypeItem1.setValue(validType1);
        validList.add(validTypeItem0);
        validList.add(validTypeItem1);

        // Test valid index "0"
        RespondentSumTypeItem result0 = RespondentUtil
            .findRespondentSumTypeItemByIndex(validList, NumberUtils.INTEGER_ZERO.toString(), CASE_ID);
        assertThat(validType0).isEqualTo(result0.getValue());

        // Test valid index "1"
        RespondentSumTypeItem result1 = RespondentUtil
            .findRespondentSumTypeItemByIndex(validList, NumberUtils.INTEGER_ONE.toString(), CASE_ID);
        assertThat(validType1).isEqualTo(result1.getValue());

        // Test invalid: non-numeric input
        ManageCaseRoleException ex1 = assertThrows(ManageCaseRoleException.class, () ->
            RespondentUtil.findRespondentSumTypeItemByIndex(validList, INVALID_INTEGER, CASE_ID));
        assertThat(ex1.getMessage()).contains(String.format(EXCEPTION_INVALID_RESPONDENT_INDEX,
                                                            INVALID_INTEGER,
                                                            CASE_ID));

        // Test invalid: index out of bounds
        ManageCaseRoleException ex2 = assertThrows(ManageCaseRoleException.class, () ->
            RespondentUtil.findRespondentSumTypeItemByIndex(validList, STRING_NINE, CASE_ID));
        assertThat(ex2.getMessage()).contains(String.format(EXCEPTION_INVALID_RESPONDENT_INDEX,
                                                            STRING_NINE,
                                                            CASE_ID));

        // Test invalid: null list
        List<RespondentSumTypeItem> emptyList = new ArrayList<>();
        assertThrows(ManageCaseRoleException.class, () ->
            RespondentUtil.findRespondentSumTypeItemByIndex(emptyList, STRING_MINUS_ONE, CASE_ID));

        // Test invalid: null item in list
        List<RespondentSumTypeItem> listWithNull = new ArrayList<>();
        listWithNull.add(null);
        ManageCaseRoleException ex4 = assertThrows(ManageCaseRoleException.class, () ->
            RespondentUtil.findRespondentSumTypeItemByIndex(listWithNull, STRING_ZERO, CASE_ID));
        assertThat(ex4.getMessage()).contains(String.format(
            EXCEPTION_RESPONDENT_NOT_FOUND_WITH_INDEX,
            NumberUtils.INTEGER_ZERO));

        // Test invalid: item with null value
        List<RespondentSumTypeItem> listWithNullValue = new ArrayList<>();
        RespondentSumTypeItem itemWithNullValue = new RespondentSumTypeItem();
        itemWithNullValue.setValue(null);
        listWithNullValue.add(itemWithNullValue);
        ManageCaseRoleException ex5 = assertThrows(ManageCaseRoleException.class, () ->
            RespondentUtil.findRespondentSumTypeItemByIndex(listWithNullValue, STRING_MINUS_ONE, CASE_ID));
        assertThat(ex5.getMessage()).contains(String.format(EXCEPTION_INVALID_RESPONDENT_INDEX,
                                                            STRING_MINUS_ONE,
                                                            CASE_ID));
    }

    @Test
    void testFindRespondentRepresentative_AllScenarios() {
        String matchingRespondentId = "resp-001";

        // Set up a valid RespondentSumTypeItem
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        respondent.setId(matchingRespondentId);

        // Set up a matching representative
        RepresentedTypeR matchingRepValue = new RepresentedTypeR();
        matchingRepValue.setRespondentId(matchingRespondentId);
        RepresentedTypeRItem matchingRep = new RepresentedTypeRItem();
        matchingRep.setValue(matchingRepValue);

        // Non-matching representative
        RepresentedTypeR nonMatchingRepValue = new RepresentedTypeR();
        nonMatchingRepValue.setRespondentId("some-other-id");
        RepresentedTypeRItem nonMatchingRep = new RepresentedTypeRItem();
        nonMatchingRep.setValue(nonMatchingRepValue);

        // Valid list with match
        List<RepresentedTypeRItem> repListWithMatch = new ArrayList<>();
        repListWithMatch.add(nonMatchingRep);
        repListWithMatch.add(matchingRep);

        // Valid case: should return matchingRep
        String caseId = "12345";
        RepresentedTypeRItem result = RespondentUtil.findRespondentRepresentative(
            respondent, repListWithMatch, caseId);
        assertThat(matchingRep).isEqualTo(result);

        // Test null respondent
        ManageCaseRoleException ex1 = assertThrows(ManageCaseRoleException.class, () ->
            RespondentUtil.findRespondentRepresentative(null, repListWithMatch, caseId));
        assertThat(ex1.getMessage()).isEqualTo(String.format(EXCEPTION_INVALID_RESPONDENT_INDEX_WITH_CASE_ID, caseId));

        // Test empty representative list
        List<RepresentedTypeRItem> emptyRepresentativeList = new ArrayList<>();
        ManageCaseRoleException ex2 = assertThrows(ManageCaseRoleException.class, () ->
            RespondentUtil.findRespondentRepresentative(respondent, emptyRepresentativeList, caseId));
        assertThat(ex2.getMessage()).isEqualTo(String.format(EXCEPTION_RESPONDENT_REPRESENTATIVE_NOT_FOUND, caseId));

        // Test no match
        List<RepresentedTypeRItem> noMatchList = new ArrayList<>();
        noMatchList.add(nonMatchingRep);
        ManageCaseRoleException ex3 = assertThrows(ManageCaseRoleException.class, () ->
            RespondentUtil.findRespondentRepresentative(respondent, noMatchList, caseId));
        assertThat(ex3.getMessage()).isEqualTo(String.format(EXCEPTION_RESPONDENT_REPRESENTATIVE_NOT_FOUND, caseId));

        // Test representative with null value
        RepresentedTypeRItem nullValueRep = new RepresentedTypeRItem();
        nullValueRep.setValue(null);
        List<RepresentedTypeRItem> nullValueList = new ArrayList<>();
        nullValueList.add(nullValueRep);
        ManageCaseRoleException ex4 = assertThrows(ManageCaseRoleException.class, () ->
            RespondentUtil.findRespondentRepresentative(respondent, nullValueList, caseId));
        assertThat(ex4.getMessage()).isEqualTo(String.format(EXCEPTION_RESPONDENT_REPRESENTATIVE_NOT_FOUND, caseId));
    }
}
