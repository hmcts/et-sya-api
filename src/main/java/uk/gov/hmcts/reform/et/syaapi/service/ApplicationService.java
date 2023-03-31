package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper;
import uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimantApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.models.RespondToApplicationRequest;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse.APP_TYPE_MAP;
import static uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper.getRespondentNames;

@RequiredArgsConstructor
@Service
@Slf4j
public class ApplicationService {
    private static final String TSE_FILENAME = "Contact the tribunal.pdf";
    private final CaseService caseService;
    private final NotificationService notificationService;
    private final CaseDocumentService caseDocumentService;
    private final CaseDetailsConverter caseDetailsConverter;
    public static final String YES = "Yes";
    public static final String WEEKS_78 = "78 weeks";

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
                log.error("Couldn't upload pdf of TSE application " + e.getMessage());
            }
        }

        CaseDetails finalCaseDetails = caseService.triggerEvent(
            authorization,
            request.getCaseId(),
            CaseEvent.SUBMIT_CLAIMANT_TSE,
            caseTypeId,
            caseDetails.getData()
        );

        sendAcknowledgementEmails(authorization, request, finalCaseDetails);

        return finalCaseDetails;
    }

    public CaseDetails respondToApplication(String authorization, RespondToApplicationRequest request) {
        StartEventResponse startEventResponse = caseService.startUpdate(
            authorization,
            request.getCaseId(),
            request.getCaseTypeId(),
            CaseEvent.CLAIMANT_TSE_RESPOND
        );

        CaseData caseData = EmployeeObjectMapper
            .mapRequestCaseDataToCaseData(startEventResponse.getCaseDetails().getData());

        GenericTseApplicationTypeItem appToModify = TseApplicationHelper.getSelectedApplication(
            request,
            caseData.getGenericTseApplicationCollection()
        );
        if (appToModify != null) {
            TseApplicationHelper.setRespondentApplicationWithResponse(
                request,
                appToModify.getValue(),
                caseData,
                caseDocumentService
            );

            createPdfOfResponse(authorization, request, caseData, appToModify.getValue());

            CaseDataContent content = caseDetailsConverter.caseDataContent(startEventResponse, caseData);
            CaseDetails caseDetails = caseService.submitUpdate(
                authorization,
                request.getCaseId(),
                content,
                request.getCaseTypeId()
            );

            sendResponseToApplicationEmails(appToModify.getValue(), caseData, request.getCaseId());

            return caseDetails;
        } else {
            throw new IllegalArgumentException("Application id provided is incorrect");
        }
    }

    private void createPdfOfResponse(String authorization,
                                     RespondToApplicationRequest request,
                                     CaseData caseData,
                                     GenericTseApplicationType application) {
        if (YES.equals(request.getResponse().getCopyToOtherParty())) {
            try {
                log.info("Uploading pdf of claimant response to application");
                caseService.createResponsePdf(
                    authorization,
                    caseData,
                    request.getCaseTypeId(),
                    request.getResponse(),
                    application.getType()
                );
            } catch (CaseDocumentException | DocumentGenerationException e) {
                log.error("Couldn't upload pdf of TSE application " + e.getMessage());
            }
        }
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

    private void sendResponseToApplicationEmails(GenericTseApplicationType application,
                                                 CaseData caseData,
                                                 String caseId) {
        String claimant = caseData.getClaimantIndType().getClaimantFirstNames() + " "
            + caseData.getClaimantIndType().getClaimantLastName();

        String caseNumber = caseData.getEthosCaseReference();
        String respondentNames = getRespondentNames(caseData);
        String hearingDate = NotificationsHelper.getNearestHearingToReferral(caseData, "Not set");

        notificationService.sendResponseEmailToTribunal(
            caseData,
            claimant,
            caseNumber,
            respondentNames,
            hearingDate,
            caseId,
            application.getType()
        );
    }

    private JSONObject getDocumentDownload(String authorization, CaseData caseData)
        throws NotificationClientException {

        List<DocumentTypeItem> tseFiles = caseData.getDocumentCollection().stream()
            .filter(n -> TSE_FILENAME.equals(n.getValue().getUploadedDocument().getDocumentFilename()))
            .collect(Collectors.toList());
        if (!tseFiles.isEmpty()) {
            String documentUrl = tseFiles.get(tseFiles.size() - 1)
                .getValue().getUploadedDocument().getDocumentUrl();
            String docId = documentUrl.substring(documentUrl.lastIndexOf('/') + 1);
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
                    WEEKS_78
                );
            }
        }
        return null;
    }
}
