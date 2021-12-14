package de.rwth.dbis.neologism.recommender.lov;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class JsonLovTermSearch {
    private int totalResults;
    private int page;
    private int pageSize;
    private String queryString;
    private JsonObject filters;
    private JsonObject aggregations;
    private JsonObject types;
    private JsonObject vocabs;
    private ArrayList<Result> results;


    public int getTotalResults() {
        return totalResults;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public String getQueryString() {
        return queryString;
    }

    public JsonObject getFilters() {
        return filters;
    }

    public JsonObject getAggregations() {
        return aggregations;
    }

    public JsonObject getTypes() {
        return types;
    }

    public JsonObject getVocabs() {
        return vocabs;
    }

    public List<Result> getResults() {
        return results;
    }

    public static class Result {
        private ArrayList<String> prefixedName;
        @SerializedName(value = "metrics.reusedByDatasets")
        private ArrayList<Integer> metricsReusedByDatasets;
        @SerializedName(value = "vocabulary.prefix")
        private ArrayList<String> vocabularyPrefix;
        @SerializedName(value = "metrics.occurrencesInDatasets")
        private ArrayList<Integer> metricsOccurrencesInDatasets;
        private ArrayList<String> uri;
        private String type;
        private double score;
        private JsonObject highlight;

        public List<String> getPrefixedName() {
            return prefixedName;
        }

        public List<Integer> getMetricsReusedByDatasets() {
            return metricsReusedByDatasets;
        }

        public List<String> getVocabularyPrefix() {
            return vocabularyPrefix;
        }

        public List<Integer> getMetricsOccurrencesInDatasets() {
            return metricsOccurrencesInDatasets;
        }

        public List<String> getUri() {
            return uri;
        }

        public String getType() {
            return type;
        }

        public double getScore() {
            return score;
        }

        public JsonObject getHighlight() {
            return highlight;
        }

        @Override
        public String toString() {
            return "Result [prefixedName=" + prefixedName + ", metrics_reusedByDatasets=" + metricsReusedByDatasets
                    + ", vocabulary_prefix=" + vocabularyPrefix + ", metrics_occurrencesInDatasets="
                    + metricsOccurrencesInDatasets + ", uri=" + uri + ", type=" + type + ", score=" + score
                    + ", highlight=" + highlight + "]";
        }


    }


}
