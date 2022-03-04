package uk.gov.hmcts.reform.et.syaapi.service;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.et.syaapi.client.CcdApiClient;

@Slf4j
@Service
public class CaseService {

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private CcdApiClient ccdApiClient;

    @Retryable(value = {FeignException.class, RuntimeException.class})
    public CaseDetails getCaseData(String authorization, String caseId) {
        return ccdApiClient.getCase(authorization, authTokenGenerator.generate(), caseId);
    }

    @Retryable(value = {FeignException.class, RuntimeException.class})
    public CaseDetails createCase(String authorization, String caseType, String eventId) {
        String s2sToken = authTokenGenerator.generate();
        var ccdCase = ccdApiClient.startCase(authorization, s2sToken, caseType, eventId);
        CaseDataContent caseDataContent = CaseDataContent.builder()
            .event(Event.builder().id(eventId).build())
            .eventToken(ccdCase.getToken())
            .build();
        return ccdApiClient.createCase(authorization, s2sToken, caseType, caseDataContent);
    }
}
