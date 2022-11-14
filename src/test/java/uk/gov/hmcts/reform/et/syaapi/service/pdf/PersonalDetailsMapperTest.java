package uk.gov.hmcts.reform.et.syaapi.service.pdf;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"PMD.UseConcurrentHashMap"})
class PersonalDetailsMapperTest {
    @Test
    void mapNullCaseData() {
        assertThat(new PersonalDetailsMapper().mapPersonalDetails(new CaseData())).isEmpty();
    }

    @Test
    void mapClaimantIndType() {
        ClaimantIndType claimantIndType = new ClaimantIndType();
        claimantIndType.setClaimantPreferredTitle("Other_Specify");
        claimantIndType.setClaimantTitleOther("ABC");
        claimantIndType.setClaimantFirstNames("ClaimantFirstNames");
        claimantIndType.setClaimantLastName("ClaimantLastName");
        claimantIndType.setClaimantDateOfBirth("1976-07-12");
        claimantIndType.setClaimantSex("Female");

        CaseData caseData = new CaseData();
        caseData.setClaimantIndType(claimantIndType);

        assertThat(new PersonalDetailsMapper().mapPersonalDetails(caseData)).containsExactlyInAnyOrderEntriesOf(
            Map.of(
                "1.1 other specify",
                Optional.empty(),
                "1.2 first names",
                Optional.of("ClaimantFirstNames"),
               "1.3 surname",
               Optional.of("ClaimantLastName"),
               "1.4 DOB day",
               Optional.of("12"),
               "1.4 DOB month",
               Optional.of("07"),
               "1.4 DOB year",
               Optional.of("1976"),
               "1.5 sex female",
               Optional.of("female")
            )
        );
    }

    @Test
    void mapClaimantIndTypeWithMaleSex() {
        ClaimantIndType claimantIndType = new ClaimantIndType();
        claimantIndType.setClaimantSex("Male");

        CaseData caseData = new CaseData();
        caseData.setClaimantIndType(claimantIndType);

        Map<String, Optional<String>> mappedDetails = new PersonalDetailsMapper().mapPersonalDetails(caseData);
        assertThat(mappedDetails).containsEntry(
            "1.5 sex",
            Optional.of("Yes")
        );
        assertThat(mappedDetails.values().stream().filter(Optional::isPresent)).hasSize(1);
    }

    @Test
    void mapClaimantIndTypeWithPreferNotToSaySex() {
        ClaimantIndType claimantIndType = new ClaimantIndType();
        claimantIndType.setClaimantSex("Prefer not to say");

        CaseData caseData = new CaseData();
        caseData.setClaimantIndType(claimantIndType);

        Map<String, Optional<String>> mappedDetails = new PersonalDetailsMapper().mapPersonalDetails(caseData);
        assertThat(mappedDetails).containsEntry(
            "1.5 sex prefer not to say",
            Optional.of("prefer not to say")
        );
        assertThat(mappedDetails.values().stream().filter(Optional::isPresent)).hasSize(1);
    }

    @Test
    void mapClaimantType() {
        Address claimantAddressUK = new Address();
        claimantAddressUK.setAddressLine1("AddressLine1");
        claimantAddressUK.setAddressLine2("AddressLine2");
        claimantAddressUK.setPostTown("PostTown");
        claimantAddressUK.setCounty("County");
        claimantAddressUK.setPostCode("SW1A 1AA");

        ClaimantType claimantType = new ClaimantType();
        claimantType.setClaimantAddressUK(claimantAddressUK);
        claimantType.setClaimantPhoneNumber("ClaimantPhoneNumber");
        claimantType.setClaimantMobileNumber("ClaimantMobileNumber");
        claimantType.setClaimantEmailAddress("ClaimantEmailAddress");
        claimantType.setClaimantContactPreference("Email");

        CaseData caseData = new CaseData();
        caseData.setClaimantType(claimantType);

        assertThat(new PersonalDetailsMapper().mapPersonalDetails(caseData)).containsExactlyInAnyOrderEntriesOf(
            Map.of(
                "1.5 number",
                   Optional.of("AddressLine1"),
                "1.5 town city",
                   Optional.of("PostTown"),
                "1.5 county",
                   Optional.of("County"),
                "1.5 postcode",
                   Optional.of("SW1A1AA"),
                "1.5 street",
                   Optional.of("AddressLine2"),
                "1.6 phone number",
                   Optional.of("ClaimantPhoneNumber"),
                "1.7 mobile number",
                   Optional.of("ClaimantMobileNumber"),
                "1.8 How should we contact you - Email",
                Optional.of("Email"),
                "1.9 email",
                Optional.of("ClaimantEmailAddress")
            )
        );
    }

    @Test
    void mapClaimantTypeWithPostPreference() {
        ClaimantType claimantType = new ClaimantType();
        claimantType.setClaimantContactPreference("Post");

        CaseData caseData = new CaseData();
        caseData.setClaimantType(claimantType);

        Map<String, Optional<String>> mappedDetails = new PersonalDetailsMapper().mapPersonalDetails(caseData);
        assertThat(mappedDetails).containsEntry(
            "1.8 How should we contact you - Post",
            Optional.of("Post")
        );
        assertThat(mappedDetails.values().stream().filter(Optional::isPresent)).hasSize(1);
    }
}
