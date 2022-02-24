package uk.gov.hmcts.reform.et.syaapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import uk.gov.dwp.regex.InvalidPostcodeException;
import uk.gov.hmcts.reform.et.syaapi.model.helper.TribunalOffice;
import uk.gov.hmcts.reform.et.syaapi.service.PostcodeToOfficeService;
import uk.gov.hmcts.reform.et.syaapi.service.exceptions.PostcodeNotFoundInLookupException;

import static org.springframework.http.ResponseEntity.ok;

@Component
@RestController
@RequestMapping("/api/v1/postcode")
public class PostcodeController {

    @Autowired
    private PostcodeToOfficeService postcodeToOfficeService;

    @GetMapping("/{postcode}")
    public ResponseEntity<String> getOffice(@PathVariable("postcode") String postcode) throws InvalidPostcodeException {
        try {
            TribunalOffice foundOffice = this.postcodeToOfficeService.getTribunalOfficeFromPostcode(postcode);
            return ok(foundOffice.getOfficeName());

        } catch (InvalidPostcodeException | PostcodeNotFoundInLookupException e){
            return ok("not found");
        }
    }



}
