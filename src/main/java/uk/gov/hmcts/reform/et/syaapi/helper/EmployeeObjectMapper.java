package uk.gov.hmcts.reform.et.syaapi.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.ccd.Et1CaseData;

@Slf4j
@Service
public class EmployeeObjectMapper {

    /**
     * @param caseData which would be in json format
     * @return @link Et1CaseData format of input json object
     */
    public Et1CaseData getEmploymentCaseData(String caseData) {
        ObjectMapper mapper = new ObjectMapper();
        Et1CaseData data = null;
        try {
            data = mapper.readValue(caseData, Et1CaseData.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse the input json request body , {}", e);
        }
        return data;
    }
}
