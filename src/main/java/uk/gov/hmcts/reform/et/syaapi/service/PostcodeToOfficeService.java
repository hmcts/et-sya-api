package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.dwp.regex.InvalidPostcodeException;
import uk.gov.dwp.regex.PostCodeValidator;
import uk.gov.hmcts.reform.et.syaapi.config.PostcodeToOfficeLookup;
import uk.gov.hmcts.reform.et.syaapi.model.helper.TribunalOffice;
import uk.gov.hmcts.reform.et.syaapi.service.exceptions.PostcodeNotFoundInLookupException;

//@AllArgsConstructor
@Component
@Service
@Slf4j
public class PostcodeToOfficeService {
    @Autowired
    private PostcodeToOfficeLookup config;

//    public PostcodeToOfficeService(PostcodeToOfficeLookup config) {
//        this.config = config;
//    }

    public PostCodeValidator createPostCodeValidator(String postcode) throws InvalidPostcodeException {
        return new PostCodeValidator(postcode);
    }

    public TribunalOffice getTribunalOffice(String officeName) {
        System.out.println("get tribunal office called with " + officeName);
        return TribunalOffice.valueOfOfficeName(officeName);
    }

    public TribunalOffice getTribunalOfficeFromPostcode(String postcode) throws InvalidPostcodeException, PostcodeNotFoundInLookupException {

        // validation
        PostCodeValidator postCodeValidator = createPostCodeValidator(postcode);
        String outCode = postCodeValidator.returnOutwardCode();
        String area = postCodeValidator.returnArea();

        // Lookup exists
        Boolean boolOutcode = config.getPostcodes().containsKey(outCode);
        Boolean boolArea = config.getPostcodes().containsKey(area);

        if (boolOutcode) {
            return getTribunalOffice(config.getPostcodes().get(outCode));
        } else if (boolArea) {
            return getTribunalOffice(config.getPostcodes().get(area));
        } else {
            throw new PostcodeNotFoundInLookupException("Not found: " + postcode);
        }
    }
}
