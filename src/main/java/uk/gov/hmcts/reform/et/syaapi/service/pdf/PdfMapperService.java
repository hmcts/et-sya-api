package uk.gov.hmcts.reform.et.syaapi.service.pdf;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.et.syaapi.service.utils.GenericServiceUtil;
import uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperClaimDescriptionUtil;
import uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperClaimDetailsUtil;
import uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants;
import uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperEmploymentDetailsUtil;
import uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperHearingPreferencesUtil;
import uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperPersonalDetailsUtil;
import uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperRepresentativeUtil;
import uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperRespondentUtil;
import uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperServiceUtil;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.UNASSIGNED_OFFICE;

/**
 * Maps Case Data attributes to fields within the PDF template.
 * Inputs that are accepted from the ET1 form can then be mapped to
 * the corresponding questions within the template PDF (ver. ET1_0922)
 * as described in {@link PdfMapperConstants}
 */
@Slf4j
@Service
public class PdfMapperService {

    private static final String YES_LOWERCASE = "yes";

    /**
     * Maps the parameters within case data to the inputs of the PDF Template.
     *
     * @param caseData          the case data that is to be mapped to the inputs in the PDF Template.
     * @return                  a Map containing the values from case data with the
     *                          corresponding PDF input labels as a key.
     */
    public Map<String, Optional<String>> mapHeadersToPdf(CaseData caseData) {
        ConcurrentMap<String, Optional<String>> printFields = new ConcurrentHashMap<>();
        if (ObjectUtils.isEmpty(caseData)) {
            return printFields;
        }
        putGenericFields(caseData, printFields);
        PdfMapperPersonalDetailsUtil.putPersonalDetails(caseData, printFields);
        PdfMapperClaimDescriptionUtil.putClaimDescription(caseData, printFields);
        PdfMapperRepresentativeUtil.putRepresentative(caseData, printFields);
        PdfMapperHearingPreferencesUtil.putHearingPreferences(caseData, printFields);
        PdfMapperRespondentUtil.putRespondents(caseData, printFields);
        putMultipleClaimsDetails(caseData, printFields);
        PdfMapperEmploymentDetailsUtil.putEmploymentDetails(caseData, printFields);
        PdfMapperClaimDetailsUtil.putClaimDetails(caseData, printFields);
        try {
            printFields.putAll(printCompensation(caseData));
            printFields.putAll(printWhistleBlowing(caseData));
        } catch (Exception e) {
            GenericServiceUtil.logException("An error occurred while printing compensation and "
                                                + "whistle blowing to pdf file",
                                            caseData.getEthosCaseReference(),
                                            e.getMessage(),
                                            "PdfMapperService",
                                            "mapHeadersToPdf");
        }
        return printFields;
    }

    private static void putGenericFields(CaseData caseData, ConcurrentMap<String, Optional<String>> printFields) {
        printFields.put(PdfMapperConstants.Q15_ADDITIONAL_INFORMATION,
                        ofNullable(caseData.getEt1VettingAdditionalInformationTextArea()));
        printFields.put(PdfMapperConstants.TRIBUNAL_OFFICE, ofNullable(printTribunalOffice(caseData)));

        printFields.put(PdfMapperConstants.CASE_NUMBER, ofNullable(caseData.getEthosCaseReference()));
        printFields.put(PdfMapperConstants.DATE_RECEIVED,
                        ofNullable(PdfMapperServiceUtil.formatDate(caseData.getReceiptDate())));
    }

    private static String printTribunalOffice(CaseData caseData) {
        return UNASSIGNED_OFFICE.equals(caseData.getManagingOffice())
            ? ""
            : caseData.getManagingOffice();
    }


    private static void putMultipleClaimsDetails(
        CaseData caseData,
        ConcurrentMap<String, Optional<String>> printFields) {
        if ("Multiple".equals(caseData.getEcmCaseType())) {
            printFields.put(PdfMapperConstants.Q3_MORE_CLAIMS_YES, Optional.of(YES));
        } else {
            printFields.put(PdfMapperConstants.Q3_MORE_CLAIMS_NO, Optional.of(NO));
        }
    }

    private Map<String, Optional<String>> printCompensation(CaseData caseData) {
        Map<String, Optional<String>> printFields = new ConcurrentHashMap<>();
        if (caseData.getClaimantRequests() != null
            && caseData.getClaimantRequests().getClaimOutcome() != null
            && !caseData.getClaimantRequests().getClaimOutcome().isEmpty()) {
            for (String claimOutcome : caseData.getClaimantRequests().getClaimOutcome()) {
                switch (claimOutcome) {
                    case "compensation":
                        printFields.put(
                            PdfMapperConstants.Q9_CLAIM_SUCCESSFUL_REQUEST_COMPENSATION,
                            Optional.of(YES_LOWERCASE)
                        );
                        break;
                    case "tribunal":
                        printFields.put(
                            PdfMapperConstants.Q9_CLAIM_SUCCESSFUL_REQUEST_DISCRIMINATION_RECOMMENDATION,
                            Optional.of(YES_LOWERCASE)
                        );
                        break;
                    case "oldJob":
                        printFields.put(
                            PdfMapperConstants.Q9_CLAIM_SUCCESSFUL_REQUEST_OLD_JOB_BACK_AND_COMPENSATION,
                            Optional.of(YES_LOWERCASE)
                        );
                        break;
                    case "anotherJob":
                        printFields.put(
                            PdfMapperConstants.Q9_CLAIM_SUCCESSFUL_REQUEST_ANOTHER_JOB,
                            Optional.of(YES_LOWERCASE)
                        );
                        break;
                    default:
                        break;
                }
            }

            String claimantCompensation = PdfMapperServiceUtil.generateClaimantCompensation(caseData);
            String claimantTribunalRecommendation = PdfMapperServiceUtil
                .generateClaimantTribunalRecommendation(caseData);
            printFields.put(
                PdfMapperConstants.Q9_WHAT_COMPENSATION_REMEDY_ARE_YOU_SEEKING,
                Optional.of(claimantCompensation + claimantTribunalRecommendation)
            );
        }

        return printFields;
    }

    private Map<String, Optional<String>> printWhistleBlowing(CaseData caseData) {
        Map<String, Optional<String>> printFields = new ConcurrentHashMap<>();

        if (caseData.getClaimantRequests() == null) {
            return printFields;
        }

        if (caseData.getClaimantRequests().getWhistleblowing() != null
            && YES.equals(caseData.getClaimantRequests().getWhistleblowing())) {

            printFields.put(PdfMapperConstants.Q10_WHISTLE_BLOWING, Optional.of(YES_LOWERCASE));
            printFields.put(
                PdfMapperConstants.Q10_WHISTLE_BLOWING_REGULATOR,
                ofNullable(caseData.getClaimantRequests().getWhistleblowingAuthority())
            );
        }

        return printFields;
    }

}
