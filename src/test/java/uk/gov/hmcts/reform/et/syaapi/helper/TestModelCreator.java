package uk.gov.hmcts.reform.et.syaapi.helper;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantHearingPreference;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantOtherType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantRequestType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantWorkAddressType;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.et.common.model.ccd.types.NewEmploymentType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.TaskListCheckType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TestModelCreator {
    private static final String COUNTRY = "United Kingdom";
    public static final String TYPE_OF_CLAIM_BREACH_OF_CONTRACT = "breachOfContract";
    public static final String TYPE_OF_CLAIM_DISCRIMINATION = "discrimination";
    public static final String TYPE_OF_CLAIM_PAY_RELATED_CLAIM = "payRelated";
    public static final String TYPE_OF_CLAIM_UNFAIR_DISMISSAL = "unfairDismissal";
    public static final String TYPE_OF_CLAIM_WHISTLE_BLOWING = "whistleBlowing";
    private static final String MANAGING_OFFICE = "ET_Scotland";
    private static final String CASE_NOTES = "Dummy Case Notes";
    private static final String CASE_TYPE_SINGLE = "Single";
    private static final String CASE_SOURCE_INTERNET = "Internet";
    private static final String YES_OR_NO_YES = "Yes";
    private static final String CLAIMANT_SEX_MALE = "Male";
    private static final String CLAIMANT_FIRST_NAME = "Michael";
    private static final String CLAIMANT_LAST_NAME = "Jackson";
    private static final String CLAIMANT_TITLE = "Mr";
    private static final String CLAIMANT_PREFERRED_TITLE = "Mr";
    private static final String CLAIMANT_GENDER_IDENTITY = "Heterosexual";
    private static final String CLAIMANT_DATE_OF_BIRTH = new LocalDate("1979-08-05").toString();
    private static final String CLAIMANT_ADDRESS_LINE_1 = "40 Furrow way";
    private static final String CLAIMANT_COUNTRY = COUNTRY;
    private static final String CLAIMANT_COUNTY = "Berkshire";
    private static final String CLAIMANT_POST_CODE = "SL6 3NY";
    private static final String CLAIMANT_POST_TOWN = "Maidenhead";
    private static final String CLAIMANT_PHONE_NUMBER = "07444518903";
    private static final String CLAIMANT_MOBILE_NUMBER = "07444518903";
    private static final String CLAIMANT_EMAIL_ADDRESS = "michael.jackson@gmail.com";
    private static final String CLAIMANT_CONTACT_PREFERENCE = "Email";
    private static final String CLAIMANT_CONTACT_LANGUAGE = "English";

    private static final String CLAIMANT_OCCUPATION = "Software Engineer";
    private static final String CLAIMANT_EMPLOYED_FROM =
        new DateTime(DateTime.parse("2020-01-01T07:22:05Z")).toString();
    private static final String CLAIMANT_EMPLOYED_CURRENTLY = "No";
    private static final String CLAIMANT_EMPLOYED_TO =
        new DateTime(DateTime.parse("2022-01-01T07:22:05Z")).toString();
    private static final String CLAIMANT_EMPLOYED_NOTICE_PERIOD = YES_OR_NO_YES;
    private static final String CLAIMANT_DISABLED = "No";
    private static final String CLAIMANT_NOTICE_PERIOD = YES_OR_NO_YES;
    private static final String CLAIMANT_NOTICE_PERIOD_UNIT = "Weeks";
    private static final String CLAIMANT_NOTICE_PERIOD_DURATION = "2";
    private static final String CLAIMANT_AVERAGE_WEEKLY_HOURS = "36.5";
    private static final String CLAIMANT_PAY_BEFORE_TAX = "27000";
    private static final String CLAIMANT_PAY_AFTER_TAX = "22000";
    private static final String CLAIMANT_PAY_CYCLE = "Annual";
    private static final String CLAIMANT_PENSION_CONTRIBUTION = YES_OR_NO_YES;
    private static final String CLAIMANT_PENSION_WEEKLY_CONTRIBUTION = "100";
    private static final String CLAIMANT_BENEFITS = YES_OR_NO_YES;
    private static final String CLAIMANT_BENEFITS_DETAIL = "Car";
    private static final String CLAIMANT_PAST_EMPLOYER = "Test Soft";
    private static final String CLAIMANT_STILL_WORKING = "No";
    private static final String CLAIMANT_WORK_ADDRESS_LINE_1 = "9 Furrow way";
    private static final String CLAIMANT_WORK_COUNTRY = COUNTRY;
    private static final String CLAIMANT_WORK_COUNTY = "Belfast";
    private static final String CLAIMANT_WORK_POST_CODE = "BL2 1AS";
    private static final String CLAIMANT_WORK_POST_TOWN = "Belfast Center";
    private static final String CLAIMANT_WORK_PHONE_NUMBER = "07444518999";
    private static final String CLAIMANT_COMPENSATION = YES_OR_NO_YES;
    private static final String CLAIMANT_TRIBUNAL = "I want tribunal";
    private static final String CLAIMANT_COMPENSATION_AMOUNT = "2000";
    private static final String CLAIMANT_TRIBUNAL_RECOMMENDATION = YES_OR_NO_YES;

    private static final String REPRESENTATIVE_NAME = "Christiano Ronaldo";
    private static final String REPRESENTATIVE_ORGANISATION = "London Law Firm";
    private static final String REPRESENTATIVE_OCCUPATION = "Lawyer";
    private static final String REPRESENTATIVE_PHONE_NUMBER = "07444518911";
    private static final String REPRESENTATIVE_MOBILE_NUMBER = "07444518911";
    private static final String REPRESENTATIVE_EMAIL_ADDRESS = "christiano.ronaldo@gmail.com";
    private static final String REPRESENTATIVE_PREFERENCE = "Email";
    private static final String REPRESENTATIVE_ADDRESS_LINE_1 = "12 Gunthrope Rode";
    private static final String REPRESENTATIVE_COUNTRY = COUNTRY;
    private static final String REPRESENTATIVE_COUNTY = "Buckinghamshire";
    private static final String REPRESENTATIVE_POST_CODE = "SL2 5XC";
    private static final String REPRESENTATIVE_POST_TOWN = "Marlow";
    private static final String RESPONDENT1_ID = "1";
    private static final String RESPONDENT1_NAME = "Marc Judge";
    private static final String RESPONDENT1_ACAS_QUESTION = YES_OR_NO_YES;
    private static final String RESPONDENT1_ACAS = YES_OR_NO_YES;
    private static final String RESPONDENT1_ACAS_NO = "R12/45678901";
    private static final String RESPONDENT1_ADDRESS_LINE_1 = "23 Furrow way";
    private static final String RESPONDENT1_COUNTRY = COUNTRY;
    private static final String RESPONDENT1_COUNTY = "Liverpool";
    private static final String RESPONDENT1_POST_CODE = "LV1 1AA";
    private static final String RESPONDENT1_POST_TOWN = "Liverpool Center";
    private static final String RESPONDENT1_PHONE1 = "074441567897";
    private static final String RESPONDENT1_PHONE2 = "074441894561";
    private static final String RESPONDENT1_EMAIL = "marc.judge@gmail.com";
    private static final String RESPONDENT1_CONTACT_PREFERENCE = "Post";
    private static final String RESPONDENT2_ID = "2";
    private static final String RESPONDENT2_NAME = "Mehmet Tahir Dede";
    private static final String RESPONDENT2_ACAS_QUESTION = YES_OR_NO_YES;
    private static final String RESPONDENT2_ACAS = YES_OR_NO_YES;
    private static final String RESPONDENT2_ACAS_NO = "R12/31278901";
    private static final String RESPONDENT2_ADDRESS_LINE_1 = "33 Furrow way";
    private static final String RESPONDENT2_COUNTRY = COUNTRY;
    private static final String RESPONDENT2_COUNTY = "Lake Distrcit";
    private static final String RESPONDENT2_POST_CODE = "LD1 1AA";
    private static final String RESPONDENT2_POST_TOWN = "Lake District Center";
    private static final String RESPONDENT2_PHONE1 = "07444123456";
    private static final String RESPONDENT2_PHONE2 = "07444124567";
    private static final String RESPONDENT2_EMAIL = "mehmet.dede@gmail.com";
    private static final String RESPONDENT2_CONTACT_PREFERENCE = "Email";

    private static final String NEW_JOB = "Software Specialist";
    private static final String NEWLY_EMPLOYED_FROM = new DateTime(DateTime.parse("2022-03-01T07:22:05Z")).toString();
    private static final String NEW_PAY_BEFORE_TAX = "30000";
    private static final String NEW_JOB_PAY_INTERVAL = "Annual";

    private static final String HEARING_PREFERENCE_VIDEO = "Video";
    private static final String HEARING_PREFERENCE_PHONE = "Phone";
    private static final String HEARING_ASSISTANCE = "Yes I want";
    private static final String REASONABLE_ADJUSTMENTS = YES_OR_NO_YES;
    private static final String REASONABLE_ADJUSTMENTS_DETAIL = "I need a wheelchair";

    private static final String WHISTLE_BLOWING = YES_OR_NO_YES;
    private static final String WHISTLE_BLOWING_AUTHORITY = "Whistle Blowing Authority";
    private static final String CLAIM_DESCRIPTION = "This is a test claim";

    private static final String DOCUMENT_BINARY_URL = "https://document.binary.url";
    private static final String DOCUMENT_FILE_NAME = "filename";
    private static final String DOCUMENT_URL = "https://document.url";

    private static final String PERSONAL_DETAILS_CHECK = YES_OR_NO_YES;
    private static final String EMPLOYMENT_AND_RESPONDENT_CHECK = YES_OR_NO_YES;
    private static final String CLAIM_DETAILS_CHECK = YES_OR_NO_YES;

    private TestModelCreator() {

    }

    private static List<JurCodesTypeItem> getJureCodesTypeItemList() {
        JurCodesTypeItem jurCodesTypeItem = new JurCodesTypeItem();
        jurCodesTypeItem.setId("1");
        JurCodesType jurCodesType = new JurCodesType();
        jurCodesType.setDateNotified(DateTime.now().toString());
        jurCodesType.setDisposalDate(DateTime.now().withDurationAdded(1_000_000_000L, 1).toString());
        jurCodesType.setJudgmentOutcome("Judgement Outcome");
        jurCodesType.setJuridictionCodesList("Juridiction Codes List");
        jurCodesType.setJuridictionCodesSubList1("Juridiction Codes Sub List 1");
        jurCodesTypeItem.setValue(jurCodesType);
        return List.of(jurCodesTypeItem);
    }

    private static String[] getTypeOfClaimArray() {
        return new String[] {
            TYPE_OF_CLAIM_DISCRIMINATION,
            TYPE_OF_CLAIM_BREACH_OF_CONTRACT,
            TYPE_OF_CLAIM_PAY_RELATED_CLAIM,
            TYPE_OF_CLAIM_UNFAIR_DISMISSAL,
            TYPE_OF_CLAIM_WHISTLE_BLOWING};
    }

    private static ClaimantIndType getClaimantIndType() {
        ClaimantIndType claimantIndType = new ClaimantIndType();
        claimantIndType.setClaimantDateOfBirth(CLAIMANT_DATE_OF_BIRTH);
        claimantIndType.setClaimantFirstNames(CLAIMANT_FIRST_NAME);
        claimantIndType.setClaimantLastName(CLAIMANT_LAST_NAME);
        claimantIndType.setClaimantSex(CLAIMANT_SEX_MALE);
        claimantIndType.setClaimantTitle(CLAIMANT_TITLE);
        claimantIndType.setClaimantGenderIdentity(CLAIMANT_GENDER_IDENTITY);
        claimantIndType.setClaimantPreferredTitle(CLAIMANT_PREFERRED_TITLE);
        return claimantIndType;
    }

    private static Address getAddress(String addressLine1, String country,
                                      String county, String postCode, String postTown) {
        Address address = new Address();
        address.setAddressLine1(addressLine1);
        address.setCountry(country);
        address.setCounty(county);
        address.setPostCode(postCode);
        address.setPostTown(postTown);
        return address;
    }

    private static ClaimantType getClaimantType() {
        ClaimantType claimantType = new ClaimantType();
        claimantType.setClaimantAddressUK(getAddress(
            CLAIMANT_ADDRESS_LINE_1,
            CLAIMANT_COUNTRY,
            CLAIMANT_COUNTY,
            CLAIMANT_POST_CODE,
            CLAIMANT_POST_TOWN
            ));
        claimantType.setClaimantContactLanguage(CLAIMANT_CONTACT_LANGUAGE);
        claimantType.setClaimantContactPreference(CLAIMANT_CONTACT_PREFERENCE);
        claimantType.setClaimantEmailAddress(CLAIMANT_EMAIL_ADDRESS);
        claimantType.setClaimantMobileNumber(CLAIMANT_MOBILE_NUMBER);
        claimantType.setClaimantPhoneNumber(CLAIMANT_PHONE_NUMBER);
        return claimantType;
    }

    private static RepresentedTypeC getRepresentativeClaimantTypeType() {
        RepresentedTypeC representedType = new RepresentedTypeC();
        representedType.setNameOfRepresentative(REPRESENTATIVE_NAME);
        representedType.setRepresentativeAddress(getAddress(
            REPRESENTATIVE_ADDRESS_LINE_1,
            REPRESENTATIVE_COUNTRY,
            REPRESENTATIVE_COUNTY,
            REPRESENTATIVE_POST_CODE,
            REPRESENTATIVE_POST_TOWN
        ));
        representedType.setRepresentativeOccupation(REPRESENTATIVE_OCCUPATION);
        representedType.setRepresentativePreference(REPRESENTATIVE_PREFERENCE);
        representedType.setNameOfRepresentative(REPRESENTATIVE_NAME);
        representedType.setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_ADDRESS);
        representedType.setRepresentativeMobileNumber(REPRESENTATIVE_MOBILE_NUMBER);
        representedType.setNameOfOrganisation(REPRESENTATIVE_ORGANISATION);
        representedType.setRepresentativePhoneNumber(REPRESENTATIVE_PHONE_NUMBER);
        return representedType;
    }

    private static ClaimantOtherType getClaimantOtherType() {
        ClaimantOtherType claimantOtherType = new ClaimantOtherType();
        claimantOtherType.setClaimantBenefits(CLAIMANT_BENEFITS);
        claimantOtherType.setClaimantDisabled(CLAIMANT_DISABLED);
        claimantOtherType.setClaimantEmployedTo(CLAIMANT_EMPLOYED_TO);
        claimantOtherType.setClaimantEmployedNoticePeriod(CLAIMANT_EMPLOYED_NOTICE_PERIOD);
        claimantOtherType.setClaimantBenefitsDetail(CLAIMANT_BENEFITS_DETAIL);
        claimantOtherType.setClaimantOccupation(CLAIMANT_OCCUPATION);
        claimantOtherType.setClaimantAverageWeeklyHours(CLAIMANT_AVERAGE_WEEKLY_HOURS);
        claimantOtherType.setClaimantEmployedCurrently(CLAIMANT_EMPLOYED_CURRENTLY);
        claimantOtherType.setClaimantEmployedFrom(CLAIMANT_EMPLOYED_FROM);
        claimantOtherType.setClaimantEmployedNoticePeriod(CLAIMANT_NOTICE_PERIOD);
        claimantOtherType.setClaimantNoticePeriod(CLAIMANT_NOTICE_PERIOD);
        claimantOtherType.setClaimantNoticePeriodDuration(CLAIMANT_NOTICE_PERIOD_DURATION);
        claimantOtherType.setClaimantNoticePeriodUnit(CLAIMANT_NOTICE_PERIOD_UNIT);
        claimantOtherType.setClaimantPayAfterTax(CLAIMANT_PAY_AFTER_TAX);
        claimantOtherType.setClaimantPayBeforeTax(CLAIMANT_PAY_BEFORE_TAX);
        claimantOtherType.setClaimantPayCycle(CLAIMANT_PAY_CYCLE);
        claimantOtherType.setClaimantPensionContribution(CLAIMANT_PENSION_CONTRIBUTION);
        claimantOtherType.setClaimantPensionWeeklyContribution(CLAIMANT_PENSION_WEEKLY_CONTRIBUTION);
        claimantOtherType.setPastEmployer(CLAIMANT_PAST_EMPLOYER);
        claimantOtherType.setStillWorking(CLAIMANT_STILL_WORKING);
        return claimantOtherType;
    }

    private static List<RespondentSumTypeItem> getRespondentCollection() {
        RespondentSumTypeItem respondentSumTypeItem1 = new RespondentSumTypeItem();
        RespondentSumType respondentSumType1 = new RespondentSumType();
        respondentSumTypeItem1.setId(RESPONDENT1_ID);
        respondentSumType1.setRespondentName(RESPONDENT1_NAME);
        respondentSumType1.setRespondentAcasQuestion(RESPONDENT1_ACAS_QUESTION);
        respondentSumType1.setRespondentAcas(RESPONDENT1_ACAS);
        respondentSumType1.setRespondentAcasNo(RESPONDENT1_ACAS_NO);
        respondentSumType1.setRespondentAddress(getAddress(
            RESPONDENT1_ADDRESS_LINE_1,
            RESPONDENT1_COUNTRY,
            RESPONDENT1_COUNTY,
            RESPONDENT1_POST_CODE,
            RESPONDENT1_POST_TOWN
        ));
        respondentSumType1.setRespondentPhone1(RESPONDENT1_PHONE1);
        respondentSumType1.setRespondentPhone2(RESPONDENT1_PHONE2);
        respondentSumType1.setRespondentEmail(RESPONDENT1_EMAIL);
        respondentSumType1.setRespondentContactPreference(RESPONDENT1_CONTACT_PREFERENCE);
        respondentSumTypeItem1.setValue(respondentSumType1);

        RespondentSumTypeItem respondentSumTypeItem2 = new RespondentSumTypeItem();
        RespondentSumType respondentSumType2 = new RespondentSumType();
        respondentSumTypeItem2.setId(RESPONDENT2_ID);
        respondentSumType2.setRespondentName(RESPONDENT2_NAME);
        respondentSumType2.setRespondentAcasQuestion(RESPONDENT2_ACAS_QUESTION);
        respondentSumType2.setRespondentAcas(RESPONDENT2_ACAS);
        respondentSumType2.setRespondentAcasNo(RESPONDENT2_ACAS_NO);
        respondentSumType2.setRespondentAddress(getAddress(
            RESPONDENT2_ADDRESS_LINE_1,
            RESPONDENT2_COUNTRY,
            RESPONDENT2_COUNTY,
            RESPONDENT2_POST_CODE,
            RESPONDENT2_POST_TOWN
        ));
        respondentSumType2.setRespondentPhone1(RESPONDENT2_PHONE1);
        respondentSumType2.setRespondentPhone2(RESPONDENT2_PHONE2);
        respondentSumType2.setRespondentEmail(RESPONDENT2_EMAIL);
        respondentSumType2.setRespondentContactPreference(RESPONDENT2_CONTACT_PREFERENCE);
        respondentSumTypeItem2.setValue(respondentSumType2);
        List<RespondentSumTypeItem> respondentSumTypeItems = new ArrayList<>();
        respondentSumTypeItems.add(respondentSumTypeItem1);
        respondentSumTypeItems.add(respondentSumTypeItem2);
        return respondentSumTypeItems;
    }

    private static ClaimantWorkAddressType getClaimantWorkAddress() {
        ClaimantWorkAddressType claimantWorkAddressType = new ClaimantWorkAddressType();
        claimantWorkAddressType.setClaimantWorkAddress(getAddress(
            CLAIMANT_WORK_ADDRESS_LINE_1,
            CLAIMANT_WORK_COUNTRY,
            CLAIMANT_WORK_COUNTY,
            CLAIMANT_WORK_POST_CODE,
            CLAIMANT_WORK_POST_TOWN
        ));
        claimantWorkAddressType.setClaimantWorkPhoneNumber(CLAIMANT_WORK_PHONE_NUMBER);
        return claimantWorkAddressType;
    }

    private static NewEmploymentType getNewEmploymentType() {
        NewEmploymentType newEmploymentType = new NewEmploymentType();
        newEmploymentType.setNewJob(NEW_JOB);
        newEmploymentType.setNewJobPayInterval(NEW_JOB_PAY_INTERVAL);
        newEmploymentType.setNewPayBeforeTax(NEW_PAY_BEFORE_TAX);
        newEmploymentType.setNewlyEmployedFrom(NEWLY_EMPLOYED_FROM);
        return newEmploymentType;
    }

    private static UploadedDocumentType getUploadedDocumentType() {
        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentBinaryUrl(DOCUMENT_BINARY_URL);
        uploadedDocumentType.setDocumentFilename(DOCUMENT_FILE_NAME);
        uploadedDocumentType.setDocumentUrl(DOCUMENT_URL);
        return uploadedDocumentType;
    }

    private static ClaimantRequestType getClaimantRequests() {
        ClaimantRequestType claimantRequestType = new ClaimantRequestType();
        claimantRequestType.setClaimantCompensationText(CLAIMANT_COMPENSATION);
        claimantRequestType.setClaimantTribunalRecommendation(CLAIMANT_TRIBUNAL);
        claimantRequestType.setClaimantCompensationAmount(CLAIMANT_COMPENSATION_AMOUNT);
        claimantRequestType.setClaimantTribunalRecommendation(CLAIMANT_TRIBUNAL_RECOMMENDATION);
        claimantRequestType.setClaimDescription(CLAIM_DESCRIPTION);
        claimantRequestType.setWhistleblowing(WHISTLE_BLOWING);
        claimantRequestType.setWhistleblowingAuthority(WHISTLE_BLOWING_AUTHORITY);
        claimantRequestType.setClaimDescriptionDocument(getUploadedDocumentType());

        return claimantRequestType;
    }

    private static ClaimantHearingPreference getClaimantHearingPreference() {
        ClaimantHearingPreference claimantHearingPreference = new ClaimantHearingPreference();
        claimantHearingPreference.setHearingPreferences(Arrays.asList(HEARING_PREFERENCE_VIDEO,
                                                                      HEARING_PREFERENCE_PHONE));
        claimantHearingPreference.setHearingAssistance(HEARING_ASSISTANCE);
        claimantHearingPreference.setReasonableAdjustments(REASONABLE_ADJUSTMENTS);
        claimantHearingPreference.setReasonableAdjustmentsDetail(REASONABLE_ADJUSTMENTS_DETAIL);
        return claimantHearingPreference;
    }

    private static TaskListCheckType getClaimantTaskListChecks() {
        TaskListCheckType taskListCheckType = new TaskListCheckType();
        taskListCheckType.setClaimDetailsCheck(CLAIM_DETAILS_CHECK);
        taskListCheckType.setPersonalDetailsCheck(PERSONAL_DETAILS_CHECK);
        taskListCheckType.setEmploymentAndRespondentCheck(EMPLOYMENT_AND_RESPONDENT_CHECK);
        return taskListCheckType;
    }

    public static Map<String, Object> createRequestCaseData() {
        Map<String, Object> requestCaseData = new ConcurrentHashMap<>();
        requestCaseData.put("typeOfClaim", TestModelCreator.getTypeOfClaimArray());
        requestCaseData.put("caseType", TestModelCreator.CASE_TYPE_SINGLE);
        requestCaseData.put("caseSource", TestModelCreator.CASE_SOURCE_INTERNET);
        requestCaseData.put("claimantRepresentedQuestion", TestModelCreator.YES_OR_NO_YES);
        requestCaseData.put("jurCodesCollection", TestModelCreator.getJureCodesTypeItemList());
        requestCaseData.put("claimantIndType", TestModelCreator.getClaimantIndType());
        requestCaseData.put("claimantType", TestModelCreator.getClaimantType());
        requestCaseData.put("representativeClaimantType", TestModelCreator.getRepresentativeClaimantTypeType());
        requestCaseData.put("claimantOtherType", TestModelCreator.getClaimantOtherType());
        requestCaseData.put("respondentCollection", TestModelCreator.getRespondentCollection());
        requestCaseData.put("claimantWorkAddress", TestModelCreator.getClaimantWorkAddress());
        requestCaseData.put("caseNotes", TestModelCreator.CASE_NOTES);
        requestCaseData.put("managingOffice", TestModelCreator.MANAGING_OFFICE);
        requestCaseData.put("newEmploymentType", TestModelCreator.getNewEmploymentType());
        requestCaseData.put("claimantRequests", TestModelCreator.getClaimantRequests());
        requestCaseData.put("claimantHearingPreference", TestModelCreator.getClaimantHearingPreference());
        requestCaseData.put("claimantTaskListChecks", TestModelCreator.getClaimantTaskListChecks());
        return requestCaseData;
    }
}
