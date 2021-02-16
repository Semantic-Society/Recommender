package de.rwth.dbis.neologism.recommender;

import java.util.List;

public class RecommenderInput {
    private String domain;
    private List<String> classes;
    private List<String> properties;

    public RecommenderInput(String domain, List<String> classes, List<String> properties) {
        this.domain = domain;
        this.classes = classes;
        this.properties = properties;
    }

    public RecommenderInput() {
    }

    public List<String> getProperties() {
        return properties;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public List<String> getClasses() {
        return classes;
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