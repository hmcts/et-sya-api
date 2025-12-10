package uk.gov.hmcts.reform.et.syaapi.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

/**
 * Response model for case assignment operations.
 * Includes case details and a status indicator for whether the user was newly assigned or already assigned.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseAssignmentResponse {
    
    /**
     * List of case details affected by the assignment operation.
     */
    private List<CaseDetails> caseDetails;
    
    /**
     * Status of the assignment operation.
     */
    private AssignmentStatus status;
    
    /**
     * Optional message providing additional context about the operation.
     */
    private String message;
    
    /**
     * Enum representing the status of a case assignment operation.
     */
    public enum AssignmentStatus {
        /**
         * User was successfully assigned to the case (new assignment).
         */
        ASSIGNED,
        
        /**
         * User was already assigned to the case (no new assignment made).
         */
        ALREADY_ASSIGNED,
        
        /**
         * User is a professional user (legal representative) and should use MyHMCTS.
         */
        PROFESSIONAL_USER
    }
}
