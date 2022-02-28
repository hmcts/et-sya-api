package uk.gov.hmcts.reform.et.syaapi.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.dwp.regex.InvalidPostcodeException;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.reform.et.syaapi.service.PostcodeToOfficeService;

import java.util.Optional;

import static org.springframework.http.ResponseEntity.ok;

/**
 * Used to test the features of postcode lookup.
 */
@RestController
public class PostcodeController {

    private final PostcodeToOfficeService postcodeToOfficeService;

    public PostcodeController(PostcodeToOfficeService postcodeToOfficeService) {
        this.postcodeToOfficeService = postcodeToOfficeService;
    }

    /**
     * Interface to retrieve the office for a given postcode.
     *
     * @param postcode the postcode to lookup
     * @return an Optional of the {@link TribunalOffice} based upon the postcode provided
     * @throws InvalidPostcodeException if there's a problem validating the postcode
     */
    @GetMapping("/api/v1/postcode/{postcode}")
    public ResponseEntity<Optional<TribunalOffice>> getOffice(@PathVariable("postcode") String postcode)
        throws InvalidPostcodeException {
        Optional<TribunalOffice> foundOffice = postcodeToOfficeService.getTribunalOfficeFromPostcode(postcode);
        if (foundOffice.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return ok(foundOffice);
    }
}
