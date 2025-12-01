package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.service.pdf.PdfDecodedMultipartFile;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentTse;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.reform.et.syaapi.exception.NotificationException;
import uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.notification.NotificationsProperties;
import uk.gov.hmcts.reform.et.syaapi.service.utils.GenericServiceUtil;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.tika.utils.StringUtils.isBlank;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse.APP_TYPE_MAP;
import static uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse.CY_ABBREVIATED_MONTHS_MAP;
import static uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse.CY_APP_TYPE_MAP;
import static uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse.CY_RESPONDENT_APP_TYPE_MAP;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.CASE_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.FILE_NOT_EXISTS;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.HEARING_DOCUMENTS_PATH;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.LINK_TO_CITIZEN_HUB;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_ACAS_PDF1_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_ACAS_PDF2_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_ACAS_PDF3_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_ACAS_PDF4_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_ACAS_PDF5_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_APPLICANT_NAME_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_CASE_ID;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_CITIZEN_PORTAL_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_CLAIM_DESCRIPTION_FILE_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_DATEPLUS7_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_ET1PDF_ENGLISH_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_ET1PDF_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_ET1PDF_WELSH_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_EXUI_HEARING_DOCUMENTS_LINK;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_EXUI_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_FIRSTNAME_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_HEARING_DATE_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_LASTNAME_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_LINK_DOC_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_RESPONDING_USER_NAME_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_SHORTTEXT_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_SERVICE_OWNER_NAME_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_SERVICE_OWNER_NAME_VALUE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.UK_LOCAL_DATE_PATTERN;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.UNASSIGNED_OFFICE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.WELSH_LANGUAGE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.WELSH_LANGUAGE_PARAM;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.WELSH_LANGUAGE_PARAM_WITHOUT_FWDSLASH;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyrConstants.RESPONDING_USER_EMAIL_STRING;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyrConstants.THE_RESPONDENT_EMAIL_STRING;
import static uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper.getCurrentRespondentName;
import static uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper.getRespondentAndRespRepEmailAddressesMap;
import static uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper.getRespondentNames;
import static uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper.isRepresentedClaimantWithMyHmctsCase;
import static uk.gov.service.notify.NotificationClient.prepareUpload;

/**
 * Holds details for sending email to user(s) provided template been created beforehand.
 */

@Service
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports", "PMD.CyclomaticComplexity", "PMD.GodClass"})
public class NotificationService {
    static final String NOT_SET = "Not set";

    private final NotificationClient notificationClient;
    private final NotificationsProperties notificationsProperties;
    private final FeatureToggleService featureToggleService;

    private final String[] typeA =
        {"strike", "amend", "non-compliance", "other", "postpone", "vary", "respondent", "publicity"};
    private final String[] typeB = {"withdraw", "change-details", "reconsider-decision", "reconsider-judgement",
        "Change my personal details", "Consider a decision afresh",
        "Reconsider judgment"};
    private static final String TYPE_C = "witness";
    private static final String TYPE_C_RESPONDENT = "Order a witness to attend to give evidence";
    private static final String NO_CLAIMANT_EMAIL_FOUND =
        "No claimant email found - Application response acknowledgment not being sent";
    private static final String HEARING_DATE_NOT_SET_WELSH = "Heb ei anfon";

    /**
     * Record containing core details of an email.
     *
     * @param caseData        existing case details
     * @param claimant        claimant's full name
     * @param caseNumber      ethos case reference
     * @param respondentNames concatenated respondent names
     * @param hearingDate     date of the nearest hearing
     * @param caseId          16 digit case id
     */
    record CoreEmailDetails(
        CaseData caseData,
        String claimant,
        String caseNumber,
        String respondentNames,
        String hearingDate,
        String caseId) {
    }

    /**
     * Given a caseId, initialization of trigger event to start and submit update for case.
     *
     * @param templateId  - pass gov notify template id for each use case
     * @param targetEmail - recipient target email id
     * @param parameters  - map of strings to add this to the template
     * @param reference   - reference string for email template
     * @return response from notification api
     */
    SendEmailResponse sendEmail(
        String templateId, String targetEmail, Map<String, String> parameters, String reference) {
        SendEmailResponse sendEmailResponse;
        try {
            sendEmailResponse = notificationClient.sendEmail(templateId, targetEmail, parameters, reference);
        } catch (NotificationClientException ne) {
            GenericServiceUtil.logException("Error while trying to sending notification to client",
                                            GenericServiceUtil.getStringValueFromStringMap(
                                                parameters,
                                                SEND_EMAIL_PARAMS_CASE_NUMBER_KEY
                                            ),
                                            ne.getMessage(),
                                            this.getClass().getName(), "sendEmail"
            );
            throw new NotificationException(ne);
        }
        return sendEmailResponse;
    }

    /**
     * Prepares case submission confirmation email content from user and case data & sends email to the user.
     *
     * @param caseRequest  top level non-modifiable case details
     * @param caseData     user provided data
     * @param userInfo     user details from Idam
     * @param casePdfFiles pdf files of the ET1 form according to selected language
     * @return Gov notify email format
     */
    SendEmailResponse sendSubmitCaseConfirmationEmail(CaseRequest caseRequest, CaseData caseData,
                                                      UserInfo userInfo,
                                                      List<PdfDecodedMultipartFile> casePdfFiles) {
        SendEmailResponse sendEmailResponse = null;
        if (GenericServiceUtil.hasPdfFile(casePdfFiles, 0)) {
            String firstName = GenericServiceUtil.findClaimantFirstNameByCaseDataUserInfo(caseData, userInfo);
            String lastName = GenericServiceUtil.findClaimantLastNameByCaseDataUserInfo(caseData, userInfo);
            String caseId = caseRequest.getCaseId() == null ? CASE_ID_NOT_FOUND : caseRequest.getCaseId();
            String selectedLanguage = GenericServiceUtil.findClaimantLanguage(caseData);

            boolean isWelsh = WELSH_LANGUAGE.equals(selectedLanguage);
            String emailTemplateId = isWelsh
                ? notificationsProperties.getCySubmitCaseEmailTemplateId()
                : notificationsProperties.getSubmitCaseEmailTemplateId();
            String citizenPortalLink = notificationsProperties.getCitizenPortalLink() + "%s"
                + (isWelsh ? WELSH_LANGUAGE_PARAM : "");

            byte[] et1Pdf = GenericServiceUtil.findPdfFileBySelectedLanguage(casePdfFiles, selectedLanguage);
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
                GenericServiceUtil.logException("Submit case confirmation email was not sent to client.",
                                                caseData.getEthosCaseReference(), ne.getMessage(),
                                                this.getClass().getName(), "sendSubmitCaseConfirmationEmail"
                );
            }
        }
        return sendEmailResponse;
    }

    /**
     * Prepared doc upload error alert email content from user and case data then sends email to the service.
     *
     * @param caseRequest      top level non-modifiable case details
     * @param casePdfFiles     pdf copy of ET1 form content
     * @param acasCertificates pdf copy of Acas Certificates
     * @return Gov notify email format
     */
    SendEmailResponse sendDocUploadErrorEmail(CaseRequest caseRequest,
                                              List<PdfDecodedMultipartFile> casePdfFiles,
                                              List<PdfDecodedMultipartFile> acasCertificates,
                                              UploadedDocumentType claimDescriptionDocument) {
        SendEmailResponse sendEmailResponse = null;
        try {
            String caseNumber = caseRequest.getCaseId() == null ? CASE_ID_NOT_FOUND : caseRequest.getCaseId();
            Map<String, Object> parameters = new ConcurrentHashMap<>();
            parameters.put(SEND_EMAIL_SERVICE_OWNER_NAME_KEY, SEND_EMAIL_SERVICE_OWNER_NAME_VALUE);
            parameters.put(SEND_EMAIL_PARAMS_CASE_NUMBER_KEY, caseNumber);
            parameters.put(
                SEND_EMAIL_PARAMS_ET1PDF_ENGLISH_LINK_KEY,
                GenericServiceUtil.prepareUpload(casePdfFiles, 0)
            );
            parameters.put(
                SEND_EMAIL_PARAMS_ET1PDF_WELSH_LINK_KEY,
                GenericServiceUtil.prepareUpload(casePdfFiles, 1)
            );
            parameters.put(
                SEND_EMAIL_PARAMS_ACAS_PDF1_LINK_KEY,
                GenericServiceUtil.prepareUpload(acasCertificates, 0)
            );
            parameters.put(
                SEND_EMAIL_PARAMS_ACAS_PDF2_LINK_KEY,
                GenericServiceUtil.prepareUpload(acasCertificates, 1)
            );
            parameters.put(
                SEND_EMAIL_PARAMS_ACAS_PDF3_LINK_KEY,
                GenericServiceUtil.prepareUpload(acasCertificates, 2)
            );
            parameters.put(
                SEND_EMAIL_PARAMS_ACAS_PDF4_LINK_KEY,
                GenericServiceUtil.prepareUpload(acasCertificates, 3)
            );
            parameters.put(
                SEND_EMAIL_PARAMS_ACAS_PDF5_LINK_KEY,
                GenericServiceUtil.prepareUpload(acasCertificates, 4)
            );
            parameters.put(
                SEND_EMAIL_PARAMS_CLAIM_DESCRIPTION_FILE_LINK_KEY,
                ObjectUtils.isNotEmpty(claimDescriptionDocument)
                    && StringUtils.isNotBlank(claimDescriptionDocument.getDocumentUrl())
                    ? claimDescriptionDocument.getDocumentUrl()
                    : FILE_NOT_EXISTS
            );

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
            GenericServiceUtil.logException(
                "Case Documents Upload error - Failed to send document upload error message",
                caseRequest.getCaseId(), ne.getMessage(),
                this.getClass().getName(), "sendDocUploadErrorEmail"
            );
        }
        return sendEmailResponse;
    }

    /**
     * Format details of claimant request and retrieve case data, then send email.
     *
     * @param details             core details of the email
     * @param claimantApplication application request data
     * @return Gov notify email format
     */
    SendEmailResponse sendAcknowledgementEmailToClaimant(CoreEmailDetails details, ClaimantTse claimantApplication) {
        boolean isWelsh = isWelshLanguage(details.caseData());
        String hearingDate = getHearingDate(details.hearingDate(), isWelsh);
        Map<String, Object> claimantParameters = prepareEmailParameters(details, hearingDate, isWelsh);
        String emailToClaimantTemplate = getAndSetAckEmailTemplate(claimantApplication, hearingDate,
                                                                   claimantParameters, isWelsh, false);
        return sendEmailToClaimant(details.caseData(), details.caseId(), emailToClaimantTemplate, claimantParameters);
    }

    /**
     * Format details of respondent request and retrieve case data, then send email to confirmation to respondent.
     *
     * @param details             core details of the email
     * @param respondentApplication application request data
     */
    void sendRespondentAppAcknowledgementEmailToRespondent(CoreEmailDetails details,
                                                           RespondentTse respondentApplication,
                                                           JSONObject documentJson) {
        CaseData caseData = details.caseData();
        Set<String> sentEmailAddresses = new HashSet<>();
        String applicantName = getCurrentRespondentName(caseData, respondentApplication.getRespondentIdamId());

        caseData.getRespondentCollection().forEach(resp -> {
            Map<String, Boolean> emailAddressesMap =
                getRespondentAndRespRepEmailAddressesMap(caseData, resp.getValue());

            emailAddressesMap.forEach((email, isRespondent) -> {
                if (sentEmailAddresses.add(email)) {
                    prepareAndSendEmail(details, resp, respondentApplication, email,
                                        isRespondent, applicantName, documentJson);
                }
            });
        });
    }

    private void prepareAndSendEmail(CoreEmailDetails details, RespondentSumTypeItem respondent,
                                     RespondentTse respondentApplication, String email, boolean isRespondent,
                                     String applicantName, JSONObject documentJson) {
        boolean isWelsh = isWelshLanguage(respondent);
        String hearingDate = getHearingDate(details.hearingDate(), isWelsh);
        Map<String, Object> respondentParameters = prepareEmailParameters(details, hearingDate, isWelsh);

        String linkToCase = isRespondent
            ? getRespondentPortalLink(details.caseId(), respondent.getId(), isWelsh)
            : getRespondentRepPortalLink(details.caseId());

        respondentParameters.put(SEND_EMAIL_PARAMS_APPLICANT_NAME_KEY, applicantName);
        respondentParameters.put(
            isRespondent ? SEND_EMAIL_PARAMS_CITIZEN_PORTAL_LINK_KEY : SEND_EMAIL_PARAMS_EXUI_LINK_KEY,
            linkToCase
        );

        boolean isApplicant =
            respondentApplication.getRespondentIdamId().equals(respondent.getValue().getIdamId()) && isRespondent;
        String emailToRespondentTemplate;
        if (isApplicant) {
            emailToRespondentTemplate = getAndSetAckEmailTemplate(respondentApplication, hearingDate,
                                                                  respondentParameters, isWelsh,
                                                                  true);
        } else {
            emailToRespondentTemplate = getNonApplicantTemplateId(respondentApplication, isWelsh);
            String applicationType = getApplicationType(respondentApplication, true);
            respondentParameters.put(SEND_EMAIL_PARAMS_SHORTTEXT_KEY, applicationType);
            respondentParameters.put(SEND_EMAIL_PARAMS_DATEPLUS7_KEY,
                                    LocalDate.now().plusDays(7).format(UK_LOCAL_DATE_PATTERN));
            respondentParameters.put(SEND_EMAIL_PARAMS_LINK_DOC_KEY, Objects.requireNonNullElse(documentJson, ""));

        }
        sendEmailToRespondent(email, emailToRespondentTemplate, respondentParameters, details.caseId());
    }

    private void sendEmailToRespondent(String respondentEmailAddress, String emailTemplate,
                                       Map<String, Object> parameters, String caseId) {
        try {
            notificationClient.sendEmail(emailTemplate, respondentEmailAddress, parameters, caseId);
            log.info("Sent email to respondent");
        } catch (NotificationClientException ne) {
            throw new NotificationException(ne);
        }
    }

    private Map<String, Object> prepareEmailParameters(CoreEmailDetails details, String hearingDate, boolean isWelsh) {
        Map<String, Object> parameters = new ConcurrentHashMap<>();
        parameters.put(SEND_EMAIL_PARAMS_HEARING_DATE_KEY, hearingDate);
        NotificationsHelper.addCommonParameters(parameters, details.claimant(), details.respondentNames(),
                                                details.caseId(), details.caseNumber());
        // citizenPortalLink
        String citizenPortalLink = getCitizenPortalLink(details.caseId(), isWelsh);
        parameters.put(SEND_EMAIL_PARAMS_CITIZEN_PORTAL_LINK_KEY, citizenPortalLink);
        return parameters;
    }

    private boolean isWelshLanguage(CaseData caseData) {
        boolean welshFlagEnabled = featureToggleService.isWelshEnabled();
        log.info("Welsh feature flag is set to {}", welshFlagEnabled);
        if (caseData.getClaimantHearingPreference() == null) {
            log.warn("Claimant hearing preference is not set");
            return false;
        }
        return welshFlagEnabled && WELSH_LANGUAGE.equals(caseData.getClaimantHearingPreference().getContactLanguage());
    }

    private boolean isWelshLanguage(RespondentSumTypeItem respondent) {
        boolean welshFlagEnabled = featureToggleService.isWelshEnabled();
        log.info("Welsh feature flag is set to {}", welshFlagEnabled);
        return welshFlagEnabled && WELSH_LANGUAGE.equals(respondent.getValue().getEt3ResponseLanguagePreference());
    }

    private String getHearingDate(String hearingDate, boolean isWelsh) {
        if (NOT_SET.equals(hearingDate)) {
            return isWelsh ? HEARING_DATE_NOT_SET_WELSH : NOT_SET;
        }
        return isWelsh ? translateHearingDateToWelsh(hearingDate) : hearingDate;
    }

    private String getCitizenPortalLink(String caseId, boolean isWelsh) {
        return notificationsProperties.getCitizenPortalLink() + caseId + (isWelsh
            ? WELSH_LANGUAGE_PARAM_WITHOUT_FWDSLASH : "");
    }

    private String getRespondentPortalLink(String caseId, String respondentId, boolean isWelsh) {
        return notificationsProperties.getRespondentPortalLink() + caseId + "/" + respondentId + (isWelsh
            ? WELSH_LANGUAGE_PARAM_WITHOUT_FWDSLASH : "");
    }

    private String getRespondentRepPortalLink(String caseId) {
        return notificationsProperties.getExuiCaseDetailsLink() + caseId;
    }

    private SendEmailResponse sendEmailToClaimant(CaseData caseData, String caseId, String emailTemplate,
                                                  Map<String, Object> parameters) {
        try {
            String claimantEmail = isRepresentedClaimantWithMyHmctsCase(caseData)
                ? caseData.getRepresentativeClaimantType().getRepresentativeEmailAddress()
                : caseData.getClaimantType().getClaimantEmailAddress();

            if (isBlank(claimantEmail)) {
                log.info(NO_CLAIMANT_EMAIL_FOUND);
                return null;
            }
            return notificationClient.sendEmail(emailTemplate, claimantEmail, parameters, caseId);
        } catch (NotificationClientException e) {
            log.error("Failed to send acknowledgment email to claimant", e);
            throw new NotificationException(e);
        }
    }

    private String translateHearingDateToWelsh(String hearingDate) {
        return CY_ABBREVIATED_MONTHS_MAP.entrySet().stream()
            .filter(entry -> hearingDate.contains(entry.getKey()))
            .findFirst()
            .map(entry -> hearingDate.replace(entry.getKey(), entry.getValue()))
            .orElse(hearingDate);
    }

    /**
     * Format details of claimant request and retrieve case data, then send email to confirmation to respondent.
     *
     * @param details             core details of the email
     * @param claimantApplication application request data
     */
    void sendAcknowledgementEmailToRespondents(CoreEmailDetails details,
                                               JSONObject documentJson,
                                               ClaimantTse claimantApplication) {
        if (TYPE_C.equals(claimantApplication.getContactApplicationType())
            || NO.equals(claimantApplication.getCopyToOtherPartyYesOrNo())) {
            log.info("Acknowledgement email not sent to respondents for this application type");
            return;
        }

        Map<String, Object> respondentParameters = new ConcurrentHashMap<>();
        NotificationsHelper.addCommonParameters(respondentParameters,
                                                details.claimant(),
                                                details.respondentNames(),
                                                details.caseId(),
                                                details.caseNumber());
        respondentParameters.put(SEND_EMAIL_PARAMS_HEARING_DATE_KEY, details.hearingDate());
        respondentParameters.put(SEND_EMAIL_PARAMS_SHORTTEXT_KEY, APP_TYPE_MAP.get(
            claimantApplication.getContactApplicationType()));
        respondentParameters.put(SEND_EMAIL_PARAMS_DATEPLUS7_KEY,
                                 LocalDate.now().plusDays(7).format(UK_LOCAL_DATE_PATTERN));
        respondentParameters.put(SEND_EMAIL_PARAMS_LINK_DOC_KEY,
                                 Objects.requireNonNullElse(documentJson, ""));
        respondentParameters.put(SEND_EMAIL_PARAMS_EXUI_LINK_KEY,
                                 notificationsProperties.getExuiCaseDetailsLink() + details.caseId());

        String emailToRespondentTemplate = Stream.of(typeB).anyMatch(
            appType -> Objects.equals(appType, claimantApplication.getContactApplicationType()))
            ? notificationsProperties.getRespondentTseEmailTypeBTemplateId()
            : notificationsProperties.getRespondentTseEmailTypeATemplateId();

        sendRespondentEmails(details.caseData(), details.caseId(), respondentParameters, emailToRespondentTemplate);
    }

    void sendRespondentAppAcknowledgementEmailToClaimant(CoreEmailDetails details,
                                               JSONObject documentJson,
                                               RespondentTse respondentTse) {
        if (TYPE_C_RESPONDENT.equals(respondentTse.getContactApplicationType())
            || NO.equals(respondentTse.getCopyToOtherPartyYesOrNo())) {
            log.info("Acknowledgement email not sent to claimant for this application type");
            return;
        }
        Map<String, Object> claimantParameters = new ConcurrentHashMap<>();
        NotificationsHelper.addCommonParameters(claimantParameters,
                                                details.claimant(),
                                                details.respondentNames(),
                                                details.caseId(),
                                                details.caseNumber());

        String appTypeByLanguage =
            isWelshLanguage(details.caseData())
                                ? CY_RESPONDENT_APP_TYPE_MAP.get(respondentTse.getContactApplicationType())
                                : respondentTse.getContactApplicationType();

        CaseData caseData = details.caseData();
        String applicantName = getCurrentRespondentName(caseData, respondentTse.getRespondentIdamId());

        claimantParameters.put(SEND_EMAIL_PARAMS_APPLICANT_NAME_KEY, applicantName);
        claimantParameters.put(SEND_EMAIL_PARAMS_HEARING_DATE_KEY, details.hearingDate());
        claimantParameters.put(SEND_EMAIL_PARAMS_SHORTTEXT_KEY, appTypeByLanguage);
        claimantParameters.put(SEND_EMAIL_PARAMS_DATEPLUS7_KEY,
                                 LocalDate.now().plusDays(7).format(UK_LOCAL_DATE_PATTERN));
        claimantParameters.put(SEND_EMAIL_PARAMS_LINK_DOC_KEY,
                                 Objects.requireNonNullElse(documentJson, ""));
        // will have to handle claimant and claimant representative emails
        if (isRepresentedClaimantWithMyHmctsCase(caseData)) {
            claimantParameters.put(SEND_EMAIL_PARAMS_EXUI_LINK_KEY,
                                   notificationsProperties.getExuiCaseDetailsLink() + details.caseId());
        } else {
            claimantParameters.put(SEND_EMAIL_PARAMS_EXUI_LINK_KEY,
                                   notificationsProperties.getCitizenPortalLink() + details.caseId());
        }

        boolean isWelsh = isWelshLanguage(caseData);
        String emailToClaimantTemplate = getNonApplicantTemplateId(respondentTse, isWelsh);
        sendEmailToClaimant(caseData, details.caseId(), emailToClaimantTemplate, claimantParameters);
    }

    private String getNonApplicantTemplateId(RespondentTse respondentTse, boolean isWelsh) {
        if (Stream.of(typeB).anyMatch(appType -> Objects.equals(appType, respondentTse.getContactApplicationType()))) {
            return isWelsh
                ? notificationsProperties.getCyRespondentTseTypeBClaimantAckTemplateId()
                : notificationsProperties.getRespondentTseTypeBClaimantAckTemplateId();
        } else {
            return isWelsh
                ? notificationsProperties.getCyRespondentTseTypeAClaimantAckTemplateId()
                : notificationsProperties.getRespondentTseTypeAClaimantAckTemplateId();
        }
    }

    /**
     * Format details of claimant request and retrieve case data, then send email to confirmation to tribunal.
     *
     * @param details         core details of the email
     * @param applicationType type of application
     */
    void sendAcknowledgementEmailToTribunal(CoreEmailDetails details, String applicationType, boolean isRespondentApp) {
        Map<String, Object> tribunalParameters = new ConcurrentHashMap<>();

        String applicationTypeName = isRespondentApp ? applicationType : APP_TYPE_MAP.get(applicationType);
        NotificationsHelper.addCommonParameters(
            tribunalParameters,
            details.claimant,
            details.respondentNames,
            details.caseId,
            details.caseNumber,
            String.join(" ", details.caseNumber, applicationTypeName)
        );
        tribunalParameters.put(
            SEND_EMAIL_PARAMS_HEARING_DATE_KEY,
            details.hearingDate
        );
        tribunalParameters.put(
            SEND_EMAIL_PARAMS_EXUI_LINK_KEY,
            notificationsProperties.getExuiCaseDetailsLink() + details.caseId
        );

        String managingOffice = details.caseData.getManagingOffice();
        if (managingOffice.equals(UNASSIGNED_OFFICE) || isNullOrEmpty(managingOffice)) {
            log.info("Could not send email as no office has been assigned");
            return;
        }

        try {
            notificationClient.sendEmail(
                notificationsProperties.getTribunalAcknowledgementTemplateId(),
                details.caseData.getTribunalCorrespondenceEmail(),
                tribunalParameters,
                details.caseId
            );
        } catch (NotificationClientException ne) {
            throw new NotificationException(ne);
        }
    }

    /**
     * Format details of claimant request and retrieve case data, then send email to confirmation to tribunal.
     *
     * @param details                      core details of the email
     * @param applicationType              type of application
     * @param isRespondingToRequestOrOrder indicates whether the reply is to a tribunal order or not
     */
    void sendResponseEmailToTribunal(CoreEmailDetails details, String applicationType,
                                     boolean isRespondingToRequestOrOrder) {

        Map<String, Object> tribunalParameters = new ConcurrentHashMap<>();
        NotificationsHelper.addCommonParameters(
            tribunalParameters,
            details.claimant,
            details.respondentNames,
            details.caseId,
            details.caseNumber,
            String.join(" ", details.caseNumber, applicationType),
            applicationType
        );

        tribunalParameters.put(
            SEND_EMAIL_PARAMS_HEARING_DATE_KEY,
            details.hearingDate
        );
        tribunalParameters.put(
            SEND_EMAIL_PARAMS_EXUI_LINK_KEY,
            notificationsProperties.getExuiCaseDetailsLink() + details.caseId
        );

        String emailTemplate = isRespondingToRequestOrOrder
            ? notificationsProperties.getTseTribunalResponseToRequestTemplateId()
            : notificationsProperties.getTseTribunalResponseTemplateId();

        sendTribunalEmail(
            details.caseData,
            details.caseId,
            tribunalParameters,
            emailTemplate
        );
    }

    /**
     * Send acknowledgment email to the claimant when they are responding to
     * an application (type A/B) made by the Respondent.
     *
     * @param details                      core details of the email
     * @param applicationType              type of application
     * @param copyToOtherParty             whether to notify other party
     * @param isRespondingToRequestOrOrder indicates whether the reply is to a tribunal order or not
     */
    void sendResponseEmailToClaimant(CoreEmailDetails details,
                                               String applicationType,
                                               String copyToOtherParty,
                                               boolean isRespondingToRequestOrOrder) {
        if (TYPE_C.equals(applicationType) || TYPE_C_RESPONDENT.equals(applicationType)) {
            log.info("Type C application -  Claimant is only notified of "
                         + "Type A/B application responses, email not being sent");
            return;
        }

        boolean isClaimantRepresented = isRepresentedClaimantWithMyHmctsCase(details.caseData);

        String claimantEmailAddress = isClaimantRepresented
            ? details.caseData.getRepresentativeClaimantType().getRepresentativeEmailAddress()
            : details.caseData.getClaimantType().getClaimantEmailAddress();

        if (isBlank(claimantEmailAddress)) {
            log.info(NO_CLAIMANT_EMAIL_FOUND);
            return;
        }

        Map<String, Object> claimantParameters = prepareResponseEmailCommonParameters(details, applicationType);

        String caseLink = isClaimantRepresented
            ? notificationsProperties.getExuiCaseDetailsLink() + details.caseId
            : notificationsProperties.getCitizenPortalLink() + details.caseId;
        claimantParameters.put(
            SEND_EMAIL_PARAMS_CITIZEN_PORTAL_LINK_KEY,
            caseLink
        );

        String emailToClaimantTemplate;
        if (isRespondingToRequestOrOrder) {
            emailToClaimantTemplate = NO.equals(copyToOtherParty)
                ? notificationsProperties.getTseClaimantResponseToRequestNoTemplateId()
                : notificationsProperties.getTseClaimantResponseToRequestYesTemplateId();
        } else {
            emailToClaimantTemplate = NO.equals(copyToOtherParty)
                ? notificationsProperties.getTseClaimantResponseNoTemplateId()
                : notificationsProperties.getTseClaimantResponseYesTemplateId();
        }

        try {
            notificationClient.sendEmail(
                emailToClaimantTemplate,
                claimantEmailAddress,
                claimantParameters,
                details.caseId
            );
        } catch (NotificationClientException ne) {
            throw new NotificationException(ne);
        }
    }

    /**
     * Send acknowledgment email to the respondent when they are responding to
     * an application (type A/B) made by the Claimant.
     *
     * @param details                      core details of the email
     * @param applicationType              type of application
     * @param copyToOtherParty             whether to notify other party
     * @param isRespondingToRequestOrOrder indicates whether the reply is to a tribunal order or not
     */
    void sendRespondentResponseEmailToRespondent(CoreEmailDetails details, String applicationType,
                                                 String copyToOtherParty, boolean isRespondingToRequestOrOrder,
                                                 String respondingUserIdamId) {
        if (TYPE_C.equals(applicationType) || TYPE_C_RESPONDENT.equals(applicationType)) {
            log.info("Type C application -  Respondent is only notified of "
                         + "Type A/B application responses, email not being sent");
            return;
        }

        String emailTemplate = getRespondentResponseEmailTemplate(isRespondingToRequestOrOrder,
                                                                  copyToOtherParty);
        Set<String> sentEmailAddresses = new HashSet<>();

        CaseData caseData = details.caseData();
        String respondingUserName = getCurrentRespondentName(details.caseData(), respondingUserIdamId);
        caseData.getRespondentCollection()
            .forEach(resp -> {

                Map<String, Boolean> emailAddressesMap =
                    getRespondentAndRespRepEmailAddressesMap(caseData, resp.getValue());

                emailAddressesMap.forEach((emailAddress, isRespondent) -> {
                    if (sentEmailAddresses.add(emailAddress)) {
                        prepareAndSendRespondingToAppEmail(details, isRespondent, applicationType, emailAddress,
                                                           emailTemplate, respondingUserName, resp,
                                                           respondingUserIdamId);
                    }
                });
            });
    }

    private void prepareAndSendRespondingToAppEmail(CoreEmailDetails details, boolean isRespondent,
                                                    String applicationType, String emailAddress, String emailTemplate,
                                                    String respondingUserName, RespondentSumTypeItem respondent,
                                                    String respondingUserIdamId) {
        Map<String, Object> emailParameters = prepareResponseEmailCommonParameters(details, applicationType);

        boolean isRespondingUser =
            respondingUserIdamId.equals(respondent.getValue().getIdamId()) && isRespondent;

        if (isRespondingUser) {
            emailParameters.put(SEND_EMAIL_PARAMS_RESPONDING_USER_NAME_KEY, RESPONDING_USER_EMAIL_STRING);
        } else {
            emailParameters.put(SEND_EMAIL_PARAMS_RESPONDING_USER_NAME_KEY,
                                THE_RESPONDENT_EMAIL_STRING + respondingUserName);
        }

        String linkToCase = isRespondent
            ? getRespondentPortalLink(details.caseId(), respondent.getId(), false)
            : getRespondentRepPortalLink(details.caseId());
        emailParameters.put(SEND_EMAIL_PARAMS_CITIZEN_PORTAL_LINK_KEY, linkToCase);


        sendEmailToRespondent(emailAddress, emailTemplate, emailParameters, details.caseId);
    }

    private Map<String, Object> prepareResponseEmailCommonParameters(CoreEmailDetails details,
                                                                     String applicationType) {
        Map<String, Object> emailParameters = new ConcurrentHashMap<>();

        NotificationsHelper.addCommonParameters(
            emailParameters,
            details.claimant,
            details.respondentNames,
            details.caseId,
            details.caseNumber,
            String.join(" ", details.caseNumber, applicationType),
            applicationType
        );
        emailParameters.put(SEND_EMAIL_PARAMS_HEARING_DATE_KEY, details.hearingDate);

        return emailParameters;
    }

    private String getRespondentResponseEmailTemplate(boolean isRespondingToRequestOrOrder, String copyToOtherParty) {
        String emailTemplate;
        if (isRespondingToRequestOrOrder) {
            emailTemplate = NO.equals(copyToOtherParty)
                ? notificationsProperties.getTseRespondentResponseToRequestNoTemplateId()
                : notificationsProperties.getTseRespondentResponseToRequestYesTemplateId();
        } else {
            emailTemplate = NO.equals(copyToOtherParty)
                ? notificationsProperties.getTseRespondentResponseNoTemplateId()
                : notificationsProperties.getTseRespondentResponseYesTemplateId();
        }

        return emailTemplate;
    }

    /**
     * Send acknowledgment email to the respondent when they are responding to
     * an application (type A/B) made by the Respondent.
     *
     * @param details          core details of the email
     * @param applicationType  type of application
     * @param copyToOtherParty should copy response to other party
     */
    void sendResponseEmailToRespondent(CoreEmailDetails details, String applicationType, String copyToOtherParty) {
        if (TYPE_C.equals(applicationType) || NO.equals(copyToOtherParty)
            || TYPE_C_RESPONDENT.equals(applicationType)) {
            log.info("Acknowledgement email not sent to respondents for this application type");
            return;
        }
        Map<String, Object> respondentParameters = new ConcurrentHashMap<>();

        NotificationsHelper.addCommonParameters(
            respondentParameters,
            details.claimant,
            details.respondentNames,
            details.caseId,
            details.caseNumber,
            String.join(" ", details.caseNumber, applicationType),
            applicationType
        );
        respondentParameters.put(SEND_EMAIL_PARAMS_HEARING_DATE_KEY, details.hearingDate);
        respondentParameters.put(
            SEND_EMAIL_PARAMS_EXUI_LINK_KEY,
            notificationsProperties.getExuiCaseDetailsLink() + details.caseId
        );

        String emailToRespondentTemplate = notificationsProperties.getTseRespondentResponseTemplateId();

        sendRespondentEmails(details.caseData, details.caseId, respondentParameters, emailToRespondentTemplate);
    }

    /**
     * Email respondent when claimant responds to a request for info from the tribunal on a TSE application.
     *
     * @param caseData   existing case details
     * @param caseNumber ethos case reference
     * @param caseId     16 digit case id
     */
    void sendReplyEmailToRespondent(
        CaseData caseData,
        String caseNumber,
        String caseId,
        String copyToOtherParty
    ) {
        if (NO.equals(copyToOtherParty)) {
            log.info("Answered no for Rule 92, not sending email to respondents");
            return;
        }

        Map<String, Object> respondentParameters = new ConcurrentHashMap<>();
        respondentParameters.put(SEND_EMAIL_PARAMS_CASE_NUMBER_KEY, caseNumber);
        respondentParameters.put(
            SEND_EMAIL_PARAMS_EXUI_LINK_KEY,
            notificationsProperties.getExuiCaseDetailsLink() + caseId
        );

        String emailToRespondentTemplate = notificationsProperties.getTseReplyToTribunalToRespondentTemplateId();

        sendRespondentEmails(caseData, caseId, respondentParameters, emailToRespondentTemplate);
    }

    /**
     * Email claimant when respondent responds to a request for info from the tribunal on a TSE application.
     *
     * @param caseData   existing case details
     * @param caseNumber ethos case reference
     * @param caseId     16 digit case id
     */
    void sendReplyEmailToClaimant(
        CaseData caseData,
        String caseNumber,
        String caseId,
        String copyToOtherParty
    ) {
        if (NO.equals(copyToOtherParty)) {
            log.info("Answered no for Rule 92, not sending email to claimant");
            return;
        }

        boolean isClaimantRepresented = isRepresentedClaimantWithMyHmctsCase(caseData);

        String claimantEmailAddress = isClaimantRepresented
            ? caseData.getRepresentativeClaimantType().getRepresentativeEmailAddress()
            : caseData.getClaimantType().getClaimantEmailAddress();

        if (isBlank(claimantEmailAddress)) {
            log.info(NO_CLAIMANT_EMAIL_FOUND);
            return;
        }

        Map<String, Object> claimantParameters = new ConcurrentHashMap<>();
        claimantParameters.put(SEND_EMAIL_PARAMS_CASE_NUMBER_KEY, caseNumber);

        String caseLink = isClaimantRepresented
            ? notificationsProperties.getExuiCaseDetailsLink() + caseId
            : notificationsProperties.getCitizenPortalLink() + caseId;
        claimantParameters.put(
            LINK_TO_CITIZEN_HUB,
            caseLink
        );

        String emailTemplate = notificationsProperties.getTseReplyToTribunalToRespondentTemplateId();

        try {
            notificationClient.sendEmail(
                emailTemplate,
                claimantEmailAddress,
                claimantParameters,
                caseId
            );
        } catch (NotificationClientException ne) {
            throw new NotificationException(ne);
        }
    }

    /**
     * Send acknowledgment email to the respondent when they are responding to
     * an application (type A/B) made by the Respondent.
     *
     * @param details          core details of the email
     * @param applicationType  type of application
     * @param copyToOtherParty should copy response to other party
     */
    void sendRespondentResponseEmailToClaimant(CoreEmailDetails details, String applicationType,
                                               String copyToOtherParty) {
        if (TYPE_C.equals(applicationType) || TYPE_C_RESPONDENT.equals(applicationType)
            || NO.equals(copyToOtherParty)) {
            log.info("Acknowledgement email not sent to claimants for this application type");
            return;
        }

        String claimantEmailAddress = details.caseData.getClaimantType().getClaimantEmailAddress();
        if (isBlank(claimantEmailAddress)) {
            log.info(NO_CLAIMANT_EMAIL_FOUND);
            return;
        }

        Map<String, Object> claimantParameters = new ConcurrentHashMap<>();

        NotificationsHelper.addCommonParameters(
            claimantParameters,
            details.claimant,
            details.respondentNames,
            details.caseId,
            details.caseNumber,
            String.join(" ", details.caseNumber, applicationType),
            applicationType
        );
        claimantParameters.put(SEND_EMAIL_PARAMS_HEARING_DATE_KEY, details.hearingDate);
        claimantParameters.put(
            SEND_EMAIL_PARAMS_EXUI_LINK_KEY,
            notificationsProperties.getCitizenPortalLink() + details.caseId
        );

        String emailTemplate = notificationsProperties.getTseRespondentResponseTemplateId();

        try {
            notificationClient.sendEmail(
                emailTemplate,
                claimantEmailAddress,
                claimantParameters,
                details.caseId
            );
        } catch (NotificationClientException ne) {
            throw new NotificationException(ne);
        }
    }

    void sendResponseNotificationEmailToTribunal(CaseData caseData, String caseId) {
        Map<String, Object> tribunalParameters = new ConcurrentHashMap<>();
        NotificationsHelper.addCommonParameters(tribunalParameters, caseData, caseId);

        tribunalParameters.put(
            SEND_EMAIL_PARAMS_HEARING_DATE_KEY,
            NotificationsHelper.getNearestHearingToReferral(caseData, NOT_SET)
        );

        tribunalParameters.put(
            SEND_EMAIL_PARAMS_EXUI_LINK_KEY,
            notificationsProperties.getExuiCaseDetailsLink() + caseId
        );

        sendTribunalEmail(
            caseData,
            caseId,
            tribunalParameters,
            notificationsProperties.getPseTribunalResponseTemplateId()
        );
    }

    void sendResponseNotificationEmailToRespondent(
        CaseData caseData,
        String caseId,
        String copyToOtherParty,
        boolean isClaimantPseResponse,
        String respondentIdamId
    ) {
        // don't send email to respondents if this is a claimant PSE response and they opted out
        if ((NO.equals(copyToOtherParty) && isClaimantPseResponse)
            || copyToOtherParty == null) {
            log.info("Acknowledgement email not sent to respondents");
            return;
        }


        Map<String, Object> respondentParameters = new ConcurrentHashMap<>();
        NotificationsHelper.addCommonParameters(respondentParameters, caseData, caseId);
        respondentParameters.put(
            SEND_EMAIL_PARAMS_HEARING_DATE_KEY,
            NotificationsHelper.getNearestHearingToReferral(caseData, NOT_SET)
        );

        String emailTemplate;
        if (isClaimantPseResponse) {
            emailTemplate = notificationsProperties.getPseRespondentResponseTemplateId();
            sendRespondentEmails(caseData, caseId, respondentParameters, emailTemplate);
        } else {
            // respondent PSE response
            // only the current respondent gets the email
            if (NO.equals(copyToOtherParty)) {
                emailTemplate = notificationsProperties.getPseClaimantResponseNoTemplateId();
                RespondentSumTypeItem respondent = getRespondent(caseData, respondentIdamId);
                String emailAddress = getRespondentEmail(respondent);
                if (isBlank(emailAddress)) {
                    log.info("Respondent does not have an email address associated with their account");
                    return;
                }
                sendEmailToRespondent(emailAddress, emailTemplate, respondentParameters, caseId);
            } else {
                // send email to all the respondents
                emailTemplate = notificationsProperties.getPseClaimantResponseYesTemplateId();
                sendRespondentEmails(caseData, caseId, respondentParameters, emailTemplate);
            }
        }
    }

    void sendResponseNotificationEmailToClaimant(
        CaseData caseData,
        String caseId,
        String copyToOtherParty,
        boolean isClaimantPseResponse
    ) {

        String claimantEmail;
        if (isClaimantPseResponse && caseData.getClaimantType() != null) {
            claimantEmail = caseData.getClaimantType().getClaimantEmailAddress();
        } else {
            claimantEmail = isRepresentedClaimantWithMyHmctsCase(caseData)
                ? caseData.getRepresentativeClaimantType().getRepresentativeEmailAddress()
                : caseData.getClaimantType().getClaimantEmailAddress();
        }

        // don't send email to claimant if this is a respondent PSE response and they opted out
        if ((NO.equals(copyToOtherParty) && !isClaimantPseResponse)
            || copyToOtherParty == null || isBlank(claimantEmail)) {
            log.info("Acknowledgement email not sent to claimants");
            return;
        }

        String emailToClaimantTemplate;
        if (isClaimantPseResponse) {
            emailToClaimantTemplate = NO.equals(copyToOtherParty)
                ? notificationsProperties.getPseClaimantResponseNoTemplateId()
                : notificationsProperties.getPseClaimantResponseYesTemplateId();
        } else {
            emailToClaimantTemplate = notificationsProperties.getPseRespondentResponseTemplateId();
        }

        Map<String, Object> claimantParameters = new ConcurrentHashMap<>();
        NotificationsHelper.addCommonParameters(claimantParameters, caseData, caseId);

        claimantParameters.put(
            SEND_EMAIL_PARAMS_HEARING_DATE_KEY,
            NotificationsHelper.getNearestHearingToReferral(caseData, NOT_SET)
        );
        claimantParameters.put(
            SEND_EMAIL_PARAMS_CITIZEN_PORTAL_LINK_KEY,
            notificationsProperties.getCitizenPortalLink() + caseId
        );
        claimantParameters.put(
            SEND_EMAIL_PARAMS_EXUI_LINK_KEY,
            notificationsProperties.getCitizenPortalLink() + caseId
        );

        try {
            notificationClient.sendEmail(
                emailToClaimantTemplate,
                claimantEmail,
                claimantParameters,
                caseId
            );
        } catch (NotificationClientException ne) {
            throw new NotificationException(ne);
        }
    }

    void sendStoredEmailToClaimant(CoreEmailDetails details, String shortText) {
        sendStoreConfirmationEmail(
            notificationsProperties.getClaimantTseEmailStoredTemplateId(),
            details,
            shortText
        );
    }

    void sendSubmitStoredEmailToClaimant(CoreEmailDetails details, String shortText) {
        sendStoreConfirmationEmail(
            notificationsProperties.getClaimantTseEmailSubmitStoredTemplateId(),
            details,
            shortText
        );
    }

    CoreEmailDetails formatCoreEmailDetails(CaseData caseData, String caseId) {
        return new CoreEmailDetails(
            caseData,
            caseData.getClaimantIndType().getClaimantFirstNames() + " "
                + caseData.getClaimantIndType().getClaimantLastName(),
            caseData.getEthosCaseReference(),
            getRespondentNames(caseData),
            NotificationsHelper.getNearestHearingToReferral(caseData, NOT_SET),
            caseId
        );
    }

    private void sendTribunalEmail(CaseData caseData,
                                   String caseId,
                                   Map<String, Object> tribunalParameters,
                                   String templateId) {
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
                templateId,
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
                Map<String, Boolean> respondentEmailAddress =
                    getRespondentAndRespRepEmailAddressesMap(caseData, resp.getValue());
                boolean isWelsh = isWelshLanguage(resp);
                respondentEmailAddress.forEach((email, isRespondent) -> {
                    if (isNullOrEmpty(email)) {
                        log.info("Respondent does not not have an email address associated with their account");
                    } else {

                        String linkToCase = Boolean.TRUE.equals(isRespondent)
                            ? getRespondentPortalLink(caseId, resp.getId(), isWelsh)
                            : getRespondentRepPortalLink(caseId);
                        respondentParameters.put(SEND_EMAIL_PARAMS_EXUI_LINK_KEY, linkToCase);
                        respondentParameters.put(SEND_EMAIL_PARAMS_CITIZEN_PORTAL_LINK_KEY, linkToCase);
                        sendEmailToRespondent(email, emailToRespondentTemplate, respondentParameters, caseId);
                    }
                });
            });
    }

    private void sendStoreConfirmationEmail(String emailToClaimantTemplate, CoreEmailDetails details,
                                            String shortText) {
        String claimantEmailAddress = details.caseData.getClaimantType().getClaimantEmailAddress();
        if (isBlank(claimantEmailAddress)) {
            log.info(NO_CLAIMANT_EMAIL_FOUND);
            return;
        }

        Map<String, Object> claimantParameters = new ConcurrentHashMap<>();

        NotificationsHelper.addCommonParameters(
            claimantParameters,
            details.claimant,
            details.respondentNames,
            details.caseId,
            details.caseNumber
        );
        claimantParameters.put(SEND_EMAIL_PARAMS_HEARING_DATE_KEY, details.hearingDate);
        claimantParameters.put(SEND_EMAIL_PARAMS_SHORTTEXT_KEY, defaultIfEmpty(shortText, ""));
        claimantParameters.put(
            SEND_EMAIL_PARAMS_CITIZEN_PORTAL_LINK_KEY,
            notificationsProperties.getCitizenPortalLink() + details.caseId
        );

        try {
            notificationClient.sendEmail(
                emailToClaimantTemplate,
                claimantEmailAddress,
                claimantParameters,
                details.caseId
            );
        } catch (NotificationClientException ne) {
            throw new NotificationException(ne);
        }
    }

    /**
     * Sends email to all respondents/legal reps plus the tribunal when the claimant submits a bundle.
     * Content of email is the same therefore the same template is used.
     *
     * @param caseData  existing case data
     * @param caseId    id of case
     * @param hearingId id of hearing
     */
    public void sendBundlesEmails(CaseData caseData,
                                  String caseId,
                                  String hearingId) {

        Map<String, Object> emailParameters = new ConcurrentHashMap<>();
        NotificationsHelper.addCommonParameters(emailParameters, caseData, caseId);

        String hearingDate = NotificationsHelper.getEarliestDateForHearing(
            caseData.getHearingCollection(),
            hearingId
        );
        emailParameters.put(
            SEND_EMAIL_PARAMS_HEARING_DATE_KEY,
            hearingDate
        );

        emailParameters.put(
            SEND_EMAIL_PARAMS_EXUI_HEARING_DOCUMENTS_LINK,
            notificationsProperties.getExuiCaseDetailsLink() + caseId + HEARING_DOCUMENTS_PATH
        );

        sendTribunalEmail(
            caseData,
            caseId,
            emailParameters,
            notificationsProperties.getBundlesClaimantSubmittedNotificationTemplateId()
        );

        sendRespondentEmails(
            caseData,
            caseId,
            emailParameters,
            notificationsProperties.getBundlesClaimantSubmittedNotificationTemplateId()
        );
    }

    String getAndSetAckEmailTemplate(Object application, String hearingDate, Map<String, Object> parameters,
                                     boolean isWelsh, boolean isRespondentOrRep) {
        parameters.put(SEND_EMAIL_PARAMS_HEARING_DATE_KEY, hearingDate);
        Map<String, String> selectedMap = isWelsh ? CY_APP_TYPE_MAP : APP_TYPE_MAP;
        String applicationType = getApplicationType(application, isRespondentOrRep);
        String shortText = isRespondentOrRep ? applicationType : selectedMap.get(applicationType);
        parameters.put(SEND_EMAIL_PARAMS_SHORTTEXT_KEY, shortText);

        String copyToOtherParty = getCopyToOtherParty(application, isRespondentOrRep);
        return getTemplateId(applicationType, copyToOtherParty, isWelsh, isRespondentOrRep);
    }

    private String getApplicationType(Object application, boolean isRespondent) {
        return isRespondent
            ? ((RespondentTse) application).getContactApplicationType()
            : ((ClaimantTse) application).getContactApplicationType();
    }

    private String getCopyToOtherParty(Object application, boolean isRespondent) {
        return isRespondent
            ? ((RespondentTse) application).getCopyToOtherPartyYesOrNo()
            : ((ClaimantTse) application).getCopyToOtherPartyYesOrNo();
    }

    private String getTemplateId(String applicationType, String copyToOtherParty,
                                 boolean isWelsh, boolean isApplicant) {
        if (TYPE_C.equals(applicationType) || TYPE_C_RESPONDENT.equals(applicationType)) {
            return getTypeCTemplateId(isWelsh, isApplicant);
        }

        if (NO.equals(copyToOtherParty)) {
            return getNoCopyTemplateId(isWelsh, isApplicant);
        }

        if (Arrays.asList(typeB).contains(applicationType)) {
            return getTypeBTemplateId(isWelsh, isApplicant);
        }

        return getTypeATemplateId(isWelsh, isApplicant);
    }

    private String getTypeCTemplateId(boolean isWelsh, boolean isRespondent) {
        if (isWelsh) {
            return isRespondent
                ? notificationsProperties.getCyRespondentTseTypeCRespAckTemplateId()
                : notificationsProperties.getCyClaimantTseEmailTypeCTemplateId();
        } else {
            return isRespondent
                ? notificationsProperties.getRespondentTseTypeCRespAckTemplateId()
                : notificationsProperties.getClaimantTseEmailTypeCTemplateId();
        }
    }

    private String getNoCopyTemplateId(boolean isWelsh, boolean isRespondent) {
        if (isWelsh) {
            return isRespondent
                ? notificationsProperties.getCyRespondentTseNoRespAckTemplateId()
                : notificationsProperties.getCyClaimantTseEmailNoTemplateId();
        } else {
            return isRespondent
                ? notificationsProperties.getRespondentTseNoRespAckTemplateId()
                : notificationsProperties.getClaimantTseEmailNoTemplateId();
        }
    }

    private String getTypeBTemplateId(boolean isWelsh, boolean isRespondent) {
        if (isWelsh) {
            return isRespondent
                ? notificationsProperties.getCyRespondentTseTypeBRespAckTemplateId()
                : notificationsProperties.getCyClaimantTseEmailTypeBTemplateId();
        } else {
            return isRespondent
                ? notificationsProperties.getRespondentTseTypeBRespAckTemplateId()
                : notificationsProperties.getClaimantTseEmailTypeBTemplateId();
        }
    }

    private String getTypeATemplateId(boolean isWelsh, boolean isRespondent) {
        if (isWelsh) {
            return isRespondent
                ? notificationsProperties.getCyRespondentTseTypeARespAckTemplateId()
                : notificationsProperties.getCyClaimantTseEmailTypeATemplateId();
        } else {
            return isRespondent
                ? notificationsProperties.getRespondentTseTypeARespAckTemplateId()
                : notificationsProperties.getClaimantTseEmailTypeATemplateId();
        }
    }

    public void sendEt3ConfirmationEmail(String email, CaseData caseData, String caseId) {
        Map<String, Object> parameters = new ConcurrentHashMap<>();
        parameters.put(SEND_EMAIL_PARAMS_CASE_NUMBER_KEY, caseData.getEthosCaseReference());
        parameters.put("claimant", caseData.getClaimant());
        parameters.put("list_of_respondents", getRespondentNames(caseData));
        parameters.put("linkToPortal",
                       notificationsProperties.getRespondentPortalLink() + "case-details/" + caseId);

        try {
            notificationClient.sendEmail(
                notificationsProperties.getEt3SubmissionConfirmationTemplateId(),
                email,
                parameters,
                UUID.randomUUID().toString()
            );
        } catch (NotificationClientException ne) {
            throw new NotificationException(ne);
        }
    }

    void sendNotificationStoredEmailToClaimant(CoreEmailDetails details, String shortText) {
        String emailAddress = details.caseData.getClaimantType().getClaimantEmailAddress();
        if (isBlank(emailAddress)) {
            log.info(NO_CLAIMANT_EMAIL_FOUND);
            return;
        }

        sendNotificationStoredEmail(
            notificationsProperties.getClaimantTseEmailStoredTemplateId(),
            details,
            shortText,
            emailAddress,
            notificationsProperties.getCitizenPortalLink() + details.caseId
        );
    }

    void sendNotificationStoredEmailToRespondent(CoreEmailDetails details, String shortText, String respondentIdamId) {
        RespondentSumTypeItem respondent = getRespondent(details.caseData(), respondentIdamId);

        String emailAddress = getRespondentEmail(respondent);
        if (isBlank(emailAddress)) {
            log.info("Respondent does not have an email address associated with their account");
            return;
        }
        String portalLink = notificationsProperties.getRespondentPortalLink()
            + details.caseId + "/" + respondent.getId()
            + (isWelshLanguage(respondent) ? WELSH_LANGUAGE_PARAM_WITHOUT_FWDSLASH : "");

        sendNotificationStoredEmail(
            notificationsProperties.getClaimantTseEmailStoredTemplateId(),
            details,
            shortText,
            emailAddress,
            portalLink
        );
    }

    private void sendNotificationStoredEmail(String emailTemplate, CoreEmailDetails details,
                                            String shortText, String emailAddress, String portalLinkKey) {

        Map<String, Object> claimantParameters = new ConcurrentHashMap<>();

        NotificationsHelper.addCommonParameters(
            claimantParameters,
            details.claimant,
            details.respondentNames,
            details.caseId,
            details.caseNumber
        );
        claimantParameters.put(SEND_EMAIL_PARAMS_SHORTTEXT_KEY, defaultIfEmpty(shortText, ""));
        claimantParameters.put(SEND_EMAIL_PARAMS_CITIZEN_PORTAL_LINK_KEY, portalLinkKey);

        try {
            notificationClient.sendEmail(
                emailTemplate,
                emailAddress,
                claimantParameters,
                details.caseId
            );
        } catch (NotificationClientException ne) {
            throw new NotificationException(ne);
        }
    }

    private RespondentSumTypeItem getRespondent(CaseData caseData, String userIdamId) {
        return caseData.getRespondentCollection().stream()
            .filter(r -> userIdamId.equals(r.getValue().getIdamId()))
            .findFirst()
            .orElse(null);
    }

    private String getRespondentEmail(RespondentSumTypeItem respondentSumTypeItem) {
        if (respondentSumTypeItem == null || respondentSumTypeItem.getValue() == null) {
            return null;
        }

        RespondentSumType respondent = respondentSumTypeItem.getValue();
        String responseEmail = respondent.getResponseRespondentEmail();
        if (StringUtils.isNotBlank(responseEmail)) {
            return responseEmail;
        }
        String respondentEmail = respondent.getRespondentEmail();
        if (StringUtils.isNotBlank(respondentEmail)) {
            return respondentEmail;
        }
        return null;
    }
}
