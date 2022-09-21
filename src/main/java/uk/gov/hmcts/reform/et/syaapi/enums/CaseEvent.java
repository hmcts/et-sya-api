package uk.gov.hmcts.reform.et.syaapi.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CaseEvent {
    INITIATE_CASE_DRAFT,
    UPDATE_CASE_DRAFT,
    SUBMIT_CASE_DRAFT,
    UPDATE_CASE_SUBMITTED
}
