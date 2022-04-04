package uk.gov.hmcts.reform.et.syaapi.helper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseState;
import uk.gov.hmcts.reform.et.syaapi.models.EmploymentCaseData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaseDetailsConverter {

    private final ObjectMapper objectMapper;

    public CaseDetailsConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    public CaseData toCaseData(CaseDetails caseDetails) {
        Map<String, Object> data = new ConcurrentHashMap<>(caseDetails.getData());
        data.put("ccdCaseReference", caseDetails.getId());
        if (caseDetails.getState() != null) {
            data.put("ccdState", CaseState.valueOf(caseDetails.getState()));
        }
        return objectMapper.convertValue(data, CaseData.class);
    }

    public CaseData toCaseData(Map<String, Object> caseDataMap) {
        return objectMapper.convertValue(caseDataMap, CaseData.class);
    }

    public CaseDataContent caseDataContent(StartEventResponse startEventResponse, EmploymentCaseData data) {

        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder().id(startEventResponse.getEventId()).build())
            .data(data)
            .build();
    }
}

