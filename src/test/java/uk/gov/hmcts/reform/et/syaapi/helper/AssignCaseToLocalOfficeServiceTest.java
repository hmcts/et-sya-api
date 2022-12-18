package uk.gov.hmcts.reform.et.syaapi.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.dwp.regex.InvalidPostcodeException;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.service.AssignCaseToLocalOfficeService;
import uk.gov.hmcts.reform.et.syaapi.service.PostcodeToOfficeService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.UNASSIGNED_OFFICE;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.LawOfDemeter"})
class AssignCaseToLocalOfficeServiceTest {
    @InjectMocks
    private AssignCaseToLocalOfficeService assignCaseToLocalOfficeService;
    @Mock
    private PostcodeToOfficeService postcodeToOfficeService;
    private TestData testData;

    @BeforeEach
    void beforeEach() {
        testData = new TestData();
    }

    @Test
    void shouldAssignManagingAddressFromClaimantWorkAddress() throws InvalidPostcodeException {
        CaseRequest request = testData.getCaseRequest();
        when(postcodeToOfficeService.getTribunalOfficeFromPostcode(any())).thenReturn(Optional.of(
            TribunalOffice.GLASGOW
        ));
        assertThat(
            assignCaseToLocalOfficeService.convertCaseRequestToCaseDataWithTribunalOffice(request).getManagingOffice())
            .isEqualTo("Glasgow");
    }

    @Test
    void shouldAssignUnassignedToManagingAddressIfNoManagingAddressAndNoRespondentsAddressesArePresent() {
        CaseRequest request = testData.getEmptyCaseRequest();
        assertThat(
            assignCaseToLocalOfficeService.convertCaseRequestToCaseDataWithTribunalOffice(request).getManagingOffice())
            .isEqualTo(UNASSIGNED_OFFICE);
    }

    @Test
    void shouldAssignManagingAddressFromOneOfRespondentAddress() throws InvalidPostcodeException {
        CaseRequest request = testData.getCaseRequestWithoutManagingAddress();
        when(postcodeToOfficeService.getTribunalOfficeFromPostcode(any())).thenReturn(Optional.of(
            TribunalOffice.GLASGOW
        ));
        assertThat(
            assignCaseToLocalOfficeService.convertCaseRequestToCaseDataWithTribunalOffice(request).getManagingOffice())
            .isEqualTo("Glasgow");
    }

    @Test
    void shouldAssignUnassignedIfCaseTypeIdIsScotlandAndPostCodeFromEnglandArea() throws InvalidPostcodeException {
        CaseRequest request = testData.getCaseRequestWithoutManagingAddress();

        when(postcodeToOfficeService.getTribunalOfficeFromPostcode(any())).thenReturn(Optional.of(
            TribunalOffice.LEEDS
        ));

        assertThat(
            assignCaseToLocalOfficeService.convertCaseRequestToCaseDataWithTribunalOffice(request).getManagingOffice())
            .isEqualTo(UNASSIGNED_OFFICE);
    }

    @Test
    void shouldAssignUnassignedIfCaseTypeIdIsEnglandAndPostCodeFromScotlandArea() throws InvalidPostcodeException {
        CaseRequest request = testData.getEnglandWalesRequest();

        when(postcodeToOfficeService.getTribunalOfficeFromPostcode(any())).thenReturn(Optional.of(
            TribunalOffice.EDINBURGH
        ));

        assertThat(
            assignCaseToLocalOfficeService.convertCaseRequestToCaseDataWithTribunalOffice(request).getManagingOffice())
            .isEqualTo(UNASSIGNED_OFFICE);
    }

    @Test
    void shouldAssignAnyScottlandOfficeToGlasgowByDefault() throws InvalidPostcodeException {
        CaseRequest request = testData.getCaseRequestWithoutManagingAddress();

        when(postcodeToOfficeService.getTribunalOfficeFromPostcode(any())).thenReturn(Optional.of(
            TribunalOffice.DUNDEE
        ));

        assertThat(
            assignCaseToLocalOfficeService.convertCaseRequestToCaseDataWithTribunalOffice(request).getManagingOffice())
            .isEqualTo(TribunalOffice.GLASGOW.getOfficeName());
    }
}
