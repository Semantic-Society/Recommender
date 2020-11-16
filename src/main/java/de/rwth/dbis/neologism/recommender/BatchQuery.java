package de.rwth.dbis.neologism.recommender;

import com.google.common.collect.ImmutableSet;
import com.google.common.hash.HashCode;

import java.util.List;

public class BatchQuery {

    public final static int RESULT_LIMIT = 100;

    public final String domain;
    public final List<String> keywords;
    // Integer.MAX_VALUE if unset.
    public final int limit;


    public BatchQuery(String domain, List<String> keywords) {
        this(domain, keywords, RESULT_LIMIT);
    }

    public BatchQuery(String domain, List<String> keywords, int limit) {
        this.domain = domain;
        this.keywords = keywords;
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
