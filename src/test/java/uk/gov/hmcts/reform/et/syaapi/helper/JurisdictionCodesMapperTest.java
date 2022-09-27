package uk.gov.hmcts.reform.et.syaapi.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantRequestType;
import uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants;
import uk.gov.hmcts.reform.et.syaapi.constants.JurisdictionCodesConstants;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class JurisdictionCodesMapperTest {

    private JurisdictionCodesMapper jurisdictionCodesMapper;

    private CaseData data;

    @BeforeEach
    void setUp() {
        data = new CaseData();
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

        assertThat(items).hasAtLeastOneElementOfType(JurCodesTypeItem.class);
        assertThat(plainCodes).containsAll(expectedCodes);
    }

    @Test
    void shouldNotMapTypesWithoutJurCodes() {
        data.getClaimantRequests().setPayClaims(List.of(ClaimTypesConstants.OTHER_TYPES,
                                                        ClaimTypesConstants.OTHER_PAYMENTS));
        data.setTypeOfClaim(List.of(ClaimTypesConstants.PAY_RELATED_CLAIM));
        List<JurCodesTypeItem> items = jurisdictionCodesMapper.mapToJurCodes(data);
        assertThat(items).isEmpty();
    }

    @Test
    void shouldNotMapIfTypesNotPresented() {
        List<JurCodesTypeItem> items = jurisdictionCodesMapper.mapToJurCodes(data);
        assertThat(items).isEmpty();
    }

    private CaseData mockCaseDataWithTypesOfClaims() {
        data.setTypeOfClaim(List.of(ClaimTypesConstants.BREACH_OF_CONTRACT, ClaimTypesConstants.DISCRIMINATION));
        data.getClaimantRequests().setDiscriminationClaims(List.of(
            ClaimTypesConstants.AGE,
            ClaimTypesConstants.DISABILITY
        ));
        data.getClaimantRequests().setPayClaims(List.of(ClaimTypesConstants.ARREARS, ClaimTypesConstants.HOLIDAY_PAY));
        return data;
    }

}
