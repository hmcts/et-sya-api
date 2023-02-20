package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimantApplicationRequest;
import uk.gov.service.notify.NotificationClientException;

import static uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper.getRespondentNames;

@RequiredArgsConstructor
@Service
@Slf4j
public class ApplicationService {
    private final CaseService caseService;
    private final NotificationService notificationService;
    public static final String YES = "Yes";

    public CaseDetails submitApplication(String authorization, ClaimantApplicationRequest request)
        throws NotificationClientException {

        String caseTypeId = request.getCaseTypeId();
        CaseDetails caseDetails = caseService.getUserCase(authorization, request.getCaseId());
        ClaimantTse claimantTse = request.getClaimantTse();
        caseDetails.getData().put("claimantTse", claimantTse);

        UploadedDocumentType contactApplicationFile = claimantTse.getContactApplicationFile();
        if (contactApplicationFile != null) {
            caseService.uploadTseSupportingDocument(caseDetails, contactApplicationFile);
        }

        if (!request.isTypeC() && YES.equals(claimantTse.getCopyToOtherPartyYesOrNo())) {
            try {
                caseService.uploadTseCyaAsPdf(authorization, caseDetails, claimantTse, caseTypeId);
            } catch (CaseDocumentException | DocumentGenerationException e) {
                log.error("Couldn't upload pdf of TSE application");
            }
        }

        CaseDetails finalCaseDetails = caseService.triggerEvent(
            authorization,
            request.getCaseId(),
            CaseEvent.UPDATE_CASE_SUBMITTED,
            caseTypeId,
            caseDetails.getData()
        );

        CaseData caseData = EmployeeObjectMapper.mapRequestCaseDataToCaseData(finalCaseDetails.getData());
        String claimant = caseData.getClaimantIndType().getClaimantFirstNames() + " "
            + caseData.getClaimantIndType().getClaimantLastName();
        String caseNumber = caseData.getEthosCaseReference();
        String respondentNames = getRespondentNames(caseData);
        String hearingDate = NotificationsHelper.getNearestHearingToReferral(caseData, "Not set");
        String caseId = finalCaseDetails.getId() == null ? "case id not found" : finalCaseDetails.getId().toString();

        notificationService.sendAcknowledgementEmailToClaimant(
            caseData,
            claimant,
            caseNumber,
            respondentNames,
            hearingDate,
            caseId,
            request.getClaimantTse()
        );

        notificationService.sendEmailToRespondents(
            caseData,
            claimant,
            caseNumber,
            respondentNames,
            hearingDate,
            caseId,
            request.getClaimantTse()
        );

        notificationService.sendAcknowledgementEmailToTribunal(
            caseData,
            claimant,
            caseNumber,
            respondentNames,
            hearingDate,
            caseId
        );


        return finalCaseDetails;
    }
}
