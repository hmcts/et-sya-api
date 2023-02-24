package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimantApplicationRequest;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper.getRespondentNames;

@RequiredArgsConstructor
@Service
@Slf4j
public class ApplicationService {
    private static final String TSE_FILENAME = "Contact the tribunal.pdf";
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
            log.info("Uploading supporting file to document collection");
            caseService.uploadTseSupportingDocument(caseDetails, contactApplicationFile);
        }

        if (!request.isTypeC() && YES.equals(claimantTse.getCopyToOtherPartyYesOrNo())) {
            try {
                log.info("Uploading pdf of TSE application");
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

        JSONObject documentJson = getDocumentDownload(authorization, caseData);

        notificationService.sendAcknowledgementEmailToRespondents(
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
            caseId,
            request.getClaimantTse()
        );
    }

    private JSONObject getDocumentDownload(String authorization, CaseData caseData)
        throws NotificationClientException {

        List<DocumentTypeItem> tseFiles = caseData.getDocumentCollection().stream()
            .filter(n -> TSE_FILENAME.equals(n.getValue().getUploadedDocument().getDocumentFilename()))
            .collect(Collectors.toList());
        if (!tseFiles.isEmpty()) {
            String documentBinaryUrl = Collections.max(
                tseFiles,
                Comparator.comparing(c -> c.getValue().getCreationDate())
            ).getValue().getUploadedDocument().getDocumentBinaryUrl();

            String docId = documentBinaryUrl.substring(documentBinaryUrl.lastIndexOf('/') + 1);
            UUID doc = UUID.fromString(docId);

            log.info("Downloading pdf of TSE application");
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
        }
        return null;
    }
}
