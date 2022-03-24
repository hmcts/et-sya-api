package uk.gov.hmcts.reform.et.syaapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.et.syaapi.client.CcdApiClient;
import uk.gov.hmcts.reform.et.syaapi.models.EmploymentCaseData;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;

@Slf4j
@Service
public class CaseService {

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private CcdApiClient ccdApiClient;

    @Autowired
    private IdamClient idamClient;

    @Retryable({FeignException.class, RuntimeException.class})
    public CaseDetails getCaseData(String authorization, String caseId) {
        return ccdApiClient.getCase(authorization, authTokenGenerator.generate(), caseId);
    }

    @Retryable({FeignException.class, RuntimeException.class})
    public List<CaseDetails> getCaseDataByUser(String authorization, String caseType, String searchString) {
        SearchResult searchResult =ccdApiClient.searchCases(authorization, authTokenGenerator.generate(), caseType, searchString);
        return searchResult.getCases();
    }

    @Retryable({FeignException.class, RuntimeException.class})
    public CaseDetails createCase(String authorization, String caseType, String eventId) {
        String s2sToken = authTokenGenerator.generate();
        var ccdCase = ccdApiClient.startCase(authorization, s2sToken, caseType, eventId);
        CaseDataContent caseDataContent = CaseDataContent.builder()
            .event(Event.builder().id(eventId).build())
            .eventToken(ccdCase.getToken())
            .build();
        return ccdApiClient.createCase(authorization, s2sToken, caseType, caseDataContent);
    }

    @Retryable({FeignException.class, RuntimeException.class})
    public CaseDetails createCaseWithBody(String authorization, String caseType, String jurisdictionId, String eventId,
                                          String caseData) {
        ObjectMapper mapper = new ObjectMapper();
        EmploymentCaseData data = null;
        try {
            data = mapper.readValue(caseData, EmploymentCaseData.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        String s2sToken = authTokenGenerator.generate();
        UserDetails userDetails = idamClient.getUserDetails(authorization);
        var newCase = ccdApiClient.startForCitizen(
            authorization,
            s2sToken,
            userDetails.getId(),
            jurisdictionId,
            caseType,
            eventId
        );

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .event(Event.builder().id(eventId).build())
            .eventToken(newCase.getToken())
            .data(data)
            .build();
        return ccdApiClient.submitForCitizen(
            authorization,
            s2sToken,
            userDetails.getId(),
            jurisdictionId,
            caseType,
            true,
            caseDataContent
        );
    }
}
