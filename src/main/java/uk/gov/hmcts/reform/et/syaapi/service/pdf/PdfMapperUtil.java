package uk.gov.hmcts.reform.et.syaapi.service.pdf;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.gov.dwp.regex.InvalidPostcodeException;
import uk.gov.dwp.regex.PostCodeValidator;
import uk.gov.hmcts.et.common.model.ccd.Address;

import java.util.Locale;
import java.util.Set;

/**
 *  This class is implemented as a utility for PDF Mapper class.
 *  All methods and variables are defined as static.
 *  There is a private constructor implemented not to let class have a
 *  public or default constructor because it is a utility class
 * @author Mehmet Tahir Dede
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public final class PdfMapperUtil {

    private static final Set<String> UK_COUNTRY_NAMES = Set.of("ENGLAND",
                                                               "SCOTLAND",
                                                               "NORTHERN IRELAND",
                                                               "NORTHERNIRELAND",
                                                               "WALES",
                                                               "UNITED KINGDOM",
                                                               "UK",
                                                               "UNITEDKINGDOM",
                                                               "GB",
                                                               "GREAT BRITAIN",
                                                               "GREATBRITAIN",
                                                               "BRITAIN");

    private PdfMapperUtil() {

    }

    /**
     * Returns boolean true when given countryName parameter is one of UK Countries
     * which are "ENGLAND", "SCOTLAND", "NORTHERN IRELAND" or "WALES".
     * <a href="https://en.wikipedia.org/wiki/Countries_of_the_United_Kingdom"> UK Countries</a>
     * @param countryName Name of the country
     * @return boolean true when UK, false when not UK country
     */
    private static boolean isUkCountry(String countryName) {
        return StringUtils.isEmpty(countryName) || UK_COUNTRY_NAMES.contains(countryName
                                                                                 .replace(" ", "")
                                                                                 .toUpperCase(Locale.UK)
                                                                                 .trim());
    }

    /**
     * Returns formatted value of given addressLine
     * It converts each address line wordings to capital letters.
     * Such as for given a value as 40 FURROW WAY it formats as 40 Furrow Way
     * @param addressLine Input value of address first line.
     * @return the formatted adressLine value
     */
    private static String convertFirstCharactersOfWordsToCapitalCase(String addressLine) {
        String[] addressLineWords = addressLine.toLowerCase(Locale.UK).split(" ");

        StringBuilder addressLineModified = new StringBuilder();
        for (String word : addressLineWords) {
            if (!StringUtils.isEmpty(word.trim())) {
                addressLineModified.append(word.substring(0, 1).toUpperCase(Locale.UK))
                    .append(word.substring(1)).append(' ');
            }
        }
        return addressLineModified.toString().trim();
    }

    /**
     * Returns a string value for the given Address. Address has 6 values to be converted to String
     * for showing them in PDF text fields.
     * 3 of those values which are AddressLine1, PostTown and Country are compulsory fields. If one or more
     * of those is not entered it returns null.
     * Adds all fields to a string by adding comma and new line to each field.
     * All fields are also converted to have each of their wordings start with a capital letter.
     * @param address model that holds address data
     * @return converted String value of address model.
     */
    public static String formatAddressForTextField(Address address) {
        StringBuilder addressStringValue = new StringBuilder();

        if (StringUtils.isNotEmpty(address.getAddressLine1())) {
            addressStringValue
                .append(convertFirstCharactersOfWordsToCapitalCase(address.getAddressLine1()))
                .append(',');
        } else {
            return null;
        }
        if (StringUtils.isNotEmpty(address.getAddressLine2())) {
            addressStringValue
                .append('\n')
                .append(convertFirstCharactersOfWordsToCapitalCase(address.getAddressLine2()))
                .append(',');
        }
        if (StringUtils.isNotEmpty(address.getAddressLine3())) {
            addressStringValue
                .append('\n')
                .append(convertFirstCharactersOfWordsToCapitalCase(address.getAddressLine3()))
                .append(',');
        }
        if (StringUtils.isNotEmpty(address.getPostTown())) {
            addressStringValue
                .append('\n')
                .append(convertFirstCharactersOfWordsToCapitalCase(address.getPostTown()))
                .append(',');
        } else {
            return null;
        }
        if (StringUtils.isNotEmpty(address.getCounty())) {
            addressStringValue
                .append('\n')
                .append(convertFirstCharactersOfWordsToCapitalCase(address.getCounty()))
                .append(',');
        }
        if (StringUtils.isNotEmpty(address.getCountry())) {
            addressStringValue
                .append('\n')
                .append(convertFirstCharactersOfWordsToCapitalCase(address.getCountry()));
        } else {
            return null;
        }
        return StringUtils.isNotEmpty(addressStringValue.toString()) ? addressStringValue.toString() : null;
    }

    /**
     * Returns formatted UK Postcode.
     * A UK postcode has a space character before 3rd character of it' s last character.
     * Such as SL63NY should be formatted as SL6 3NY or WF102SX as WF10 2SX etc...
     * @param address address model that holds address data
     * @return formatted String value of Postcode
     */
    public static String formatUkPostcode(Address address) {
        if (isUkCountry(address.getCountry())) {
            try {
                if (StringUtils.isNotBlank(address.getPostCode())) {
                    PostCodeValidator postCodeValidator = new PostCodeValidator(address.getPostCode());

                    String outward = postCodeValidator.returnOutwardCode().trim() + " ";
                    String inward = postCodeValidator.returnInwardCode().trim();

                    return outward + inward;
                } else {
                    return "";
                }
            } catch (InvalidPostcodeException e) {
                log.error("Exception occurred when formatting postcode " + address.getPostCode(), e);
                return address.getPostCode();
            }
        } else {
            return address.getPostCode();
        }
    }
}
