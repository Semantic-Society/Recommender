package de.rwth.dbis.neologism.recommender;

import java.util.List;

public class BatchQuery {



    public final String domain;
    public final List<String> classes;
    public final List<String> properties;


    public BatchQuery(String domain, List<String> classes, List<String> properties) {
        this.domain = domain;
        this.classes = classes;
        this.properties = properties;
    }

    public String getDomain() {
        return domain;
    }

    public List<String> getClasses() {
        return classes;
    }


}
