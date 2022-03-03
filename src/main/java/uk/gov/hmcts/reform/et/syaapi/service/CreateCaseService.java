package uk.gov.hmcts.reform.et.syaapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.et.syaapi.client.CcdApiClient;

import java.util.Map;

@Slf4j
@Service
public class CreateCaseService {

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private CcdApiClient ccdApiClient;

    public CaseDetails getCaseData(String authorization, String id) {
        return ccdApiClient.getCase(authorization, authTokenGenerator.generate(), id);
    }

    public CaseDetails createCase(String authorization, String caseType, String eventId) {
        String s2sToken = authTokenGenerator.generate();
        var ccdCase = ccdApiClient.startCase(authorization, s2sToken, caseType, eventId);
        CaseData caseData = new CaseData();
        caseData.setReceiptDate("01/03/2022");
        caseData.setFeeGroupReference("test");
        caseData.setManagingOffice("Edinburgh");
        caseData.setEcmCaseType(caseType);
        caseData.setClaimantTypeOfClaimant("Individual");
        caseData.setClaimantType(new ClaimantType());
        caseData.setClaimantWorkAddressQRespondent(new DynamicFixedListType());
        caseData.setClaimantWorkAddressQuestion("test");
        caseData.setClaimantRepresentedQuestion("You okay???");
        CaseDataContent caseDataContent = CaseDataContent.builder()
            .event(Event.builder().id(eventId).build())
            .data(caseData)
            .eventToken(ccdCase.getToken())
            .build();
        return ccdApiClient.createCase(authorization, s2sToken, caseType, caseDataContent);
    }
}
