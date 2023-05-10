package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.reform.et.syaapi.exception.NotificationException;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.notification.NotificationsProperties;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfDecodedMultipartFile;
import uk.gov.hmcts.reform.et.syaapi.service.util.ServiceUtil;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.CASE_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.FILE_NOT_EXISTS;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_ACAS_PDF1_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_ACAS_PDF2_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_ACAS_PDF3_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_ACAS_PDF4_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_ACAS_PDF5_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_CITIZEN_PORTAL_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_CLAIM_DESCRIPTION_FILE_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_ET1PDF_ENGLISH_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_ET1PDF_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_ET1PDF_WELSH_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_FIRSTNAME_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_LASTNAME_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_SERVICE_OWNER_NAME_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_SERVICE_OWNER_NAME_VALUE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.WELSH_LANGUAGE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.WELSH_LANGUAGE_PARAM;
import static uk.gov.service.notify.NotificationClient.prepareUpload;

/**
 * Holds details for sending email to user(s) provided template been created beforehand.
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationClient notificationClient;
    private final NotificationsProperties notificationsProperties;

    /**
     * Given a caseId, initialization of trigger event to start and submit update for case.
     *
     * @param templateId  - pass gov notify template id for each use case
     * @param targetEmail - recipient target email id
     * @param parameters  - map of strings to add this to the template
     * @param reference   - reference string for email template
     * @return response from notification api
     */
    public SendEmailResponse sendEmail(
        String templateId, String targetEmail, Map<String, String> parameters, String reference) {
        SendEmailResponse sendEmailResponse;
        try {
            sendEmailResponse = notificationClient.sendEmail(templateId, targetEmail, parameters, reference);
        } catch (NotificationClientException ne) {
            ServiceUtil.logException("Error while trying to sending notification to client",
                                     ServiceUtil.getStringValueFromStringMap(parameters,
                                                                             SEND_EMAIL_PARAMS_CASE_NUMBER_KEY),
                                     ne.getMessage(),
                                     this.getClass().getName(), "sendEmail");
            throw new NotificationException(ne);
        }
        return sendEmailResponse;
    }

    /**
     * Prepares case submission confirmation email content from user and case data & sends email to the user.
     *
     * @param caseRequest  top level non-modifiable case details
     * @param caseData  user provided data
     * @param userInfo   user details from Idam
     * @param casePdfFiles  pdf files of the ET1 form according to selected language
     * @return Gov notify email format
     */
    public SendEmailResponse sendSubmitCaseConfirmationEmail(CaseRequest caseRequest, CaseData caseData,
                                                             UserInfo userInfo,
                                                             List<PdfDecodedMultipartFile> casePdfFiles) {
        SendEmailResponse sendEmailResponse = null;
        if (ServiceUtil.hasPdfFile(casePdfFiles, 0)) {
            String firstName = ServiceUtil.findClaimantFirstNameByCaseDataUserInfo(caseData, userInfo);
            String lastName = ServiceUtil.findClaimantLastNameByCaseDataUserInfo(caseData, userInfo);
            String caseNumber = caseRequest.getCaseId() == null ? CASE_ID_NOT_FOUND : caseRequest.getCaseId();
            String selectedLanguage = ServiceUtil.findClaimantLanguage(caseData);

            boolean isWelsh = WELSH_LANGUAGE.equals(selectedLanguage);
            String emailTemplateId = isWelsh
                ? notificationsProperties.getCySubmitCaseEmailTemplateId()
                : notificationsProperties.getSubmitCaseEmailTemplateId();
            String citizenPortalLink = notificationsProperties.getCitizenPortalLink() + "%s"
                + (isWelsh ? WELSH_LANGUAGE_PARAM : "");

            byte[] et1Pdf = ServiceUtil.findPdfFileBySelectedLanguage(casePdfFiles, selectedLanguage);
            try {
                Map<String, Object> parameters = new ConcurrentHashMap<>();
                parameters.put(SEND_EMAIL_PARAMS_FIRSTNAME_KEY, firstName);
                parameters.put(SEND_EMAIL_PARAMS_LASTNAME_KEY, lastName);
                parameters.put(SEND_EMAIL_PARAMS_CASE_NUMBER_KEY, caseNumber);
                parameters.put(SEND_EMAIL_PARAMS_CITIZEN_PORTAL_LINK_KEY, String.format(citizenPortalLink, caseNumber));
                parameters.put(SEND_EMAIL_PARAMS_ET1PDF_LINK_KEY, prepareUpload(et1Pdf));

                String claimantEmailAddress = caseData.getClaimantType().getClaimantEmailAddress();
                sendEmailResponse = notificationClient.sendEmail(
                    emailTemplateId,
                    claimantEmailAddress,
                    parameters,
                    caseNumber
                );
            } catch (NotificationClientException ne) {
                ServiceUtil.logException("Submit case confirmation email was not sent to client.",
                                         caseData.getEthosCaseReference(), ne.getMessage(),
                                         this.getClass().getName(), "sendSubmitCaseConfirmationEmail");
            }
        }
        return sendEmailResponse;
    }

    /**
     * Prepared doc upload error alert email content from user and case data then sends email to the service.
     *
     * @param caseRequest  top level non-modifiable case details
     * @param casePdfFiles  pdf copy of ET1 form content
     * @param acasCertificates  pdf copy of Acas Certificates
     * @return Gov notify email format
     */
    public SendEmailResponse sendDocUploadErrorEmail(CaseRequest caseRequest,
                                                     List<PdfDecodedMultipartFile> casePdfFiles,
                                                     List<PdfDecodedMultipartFile> acasCertificates,
                                                     UploadedDocumentType claimDescriptionDocument) {
        SendEmailResponse sendEmailResponse = null;
        try {
            String caseNumber = caseRequest.getCaseId() == null ? CASE_ID_NOT_FOUND : caseRequest.getCaseId();
            Map<String, Object> parameters = new ConcurrentHashMap<>();
            parameters.put(SEND_EMAIL_SERVICE_OWNER_NAME_KEY, SEND_EMAIL_SERVICE_OWNER_NAME_VALUE);
            parameters.put(SEND_EMAIL_PARAMS_CASE_NUMBER_KEY, caseNumber);
            parameters.put(SEND_EMAIL_PARAMS_ET1PDF_ENGLISH_LINK_KEY, ServiceUtil.prepareUpload(casePdfFiles, 0));
            parameters.put(SEND_EMAIL_PARAMS_ET1PDF_WELSH_LINK_KEY, ServiceUtil.prepareUpload(casePdfFiles, 1));
            parameters.put(SEND_EMAIL_PARAMS_ACAS_PDF1_LINK_KEY, ServiceUtil.prepareUpload(acasCertificates, 0));
            parameters.put(SEND_EMAIL_PARAMS_ACAS_PDF2_LINK_KEY, ServiceUtil.prepareUpload(acasCertificates, 1));
            parameters.put(SEND_EMAIL_PARAMS_ACAS_PDF3_LINK_KEY, ServiceUtil.prepareUpload(acasCertificates, 2));
            parameters.put(SEND_EMAIL_PARAMS_ACAS_PDF4_LINK_KEY, ServiceUtil.prepareUpload(acasCertificates, 3));
            parameters.put(SEND_EMAIL_PARAMS_ACAS_PDF5_LINK_KEY, ServiceUtil.prepareUpload(acasCertificates, 4));
            parameters.put(SEND_EMAIL_PARAMS_CLAIM_DESCRIPTION_FILE_LINK_KEY,
                           ObjectUtils.isNotEmpty(claimDescriptionDocument)
                                && StringUtils.isNotBlank(claimDescriptionDocument.getDocumentUrl())
                                ? claimDescriptionDocument.getDocumentUrl()
                                : FILE_NOT_EXISTS);

            String emailTemplateId = notificationsProperties.getSubmitCaseDocUploadErrorEmailTemplateId();

            // Send an alert email to the service owner
            sendEmailResponse = notificationClient.sendEmail(
                emailTemplateId,
                notificationsProperties.getEt1ServiceOwnerNotificationEmail(),
                parameters,
                caseNumber
            );

            // Send a copy alert email to ECM DTS core team
            notificationClient.sendEmail(
                emailTemplateId,
                notificationsProperties.getEt1EcmDtsCoreTeamSlackNotificationEmail(),
                parameters,
                caseNumber
            );
        } catch (NotificationClientException ne) {
            ServiceUtil.logException("Case Documents Upload error - Failed to send document upload error message",
                                     caseRequest.getCaseId(), ne.getMessage(),
                                     this.getClass().getName(), "sendDocUploadErrorEmail");
        }
        return sendEmailResponse;
    }
}
