package de.rwth.dbis.neologism.recommender.bioportal;

import com.google.gson.JsonArray;
import com.google.gson.annotations.SerializedName;

import java.util.Collection;


public class JsonOntologyItem {

    private double evaluationScore;
    private Collection<Ontology> ontologies;
    private CoverageResult coverageResult; //check, by the annotator service, how the ontology fits the searched words
    private SpecializationResult specializationResult; //how much  the ontology field matches your words context
    private AcceptanceResult acceptanceResult; //how much can you trunst in this ontology
    private DetailResult detailResult; //check  how much your terms fit this ontology

    public double getEvaluationScore() {
        return evaluationScore;
    }

    public Collection<Ontology> getOntologies() {
        return ontologies;
    }

    public CoverageResult getCoverageResult() {
        return coverageResult;
    }

    public SpecializationResult getSpecializationResult() {
        return specializationResult;
    }

    public AcceptanceResult getAcceptanceResult() {
        return acceptanceResult;
    }

    public DetailResult getDetailResult() {
        return detailResult;
    }

    @Override
    public String toString() {
        return "RecommendationItem [evaluationScore=" + evaluationScore + ", ontologies=" + ontologies
                + ", coverageResult=" + coverageResult + ", specializationResult=" + specializationResult
                + ", acceptanceResult=" + acceptanceResult + ", detailResult=" + detailResult + "]";
    }

    public static class Ontology {
        private String acronym;
        private String id;
        private String type;
        private Links links;
        @SerializedName("@context")
        private Object context;


        public String getAcronym() {
            return acronym;
        }

        public String getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public Links getLinks() {
            return links;
        }

        public Object getContext() {
            return context;
        }

        @Override
        public String toString() {
            return "Ontology [acronym=" + acronym + ", id=" + id + ", type=" + type + ", links=" + links + ", context="
                    + context + "]";
        }

        public static class Links {
            private String submissions;
            private String properties;
            private String classes;
            private String single_class;
            private String roots;
            private String instances;
            private String metrics;
            private String reviews;
            private String notes;
            private String groups;
            private String categories;
            private String latest_submission;
            private String projects;
            private String download;
            private String views;
            private String analytics;
            private String ui;
            private Object context;


            public String getSubmissions() {
                return submissions;
            }

            public String getProperties() {
                return properties;
            }

            public String getClasses() {
                return classes;
            }

            public String getSingle_class() {
                return single_class;
            }

            public String getRoots() {
                return roots;
            }

            public String getInstances() {
                return instances;
            }

            public String getMetrics() {
                return metrics;
            }

            public String getReviews() {
                return reviews;
            }

            public String getNotes() {
                return notes;
            }

            public String getGroups() {
                return groups;
            }

            public String getCategories() {
                return categories;
            }

            public String getLatest_submission() {
                return latest_submission;
            }

            public String getProjects() {
                return projects;
            }

            public String getDownload() {
                return download;
            }

            public String getViews() {
                return views;
            }

            public String getAnalytics() {
                return analytics;
            }

            public String getUi() {
                return ui;
            }

            public Object getContext() {
                return context;
            }

            @Override
            public String toString() {
                return "Links [submissions=" + submissions + ", properties=" + properties + ", classes=" + classes
                        + ", single_class=" + single_class + ", roots=" + roots + ", instances=" + instances + ", metrics="
                        + metrics + ", reviews=" + reviews + ", notes=" + notes + ", groups=" + groups + ", categories="
                        + categories + ", latest_submission=" + latest_submission + ", projects=" + projects + ", download="
                        + download + ", views=" + views + ", analytics=" + analytics + ", ui=" + ui + ", context=" + context
                        + "]";
            }

        }

    }

    public static class CoverageResult {

        private double score;
        private double normalizedScore;
        private int numberTermsCovered;
        private int numberWordsCovered;
        private JsonArray annotations;


        public double getScore() {
            return score;
        }

        public double getNormalizedScore() {
            return normalizedScore;
        }

        public int getNumberTermsCovered() {
            return numberTermsCovered;
        }

        public int getNumberWordsCovered() {
            return numberWordsCovered;
        }

        public JsonArray getAnnotations() {
            return annotations;
        }

        @Override
        public String toString() {
            return "CoverageResult [score=" + score + ", normalizedScore=" + normalizedScore + ", numberTermsCovered="
                    + numberTermsCovered + ", numberWordsCovered=" + numberWordsCovered + ", annotations=" + annotations
                    + "]";
        }

    }

    public static class SpecializationResult {
        private double normalizedScore;
        private double score;

        public double getNormalizedScore() {
            return normalizedScore;
        }

        public double getScore() {
            return score;
        }

        @Override
        public String toString() {
            return "SpecializationResult [normalizedScore=" + normalizedScore + ", score=" + score + "]";
        }

    }

    public static class AcceptanceResult {
        private double normalizedScore;
        private double bioportalScore;
        private double umlsScore;


        public double getNormalizedScore() {
            return normalizedScore;
        }

        public double getBioportalScore() {
            return bioportalScore;
        }

        public double getUmlsScore() {
            return umlsScore;
        }

        @Override
        public String toString() {
            return "AcceptanceResult [normalizedScore=" + normalizedScore + ", bioportalScore=" + bioportalScore
                    + ", umlsScore=" + umlsScore + "]";
        }

    }

    public static class DetailResult {
        private double normalizedScore;
        private double definitionsScore;
        private double synonymsScore;
        private double propertiesScore;


        public double getNormalizedScore() {
            return normalizedScore;
        }

        public double getDefinitionsScore() {
            return definitionsScore;
        }

        public double getSynonymsScore() {
            return synonymsScore;
        }

        public double getPropertiesScore() {
            return propertiesScore;
        }

        @Override
        public String toString() {
            return "DetailResult [normalizedScore=" + normalizedScore + ", definitionsScore=" + definitionsScore
                    + ", synonymsScore=" + synonymsScore + ", propertiesScore=" + propertiesScore + "]";
        }

    }
}
