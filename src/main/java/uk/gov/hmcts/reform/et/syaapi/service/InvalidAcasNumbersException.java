package uk.gov.hmcts.reform.et.syaapi.service;

import java.util.List;

public class InvalidAcasNumbersException extends Exception {

    private final List<String> invalidAcasNumbers;

    /**
     * Creates an {@link InvalidAcasNumbersException} with the specified acas numbers that had been provided which are
     * invalid.
     *
     * @param invalidAcasNumbers the list of acas numbers which are invalid
     */
    public InvalidAcasNumbersException(List<String> invalidAcasNumbers) {
        super();
        this.invalidAcasNumbers = invalidAcasNumbers;
    }

    /**
     * Gets the list of acas numbers which are invalid.
     *
     * @return the list of acas numbers which are invalid
     */
    public List<String> getInvalidAcasNumbers() {
        return invalidAcasNumbers;
    }
}
