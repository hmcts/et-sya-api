package uk.gov.hmcts.reform.et.syaapi.model.helper;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.et.syaapi.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.reform.et.syaapi.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;


public enum TribunalOffice {
    ABERDEEN("Aberdeen"),
    BRISTOL("Bristol", "14"),
    DUNDEE("Dundee"),
    EDINBURGH("Edinburgh"),
    GLASGOW("Glasgow", "41"),
    LEEDS("Leeds", "18"),
    LONDON_CENTRAL("London Central", "22"),
    LONDON_EAST("London East", "32"),
    LONDON_SOUTH("London South", "23"),
    MANCHESTER("Manchester", "24"),
    MIDLANDS_EAST("Midlands East", "26"),
    MIDLANDS_WEST("Midlands West", "13"),
    NEWCASTLE("Newcastle", "25"),
    WALES("Wales", "16"),
    WATFORD("Watford", "33"),
    SCOTLAND("Scotland");

    public static final List<TribunalOffice> ENGLANDWALES_OFFICES = List.of(
        BRISTOL, LEEDS, LONDON_CENTRAL, LONDON_EAST, LONDON_SOUTH, MANCHESTER, MIDLANDS_EAST, MIDLANDS_WEST,
        NEWCASTLE, WALES, WATFORD
    );

    public static final List<TribunalOffice> SCOTLAND_OFFICES = List.of(
        ABERDEEN, DUNDEE, EDINBURGH, GLASGOW
    );

    private final String officeName;
    private final String officeNumber;

    TribunalOffice(String officeName) {
        this(officeName, null);
    }

    TribunalOffice(String officeName, String officeNumber) {
        this.officeName = officeName;
        this.officeNumber = officeNumber;
    }

    public String getOfficeName() {
        return officeName;
    }

    public String getOfficeNumber() {
        return officeNumber;
    }

    public static Optional<TribunalOffice> valueOfOfficeNumber(String officeNumber) {
        return Arrays.stream(values())
            .filter(t -> t.officeNumber != null && t.officeNumber.equals(officeNumber))
            .findFirst();
    }

    public static TribunalOffice valueOfOfficeName(String officeName) {
        for (TribunalOffice tribunalOffice : values()) {
            if (tribunalOffice.officeName.equals(officeName)) {
                return tribunalOffice;
            }
        }
        throw new IllegalArgumentException(String.format("Office name %s not recognised", officeName));
    }

    public static String getCaseTypeId(String officeName) {
        TribunalOffice tribunalOffice = TribunalOffice.valueOfOfficeName(officeName);
        if (ENGLANDWALES_OFFICES.contains(tribunalOffice)) {
            return ENGLANDWALES_CASE_TYPE_ID;
        } else if (SCOTLAND_OFFICES.contains(tribunalOffice)) {
            return SCOTLAND_CASE_TYPE_ID;
        } else {
            throw new IllegalArgumentException("Unexpected office " + officeName);
        }
    }

    public static boolean isEnglandWalesOffice(String officeName) {
        return ENGLANDWALES_CASE_TYPE_ID.equals(getCaseTypeId(officeName));
    }

    public static boolean isScotlandOffice(String officeName) {
        return SCOTLAND_CASE_TYPE_ID.equals(getCaseTypeId(officeName));
    }
}
