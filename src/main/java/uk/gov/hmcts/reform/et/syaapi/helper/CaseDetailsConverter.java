package uk.gov.hmcts.reform.et.syaapi.helper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.Et1CaseData;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Converts {@link CaseDetails} to other case related classes using {@link ObjectMapper}.
 */
@Service
public class CaseDetailsConverter {

    private final ObjectMapper objectMapper;

    /**
     * Constructor for @ObjectMapper class.
     *
     * @param objectMapper jackson {@link ObjectMapper} object to initialize object
     */
    public CaseDetailsConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * For a case, Converts @CaseDetails.data to @CaseData object.
     *
     * @param caseDetails would represent CaseData for the case
     * @return caseData represent cases in java object model
     */
    public CaseData toCaseData(CaseDetails caseDetails) {
        Map<String, Object> data = new ConcurrentHashMap<>(caseDetails.getData());
        data.put("ccdCaseReference", caseDetails.getId());
        if (caseDetails.getState() != null) {
            data.put("ccdState", CaseState.valueOf(caseDetails.getState()));
        }
        return objectMapper.convertValue(data, CaseData.class);
    }

    /**
     * Converts pre-submitted case related details to CaseDataContent which gets saved to CCD.
     *
     * @param startEventResponse associated case details updated
     * @param et1CaseData original json format represented object
     * @return {@link CaseDataContent} which returns overall contents of the case
     */
    public CaseDataContent et1ToCaseDataContent(StartEventResponse startEventResponse, Et1CaseData et1CaseData) {
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder().id(startEventResponse.getEventId()).build())
            .data(et1CaseData)
            .build();
    }

    /**
     * Converts Case related details to CaseDataContent which gets saved to CCD.
     *
     * @param startEventResponse associated case details updated
     * @param caseData original json format represented object
     * @return {@link CaseDataContent} which returns overall contents of the case
     */
    public CaseDataContent caseDataContent(StartEventResponse startEventResponse, CaseData caseData) {
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder().id(startEventResponse.getEventId()).build())
            .data(caseData)
            .build();
    }
}
