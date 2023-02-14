package uk.gov.hmcts.reform.et.syaapi.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;

@Data
@Builder
@Jacksonized
public class ClaimantApplicationRequest {

    @JsonProperty("case_id")
    private String caseId;
    @JsonProperty("case_type_id")
    private String caseTypeId;
    @JsonProperty("is_type_c")
    private boolean isTypeC;
    @JsonProperty("claimant_tse")
    private ClaimantTse claimantTse;
}
