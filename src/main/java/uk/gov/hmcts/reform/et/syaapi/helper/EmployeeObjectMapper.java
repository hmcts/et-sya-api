package uk.gov.hmcts.reform.et.syaapi.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.et.syaapi.models.EmploymentCaseData;

@Slf4j
@Service
public class EmployeeObjectMapper {

    public EmploymentCaseData getEmploymentCaseData(String caseData) {
        ObjectMapper mapper = new ObjectMapper();
        EmploymentCaseData data = null;
        try {
            data = mapper.readValue(caseData, EmploymentCaseData.class);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return data;
    }
}
