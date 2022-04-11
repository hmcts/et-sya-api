package uk.gov.hmcts.reform.et.syaapi.service;

import java.util.Arrays;

/**
 * Is thrown when errors are found in ACAS numbers in order to pinpoint the details.
 */
public class InvalidAcasNumbersException extends Exception {

    private static final long serialVersionUID = -3042681110164047285L;
    private final String[] invalidAcasNumbers;

    /**
     * Creates an {@link InvalidAcasNumbersException} with a message and the specified acas numbers that had been
     * provided which are invalid.
     *
     * @param message            the message explaining why this exception is thrown
     * @param invalidAcasNumbers the list of acas numbers which are invalid
     */
    public InvalidAcasNumbersException(String message, String... invalidAcasNumbers) {
        super(message);
        this.invalidAcasNumbers = Arrays.copyOf(invalidAcasNumbers, invalidAcasNumbers.length);
    }

    /**
     * Gets the list of acas numbers which are invalid.
     *
     * @return the list of acas numbers which are invalid
     */
    public String[] getInvalidAcasNumbers() {
        return Arrays.copyOf(invalidAcasNumbers, invalidAcasNumbers.length);
    }
}
