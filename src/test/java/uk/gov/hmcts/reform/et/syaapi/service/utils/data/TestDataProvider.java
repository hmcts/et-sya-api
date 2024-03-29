package uk.gov.hmcts.reform.et.syaapi.service.utils.data;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantWorkAddressType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.reform.et.syaapi.models.CaseDocument;
import uk.gov.hmcts.reform.et.syaapi.models.GenericTseApplication;
import uk.gov.hmcts.reform.et.syaapi.service.utils.ResourceLoader;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_CASE_DATA_JSON_FILE;

public final class TestDataProvider {

    private TestDataProvider() {
        // Utility classes should not have a public or default constructor.
    }

    public static Address generateTestAddressByPostcodeCountry(String postCode, String country) {
        CaseData caseData = ResourceLoader.fromString(TEST_CASE_DATA_JSON_FILE, CaseData.class);
        if (postCode != null) {
            caseData.getClaimantType().getClaimantAddressUK().setPostCode(postCode);
        }
        if (country != null) {
            caseData.getClaimantType().getClaimantAddressUK().setCountry(country);
        }
        return caseData.getClaimantType().getClaimantAddressUK();
    }

    public static CaseData generateTestCaseDataByClaimantCompensation(String claimantCompensationText,
                                                                  String claimantCompensationAmount,
                                                                  String claimantTribunalRecommendation) {
        CaseData caseData = ResourceLoader.fromString(TEST_CASE_DATA_JSON_FILE, CaseData.class);
        caseData.getClaimantRequests().setClaimantCompensationText(claimantCompensationText);
        caseData.getClaimantRequests().setClaimantCompensationAmount(claimantCompensationAmount);
        caseData.getClaimantRequests().setClaimantTribunalRecommendation(claimantTribunalRecommendation);
        return caseData;
    }

    public static CaseData generateTestCaseDataByClaimantHearingPreferenceContactLanguage(
        boolean isClaimantHearingPreferenceEmpty,
        String contactLanguage) {
        CaseData caseData = ResourceLoader.fromString(TEST_CASE_DATA_JSON_FILE, CaseData.class);
        if (isClaimantHearingPreferenceEmpty) {
            caseData.setClaimantHearingPreference(null);
        } else {
            caseData.getClaimantHearingPreference().setContactLanguage(contactLanguage);
        }
        return caseData;
    }

    public static CaseData generateTestCaseDataByFirstNames(
        String claimantIndTypeEmptyOrNull,
        String firstNames) {
        CaseData caseData = ResourceLoader.fromString(TEST_CASE_DATA_JSON_FILE, CaseData.class);
        if ("null".equals(claimantIndTypeEmptyOrNull)) {
            caseData.setClaimantIndType(null);
        } else if ("empty".equals(claimantIndTypeEmptyOrNull)) {
            caseData.setClaimantIndType(new ClaimantIndType());
        } else {
            caseData.getClaimantIndType().setClaimantFirstNames(firstNames);
        }
        return caseData;
    }

    public static Address generateAddressByAddressFields(String addressLine1,
                                                         String addressLine2,
                                                         String addressLine3,
                                                         String postTown,
                                                         String county,
                                                         String country,
                                                         String postCode) {
        Address address = new Address();
        address.setAddressLine1(addressLine1);
        address.setAddressLine2(addressLine2);
        address.setAddressLine3(addressLine3);
        address.setPostTown(postTown);
        address.setCounty(county);
        address.setCountry(country);
        address.setPostCode(postCode);
        return address;
    }

    public static CaseData generateCaseDataForRespondent(String ethosCaseReference,
                                                         String claimantWorkAddressQuestion,
                                                         Address claimantWorkAddress) {
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference(ethosCaseReference);
        caseData.setClaimantWorkAddressQuestion(claimantWorkAddressQuestion);
        ClaimantWorkAddressType claimantWorkAddressType = new ClaimantWorkAddressType();
        claimantWorkAddressType.setClaimantWorkAddress(claimantWorkAddress);
        caseData.setClaimantWorkAddress(claimantWorkAddressType);

        return caseData;
    }

    public static ResponseEntity<CaseDocument> getDocumentDetailsFromCdam() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<>(
            CaseDocument.builder()
                .size("size").mimeType("mimeType").hashToken("token").createdOn("createdOn").createdBy("createdBy")
                .lastModifiedBy("lastModifiedBy").modifiedOn("modifiedOn").ttl("ttl")
                .metadata(Map.of("test", "test"))
                .originalDocumentName("docName.txt").classification("PUBLIC")
                .links(Map.of("self", Map.of("href", "TestURL.com"))).build(),
            headers,
            HttpStatus.OK
        );
    }

    public static GenericTseApplicationTypeItem generateGenericTseAppTypeItem(List<String> argumentsList) {
        GenericTseApplicationTypeItem tseAppTypeItem = new GenericTseApplicationTypeItem();
        GenericTseApplicationType tseApplicationType = new GenericTseApplicationType();
        tseApplicationType.setApplicant(argumentsList.get(0));
        tseApplicationType.setDate(argumentsList.get(3));
        tseAppTypeItem.setValue(tseApplicationType);
        return tseAppTypeItem;
    }

    public static ClaimantTse generateClaimantTse(List<String> argumentsList) {
        ClaimantTse claimantTse = new ClaimantTse();
        claimantTse.setContactApplicationText(argumentsList.get(1));
        claimantTse.setContactApplicationType(argumentsList.get(2));
        claimantTse.setCopyToOtherPartyYesOrNo(argumentsList.get(4));
        claimantTse.setCopyToOtherPartyText(argumentsList.get(5));
        UploadedDocumentType docType = new UploadedDocumentType();
        docType.setDocumentFilename(argumentsList.get(6));
        claimantTse.setContactApplicationFile(docType);
        return claimantTse;
    }

    public static GenericTseApplication generateExpectedTseApp(List<String> argumentsList) {
        return GenericTseApplication.builder()
            .caseNumber(argumentsList.get(7))
            .applicant(argumentsList.get(0))
            .applicationType(argumentsList.get(2))
            .applicationDate(argumentsList.get(3))
            .tellOrAskTribunal(argumentsList.get(1))
            .supportingEvidence(argumentsList.get(6))
            .copyToOtherPartyYesOrNo(argumentsList.get(4))
            .copyToOtherPartyText(argumentsList.get(5))
            .build();
    }
}
