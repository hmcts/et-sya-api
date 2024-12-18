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
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimantApplicationRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.STORED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.STORED_STATE;
import static uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse.APP_TYPE_MAP;
import static uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper.getRespondentNames;

@RequiredArgsConstructor
@Service
@Slf4j
public class StoredApplicationService {

    private final CaseService caseService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final NotificationService notificationService;

    private static final String FINAL_CASE_DETAILS_NOT_FOUND = "submitUpdate finalCaseDetails not found";

    /**
     * Store Claimant Application.
     *
     * @param authorization - authorization
     * @param request - application request from the claimant
     * @return the associated {@link CaseDetails} for the ID provided in request
     */
    public CaseDetails storeApplication(String authorization, ClaimantApplicationRequest request) {
        StartEventResponse startEventResponse = caseService.startUpdate(
            authorization,
            request.getCaseId(),
            request.getCaseTypeId(),
            CaseEvent.STORE_CLAIMANT_TSE
        );

        CaseData caseData = EmployeeObjectMapper
            .mapRequestCaseDataToCaseData(startEventResponse.getCaseDetails().getData());

        // Create new GenericTseApplicationTypeItem
        ClaimantTse claimantTse = request.getClaimantTse();
        GenericTseApplicationType newStoreType = getToStoreTseAppType(claimantTse);
        GenericTseApplicationTypeItem newStoreTypeItem = getToStoreTseAppTypeItem(newStoreType);

        // Add item into TseApplicationStoredCollection
        if (CollectionUtils.isEmpty(caseData.getTseApplicationStoredCollection())) {
            caseData.setTseApplicationStoredCollection(new ArrayList<>());
        }
        caseData.getTseApplicationStoredCollection().add(newStoreTypeItem);

        // Submit Update
        CaseDetails finalCaseDetails =  caseService.submitUpdate(
            authorization,
            request.getCaseId(),
            caseDetailsConverter.caseDataContent(startEventResponse, caseData),
            request.getCaseTypeId()
        );

        // Send Acknowledgement Emails
        if (finalCaseDetails == null) {
            throw new IllegalArgumentException(FINAL_CASE_DETAILS_NOT_FOUND);
        }
        sendAcknowledgementEmails(request, finalCaseDetails);

        return finalCaseDetails;
    }

    private static GenericTseApplicationTypeItem getToStoreTseAppTypeItem(GenericTseApplicationType application) {
        GenericTseApplicationTypeItem tseApplicationTypeItem = new GenericTseApplicationTypeItem();
        tseApplicationTypeItem.setId(UUID.randomUUID().toString());
        tseApplicationTypeItem.setValue(application);

        return tseApplicationTypeItem;
    }

    private static GenericTseApplicationType getToStoreTseAppType(ClaimantTse claimantTse) {
        GenericTseApplicationType application = new GenericTseApplicationType();

        application.setDate(UtilHelper.formatCurrentDate(LocalDate.now()));
        application.setApplicant(CLAIMANT_TITLE);
        application.setApplicationState(STORED);
        application.setStatus(STORED_STATE);

        application.setType(ClaimantTse.APP_TYPE_MAP.get(claimantTse.getContactApplicationType()));
        application.setDetails(claimantTse.getContactApplicationText());
        application.setDocumentUpload(claimantTse.getContactApplicationFile());
        application.setCopyToOtherPartyYesOrNo(claimantTse.getCopyToOtherPartyYesOrNo());
        application.setCopyToOtherPartyText(claimantTse.getCopyToOtherPartyText());

        return application;
    }

    private void sendAcknowledgementEmails(
        ClaimantApplicationRequest request,
        CaseDetails finalCaseDetails
    ) {
        CaseData caseData = EmployeeObjectMapper.mapRequestCaseDataToCaseData(finalCaseDetails.getData());
        ClaimantIndType claimantIndType = caseData.getClaimantIndType();
        String hearingDate = NotificationsHelper.getNearestHearingToReferral(caseData, "Not set");
        NotificationService.CoreEmailDetails details = new NotificationService.CoreEmailDetails(
            caseData,
            claimantIndType.getClaimantFirstNames() + " " + claimantIndType.getClaimantLastName(),
            caseData.getEthosCaseReference(),
            getRespondentNames(caseData),
            hearingDate,
            finalCaseDetails.getId().toString()
        );

        ClaimantTse claimantTse = request.getClaimantTse();

        notificationService.sendStoredEmailToClaimant(
            details, APP_TYPE_MAP.get(claimantTse.getContactApplicationType()));
    }
}
