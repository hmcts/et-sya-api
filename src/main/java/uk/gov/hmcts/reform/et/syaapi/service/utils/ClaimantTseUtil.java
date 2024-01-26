package uk.gov.hmcts.reform.et.syaapi.service.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.TypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.reform.et.syaapi.models.GenericTseApplication;

import java.util.List;

@Slf4j
public final class ClaimantTseUtil {

    private ClaimantTseUtil() {
    }

    public static GenericTseApplication getCurrentGenericTseApplication(ClaimantTse claimantTse,
                                                                        List<TypeItem<GenericTseApplicationType>>
                                                                            items, String caseReference) {
        if (claimantTse == null && items == null) {
            return null;
        }

        UploadedDocumentType contactApplicationFile =
            claimantTse != null ? claimantTse.getContactApplicationFile() : null;
        String supportingEvidence = contactApplicationFile != null
            ? contactApplicationFile.getDocumentFilename() : null;
        TypeItem<GenericTseApplicationType> tseApplicationTypeItem = getGenericTseApplicationTypeItem(items);
        String contactApplicationDate = tseApplicationTypeItem != null
            ? tseApplicationTypeItem.getValue().getDate() : null;
        String contactApplicant = tseApplicationTypeItem != null
            ? tseApplicationTypeItem.getValue().getApplicant() : null;

        if (claimantTse != null) {
            return GenericTseApplication.builder()
                .caseNumber(caseReference)
                .applicant(contactApplicant)
                .applicationType(claimantTse.getContactApplicationType())
                .applicationDate(contactApplicationDate)
                .tellOrAskTribunal(claimantTse.getContactApplicationText())
                .supportingEvidence(supportingEvidence)
                .copyToOtherPartyYesOrNo(claimantTse.getCopyToOtherPartyYesOrNo())
                .copyToOtherPartyText(claimantTse.getCopyToOtherPartyText())
                .build();
        } else {
            return GenericTseApplication.builder()
                .caseNumber(caseReference)
                .applicant(contactApplicant)
                .applicationType("")
                .applicationDate(contactApplicationDate)
                .tellOrAskTribunal("")
                .supportingEvidence("")
                .copyToOtherPartyYesOrNo("")
                .copyToOtherPartyText("")
                .build();
        }
    }

    private static TypeItem<GenericTseApplicationType> getGenericTseApplicationTypeItem(
        List<TypeItem<GenericTseApplicationType>> genericTseApplications) {
        if (genericTseApplications == null) {
            return null;
        }

        return genericTseApplications.get(genericTseApplications.size() - 1);
    }
}
