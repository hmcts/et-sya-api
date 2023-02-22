package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimantApplicationRequest;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.UUID;

import static uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper.getRespondentNames;

@RequiredArgsConstructor
@Service
@Slf4j
public class ApplicationService {
    private final CaseService caseService;
    private final NotificationService notificationService;
    private final CaseDocumentService caseDocumentService;
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

        sendAcknowledgementEmails(authorization, request, finalCaseDetails);

        return finalCaseDetails;
    }

    private void sendAcknowledgementEmails(String authorization,
                                           ClaimantApplicationRequest request,
                                           CaseDetails finalCaseDetails) throws NotificationClientException {
        CaseData caseData = EmployeeObjectMapper.mapRequestCaseDataToCaseData(finalCaseDetails.getData());
        String claimant = caseData.getClaimantIndType().getClaimantFirstNames() + " "
            + caseData.getClaimantIndType().getClaimantLastName();
        String caseNumber = caseData.getEthosCaseReference();
        String respondentNames = getRespondentNames(caseData);
        String hearingDate = NotificationsHelper.getNearestHearingToReferral(caseData, "Not set");
        String caseId = finalCaseDetails.getId().toString();

        notificationService.sendAcknowledgementEmailToClaimant(
            caseData,
            claimant,
            caseNumber,
            respondentNames,
            hearingDate,
            caseId,
            request.getClaimantTse()
        );

        Object documentJson = getDocumentDownload(authorization, request.getClaimantTse());

        notificationService.sendEmailToRespondents(
            caseData,
            claimant,
            caseNumber,
            respondentNames,
            hearingDate,
            caseId,
            documentJson,
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
    }

    private Object getDocumentDownload(String authorization, ClaimantTse claimantApplication)
        throws NotificationClientException {
        String documentBinaryUrl = claimantApplication.getContactApplicationFile().getDocumentBinaryUrl();
        if (documentBinaryUrl == null) {
            return "Supporting file was not provided by claimant.";
        }
        String docId = documentBinaryUrl.substring(documentBinaryUrl.lastIndexOf('/') + 1);
        UUID doc = UUID.fromString(docId);
        ByteArrayResource downloadDocument = caseDocumentService.downloadDocument(
            authorization,
            doc
        ).getBody();
        if (downloadDocument != null) {
            return NotificationClient.prepareUpload(
                downloadDocument.getByteArray(),
                false,
                true,
                "52 weeks"
            );
        }
        return "Could not retrieve claimant's supporting file.";
    }
}
