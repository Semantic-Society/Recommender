package de.rwth.dbis.neologism.recommender;

import java.util.List;

public class BatchQuery {

    public final static int RESULT_LIMIT = 100;

    public final String domain;
    public final List<String> classes;
    public final List<String> properties;
    // Integer.MAX_VALUE if unset.
    public final int limit;


    public BatchQuery(String domain, List<String> classes, List<String> properties) {
        this(domain, classes, properties, RESULT_LIMIT);
    }

    public BatchQuery(String domain, List<String> classes, List<String> properties, int limit) {
        this.domain = domain;
        this.classes = classes;
        this.properties = properties;
        this.limit = limit;
    }
    public String getDomain() {
        return domain;
    }

    public List<String> getClasses() {
        return classes;
    }

    public int getLimit() {
        return limit;
    }



    }
