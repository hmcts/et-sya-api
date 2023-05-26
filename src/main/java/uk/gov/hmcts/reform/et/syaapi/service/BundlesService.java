package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.types.HearingBundleType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimantBundlesRequest;

import java.util.ArrayList;

@RequiredArgsConstructor
@Service
@Slf4j
public class BundlesService {

        private final CaseService caseService;

        /**
         * Submit Claimant Bundles.
         *
         * @param authorization - authorization
         * @param request - bundles request from the claimant
         * @return the associated {@link CaseDetails} for the ID provided in request
         */
        public CaseDetails submitBundles(String authorization, ClaimantBundlesRequest request) {

            String caseTypeId = request.getCaseTypeId();
            CaseDetails caseDetails = caseService.getUserCase(authorization, request.getCaseId());
            HearingBundleType claimantBundles = request.getClaimantBundles();
            log.info("claimant bundle to add is " + claimantBundles.toString());

            caseDetails.getData().put("claimantBundles", claimantBundles);

            UploadedDocumentType hearingDoc = claimantBundles.getUploadFile();
            if (hearingDoc != null) {
                log.info("Uploading hearing document bundles file to document collection");
                caseService.uploadBundlesHearingDoc(caseDetails, hearingDoc);
            }

            CaseDetails finalCaseDetails = caseService.triggerEvent(
                authorization,
                request.getCaseId(),
                CaseEvent.SUBMIT_CLAIMANT_BUNDLES,
                caseTypeId,
                caseDetails.getData()
            );
            return finalCaseDetails;
        }
}
