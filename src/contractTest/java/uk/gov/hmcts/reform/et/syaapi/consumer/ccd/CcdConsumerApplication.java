package uk.gov.hmcts.reform.et.syaapi.consumer.ccd;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.et.syaapi.service.CaseService;

@SpringBootApplication
@EnableFeignClients(clients = {
    CaseService.class
})
public class CcdConsumerApplication {
    @MockBean
    RestTemplate restTemplate;
}
