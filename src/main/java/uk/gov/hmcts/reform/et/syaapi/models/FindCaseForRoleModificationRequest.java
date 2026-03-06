package uk.gov.hmcts.reform.et.syaapi.models;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class FindCaseForRoleModificationRequest {

    @JsonProperty("caseSubmissionReference")
    private String caseSubmissionReference;
    @JsonProperty("respondentName")
    private String respondentName;
    @JsonProperty("claimantFirstNames")
    private String claimantFirstNames;
    @JsonProperty("claimantLastName")
    private String claimantLastName;
    @JsonProperty("applicationName")
    private String applicationName;

}
