package uk.gov.hmcts.reform.et.syaapi.service.utils.data;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantRequestType;
import uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants;

import java.util.stream.Stream;

public final class PdfMapperClaimDescriptionUtilTestDataProvider {

    private PdfMapperClaimDescriptionUtilTestDataProvider() {
        // Utility classes should not have a public or default constructor.
    }

    public static Stream<Arguments> generateClaimantRequests() {

        ClaimantRequestType claimantRequestClaimDescriptionNull = new ClaimantRequestType();
        claimantRequestClaimDescriptionNull.setClaimDescription(null);
        ClaimantRequestType claimantRequestClaimDescriptionEmpty = new ClaimantRequestType();
        claimantRequestClaimDescriptionEmpty.setClaimDescription(TestConstants.EMPTY_STRING);
        ClaimantRequestType claimantRequestClaimDescriptionBlank = new ClaimantRequestType();
        claimantRequestClaimDescriptionBlank.setClaimDescription(TestConstants.BLANK_STRING);
        ClaimantRequestType claimantRequestClaimDescriptionFilled = new ClaimantRequestType();
        claimantRequestClaimDescriptionFilled.setClaimDescription(TestConstants.CLAIM_DESCRIPTION);
        ClaimantRequestType claimantRequestEmpty = new ClaimantRequestType();

        return Stream.of(
            Arguments.of(null, null),
            Arguments.of(claimantRequestEmpty, null),
            Arguments.of(claimantRequestClaimDescriptionNull, null),
            Arguments.of(claimantRequestClaimDescriptionEmpty, null),
            Arguments.of(claimantRequestClaimDescriptionBlank, null),
            Arguments.of(claimantRequestClaimDescriptionFilled, TestConstants.CLAIM_DESCRIPTION)
        );
    }

}
