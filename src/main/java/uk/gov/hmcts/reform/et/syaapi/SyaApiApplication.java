package uk.gov.hmcts.reform.et.syaapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.idam.client.IdamApi;

@SpringBootApplication(
    scanBasePackages = {"uk.gov.hmcts.ccd.sdk", "uk.gov.hmcts.reform.et.syaapi", "uk.gov.hmcts.ecm.common"}
)
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
@EnableFeignClients(
    clients = {
        IdamApi.class,
        ServiceAuthorisationApi.class,
        CaseUserApi.class,
        CoreCaseDataApi.class,
        CaseAssignmentApi.class,
    }
)
@EnableCaching
public class SyaApiApplication {

    public static void main(final String[] args) {
        SpringApplication.run(SyaApiApplication.class, args);
    }
}
