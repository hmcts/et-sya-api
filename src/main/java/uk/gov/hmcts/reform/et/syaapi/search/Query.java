package uk.gov.hmcts.reform.et.syaapi.search;

import org.elasticsearch.index.query.QueryBuilder;

import java.util.Objects;

/**
 * Object for performing queries.
 */
public class Query {

    private final QueryBuilder queryBuilder;
    private final int startIndex;

    /**
     * Accepts {@link QueryBuilder} and index as parameters and creates a query.
     * @param queryBuilder query to be executed, must not be null
     * @param startIndex index order in which to execute the query, must be > 1
     */
    public Query(QueryBuilder queryBuilder, int startIndex) {
        Objects.requireNonNull(queryBuilder, "QueryBuilder cannot be null in search");
        if (startIndex < 0) {
            throw new IllegalArgumentException("Start index cannot be less than 0");
        }
        this.queryBuilder = queryBuilder;
        this.startIndex = startIndex;
    }

    /**
     * convert to a readable format.
     * @return the query in a readable format
     */
    @Override
    public String toString() {
        return "{"
            + "\"query\": " + queryBuilder.toString() + ", "
            + "\"from\": " + startIndex
            + "}";
    }
}
