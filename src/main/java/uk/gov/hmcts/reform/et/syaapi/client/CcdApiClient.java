package uk.gov.hmcts.reform.et.syaapi.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(name = "ccd-api", url = "${core_case_data.api.url}")
public interface CcdApiClient extends CoreCaseDataApi {

    @PostMapping(
        path = "/case-types/{caseTypeId}/cases",
        headers = EXPERIMENTAL
    )
    CaseDetails createCase(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @PathVariable("caseTypeId") String caseType,
        @RequestBody CaseDataContent caseDataContent
    );
}
