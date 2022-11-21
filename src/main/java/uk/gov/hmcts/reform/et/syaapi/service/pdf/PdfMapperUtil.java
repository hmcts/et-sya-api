package uk.gov.hmcts.reform.et.syaapi.service.pdf;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.gov.dwp.regex.InvalidPostcodeException;
import uk.gov.dwp.regex.PostCodeValidator;
import uk.gov.hmcts.et.common.model.ccd.Address;

import java.util.Locale;
import java.util.Set;

@Slf4j
public final class PdfMapperUtil {

    private static final Set<String> UK_COUNTRIES = Set.of("ENGLAND", "SCOTLAND", "NORTHERN IRELAND", "WALES");

    private PdfMapperUtil() {

    }

    private static boolean isUkCountry(String countryName) {
        return StringUtils.isEmpty(countryName) || UK_COUNTRIES.contains(countryName);
    }

    private static String formatAddressLines(String addressLine) {
        return toCapitalCase(addressLine).trim();
    }

    private static String toCapitalCase(String addressLine) {
        String[] addressLineWords = addressLine.toLowerCase(Locale.UK).split(" ");

        StringBuilder addressLineModified = new StringBuilder();
        for (String word : addressLineWords) {
            addressLineModified.append(word.substring(0, 1).toUpperCase(Locale.UK))
                .append(word.substring(1)).append(' ');
        }
        return addressLineModified.toString();
    }

    public static String formatAddressForTextField(Address address) {
        StringBuilder addressStringValue = new StringBuilder();

        if (StringUtils.isNotEmpty(address.getAddressLine1())) {
            addressStringValue.append(formatAddressLines(address.getAddressLine1())).append(',');
        } else {
            return null;
        }
        if (StringUtils.isNotEmpty(address.getAddressLine2())) {
            addressStringValue.append('\n').append(formatAddressLines(address.getAddressLine2())).append(',');
        }
        if (StringUtils.isNotEmpty(address.getAddressLine3())) {
            addressStringValue.append('\n').append(formatAddressLines(address.getAddressLine3())).append(',');
        }
        if (StringUtils.isNotEmpty(address.getPostTown())) {
            addressStringValue.append('\n').append(formatAddressLines(address.getPostTown())).append(',');
        } else {
            return null;
        }
        if (StringUtils.isNotEmpty(address.getCounty())) {
            addressStringValue.append('\n').append(formatAddressLines(address.getCounty())).append(',');
        }
        if (StringUtils.isNotEmpty(address.getCountry())) {
            addressStringValue.append('\n').append(formatAddressLines(address.getCountry()));
        } else {
            return null;
        }
        return StringUtils.isNotEmpty(addressStringValue.toString()) ? addressStringValue.toString() : null;
    }

    public static String formatUkPostcode(Address address) {

        if (isUkCountry(address.getCountry())) {
            try {
                PostCodeValidator postCodeValidator = new PostCodeValidator(address.getPostCode());

                String outward = postCodeValidator.returnOutwardCode().trim() + " ";
                String inward = postCodeValidator.returnInwardCode().trim();

                return outward + inward;
            } catch (InvalidPostcodeException e) {
                log.error("Exception occurred when formatting postcode " + address.getPostCode(), e);
                return address.getPostCode();
            }
        } else {
            return address.getPostCode();
        }
    }
}
