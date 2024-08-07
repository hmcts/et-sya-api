package uk.gov.hmcts.reform.et.syaapi.helper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.Et1CaseData;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseState;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Converts {@link CaseDetails} to other case related classes using {@link ObjectMapper}.
 */
@Slf4j
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
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    /**
     * For a case, Converts @CaseDetails.data to @CaseData object.
     *
     * @param caseDetails would represent CaseData for the case
     * @return caseData represent cases in java object model
     */
    public CaseData toCaseData(CaseDetails caseDetails) {
        if (caseDetails == null) {
            return null;
        }

        Map<String, Object> data = new ConcurrentHashMap<>();
        if (caseDetails.getData() != null) {
            data.putAll(caseDetails.getData());
        }

        if (caseDetails.getId() != null) {
            data.put("ccdCaseReference", caseDetails.getId());
        }

        if (caseDetails.getState() != null) {
            data.put("ccdState", CaseState.valueOf(caseDetails.getState().toUpperCase(Locale.ROOT)));
        }
        return objectMapper.convertValue(data, CaseData.class);
    }

    public CaseData getCaseData(Map<String, Object> caseData) {
        return objectMapper.convertValue(caseData, CaseData.class);
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

    /**
     * Converts Request caseData field map to CaseData which is used to create CaseDataContent and save in CCD.
     *
     * @param requestData map object that contains the data field values of the request case details
     * @param latestData map object that contains the data field values of the updated case details
     *                   obtained from CCD api call
     * @return {@link CaseData} which returns latest CaseData object representing the contents of the Case Data
     */
    public CaseData mapRequestCaseDataToLatestCaseData(Map<String, Object> requestData,
                                                       Map<String, Object> latestData) {
        CaseData requestCaseData = EmployeeObjectMapper.mapRequestCaseDataToCaseData(requestData);
        log.error("Request CaseData: {}", requestData.toString());
        CaseData latestCaseData = EmployeeObjectMapper.mapRequestCaseDataToCaseData(latestData);
        log.error("Latest CaseData: {}", latestData.toString());
        copyNonNullProperties(requestCaseData, latestCaseData);
        return latestCaseData;
    }

    public void copyNonNullProperties(CaseData sourceCaseData, CaseData targetCaseData) {
        Class<?> sourceClass = sourceCaseData.getClass();
        try {
            log.error("in try block to copy copyNonNullProperties - sourceClass: {}", sourceCaseData);
            log.error("in try block to copy copyNonNullProperties - targetClass: {}", targetCaseData);
            for (Field field : sourceClass.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(sourceCaseData);
                if (value != null) {
                    field.set(targetCaseData, value);
                }
                //reset accessibility to prevent illegal access
                field.setAccessible(false);
            }
            log.error("in try block to copy copyNonNullProperties - copied targetClass: {}", targetCaseData);
        } catch (IllegalAccessException e) {
            log.error("Failed to copy the Non-Null field values of the request CaseData to the latest CaseData: {}",
                      e.getMessage());
        }
    }

}
