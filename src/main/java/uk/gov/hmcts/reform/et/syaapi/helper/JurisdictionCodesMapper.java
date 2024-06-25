package uk.gov.hmcts.reform.et.syaapi.helper;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.Et1CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants;
import uk.gov.hmcts.reform.et.syaapi.constants.JurisdictionCodesConstants;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Maps types of claims values {@link ClaimTypesConstants} to jurisdiction codes {@link JurisdictionCodesConstants}.
 */
@Service
public class JurisdictionCodesMapper {
    private static final Map<String, String> JURISDICTION_CODES = Map.ofEntries(
        Map.entry(ClaimTypesConstants.BREACH_OF_CONTRACT, JurisdictionCodesConstants.BOC),
        Map.entry(ClaimTypesConstants.UNFAIR_DISMISSAL, JurisdictionCodesConstants.UDL),
        Map.entry(ClaimTypesConstants.WHISTLE_BLOWING, JurisdictionCodesConstants.PID),
        Map.entry(ClaimTypesConstants.AGE, JurisdictionCodesConstants.DAG),
        Map.entry(ClaimTypesConstants.DISABILITY, JurisdictionCodesConstants.DDA),
        Map.entry(ClaimTypesConstants.ETHNICITY, JurisdictionCodesConstants.RRD),
        Map.entry(ClaimTypesConstants.GENDER_REASSIGNMENT, JurisdictionCodesConstants.SXD),
        Map.entry(ClaimTypesConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP, JurisdictionCodesConstants.SXD),
        Map.entry(ClaimTypesConstants.PREGNANCY_OR_MATERNITY, JurisdictionCodesConstants.MAT),
        Map.entry(ClaimTypesConstants.RACE, JurisdictionCodesConstants.RRD),
        Map.entry(ClaimTypesConstants.RELIGION_OR_BELIEF, JurisdictionCodesConstants.DRB),
        Map.entry(ClaimTypesConstants.SEX, JurisdictionCodesConstants.SXD),
        Map.entry(ClaimTypesConstants.SEXUAL_ORIENTATION, JurisdictionCodesConstants.DSO),
        Map.entry(ClaimTypesConstants.ARREARS, JurisdictionCodesConstants.WA),
        Map.entry(ClaimTypesConstants.HOLIDAY_PAY, JurisdictionCodesConstants.WTR_AL),
        Map.entry(ClaimTypesConstants.NOTICE_PAY, JurisdictionCodesConstants.BOC),
        Map.entry(ClaimTypesConstants.REDUNDANCY_PAY, JurisdictionCodesConstants.RPT)
    );

    /**
     * Extracts type of claims data from @{@link Et1CaseData}
     * object and maps to Jurisdiction codes.
     *
     * @param data which would be in json format
     * @return list of JurCodesTypeItem
     */
    public List<JurCodesTypeItem> mapToJurCodes(CaseData data) {
        Set<String> uniqueJurCodes = new HashSet<>();
        uniqueJurCodes.addAll(retrieveTypeOfClaimsCodes(data));
        uniqueJurCodes.addAll(retrieveDiscriminationCodes(data));
        uniqueJurCodes.addAll(retrievePaymentCodes(data));

        return uniqueJurCodes.stream()
            .filter(Objects::nonNull)
            .map(this::toJurCodesTypeItem)
            .toList();
    }

    private List<String> retrieveDiscriminationCodes(Et1CaseData data) {
        if (data.getClaimantRequests() == null
            || CollectionUtils.isEmpty(data.getClaimantRequests().getDiscriminationClaims())) {
            return Collections.emptyList();
        }
        return data.getClaimantRequests().getDiscriminationClaims().stream()
            .map(JURISDICTION_CODES::get)
            .toList();
    }

    private List<String> retrievePaymentCodes(Et1CaseData data) {
        if (data.getClaimantRequests() == null || CollectionUtils.isEmpty(data.getClaimantRequests().getPayClaims())) {
            return Collections.emptyList();
        }
        return data.getClaimantRequests().getPayClaims().stream()
            .map(JURISDICTION_CODES::get)
            .toList();
    }

    private List<String> retrieveTypeOfClaimsCodes(Et1CaseData data) {
        if (CollectionUtils.isEmpty(data.getTypesOfClaim())) {
            return Collections.emptyList();
        }
        return data.getTypesOfClaim().stream()
            .map(JURISDICTION_CODES::get)
            .toList();
    }

    private JurCodesTypeItem toJurCodesTypeItem(String jurCodeValue) {
        JurCodesTypeItem item = new JurCodesTypeItem();
        JurCodesType type = new JurCodesType();
        type.setJuridictionCodesList(jurCodeValue);
        item.setValue(type);
        return item;
    }

}
