package uk.gov.hmcts.reform.et.syaapi.service.exceptions;

public class PostcodeNotFoundInLookupException extends Exception {
    public PostcodeNotFoundInLookupException(String input) {
        super(input);
    }
}
