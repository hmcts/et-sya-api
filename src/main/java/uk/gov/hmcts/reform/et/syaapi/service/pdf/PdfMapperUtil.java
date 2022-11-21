package uk.gov.hmcts.reform.et.syaapi.service.pdf;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.Address;

import java.util.Locale;

public final class PdfMapperUtil {

    private PdfMapperUtil() {

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

    public static String convertAddressToString(Address address) {
        StringBuilder addressStringValue = new StringBuilder();
        if (StringUtils.isNotEmpty(address.getAddressLine1())) {
            addressStringValue.append(formatAddressLines(address.getAddressLine1()));
        }
        if (StringUtils.isNotEmpty(address.getAddressLine2())) {
            addressStringValue.append('\n').append(formatAddressLines(address.getAddressLine2()));
        }
        if (StringUtils.isNotEmpty(address.getAddressLine3())) {
            addressStringValue.append('\n').append(formatAddressLines(address.getAddressLine3()));
        }
        if (StringUtils.isNotEmpty(address.getPostTown())) {
            addressStringValue.append('\n').append(formatAddressLines(address.getPostTown()));
        }
        if (StringUtils.isNotEmpty(address.getCounty())) {
            addressStringValue.append('\n').append(formatAddressLines(address.getCounty()));
        }
        if (StringUtils.isNotEmpty(address.getCountry())) {
            addressStringValue.append('\n').append(formatAddressLines(address.getCountry()));
        }
        return StringUtils.isNotEmpty(addressStringValue.toString()) ? addressStringValue.toString() : null;
    }
}
