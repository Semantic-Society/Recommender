package de.rwth.dbis.neologism.recommender;

import java.util.List;

public class RecommenderInput {
    private String domain;
    private List<String> keywords;
    private List<String> properties;

    public RecommenderInput(String domain, List<String> keywords, List<String> properties) {
        this.domain = domain;
        this.keywords = keywords;
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

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public void setProperties(List<String> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "Input{" +
                "domain='" + domain + '\'' +
                ", keywords='" + keywords + '\'' +
                ", properties='" + properties + '\'' +
                '}';
    }
}