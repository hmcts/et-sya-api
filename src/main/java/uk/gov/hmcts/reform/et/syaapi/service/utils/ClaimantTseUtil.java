package uk.gov.hmcts.reform.et.syaapi.service.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.reform.et.syaapi.models.GenericTseApplication;

import java.util.List;

@Slf4j
public final class ClaimantTseUtil {

    private ClaimantTseUtil() {
    }

    public static GenericTseApplication getCurrentGenericTseApplication(ClaimantTse claimantTse,
                                                 List<GenericTseApplicationTypeItem> items) {
        if (claimantTse == null || items == null) {
            return null;
        }

        UploadedDocumentType contactApplicationFile = claimantTse.getContactApplicationFile();
        String supportingEvidence = contactApplicationFile != null
            ? contactApplicationFile.getDocumentFilename() : null;
        GenericTseApplicationTypeItem tseApplicationTypeItem = getGenericTseApplicationTypeItem(items);
        String contactApplicationDate = tseApplicationTypeItem != null
            ? tseApplicationTypeItem.getValue().getDate() : null;
        String contactApplicant = tseApplicationTypeItem != null
            ? tseApplicationTypeItem.getValue().getApplicant() : null;

        return GenericTseApplication.builder()
        .applicant(contactApplicant)
        .applicationType(claimantTse.getContactApplicationType())
        .applicationDate(contactApplicationDate)
        .tellOrAskTribunal(claimantTse.getContactApplicationText())
        .supportingEvidence(supportingEvidence)
        .copyToOtherPartyYesOrNo(claimantTse.getCopyToOtherPartyYesOrNo())
        .copyToOtherPartyText(claimantTse.getCopyToOtherPartyText())
        .build();
    }

    private static GenericTseApplicationTypeItem getGenericTseApplicationTypeItem(
        List<GenericTseApplicationTypeItem> genericTseApplications) {
        if (genericTseApplications == null) {
            return null;
        }

        return genericTseApplications.get(genericTseApplications.size() - 1);
    }
}
