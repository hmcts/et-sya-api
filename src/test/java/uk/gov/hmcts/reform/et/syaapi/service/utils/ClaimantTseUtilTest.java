package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.reform.et.syaapi.models.GenericTseApplication;

import java.time.LocalDate;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

class ClaimantTseUtilTest {

    private static Stream<Arguments> checkGetGenericTseApplicationFromClaimantTse() {
        String applicationText = "Test details";
        String documentFilename = "test-Document-Filename.pdf";
        String copyToOtherPartyText = "No details";
        return Stream.of(
            Arguments.of(applicationText, documentFilename, YES, null),
            Arguments.of(applicationText, null, YES, null),
            Arguments.of(null, documentFilename, YES, null),
            Arguments.of(applicationText, documentFilename, NO, copyToOtherPartyText),
            Arguments.of(applicationText, null, NO, copyToOtherPartyText),
            Arguments.of(null, documentFilename, NO, copyToOtherPartyText)
        );
    }

    @ParameterizedTest
    @MethodSource()
    void checkGetGenericTseApplicationFromClaimantTse(
        String applicationText,
        String documentFilename,
        String copyToOtherPartyYesOrNo,
        String copyToOtherPartyText
    ) {
        String caseReference = "1234/456";
        ClaimantTse claimantTse =
            getClaimantTse(applicationText, documentFilename, copyToOtherPartyYesOrNo, copyToOtherPartyText);

        GenericTseApplication actualGenericTseApplication = ClaimantTseUtil.getGenericTseApplicationFromClaimantTse(
            claimantTse, caseReference);

        assertThat(actualGenericTseApplication.getCaseNumber())
            .isEqualTo(caseReference);
        assertThat(actualGenericTseApplication.getApplicant())
            .isEqualTo(CLAIMANT_TITLE);
        assertThat(actualGenericTseApplication.getApplicationType())
            .isEqualTo("Change my personal details");
        assertThat(actualGenericTseApplication.getApplicationDate())
            .isEqualTo(UtilHelper.formatCurrentDate(LocalDate.now()));
        assertThat(actualGenericTseApplication.getTellOrAskTribunal())
            .isEqualTo(applicationText);
        assertThat(actualGenericTseApplication.getSupportingEvidence())
            .isEqualTo(documentFilename);
        assertThat(actualGenericTseApplication.getCopyToOtherPartyYesOrNo())
            .isEqualTo(copyToOtherPartyYesOrNo);
        assertThat(actualGenericTseApplication.getCopyToOtherPartyText())
            .isEqualTo(copyToOtherPartyText);
    }

    private static ClaimantTse getClaimantTse(String applicationText, String documentFilename,
                                              String copyToOtherPartyYesOrNo, String copyToOtherPartyText) {
        ClaimantTse claimantTse = new ClaimantTse();
        claimantTse.setContactApplicationText(applicationText);
        claimantTse.setContactApplicationType("change-details");
        claimantTse.setCopyToOtherPartyYesOrNo(copyToOtherPartyYesOrNo);
        claimantTse.setCopyToOtherPartyText(copyToOtherPartyText);

        if (!isNullOrEmpty(documentFilename)) {
            UploadedDocumentType docType = new UploadedDocumentType();
            docType.setDocumentFilename("test-Document-Filename.pdf");
            claimantTse.setContactApplicationFile(docType);
        }

        return claimantTse;
    }
}
