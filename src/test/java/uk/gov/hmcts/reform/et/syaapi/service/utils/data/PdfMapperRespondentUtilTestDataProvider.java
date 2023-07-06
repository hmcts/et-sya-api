package uk.gov.hmcts.reform.et.syaapi.service.utils.data;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants;
import uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_ACAS;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_COMPANY_NAME;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.WORK_ADDRESS_LINE_1;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.WORK_ADDRESS_LINE_2;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.WORK_ADDRESS_LINE_3;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.WORK_COUNTRY;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.WORK_COUNTY;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.WORK_POSTCODE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.WORK_POST_TOWN;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.YES;

public final class PdfMapperRespondentUtilTestDataProvider {

    private PdfMapperRespondentUtilTestDataProvider() {
        // Utility classes should not have a public or default constructor.
    }

    public static Stream<Arguments> generateCaseDataSamplesWithRespondentSumTypeItems() {
        Address respondentAddress = TestDataProvider.generateAddressByAddressFields(
            TestConstants.ADDRESS_LINE_1,
            TestConstants.ADDRESS_LINE_2,
            TestConstants.ADDRESS_LINE_3,
            TestConstants.POST_TOWN,
            TestConstants.COUNTY,
            TestConstants.COUNTRY,
            TestConstants.POSTCODE);
        ////// CASE DATA 1
        CaseData caseData1 = TestDataProvider.generateCaseDataForRespondent(TestConstants.STRING_NUMERIC_ONE, YES,
                                                                            TestConstants.NULL_ADDRESS);
        RespondentSumTypeItem respondentSumTypeItem = generateRespondentSumTypeItem(
            TestConstants.STRING_NUMERIC_ONE, TEST_COMPANY_NAME, respondentAddress, YES, TEST_ACAS,
            PdfMapperConstants.PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_UNFAIR_DISMISSAL);
        List<RespondentSumTypeItem> respondentCollection = new ArrayList<>();
        respondentCollection.add(respondentSumTypeItem);
        caseData1.setRespondentCollection(respondentCollection);
        ////// CASE DATA 2
        RespondentSumTypeItem respondentSumTypeItem2 = generateRespondentSumTypeItem(
            TestConstants.STRING_NUMERIC_TWO, TEST_COMPANY_NAME, respondentAddress, YES, TEST_ACAS,
            PdfMapperConstants.PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_ANOTHER_PERSON);
        RespondentSumTypeItem respondentSumTypeItem3 = generateRespondentSumTypeItem(
            TestConstants.STRING_NUMERIC_THREE, TEST_COMPANY_NAME, respondentAddress, TestConstants.NO, TEST_ACAS,
            PdfMapperConstants.PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_NO_POWER);
        RespondentSumTypeItem respondentSumTypeItem4 = generateRespondentSumTypeItem(
            TestConstants.STRING_NUMERIC_FOUR, TEST_COMPANY_NAME, respondentAddress, TestConstants.NO, TEST_ACAS,
            PdfMapperConstants.PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_UNFAIR_DISMISSAL);
        RespondentSumTypeItem respondentSumTypeItem5 = generateRespondentSumTypeItem(
            TestConstants.STRING_NUMERIC_FIVE, TEST_COMPANY_NAME, respondentAddress, TestConstants.NO, TEST_ACAS,
            PdfMapperConstants.PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_ANOTHER_PERSON);
        List<RespondentSumTypeItem> respondentCollection2 = new ArrayList<>();
        respondentCollection2.add(respondentSumTypeItem);
        respondentCollection2.add(respondentSumTypeItem2);
        respondentCollection2.add(respondentSumTypeItem3);
        respondentCollection2.add(respondentSumTypeItem4);
        respondentCollection2.add(respondentSumTypeItem5);
        Address workAddress = TestDataProvider.generateAddressByAddressFields(WORK_ADDRESS_LINE_1, WORK_ADDRESS_LINE_2,
                                                                              WORK_ADDRESS_LINE_3, WORK_POST_TOWN,
                                                                              WORK_COUNTY, WORK_COUNTRY, WORK_POSTCODE);
        CaseData caseData2 = TestDataProvider.generateCaseDataForRespondent(TestConstants.STRING_NUMERIC_TWO,
                                                                            TestConstants.NO,
                                                                            workAddress);
        caseData2.setRespondentCollection(respondentCollection2);
        ////// CASE DATA 3
        CaseData caseData3 = TestDataProvider.generateCaseDataForRespondent(TestConstants.STRING_NUMERIC_THREE,
                                                                            YES,
                                                                            TestConstants.NULL_ADDRESS);
        RespondentSumTypeItem respondentSumTypeItem6 = generateRespondentSumTypeItem(
            TestConstants.STRING_NUMERIC_TWO, TEST_COMPANY_NAME, respondentAddress, YES, TEST_ACAS,
            PdfMapperConstants.PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_EMPLOYER_ALREADY_IN_TOUCH);

        List<RespondentSumTypeItem> respondentCollection3 = new ArrayList<>();
        respondentCollection3.add(respondentSumTypeItem);
        respondentCollection3.add(respondentSumTypeItem6);
        caseData3.setRespondentCollection(respondentCollection3);

        ////// CASE DATA 4
        CaseData caseData4 = TestDataProvider.generateCaseDataForRespondent(TestConstants.STRING_NUMERIC_FOUR, YES,
                                                                            TestConstants.NULL_ADDRESS);

        return Stream.of(Arguments.of(caseData1),
                         Arguments.of(caseData2),
                         Arguments.of(caseData3),
                         Arguments.of(caseData4));
    }

    public static RespondentSumTypeItem generateRespondentSumTypeItem(String respondentSumTypeItemId,
                                                                      String name,
                                                                      Address respondentAddress,
                                                                      String respondentAcasQuestion,
                                                                      String acasNumber,
                                                                      String respondentAcasNo) {
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setId(respondentSumTypeItemId);
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName(name);
        respondentSumType.setRespondentAddress(respondentAddress);
        respondentSumType.setRespondentAcasQuestion(respondentAcasQuestion);
        respondentSumType.setRespondentAcas(acasNumber);
        respondentSumType.setRespondentAcasNo(respondentAcasNo);
        respondentSumTypeItem.setValue(respondentSumType);
        return respondentSumTypeItem;
    }

}
