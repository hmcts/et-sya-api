package uk.gov.hmcts.reform.et.syaapi.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD")
class CaseDocumentTest {

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
    void shouldReturnFalseWhenVerifyUrlLinksHrefNull() {
        assertThat(caseDocument.verifyUri()).isFalse();
    }

}
