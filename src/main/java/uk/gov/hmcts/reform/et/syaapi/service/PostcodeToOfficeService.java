package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.dwp.regex.InvalidPostcodeException;
import uk.gov.dwp.regex.PostCodeValidator;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.reform.et.syaapi.config.PostcodeToOfficeMappings;

import java.util.Optional;

/**
 * This provides services to lookup {@link TribunalOffice}'s.
 */
@Service
@Slf4j
public class PostcodeToOfficeService {

    private final PostcodeToOfficeMappings config;

    /**
     * Standard constructor.
     *
     * @param config the PostcodeToOfficeMappings to use for lookups
     */
    public PostcodeToOfficeService(PostcodeToOfficeMappings config) {
        this.config = config;
    }

    /**
     * Given an office name, this will retrieve the correct {@link TribunalOffice}.
     *
     * @param officeName is the name to seek the {@link TribunalOffice} for
     * @return the associated {@link TribunalOffice} for the office name provided
     * @throws IllegalArgumentException is a {@link TribunalOffice} doesn't exist for the name given
     */
    public TribunalOffice getTribunalOffice(String officeName) {
        return TribunalOffice.valueOfOfficeName(officeName);
    }

    /**
     * The service to obtain the {@link TribunalOffice} for a postcode.  If the postcode is not known, then an empty
     * Optional will be returned. If the postcode is invalid, then an {@link InvalidPostcodeException} will be thrown.
     *
     * @param postcode is the postcode to lookup
     * @return an Optional of the {@link TribunalOffice} located near the postcode provided
     * @throws InvalidPostcodeException if the postcode provided doesn't validate
     */
    public Optional<TribunalOffice> getTribunalOfficeFromPostcode(String postcode) throws InvalidPostcodeException {

        // validates postcode
        PostCodeValidator postCodeValidator = new PostCodeValidator(postcode);
        String outCode = postCodeValidator.returnOutwardCode();
        String area = postCodeValidator.returnArea();

        // example out codes are SW1A or RG1 and areas SW, RG
        if (config.getPostcodes().containsKey(outCode)) {
            return Optional.of(getTribunalOffice(config.getPostcodes().get(outCode)));
        } else if (config.getPostcodes().containsKey(area)) {
            return Optional.of(getTribunalOffice(config.getPostcodes().get(area)));
        }
        return Optional.empty();
    }
}
