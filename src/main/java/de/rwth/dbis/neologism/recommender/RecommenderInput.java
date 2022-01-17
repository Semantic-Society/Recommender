package de.rwth.dbis.neologism.recommender;

import java.util.Collections;
import java.util.List;

public class RecommenderInput {
    private String domain;
    private List<String> classes;
    private List<String> properties;
    private int limit;

    public RecommenderInput(String domain, List<String> classes, List<String> properties, int limit) {
        this.domain = domain;
        this.classes = classes;
        this.properties = properties;
        this.limit = limit;
    }

    public RecommenderInput() {
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public List<String> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public List<String> getClasses() {
        return Collections.unmodifiableList(classes);
    }

    public void setClasses(List<String> classes) {
        this.classes = classes;
    }

    public void setProperties(List<String> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "Input{" +
                "domain='" + domain + '\'' +
                ", keywords='" + classes + '\'' +
                ", properties='" + properties + '\'' +
                '}';
    }
}