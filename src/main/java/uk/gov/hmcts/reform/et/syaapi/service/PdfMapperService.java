package uk.gov.hmcts.reform.et.syaapi.service;

import java.util.HashMap;
import java.util.Map;

/*
 *
 */
public class PdfMapperService {
    public Map<String, String> mapHeadersToPdf(Map<String, Object> caseData) {
        Map<String, String> printFields = new HashMap<>();

        printFields.put("tribunal office", caseData.get("managingOffice").toString());
        printFields.put("case number", caseData.get("ccdID").toString());
        printFields.put("date received", caseData.get("receiptDate").toString());

        return printFields;
    }
}
