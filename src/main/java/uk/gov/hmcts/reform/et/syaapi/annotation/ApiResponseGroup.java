package uk.gov.hmcts.reform.et.syaapi.annotation;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Accessed successfully"),
    @ApiResponse(responseCode = "400", description = "Bad Request"),
    @ApiResponse(responseCode = "403", description = "Calling service is not authorised to use the endpoint"),
    @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
public @interface ApiResponseGroup {
}
