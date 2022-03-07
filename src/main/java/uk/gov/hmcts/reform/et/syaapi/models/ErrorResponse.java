package uk.gov.hmcts.reform.et.syaapi.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
    private String message;
    private Integer code;
}
