package uk.gov.hmcts.reform.et.syaapi.search;

import org.elasticsearch.index.query.QueryBuilder;

import java.util.Objects;

public class Query {

    private QueryBuilder queryBuilder;
    private int startIndex;

    public Query(QueryBuilder queryBuilder, int startIndex) {
        Objects.requireNonNull(queryBuilder, "QueryBuilder cannot be null in search");
        if (startIndex < 0) {
            throw new IllegalArgumentException("Start index cannot be less than 0");
        }
        this.queryBuilder = queryBuilder;
        this.startIndex = startIndex;
    }

    @Override
    public String toString() {
        return "{"
            + "\"query\": " + queryBuilder.toString() + ", "
            + "\"from\": " + startIndex
            + "}";
    }
}
