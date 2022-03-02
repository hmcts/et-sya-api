package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.et.syaapi.client.CcdApiClient;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

@Slf4j
@Service
@AllArgsConstructor
public class CreateCaseService {

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private CcdApiClient ccdApiClient;

    @Autowired
    private IdamApi idamApi;

    public CaseDetails getCaseData(String authorization, String id) {
        return ccdApiClient.getCase(authorization, authTokenGenerator.generate(), id);
    }

    public CaseDetails createCase(String authorization, String caseType, String eventId) {
        String s2sToken = authTokenGenerator.generate();
        UserInfo user = idamApi.retrieveUserInfo(authorization);
        var ccdCase = ccdApiClient.startCase(authorization, s2sToken, caseType, eventId);
        CaseDetails caseDetails = ccdCase.getCaseDetails();
//        CaseData caseData = new CaseData();
//        caseData.setClaimantTypeOfClaimant(caseDetails.getCaseTypeId());
        CaseDataContent caseDataContent = CaseDataContent.builder()
            .event(Event.builder().id(eventId).build())
//            .data(caseData)
            .eventToken(ccdCase.getToken())
            .build();
        CaseDetails data = ccdApiClient.createCase(authorization, s2sToken, caseType, caseDataContent);
        return data;
    }
}
