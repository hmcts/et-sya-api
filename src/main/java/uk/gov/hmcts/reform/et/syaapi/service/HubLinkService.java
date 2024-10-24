package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.models.HubLinksStatusesRequest;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

@Slf4j
@RequiredArgsConstructor
@Service
public class HubLinkService {
    private final CaseService caseService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final FeatureToggleService featureToggleService;
    private final ManageCaseRoleService manageCaseRoleService;

    /**
     * Updates case data with hub link statuses {@link CaseDetails}.
     *
     * @param request       hub link status data
     * @param authorization is used to find the {@link UserInfo} for request
     * @return the associated {@link CaseDetails} if the case is created
     */
    public CaseDetails updateHubLinkStatuses(HubLinksStatusesRequest request,
                                             String authorization,
                                             String caseUserRole) {

        if (featureToggleService.isCaseFlagsEnabled()) {
            log.info("Case flags enabled - calling UPDATE_HUBLINK_STATUS");
            StartEventResponse startEventResponse = caseService.startUpdate(
                authorization,
                request.getCaseId(),
                request.getCaseTypeId(),
                CaseEvent.UPDATE_HUBLINK_STATUS
            );

            CaseData caseData = EmployeeObjectMapper
                .mapRequestCaseDataToCaseData(startEventResponse.getCaseDetails().getData());
            caseData.setHubLinksStatuses(request.getHubLinksStatuses());

            return caseService.submitUpdate(
                authorization,
                request.getCaseId(),
                caseDetailsConverter.caseDataContent(startEventResponse, caseData),
                request.getCaseTypeId()
            );
        } else {
            log.info("Case flags disabled - calling UPDATE_CASE_SUBMITTED");
            // Added parameter case user role as creator.
            CaseDetails caseDetails = manageCaseRoleService.getUserCaseByCaseUserRole(authorization,
                                                                            request.getCaseId(),
                                                                            caseUserRole);
            caseDetails.getData().put("hubLinksStatuses", request.getHubLinksStatuses());

            return caseService.triggerEvent(
                authorization,
                request.getCaseId(),
                CaseEvent.UPDATE_CASE_SUBMITTED,
                request.getCaseTypeId(),
                caseDetails.getData()
            );
        }
    }
}
