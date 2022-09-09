package uk.gov.hmcts.reform.et.syaapi.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.et.syaapi.models.CaseDocument;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class CaseDocumentTest {

    CaseDocument caseDocument;

    @BeforeEach
    void beforeEach() {
        Map<String, Map<String, String>> links = new HashMap<>();
        Map<String, String> href = new HashMap<>();
        href.put("href", null);
        links.put("self", href);
        caseDocument = CaseDocument.builder().build();
        caseDocument.setLinks(links);
    }

    @Test
    void shouldReturnTrueWhenVerifyUrlLinksHrefNull() {
        assertThat(caseDocument.verifyUri()).isTrue();
    }

}
