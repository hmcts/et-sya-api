package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentTse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper;
import uk.gov.hmcts.reform.et.syaapi.models.RespondentApplicationRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.STORED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.STORED_STATE;
import static uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse.APP_TYPE_MAP;
import static uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper.getRespondentNames;

@RequiredArgsConstructor
@Service
@Slf4j
public class StoreRespondentTseService {
    private final CaseService caseService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final NotificationService notificationService;

    private static final String NOT_SET = "Not set";
    private static final String FINAL_CASE_DETAILS_NOT_FOUND = "submitUpdate finalCaseDetails not found";

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
            throw new IllegalArgumentException(FINAL_CASE_DETAILS_NOT_FOUND);
        }
        sendAcknowledgementEmails(request, finalCaseDetails);

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
        RespondentApplicationRequest request,
        CaseDetails finalCaseDetails
    ) {
        CaseData caseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(finalCaseDetails.getData());

        ClaimantIndType claimantIndType = caseData.getClaimantIndType();
        NotificationService.CoreEmailDetails details = new NotificationService.CoreEmailDetails(
            caseData,
            claimantIndType.getClaimantFirstNames() + " " + claimantIndType.getClaimantLastName(),
            caseData.getEthosCaseReference(),
            getRespondentNames(caseData),
            NotificationsHelper.getNearestHearingToReferral(caseData, NOT_SET),
            finalCaseDetails.getId().toString()
        );

        RespondentTse respondentTse = request.getRespondentTse();

        notificationService.sendStoredEmailToRespondent(
            details, APP_TYPE_MAP.get(respondentTse.getContactApplicationType()));
    }
}
