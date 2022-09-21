package uk.gov.hmcts.reform.et.syaapi.helper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.Et1CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantRequestType;
import uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants;
import uk.gov.hmcts.reform.et.syaapi.constants.JurisdictionCodesConstants;

import java.util.List;
import java.util.stream.Collectors;

class JurisdictionCodesMapperTest {

    private JurisdictionCodesMapper jurisdictionCodesMapper;

    private Et1CaseData data;

    @BeforeEach
    void setUp() {
        data = new Et1CaseData();
        jurisdictionCodesMapper = new JurisdictionCodesMapper();
        ClaimantRequestType requestType = new ClaimantRequestType();
        data.setClaimantRequests(requestType);
    }

    @Test
    void shouldMapJurisdictionCodes() {
        List<String> expectedCodes = List.of(JurisdictionCodesConstants.BOC,
                                             JurisdictionCodesConstants.DAG,
                                             JurisdictionCodesConstants.DDA,
                                             JurisdictionCodesConstants.WA,
                                             JurisdictionCodesConstants.WTR_AL);
        List<JurCodesTypeItem> items = jurisdictionCodesMapper.mapToJurCodes(mockCaseDataWithTypesOfClaims());
        List<String> plainCodes = items.stream()
            .map(it -> it.getValue().getJuridictionCodesList())
            .collect(Collectors.toList());

        Assertions.assertFalse(items.isEmpty());
        Assertions.assertTrue(plainCodes.containsAll(expectedCodes));
    }

    @Test
    void shouldNotMapTypesWithoutJurCodes() {
        data.getClaimantRequests().setPayClaims(List.of(ClaimTypesConstants.OTHER_TYPES,
                                                        ClaimTypesConstants.OTHER_PAYMENTS));
        data.setTypeOfClaim(List.of(ClaimTypesConstants.PAY_RELATED_CLAIM));
        List<JurCodesTypeItem> items = jurisdictionCodesMapper.mapToJurCodes(data);
        Assertions.assertTrue(items.isEmpty());
    }

    @Test
    void shouldNotMapIfTypesNotPresented() {
        List<JurCodesTypeItem> items = jurisdictionCodesMapper.mapToJurCodes(data);
        Assertions.assertTrue(items.isEmpty());
    }

    private Et1CaseData mockCaseDataWithTypesOfClaims() {
        data.setTypeOfClaim(List.of(ClaimTypesConstants.BREACH_OF_CONTRACT, ClaimTypesConstants.DISCRIMINATION));
        data.getClaimantRequests().setDiscriminationClaims(List.of(
            ClaimTypesConstants.AGE,
            ClaimTypesConstants.DISABILITY
        ));
        data.getClaimantRequests().setPayClaims(List.of(ClaimTypesConstants.ARREARS, ClaimTypesConstants.HOLIDAY_PAY));
        return data;
    }

}
