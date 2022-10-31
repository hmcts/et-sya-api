package uk.gov.hmcts.reform.et.syaapi.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.service.AssignCaseToLocalOfficeService;
import uk.gov.hmcts.reform.et.syaapi.service.PostcodeToOfficeService;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"PMD.UnusedPrivateField"})
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
    void shouldAssignManagingAddressFromClaimantWorkAddress() {
        CaseRequest request = testData.getCaseRequest();
        assertThat(
            assignCaseToLocalOfficeService.convertCaseRequestToCaseDataWithTribunalOffice(request).getManagingOffice())
            .isEqualTo("London South");
    }

    @Test
    void shouldAssignNullToManagingAddressIfNoManagingAddressAndNoRespondentsAddressesArePresent() {
        CaseRequest request = testData.getEmptyCaseRequest();
        assertThat(
            assignCaseToLocalOfficeService.convertCaseRequestToCaseDataWithTribunalOffice(request).getManagingOffice())
            .isNull();
    }

    @Test
    void shouldAssignManagingAddressFromOneOfRespondentAddress() {
        CaseRequest request = testData.getCaseRequestWithoutManagingAddress();
        assertThat(
            assignCaseToLocalOfficeService.convertCaseRequestToCaseDataWithTribunalOffice(request).getManagingOffice())
            .isEqualTo("London South");
    }
}
