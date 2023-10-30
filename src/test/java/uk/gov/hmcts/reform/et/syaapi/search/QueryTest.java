package uk.gov.hmcts.reform.et.syaapi.search;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.WrapperQueryBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(Query.class)
class QueryTest {

    @Test
    void shouldCheckQueryBuilder() {
        String searchString = "{\"match_all\": {}}";
        Query query = new Query(QueryBuilders.wrapperQuery(searchString), 0);
        assertTrue(query instanceof Query);
    }

    @Test
    void shouldCheckNegativeScenario()  throws  Exception {
        String searchString = "{\"match_all\": {}}";
        WrapperQueryBuilder queryBuilder = QueryBuilders.wrapperQuery(searchString);
        Exception ex = assertThrows(RuntimeException.class, () -> {
            new Query(queryBuilder, -1);
            }
        );
        assertTrue(ex.getMessage().contains("Start index cannot be less than 0"));
    }
}
