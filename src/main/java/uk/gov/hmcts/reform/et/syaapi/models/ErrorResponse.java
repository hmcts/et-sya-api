package uk.gov.hmcts.reform.et.syaapi.models;

import lombok.Builder;

@Builder
public class ErrorResponse {
    private String message;
    private Integer code;
}
