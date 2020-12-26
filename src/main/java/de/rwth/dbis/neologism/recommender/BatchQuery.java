package de.rwth.dbis.neologism.recommender;

import java.util.List;

public class BatchQuery {

    public final static int RESULT_LIMIT = 100;

    public final String domain;
    public final List<String> keywords;
    public final List<String> properties;
    // Integer.MAX_VALUE if unset.
    public final int limit;


    public BatchQuery(String domain, List<String> keywords, List<String> properties) {
        this(domain, keywords, properties, RESULT_LIMIT);
    }

    public BatchQuery(String domain, List<String> keywords, List<String> properties, int limit) {
        this.domain = domain;
        this.keywords = keywords;
        this.properties = properties;
        this.limit = limit;
    }
    public String getDomain() {
        return domain;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public int getLimit() {
        return limit;
    }



    }
