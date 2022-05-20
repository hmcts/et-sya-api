package uk.gov.hmcts.reform.et.syaapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Role {
    private final String code;

    public Role(String code) {
        this.code = code;
    }

    @JsonProperty("code")
    public String getCode() {
        return code;
    }
}
