package uk.gov.hmcts.reform.et.syaapi.service.pdf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PdfMapperUtilTest {

    @Test
    void convertAddressToString() {
        // Given
        Address address = new TestData().getCaseData().getClaimantType().getClaimantAddressUK();
        String expectedAddressString = "Co-operative Retail Services Ltd, 11, Merrion Way\n"
            + "Leeds\n"
            + "England";
        // When
        String actualAddressString = PdfMapperUtil.convertAddressToString(address);
        // Then
        assertEquals(actualAddressString, expectedAddressString);
    }

}
