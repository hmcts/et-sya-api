package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.reform.et.syaapi.exception.NotificationException;
import uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.notification.NotificationsProperties;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfDecodedMultipartFile;
import uk.gov.hmcts.reform.et.syaapi.service.util.ServiceUtil;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.tika.utils.StringUtils.isBlank;
import static uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse.APP_TYPE_MAP;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.CASE_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.FILE_NOT_EXISTS;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_ACAS_PDF1_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_ACAS_PDF2_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_ACAS_PDF3_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_ACAS_PDF4_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_ACAS_PDF5_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_CASE_ID;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_CITIZEN_PORTAL_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_CLAIM_DESCRIPTION_FILE_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_DATEPLUS7_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_ET1PDF_ENGLISH_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_ET1PDF_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_ET1PDF_WELSH_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_EXUI_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_FIRSTNAME_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_HEARING_DATE_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_LASTNAME_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_LINK_DOC_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_SHORTTEXT_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_SUBJECTLINE_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_SERVICE_OWNER_NAME_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_SERVICE_OWNER_NAME_VALUE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.UNASSIGNED_OFFICE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.WELSH_LANGUAGE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.WELSH_LANGUAGE_PARAM;
import static uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper.getRespondentNames;
import static uk.gov.service.notify.NotificationClient.prepareUpload;

/**
 * Holds details for sending email to user(s) provided template been created beforehand.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports"})
public class NotificationService {
    public static final String NOT_SET = "Not set";
    private final NotificationClient notificationClient;
    private final NotificationsProperties notificationsProperties;

    private final String[] typeA =
        {"strike", "amend", "non-compliance", "other", "postpone", "vary", "respondent", "publicity"};
    private final String[] typeB = {"withdraw", "change-details", "reconsider-decision", "reconsider-judgement"};
    private static final String TYPE_C = "witness";
    private static final String DONT_SEND_COPY = "No";
    private static final String CONCAT2STRINGS = "%s%s";

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
            String caseId = caseRequest.getCaseId() == null ? CASE_ID_NOT_FOUND : caseRequest.getCaseId();
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
                parameters.put(SEND_EMAIL_PARAMS_CASE_ID, caseId);
                parameters.put(SEND_EMAIL_PARAMS_CITIZEN_PORTAL_LINK_KEY, String.format(citizenPortalLink, caseId));
                parameters.put(SEND_EMAIL_PARAMS_ET1PDF_LINK_KEY, prepareUpload(et1Pdf));

                String claimantEmailAddress = caseData.getClaimantType().getClaimantEmailAddress();
                sendEmailResponse = notificationClient.sendEmail(
                    emailTemplateId,
                    claimantEmailAddress,
                    parameters,
                    caseId
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

    /**
     * Format details of claimant request and retrieve case data, then send email.
     *
     * @param caseData            existing case details
     * @param claimant            claimant's full name
     * @param caseNumber          ethos case reference
     * @param respondentNames     concatenated respondent names
     * @param hearingDate         date of the nearest hearing
     * @param caseId              16 digit case id
     * @param claimantApplication application request data
     * @return Gov notify email format
     */
    public SendEmailResponse sendAcknowledgementEmailToClaimant(CaseData caseData,
                                                                String claimant,
                                                                String caseNumber,
                                                                String respondentNames,
                                                                String hearingDate,
                                                                String caseId,
                                                                ClaimantTse claimantApplication) {

        Map<String, Object> claimantParameters = new ConcurrentHashMap<>();

        addCommonParameters(
            claimantParameters,
            claimant,
            respondentNames,
            caseId,
            caseNumber
        );

        SendEmailResponse claimantEmail;
        String emailToClaimantTemplate = TYPE_C.equals(claimantApplication.getContactApplicationType())
            ? notificationsProperties.getClaimantTseEmailTypeCTemplateId() :
            getAndSetRule92EmailTemplate(claimantApplication, hearingDate, claimantParameters);
        claimantParameters.put(
            SEND_EMAIL_PARAMS_CITIZEN_PORTAL_LINK_KEY,
            String.format(CONCAT2STRINGS, notificationsProperties.getCitizenPortalLink(), caseId)
        );

        try {
            claimantEmail = notificationClient.sendEmail(
                emailToClaimantTemplate,
                caseData.getClaimantType().getClaimantEmailAddress(),
                claimantParameters,
                caseId
            );
        } catch (NotificationClientException ne) {
            throw new NotificationException(ne);
        }
        return claimantEmail;
    }

    /**
     * Format details of claimant request and retrieve case data, then send email to confirmation to respondent.
     *
     * @param caseData            existing case details
     * @param claimant            claimant's full name
     * @param caseNumber          ethos case reference
     * @param respondentNames     concatenated respondent names
     * @param hearingDate         date of the nearest hearing
     * @param caseId              16 digit case id
     * @param claimantApplication application request data
     */
    public void sendAcknowledgementEmailToRespondents(CaseData caseData,
                                                      String claimant,
                                                      String caseNumber,
                                                      String respondentNames,
                                                      String hearingDate,
                                                      String caseId,
                                                      JSONObject documentJson,
                                                      ClaimantTse claimantApplication) {

        if (TYPE_C.equals(claimantApplication.getContactApplicationType())
            || DONT_SEND_COPY.equals(claimantApplication.getCopyToOtherPartyYesOrNo())) {
            log.info("Acknowledgement email not sent to respondents for this application type");
            return;
        }
        Map<String, Object> respondentParameters = new ConcurrentHashMap<>();
        addCommonParameters(
            respondentParameters,
            claimant,
            respondentNames,
            caseId,
            caseNumber
        );
        respondentParameters.put(
            SEND_EMAIL_PARAMS_HEARING_DATE_KEY,
            hearingDate
        );
        respondentParameters.put(
            SEND_EMAIL_PARAMS_SHORTTEXT_KEY,
            APP_TYPE_MAP.get(claimantApplication.getContactApplicationType())
        );
        respondentParameters.put(
            SEND_EMAIL_PARAMS_DATEPLUS7_KEY,
            LocalDate.now().plusDays(7).toString()
        );

        String emailToRespondentTemplate;
        if (Stream.of(typeB).anyMatch(appType -> Objects.equals(
            appType,
            claimantApplication.getContactApplicationType()
        ))) {
            emailToRespondentTemplate = notificationsProperties.getRespondentTseEmailTypeBTemplateId();
        } else {
            emailToRespondentTemplate = notificationsProperties.getRespondentTseEmailTypeATemplateId();
        }
        respondentParameters.put(
            SEND_EMAIL_PARAMS_LINK_DOC_KEY,
            Objects.requireNonNullElse(documentJson, "")
        );
        respondentParameters.put(
            SEND_EMAIL_PARAMS_EXUI_LINK_KEY,
            String.format(CONCAT2STRINGS, notificationsProperties.getExuiCaseDetailsLink(), caseId)
        );

        sendRespondentEmails(caseData, caseId, respondentParameters, emailToRespondentTemplate);
    }

    /**
     * Format details of claimant request and retrieve case data, then send email to confirmation to tribunal.
     *
     * @param caseData        existing case details
     * @param claimant        claimant's full name
     * @param caseNumber      ethos case reference
     * @param respondentNames concatenated respondent names
     * @param hearingDate     date of the nearest hearing
     * @param caseId          16 digit case id
     */
    public void sendAcknowledgementEmailToTribunal(CaseData caseData,
                                                   String claimant,
                                                   String caseNumber,
                                                   String respondentNames,
                                                   String hearingDate,
                                                   String caseId,
                                                   ClaimantTse claimantApplication
    ) {

        Map<String, Object> tribunalParameters = new ConcurrentHashMap<>();

        String subjectLine =
            String.format("%s %s", caseNumber, APP_TYPE_MAP.get(claimantApplication.getContactApplicationType()));
        addCommonParameters(
            tribunalParameters,
            claimant,
            respondentNames,
            caseId,
            caseNumber,
            subjectLine
        );
        tribunalParameters.put(
            SEND_EMAIL_PARAMS_HEARING_DATE_KEY,
            hearingDate
        );
        tribunalParameters.put(
            SEND_EMAIL_PARAMS_EXUI_LINK_KEY,
            String.format(CONCAT2STRINGS, notificationsProperties.getExuiCaseDetailsLink(), caseId)
        );

        String managingOffice = caseData.getManagingOffice();
        if (managingOffice.equals(UNASSIGNED_OFFICE) || isNullOrEmpty(managingOffice)) {
            log.info("Could not send email as no office has been assigned");
        } else {
            try {
                notificationClient.sendEmail(
                    notificationsProperties.getTribunalAcknowledgementTemplateId(),
                    caseData.getTribunalCorrespondenceEmail(),
                    tribunalParameters,
                    caseId
                );
            } catch (NotificationClientException ne) {
                throw new NotificationException(ne);
            }
        }
    }

    /**
     * Format details of claimant request and retrieve case data, then send email to confirmation to tribunal.
     *
     * @param caseData        existing case details
     * @param claimant        claimant's full name
     * @param caseNumber      ethos case reference
     * @param respondentNames concatenated respondent names
     * @param hearingDate     date of the nearest hearing
     * @param caseId          16 digit case id
     * @param applicationType type of application
     */
    public void sendResponseEmailToTribunal(
        CaseData caseData,
        String claimant,
        String caseNumber,
        String respondentNames,
        String hearingDate,
        String caseId,
        String applicationType
    ) {
        String subjectLine = String.format("%s %s", caseNumber, applicationType);

        Map<String, Object> tribunalParameters = new ConcurrentHashMap<>();
        addCommonParameters(
            tribunalParameters,
            claimant,
            respondentNames,
            caseId,
            caseNumber,
            subjectLine,
            applicationType
        );

        tribunalParameters.put(
            SEND_EMAIL_PARAMS_HEARING_DATE_KEY,
            hearingDate
        );
        tribunalParameters.put(
            SEND_EMAIL_PARAMS_EXUI_LINK_KEY,
            String.format(CONCAT2STRINGS, notificationsProperties.getExuiCaseDetailsLink(), caseId)
        );

        sendTribunalEmail(caseData, caseId, tribunalParameters);
    }

    /**
     *  Send acknowledgment email to the claimant when they are responding to
     *  an application (type A/B) made by the Respondent.
     *
     * @param caseData        existing case details
     * @param claimant        claimant's full name
     * @param caseNumber      ethos case reference
     * @param respondentNames concatenated respondent names
     * @param hearingDate     date of the nearest hearing
     * @param caseId          16 digit case id
     * @param applicationType type of application
     * @param tseRespondType  the claimant's response to the application
     */
    public void sendResponseEmailToClaimant(
        CaseData caseData,
        String claimant,
        String caseNumber,
        String respondentNames,
        String hearingDate,
        String caseId,
        String applicationType,
        TseRespondType tseRespondType
    ) {
        if (TYPE_C.equals(applicationType)) {
            log.info("Type C application -  Claimant is only notified of "
                         + "Type A/B application responses, email not being sent");
            return;
        }
        if (isBlank(caseData.getClaimantType().getClaimantEmailAddress())) {
            log.info("No claimant email found - Application response acknowledgment not being sent");
            return;
        }
        Map<String, Object> claimantParameters = new ConcurrentHashMap<>();

        String subjectLine = String.format("%s %s", caseNumber, applicationType);

        addCommonParameters(
            claimantParameters,
            claimant,
            respondentNames,
            caseId,
            caseNumber,
            subjectLine,
            applicationType
        );
        claimantParameters.put(
            SEND_EMAIL_PARAMS_HEARING_DATE_KEY,
            hearingDate
        );
        claimantParameters.put(
            SEND_EMAIL_PARAMS_CITIZEN_PORTAL_LINK_KEY,
            String.format(CONCAT2STRINGS, notificationsProperties.getCitizenPortalLink(), caseId)
        );

        String emailToClaimantTemplate = DONT_SEND_COPY.equals(tseRespondType.getCopyToOtherParty())
            ? notificationsProperties.getPseClaimantResponseNoTemplateId()
            : notificationsProperties.getPseClaimantResponseYesTemplateId();

        try {
            notificationClient.sendEmail(
                emailToClaimantTemplate,
                caseData.getClaimantType().getClaimantEmailAddress(),
                claimantParameters,
                caseId
            );
        } catch (NotificationClientException ne) {
            throw new NotificationException(ne);
        }
    }

    /**
     * Send acknowledgment email to the claimant when they are responding to
     * an application (type A/B) made by the Respondent.
     *
     * @param caseData        existing case details
     * @param claimant        claimant's full name
     * @param caseNumber      ethos case reference
     * @param respondentNames concatenated respondent names
     * @param hearingDate     date of the nearest hearing
     * @param caseId          16 digit case id
     * @param applicationType type of application
     */
    public void sendResponseEmailToRespondent(
        CaseData caseData,
        String claimant,
        String caseNumber,
        String respondentNames,
        String hearingDate,
        String caseId,
        String applicationType,
        String copyToOtherParty
    ) {
        if (TYPE_C.equals(applicationType) || DONT_SEND_COPY.equals(copyToOtherParty)) {
            log.info("Acknowledgement email not sent to respondents for this application type");
            return;
        }
        Map<String, Object> respondentParameters = new ConcurrentHashMap<>();

        String subjectLine = String.format("%s %s", caseNumber, applicationType);
        addCommonParameters(
            respondentParameters,
            claimant,
            respondentNames,
            caseId,
            caseNumber,
            subjectLine,
            applicationType
        );
        respondentParameters.put(
            SEND_EMAIL_PARAMS_HEARING_DATE_KEY,
            hearingDate
        );
        respondentParameters.put(
            SEND_EMAIL_PARAMS_EXUI_LINK_KEY,
            String.format(CONCAT2STRINGS, notificationsProperties.getExuiCaseDetailsLink(), caseId)
        );

        String emailToRespondentTemplate = notificationsProperties.getPseRespondentResponseTemplateId();

        sendRespondentEmails(caseData, caseId, respondentParameters, emailToRespondentTemplate);
    }

    public void sendResponseNotificationEmailToTribunal(CaseData caseData,
                                                        String caseId
    ) {

        Map<String, Object> tribunalParameters = new ConcurrentHashMap<>();
        addCommonParameters(tribunalParameters, caseData, caseId);
        String hearingDate = NotificationsHelper.getNearestHearingToReferral(caseData, NOT_SET);
        tribunalParameters.put(
            SEND_EMAIL_PARAMS_HEARING_DATE_KEY,
            hearingDate
        );
        tribunalParameters.put(
            SEND_EMAIL_PARAMS_EXUI_LINK_KEY,
            String.format(CONCAT2STRINGS, notificationsProperties.getExuiCaseDetailsLink(), caseId)
        );
        sendTribunalEmail(caseData, caseId, tribunalParameters);

    }

    public void sendResponseNotificationEmailToRespondent(
        CaseData caseData,
        String caseId,
        String copyToOtherParty
    ) {

        if (DONT_SEND_COPY.equals(copyToOtherParty)) {
            log.info("Acknowledgement email not sent to respondents");
            return;
        }

        Map<String, Object> respondentParameters = new ConcurrentHashMap<>();
        addCommonParameters(respondentParameters, caseData, caseId);
        String hearingDate = NotificationsHelper.getNearestHearingToReferral(caseData, NOT_SET);
        respondentParameters.put(
            SEND_EMAIL_PARAMS_HEARING_DATE_KEY,
            hearingDate
        );
        respondentParameters.put(
            SEND_EMAIL_PARAMS_EXUI_LINK_KEY,
            String.format(CONCAT2STRINGS, notificationsProperties.getExuiCaseDetailsLink(), caseId)
        );

        sendRespondentEmails(caseData, caseId, respondentParameters,
                             notificationsProperties.getPseRespondentResponseTemplateId());
    }

    public void sendResponseNotificationEmailToClaimant(
        CaseData caseData,
        String caseId,
        String copyToOtherParty
    ) {

        if (isBlank(caseData.getClaimantType().getClaimantEmailAddress())) {
            log.info("No claimant email found - Application response acknowledgment not being sent");
            return;
        }

        String emailToClaimantTemplate = DONT_SEND_COPY.equals(copyToOtherParty)
            ? notificationsProperties.getPseClaimantResponseNoTemplateId()
            : notificationsProperties.getPseClaimantResponseYesTemplateId();

        Map<String, Object> claimantParameters = new ConcurrentHashMap<>();
        addCommonParameters(claimantParameters, caseData, caseId);
        String hearingDate = NotificationsHelper.getNearestHearingToReferral(caseData, NOT_SET);
        claimantParameters.put(
            SEND_EMAIL_PARAMS_HEARING_DATE_KEY,
            hearingDate
        );
        claimantParameters.put(
            SEND_EMAIL_PARAMS_CITIZEN_PORTAL_LINK_KEY,
            String.format(CONCAT2STRINGS, notificationsProperties.getCitizenPortalLink(), caseId)
        );

        try {
            notificationClient.sendEmail(
                emailToClaimantTemplate,
                caseData.getClaimantType().getClaimantEmailAddress(),
                claimantParameters,
                caseId
            );
        } catch (NotificationClientException ne) {
            throw new NotificationException(ne);
        }
    }

    private void sendTribunalEmail(CaseData caseData, String caseId, Map<String, Object> tribunalParameters) {
        String managingOffice = caseData.getManagingOffice();
        if (UNASSIGNED_OFFICE.equals(managingOffice) || isNullOrEmpty(managingOffice)) {
            log.info("Could not send email as no office has been assigned");
            return;
        }

        String tribunalEmail = caseData.getTribunalCorrespondenceEmail();
        if (isNullOrEmpty(tribunalEmail)) {
            log.info("Could not send email. No email found");
            return;
        }

        try {
            notificationClient.sendEmail(
                notificationsProperties.getPseTribunalResponseTemplateId(),
                caseData.getTribunalCorrespondenceEmail(),
                tribunalParameters,
                caseId
            );
        } catch (NotificationClientException ne) {
            throw new NotificationException(ne);
        }

    }

    private void sendRespondentEmails(CaseData caseData, String caseId, Map<String, Object> respondentParameters,
                                      String emailToRespondentTemplate) {
        caseData.getRespondentCollection()
            .forEach(resp -> {
                String respondentEmailAddress = NotificationsHelper.getEmailAddressForRespondent(
                    caseData,
                    resp.getValue()
                );
                if (isNullOrEmpty(respondentEmailAddress)) {
                    log.info(
                        String.format("Respondent %s did not have an email address associated with their account",
                                      resp.getId()));
                } else {
                    try {
                        notificationClient.sendEmail(
                            emailToRespondentTemplate,
                            respondentEmailAddress,
                            respondentParameters,
                            caseId
                        );
                        log.info("Sent email to respondent");
                    } catch (NotificationClientException ne) {
                        throw new NotificationException(ne);
                    }
                }
            });
    }

    private static void addCommonParameters(Map<String, Object> parameters, String claimant, String respondentNames,
                                            String caseId, String caseNumber) {
        parameters.put("claimant", claimant);
        parameters.put("respondentNames", respondentNames);
        parameters.put(SEND_EMAIL_PARAMS_CASE_ID, caseId);
        parameters.put(SEND_EMAIL_PARAMS_CASE_NUMBER_KEY, caseNumber);
    }

    private static void addCommonParameters(Map<String, Object> parameters, String claimant, String respondentNames,
                                            String caseId, String caseNumber, String subjectLine) {
        addCommonParameters(parameters, claimant, respondentNames, caseId, caseNumber);
        parameters.put(SEND_EMAIL_PARAMS_SUBJECTLINE_KEY, subjectLine);
    }

    private static void addCommonParameters(Map<String, Object> parameters, String claimant, String respondentNames,
                                            String caseId, String caseNumber, String subjectLine, String shortText) {
        addCommonParameters(parameters, claimant, respondentNames, caseId, caseNumber, subjectLine);
        parameters.put(SEND_EMAIL_PARAMS_SHORTTEXT_KEY, shortText);
    }

    private static void addCommonParameters(Map<String, Object> parameters, CaseData caseData, String caseId) {
        String claimant = String.format("%s %s",
                                        caseData.getClaimantIndType().getClaimantFirstNames(),
                                        caseData.getClaimantIndType().getClaimantLastName()
        );
        String caseNumber = caseData.getEthosCaseReference();
        String respondentNames = getRespondentNames(caseData);

        addCommonParameters(parameters, claimant, respondentNames, caseId, caseNumber, caseNumber);
    }

    private String getAndSetRule92EmailTemplate(ClaimantTse claimantApplication,
                                                String hearingDate,
                                                Map<String, Object> parameters) {
        String emailTemplate;
        parameters.put(SEND_EMAIL_PARAMS_HEARING_DATE_KEY, hearingDate);
        String shortText = APP_TYPE_MAP.get(claimantApplication.getContactApplicationType());
        if (DONT_SEND_COPY.equals(claimantApplication.getCopyToOtherPartyYesOrNo())) {
            parameters.put(SEND_EMAIL_PARAMS_SHORTTEXT_KEY, shortText);
            emailTemplate = notificationsProperties.getClaimantTseEmailNoTemplateId();
        } else {
            String abText = getCustomTextForAOrBApplication(claimantApplication, shortText);
            parameters.put("abText", abText);
            emailTemplate = notificationsProperties.getClaimantTseEmailYesTemplateId();
        }
        return emailTemplate;
    }

    private String getCustomTextForAOrBApplication(ClaimantTse claimantApplication, String shortText) {
        String abText = "";
        if (Stream.of(typeA).anyMatch(appType -> Objects.equals(
            appType,
            claimantApplication.getContactApplicationType()
        ))) {
            abText = "The other party will be notified that any objections to your "
                + shortText
                + " application should be sent to the tribunal as soon as possible, "
                + "and in any event within 7 days.";

        } else if (Stream.of(typeB).anyMatch(appType -> Objects.equals(
            appType,
            claimantApplication.getContactApplicationType()
        ))) {
            abText = "The other party is not expected to respond to this application. "
                + "However, they have been notified that any objections to your "
                + shortText
                + " application should be sent to the tribunal as soon as possible, "
                + "and in any event within 7 days.";
        }
        return abText;
    }
}
