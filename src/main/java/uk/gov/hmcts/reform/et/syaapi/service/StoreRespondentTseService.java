package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentTse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.exception.NotificationException;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.models.RespondentApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.notification.NotificationsProperties;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.STORED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.STORED_STATE;
import static uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse.CY_RESPONDENT_APP_TYPE_MAP;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_CITIZEN_PORTAL_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_SHORTTEXT_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.WELSH_LANGUAGE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.WELSH_LANGUAGE_PARAM_WITHOUT_FWDSLASH;

@RequiredArgsConstructor
@Service
@Slf4j
public class StoreRespondentTseService {
    private final CaseService caseService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final NotificationsProperties notificationsProperties;
    private final NotificationClient notificationClient;

    private static final String CASE_DETAILS_NOT_FOUND = "submitUpdate CaseDetails not found";
    private static final String RESPONDENT_NOT_FOUND = "Respondent not found";

    /**
     * Store Respondent Application to Tell Something Else.
     *
     * @param authorization - authorization
     * @param request - application request from the respondent
     * @return the associated {@link CaseDetails} for the ID provided in request
     */
    public CaseDetails storeApplication(String authorization, RespondentApplicationRequest request) {
        StartEventResponse startEventResponse = caseService.startUpdate(
            authorization,
            request.getCaseId(),
            request.getCaseTypeId(),
            CaseEvent.STORE_RESPONDENT_TSE
        );

        // Create new GenericTseApplicationTypeItem
        RespondentTse respondentTse = request.getRespondentTse();
        GenericTseApplicationTypeItem newStoreItem = getToStoreTseAppTypeItem(respondentTse);

        // Add item into TseRespondentStoredCollection
        CaseData caseData = EmployeeObjectMapper
            .convertCaseDataMapToCaseDataObject(startEventResponse.getCaseDetails().getData());
        if (CollectionUtils.isEmpty(caseData.getTseRespondentStoredCollection())) {
            caseData.setTseRespondentStoredCollection(new ArrayList<>());
        }
        caseData.getTseRespondentStoredCollection().add(newStoreItem);

        // Submit update
        CaseDetails finalCaseDetails = caseService.submitUpdate(
            authorization,
            request.getCaseId(),
            caseDetailsConverter.caseDataContent(startEventResponse, caseData),
            request.getCaseTypeId()
        );

        // Send acknowledgement email to respondent
        if (finalCaseDetails == null) {
            throw new IllegalArgumentException(CASE_DETAILS_NOT_FOUND);
        }
        sendAcknowledgementEmails(request.getRespondentTse(), finalCaseDetails);

        return finalCaseDetails;
    }

    private static GenericTseApplicationTypeItem getToStoreTseAppTypeItem(RespondentTse respondentTse) {
        GenericTseApplicationType application =  GenericTseApplicationType.builder()
            .applicant(RESPONDENT_TITLE)
            .applicantIdamId(respondentTse.getRespondentIdamId())
            .date(UtilHelper.formatCurrentDate(LocalDate.now()))
            .responsesCount("0")
            .status(STORED_STATE)
            .applicationState(STORED)
            .type(respondentTse.getContactApplicationType())
            .details(respondentTse.getContactApplicationText())
            .documentUpload(respondentTse.getContactApplicationFile())
            .copyToOtherPartyYesOrNo(respondentTse.getCopyToOtherPartyYesOrNo())
            .copyToOtherPartyText(respondentTse.getCopyToOtherPartyText())
            .build();

        return GenericTseApplicationTypeItem.builder()
            .id(UUID.randomUUID().toString())
            .value(application)
            .build();
    }

    private void sendAcknowledgementEmails(
        RespondentTse respondentTse,
        CaseDetails finalCaseDetails
    ) {
        // email template (using the same template as claimant)
        String emailTemplate = notificationsProperties.getClaimantTseEmailStoredTemplateId();

        CaseData caseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(finalCaseDetails.getData());
        RespondentSumTypeItem respondent = getRespondent(caseData, respondentTse.getRespondentIdamId());
        if (respondent == null) {
            throw new IllegalArgumentException(RESPONDENT_NOT_FOUND);
        }

        // email address
        String emailAddress = getRespondentEmail(respondent.getValue());
        if (emailAddress == null) {
            return;
        }

        Map<String, Object> emailParameters = new ConcurrentHashMap<>();

        // email parameter: caseNumber
        String caseNumber = caseData.getEthosCaseReference();
        emailParameters.put(SEND_EMAIL_PARAMS_CASE_NUMBER_KEY, caseNumber);

        // email parameter: shortText
        boolean isWelsh = WELSH_LANGUAGE.equals(respondent.getValue().getEt3ResponseLanguagePreference());
        String appTypeByLanguage = isWelsh
            ? CY_RESPONDENT_APP_TYPE_MAP.get(respondentTse.getContactApplicationType())
            : respondentTse.getContactApplicationType();
        emailParameters.put(SEND_EMAIL_PARAMS_SHORTTEXT_KEY, appTypeByLanguage);

        // email parameter: citizenPortalLink
        String caseId = finalCaseDetails.getId().toString();
        String link = notificationsProperties.getRespondentPortalLink() + caseId + "/" + respondent.getId()
            + (isWelsh ? WELSH_LANGUAGE_PARAM_WITHOUT_FWDSLASH : "");
        emailParameters.put(SEND_EMAIL_PARAMS_CITIZEN_PORTAL_LINK_KEY, link);

        // send email
        try {
            notificationClient.sendEmail(
                emailTemplate,
                emailAddress,
                emailParameters,
                caseId
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

    private String getRespondentEmail(RespondentSumType respondent) {
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
