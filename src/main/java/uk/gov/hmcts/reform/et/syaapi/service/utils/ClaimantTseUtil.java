package uk.gov.hmcts.reform.et.syaapi.service.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.reform.et.syaapi.models.GenericTseApplication;

@Slf4j
public final class ClaimantTseUtil {

    private ClaimantTseUtil() {
    }

    public static GenericTseApplication getCurrentGenericTseApplication(ClaimantTse claimantTse,
                                                 ListTypeItem<GenericTseApplicationType> items,  String caseReference) {
        if (claimantTse == null || items == null) {
            return null;
        }

        UploadedDocumentType contactApplicationFile = claimantTse.getContactApplicationFile();
        String supportingEvidence = contactApplicationFile != null
            ? contactApplicationFile.getDocumentFilename() : null;
        TypeItem<GenericTseApplicationType> tseApplicationType = getGenericTseApplicationType(items);
        String contactApplicationDate = tseApplicationType != null
            ? tseApplicationType.getValue().getDate() : null;
        String contactApplicant = tseApplicationType != null
            ? tseApplicationType.getValue().getApplicant() : null;

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
    }

    private static TypeItem<GenericTseApplicationType> getGenericTseApplicationType(
        ListTypeItem<GenericTseApplicationType> genericTseApplications) {
        if (genericTseApplications == null) {
            return null;
        }

        return genericTseApplications.get(genericTseApplications.size() - 1);
    }
}
