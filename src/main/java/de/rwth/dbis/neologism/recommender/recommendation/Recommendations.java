package de.rwth.dbis.neologism.recommender.recommendation;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import de.rwth.dbis.neologism.recommender.Prefixer;
import de.rwth.dbis.neologism.recommender.batchrecommender.BatchRecommender;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Recommendations {

    private static final Joiner j = Joiner.on('\n');
    public final ImmutableList<Recommendation> list;
    public final String creator;

    public Recommendations(List<Recommendation> l, String creator) {
        this.creator = creator;
        this.list = ImmutableList.copyOf(l);
    }

    @Override
    public String toString() {
        return j.join(this.list);
    }

    public static Recommendations combineRecommendations(List<Recommendations> toCombine) {
        List<Recommendations.Recommendation> recommendations = new ArrayList<>();
        String creator = BatchRecommender.class.getName();
        for (Recommendations r : toCombine) {
            recommendations.addAll(r.list);
        }
        return new Recommendations(recommendations, creator);
    }

    public Recommendations cleanAllExceptEnglish() {
        List<Recommendation> cleanedList = new ArrayList<>();
        for (Recommendation original : this.list) {

            Recommendation.Builder b = new Recommendation.Builder(original.ontology, original.URI);
            for (StringLiteral originalLabel : original.labels) {
                if (originalLabel.language.equals(Language.EN)) {
                    b.addLabel(originalLabel);
                }
            }
            for (StringLiteral originalComment : original.comments) {
                if (originalComment.language.equals(Language.EN)) {
                    b.addComment(originalComment);
                }
            }
            if (original instanceof LOVRecommendation) {
                b.addLOVParams(((LOVRecommendation) original).getScore(), ((LOVRecommendation) original).getOccurrencesInDatasets(), ((LOVRecommendation) original).getReusedByDatasets());
            }
            Recommendation cleaned = b.build();
            cleanedList.add(cleaned);
        }
        return new Recommendations(cleanedList, this.creator);
    }

    public Recommendations giveAllALabel() {
        List<Recommendation> listWithLabel = new ArrayList<>();
        for (Recommendation original : this.list) {
            Recommendation.Builder b = new Recommendation.Builder(original.ontology, original.URI);
            for (StringLiteral originalLabel : original.labels) {
                b.addLabel(originalLabel);
            }
            if (b.labels.isEmpty()) {
                String newLabel = Prefixer.shortenWithPrefix(b.URI);
                b.addLabel(new StringLiteral(Language.EN, newLabel));
            }
            for (StringLiteral originalComment : original.comments) {
                b.addComment(originalComment);
            }
            Recommendation cleaned = b.build();
            listWithLabel.add(cleaned);
        }
        return new Recommendations(listWithLabel, this.creator);
    }

    public static class Recommendation {
        /**
         * Values for rdfs:Label
         */
        private final ImmutableList<StringLiteral> labels;
        /**
         * Values for rdfs:Comment
         */
        private final ImmutableList<StringLiteral> comments;
        private final String URI;
        private final String ontology;

        public Recommendation(String uRI, String ontology, List<StringLiteral> labels, List<StringLiteral> comments) {
            this.comments = ImmutableList.copyOf(Preconditions.checkNotNull(comments));
            this.labels = ImmutableList.copyOf(Preconditions.checkNotNull(labels));
            this.URI = Preconditions.checkNotNull(uRI);
            this.ontology = Preconditions.checkNotNull(ontology);
        }

        public List<StringLiteral> getLabel() {
            return labels;
        }

        public List<StringLiteral> getComments() {
            return comments;
        }

        public String getUri() {
            return URI;
        }

        public String getOntology() {
            return ontology;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((URI == null) ? 0 : URI.hashCode());
            result = prime * result + ((comments == null) ? 0 : comments.hashCode());
            result = prime * result + ((labels == null) ? 0 : labels.hashCode());
            result = prime * result + ((ontology == null) ? 0 : ontology.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Recommendation other = (Recommendation) obj;
            if (URI == null) {
                if (other.URI != null)
                    return false;
            } else if (!URI.equals(other.URI))
                return false;
            if (comments == null) {
                if (other.comments != null)
                    return false;
            } else if (!comments.equals(other.comments))
                return false;
            if (labels == null) {
                if (other.labels != null)
                    return false;
            } else if (!labels.equals(other.labels))
                return false;
            if (ontology == null) {
                return other.ontology == null;
            } else return ontology.equals(other.ontology);
        }

        @Override
        public String toString() {
            return this.ontology + '\t' + this.URI + '\t' + this.labels + '\t' + this.comments;
        }

        public static class Builder {

            private final String ontology;
            private final String URI;

            private final Set<StringLiteral> labels;
            private final Set<StringLiteral> comments;

            private Double score;
            private int occurrenceInDatasets;
            private int reusedByDatasets;
            private boolean isLOVRecommendation;

            public Builder(String ontology, String uRI) {
                this.ontology = ontology;
                this.URI = uRI;
                this.labels = new HashSet<>();
                this.comments = new HashSet<>();
            }

            public void addLabel(StringLiteral l) {
                labels.add(l);
            }

            public void addComment(StringLiteral l) {
                comments.add(l);
            }

            public Builder addLOVParams(double score, int occurrenceInDatasets, int reusedByDatasets) {
                this.score = score;
                this.occurrenceInDatasets = occurrenceInDatasets;
                this.reusedByDatasets = reusedByDatasets;
                this.isLOVRecommendation = true;
                return this;
            }

            public Recommendation build() {
                if (isLOVRecommendation) {
                    return new LOVRecommendation(URI, ontology, ImmutableList.copyOf(labels), ImmutableList.copyOf(comments), score, occurrenceInDatasets, reusedByDatasets);
                }
                return new Recommendation(URI, ontology, ImmutableList.copyOf(labels), ImmutableList.copyOf(comments));
            }

        }

    }

    public static class StringLiteral {
        public final Language language;
        public final String label;

        public StringLiteral(Language language, String label) {
            this.language = language;
            this.label = label;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((label == null) ? 0 : label.hashCode());
            result = prime * result + ((language == null) ? 0 : language.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            StringLiteral other = (StringLiteral) obj;
            if (label == null) {
                if (other.label != null)
                    return false;
            } else if (!label.equals(other.label))
                return false;
            if (language == null) {
                return other.language == null;
            } else return language.equals(other.language);
        }

        @Override
        public String toString() {
            return this.label + '@' + this.language;
        }

    }

    public static class Language {
        public static final Language EN = new Language("en");
        public final String languageCode;

        /**
         * Create a new language. Common ones are predefined as
         *
         * @param langcode
         */
        private Language(String langcode) {
            Preconditions.checkNotNull(langcode);
            Preconditions.checkArgument(langcode.length() == 2,
                    "Language code length must be two. Got code " + langcode);
            this.languageCode = langcode;
        }

        public static Language forLangCode(String code) {
            if (code.equalsIgnoreCase("en")) {
                return EN;
            } else {
                return new Language(code);
            }
        }

        public static Language forLangCodeDefaultEnglish(String code) {
            if (code == null || code.equals("")) {
                return EN;
            }
            if (code.equalsIgnoreCase("en")) {
                return EN;
            } else {
                return new Language(code);
            }
        }

        @Override
        public int hashCode() {
            return languageCode.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Language other = (Language) obj;
            if (languageCode == null) {
                return other.languageCode == null;
            } else return languageCode.equals(other.languageCode);
        }

        @Override
        public String toString() {
            return this.languageCode;
        }

    }

}
