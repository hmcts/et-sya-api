package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.HearingBundleType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimantBundlesRequest;

import static java.util.UUID.randomUUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class BundlesService {

    private final CaseService caseService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final NotificationService notificationService;

    /**
     * Submit Claimant Bundles.
     *
     * @param authorization - authorization
     * @param request       - bundles request from the claimant
     * @return the associated {@link CaseDetails} for the ID provided in request
     */
    public CaseDetails submitBundles(String authorization, ClaimantBundlesRequest request) {

        StartEventResponse startEventResponse = caseService.startUpdate(
            authorization,
            request.getCaseId(),
            request.getCaseTypeId(),
            CaseEvent.SUBMIT_CLAIMANT_BUNDLES
        );

        CaseData caseData = EmployeeObjectMapper
            .mapRequestCaseDataToCaseData(startEventResponse.getCaseDetails().getData());

        if (CollectionUtils.isEmpty(caseData.getBundlesClaimantCollection())) {
            caseData.setBundlesClaimantCollection(new ListTypeItem<>());
        }
        TypeItem<HearingBundleType> hearingBundleTypeItem = new TypeItem<>();
        hearingBundleTypeItem.setId(String.valueOf(randomUUID()));
        hearingBundleTypeItem.setValue(request.getClaimantBundles());

        caseData.getBundlesClaimantCollection().add(hearingBundleTypeItem);

        CaseDataContent content = caseDetailsConverter.caseDataContent(startEventResponse, caseData);

        CaseDetails response = caseService.submitUpdate(
            authorization,
            request.getCaseId(),
            content,
            request.getCaseTypeId()
        );

        notificationService.sendBundlesEmails(
            caseData,
            request.getCaseId(),
            request.getClaimantBundles().getHearing()
        );

        return response;
    }
}
