package de.rwth.dbis.neologism.recommender;

import java.util.List;
import java.util.Optional;

public class RecommenderInput {
    private String domain;
    private List<String> keywords;


    public RecommenderInput() {
    }

    public RecommenderInput(String domain, List<String> keywords) {
        this.domain = domain;
        this.keywords = keywords;
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

    @Override
    public String toString() {
        return "User{" +
                "username='" + domain + '\'' +
                ", password='" + keywords + '\'' +
                '}';
    }
}