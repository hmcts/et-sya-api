package uk.gov.hmcts.reform.et.syaapi.search;

import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QueryTest {

    @Test
    void shouldCheckQueryBuilder() {
        String searchString = "{\"match_all\": {}}";
        Query query = new Query(QueryBuilders.wrapperQuery(searchString), 0);
        assertTrue(query instanceof Query);
    }
}
