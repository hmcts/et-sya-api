package uk.gov.hmcts.reform.et.syaapi.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.dwp.regex.InvalidPostcodeException;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.service.PostcodeToOfficeService;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.service.CaseOfficeService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.UNASSIGNED_OFFICE;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.LawOfDemeter"})
class AssignCaseToLocalOfficeServiceTest {
    @InjectMocks
    private CaseOfficeService assignCaseToLocalOfficeService;
    @Mock
    private PostcodeToOfficeService postcodeToOfficeService;
    private CaseTestData caseTestData;

    @BeforeEach
    void beforeEach() {
        caseTestData = new CaseTestData();
    }

    @Test
    void shouldAssignManagingAddressFromClaimantWorkAddress() throws InvalidPostcodeException {
        CaseRequest request = caseTestData.getCaseRequest();
        when(postcodeToOfficeService.getTribunalOfficeFromPostcode(any())).thenReturn(Optional.of(
            TribunalOffice.GLASGOW
        ));
        assertThat(
            assignCaseToLocalOfficeService.convertCaseRequestToCaseDataWithTribunalOffice(request).getManagingOffice())
            .isEqualTo("Glasgow");
    }

    @Test
    void shouldReturnAssignedForWrongPostcode() throws InvalidPostcodeException {
        CaseRequest request = caseTestData.getCaseRequest();
        when(postcodeToOfficeService.getTribunalOfficeFromPostcode(any()))
            .thenThrow(new InvalidPostcodeException(""));
        assertThat(assignCaseToLocalOfficeService.convertCaseRequestToCaseDataWithTribunalOffice(
            request).getManagingOffice()).isEqualTo("Unassigned");
    }

    @Test
    void shouldReturnAssignedForEmptyOffice() throws InvalidPostcodeException {
        CaseRequest request = caseTestData.getCaseRequest();
        when(postcodeToOfficeService.getTribunalOfficeFromPostcode(any()))
            .thenReturn(Optional.empty());
        assertThat(assignCaseToLocalOfficeService.convertCaseRequestToCaseDataWithTribunalOffice(
            request).getManagingOffice()).isEqualTo("Unassigned");
    }

    @Test
    void shouldAssignUnassignedToManagingAddressIfNoManagingAddressAndNoRespondentsAddressesArePresent() {
        CaseRequest request = caseTestData.getEmptyCaseRequest();
        assertThat(
            assignCaseToLocalOfficeService.convertCaseRequestToCaseDataWithTribunalOffice(request).getManagingOffice())
            .isEqualTo(UNASSIGNED_OFFICE);
    }

    @Test
    void shouldAssignManagingAddressFromOneOfRespondentAddress() throws InvalidPostcodeException {
        CaseRequest request = caseTestData.getCaseRequestWithoutManagingAddress();
        when(postcodeToOfficeService.getTribunalOfficeFromPostcode(any())).thenReturn(Optional.of(
            TribunalOffice.GLASGOW
        ));
        assertThat(
            assignCaseToLocalOfficeService.convertCaseRequestToCaseDataWithTribunalOffice(request).getManagingOffice())
            .isEqualTo("Glasgow");
    }

    @Test
    void shouldAssignUnassignedIfCaseTypeIdIsScotlandAndPostCodeFromEnglandArea() throws InvalidPostcodeException {
        CaseRequest request = caseTestData.getCaseRequestWithoutManagingAddress();

        when(postcodeToOfficeService.getTribunalOfficeFromPostcode(any())).thenReturn(Optional.of(
            TribunalOffice.LEEDS
        ));

        assertThat(
            assignCaseToLocalOfficeService.convertCaseRequestToCaseDataWithTribunalOffice(request).getManagingOffice())
            .isEqualTo(UNASSIGNED_OFFICE);
    }

    @Test
    void shouldAssignUnassignedIfCaseTypeIdIsEnglandAndPostCodeFromScotlandArea() throws InvalidPostcodeException {
        CaseRequest request = caseTestData.getEnglandWalesRequest();

        when(postcodeToOfficeService.getTribunalOfficeFromPostcode(any())).thenReturn(Optional.of(
            TribunalOffice.EDINBURGH
        ));

        assertThat(
            assignCaseToLocalOfficeService.convertCaseRequestToCaseDataWithTribunalOffice(request).getManagingOffice())
            .isEqualTo(UNASSIGNED_OFFICE);
    }

    @Test
    void shouldAssignAnyScottlandOfficeToGlasgowByDefault() throws InvalidPostcodeException {
        CaseRequest request = caseTestData.getCaseRequestWithoutManagingAddress();

        when(postcodeToOfficeService.getTribunalOfficeFromPostcode(any())).thenReturn(Optional.of(
            TribunalOffice.DUNDEE
        ));

        assertThat(
            assignCaseToLocalOfficeService.convertCaseRequestToCaseDataWithTribunalOffice(request).getManagingOffice())
            .isEqualTo(TribunalOffice.GLASGOW.getOfficeName());
    }
}
