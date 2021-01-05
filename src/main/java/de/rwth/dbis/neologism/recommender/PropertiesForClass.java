package de.rwth.dbis.neologism.recommender;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import de.rwth.dbis.neologism.recommender.Recommendations.Language;
import de.rwth.dbis.neologism.recommender.Recommendations.StringLiteral;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertiesForClass {

    public static final PropertiesForClass EMPTY = new PropertiesForClass(ImmutableList.of());
    private static final Joiner j = Joiner.on('\n');
    public final ImmutableList<PropertyWithRange> properties;

    public PropertiesForClass(List<PropertyWithRange> properties) {
        this.properties = ImmutableList.copyOf(properties);
    }

    @Override
    public String toString() {
        return j.join(this.properties);
    }

    public PropertiesForClass cleanAllExceptEnglish() {
        List<PropertyWithRange> cleanedList = new ArrayList<>();
        for (PropertyWithRange original : this.properties) {
            PropertyWithRange.Builder b = new PropertyWithRange.Builder(original.propertyIRI, original.rangeClassIRI);

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
            PropertyWithRange cleaned = b.build();
            cleanedList.add(cleaned);
        }
        return new PropertiesForClass(cleanedList);
    }

    public PropertiesForClass giveAllALabel() {
        List<PropertyWithRange> listWithAllALabel = new ArrayList<>();
        for (PropertyWithRange original : this.properties) {
            PropertyWithRange.Builder b = new PropertyWithRange.Builder(original.propertyIRI, original.rangeClassIRI);

            for (StringLiteral originalLabel : original.labels) {
                b.addLabel(originalLabel);
            }
            if (original.labels.isEmpty()) {
                b.addLabel(new StringLiteral(Language.EN, Prefixer.shortenWithPrefix(original.propertyIRI)));
            }

            for (StringLiteral originalComment : original.comments) {
                b.addComment(originalComment);
            }
            PropertyWithRange cleaned = b.build();
            listWithAllALabel.add(cleaned);
        }
        return new PropertiesForClass(listWithAllALabel);
    }

    public static class Builder {
        // private final ImmutableList.Builder<PropertyWithRange> props = new
        // ImmutableList.Builder<>();

        private final Map<PropAndRange, PropertyWithRange.Builder> properties = new HashMap<>();

        public Builder addProperty(String propertyIRI, String rangeClassIRI) {
            this.properties.computeIfAbsent(new PropAndRange(propertyIRI, rangeClassIRI),
                    (propAndRange) -> new PropertyWithRange.Builder(propertyIRI, rangeClassIRI));
            return this;
        }

        public Builder addLabel(String propertyIRI, String rangeClassIRI, StringLiteral label) {
            de.rwth.dbis.neologism.recommender.PropertiesForClass.PropertyWithRange.Builder builder = this.properties
                    .computeIfAbsent(new PropAndRange(propertyIRI, rangeClassIRI),
                            (propAndRange) -> new PropertyWithRange.Builder(propertyIRI, rangeClassIRI));
            builder.addLabel(label);
            return this;
        }

        public Builder addComment(String propertyIRI, String rangeClassIRI, StringLiteral comment) {
            de.rwth.dbis.neologism.recommender.PropertiesForClass.PropertyWithRange.Builder builder = this.properties
                    .computeIfAbsent(new PropAndRange(propertyIRI, rangeClassIRI),
                            (propAndRange) -> new PropertyWithRange.Builder(propertyIRI, rangeClassIRI));
            builder.addComment(comment);
            return this;
        }

        public Builder addLabelAndComment(String propertyIRI, String rangeClassIRI, StringLiteral label,
                                          StringLiteral comment) {
            de.rwth.dbis.neologism.recommender.PropertiesForClass.PropertyWithRange.Builder builder = this.properties
                    .computeIfAbsent(new PropAndRange(propertyIRI, rangeClassIRI),
                            (propAndRange) -> new PropertyWithRange.Builder(propertyIRI, rangeClassIRI));
            builder.addComment(label);
            builder.addComment(comment);
            return this;
        }

        /**
         * @param solution the solution of a sparql query. Assuming that the variables
         *
         *                 <ul>
         *                 <li>?p=property</li>
         *                 <li>?range=range</li>
         *                 <li>?label=an optional label with optional language tag</li>
         *                 <li>?comment=an optional comment with an optional language
         *                 tag</li>
         *                 </ul>
         *                 If the language tag is not specified, English is assumed.
         * @return
         */
        public Builder addFromQuerySolution(QuerySolution solution) {
            String propertyIRI = solution.getResource("p").toString();
            String rangeClassIRI = solution.getResource("range").toString();

            Literal label = solution.getLiteral("label");
            Literal comment = solution.getLiteral("comment");
            if (label == null && comment == null) {
                this.addProperty(propertyIRI, rangeClassIRI);
            } else if (label == null && comment != null) {
                StringLiteral commentLiteral = new StringLiteral(
                        Language.forLangCodeDefaultEnglish(comment.getLanguage()), comment.getString());
                this.addComment(propertyIRI, rangeClassIRI, commentLiteral);
            } else if (label != null && comment == null) {
                StringLiteral labelLiteral = new StringLiteral(Language.forLangCodeDefaultEnglish(label.getLanguage()),
                        label.getString());
                this.addLabel(propertyIRI, rangeClassIRI, labelLiteral);
            } else if (label != null && comment != null) {
                StringLiteral commentLiteral = new StringLiteral(
                        Language.forLangCodeDefaultEnglish(comment.getLanguage()), comment.getString());
                StringLiteral labelLiteral = new StringLiteral(Language.forLangCodeDefaultEnglish(label.getLanguage()),
                        label.getString());
                this.addLabelAndComment(propertyIRI, rangeClassIRI, labelLiteral, commentLiteral);
            } else {
                throw new Error("Programming error, all cases should be covered already");
            }
            return this;
        }

        public void addFromPropertiesForClass(PropertiesForClass oneRecsProperties) {
            for (PropertyWithRange theProp : oneRecsProperties.properties) {
                this.addProperty(theProp.propertyIRI, theProp.rangeClassIRI);
                for (StringLiteral label : theProp.labels) {
                    this.addLabel(theProp.propertyIRI, theProp.rangeClassIRI, label);
                }
                for (StringLiteral comment : theProp.comments) {
                    this.addComment(theProp.propertyIRI, theProp.rangeClassIRI, comment);
                }
            }
        }

        public PropertiesForClass build() {
            List<PropertyWithRange> propsList = new ArrayList<>();
            for (de.rwth.dbis.neologism.recommender.PropertiesForClass.PropertyWithRange.Builder propertyWithRange : this.properties
                    .values()) {
                propsList.add(propertyWithRange.build());
            }
            return new PropertiesForClass(propsList);
        }

    }

    public static class PropertyWithRange {
        public final String propertyIRI;
        public final String rangeClassIRI;

        /**
         * Values for rdfs:Label
         */
        public final ImmutableList<StringLiteral> labels;
        /**
         * Values for rdfs:Comment
         */
        public final ImmutableList<StringLiteral> comments;

        public PropertyWithRange(String propertyIRI, String rangeClassIRI, List<StringLiteral> labels,
                                 List<StringLiteral> comments) {
            super();
            this.propertyIRI = propertyIRI;
            this.rangeClassIRI = rangeClassIRI;
            this.comments = ImmutableList.copyOf(Preconditions.checkNotNull(comments));
            this.labels = ImmutableList.copyOf(Preconditions.checkNotNull(labels));
        }

        public Builder builder(String propertyIRI, String rangeClassIRI) {
            return new Builder(propertyIRI, rangeClassIRI);
        }

        @Override
        public String toString() {
            return "PropertyWithRange [propertyIRI=" + propertyIRI + ", rangeClassIRI=" + rangeClassIRI + ", labels="
                    + labels + ", comments=" + comments + "]";
        }

        public static class Builder {
            private final String rangeClassIRI;
            private final String propertyIRI;
            private final ImmutableList.Builder<StringLiteral> labels = ImmutableList.builder();
            private final ImmutableList.Builder<StringLiteral> comments = ImmutableList.builder();

            public Builder(String propertyIRI, String rangeClassIRI) {
                this.propertyIRI = propertyIRI;
                this.rangeClassIRI = rangeClassIRI;
            }

            public Builder addLabel(StringLiteral label) {
                this.labels.add(label);
                return this;
            }

            public Builder addComment(StringLiteral comment) {
                this.comments.add(comment);
                return this;
            }

            public PropertyWithRange build() {
                return new PropertyWithRange(propertyIRI, rangeClassIRI, labels.build(), comments.build());
            }

        }

        // @Override
        // public String toString() {
        // return "PropertyWithRange [propertyIRI=" + propertyIRI + ", rangeClassIRI=" +
        // rangeClassIRI + labels + '\t' + comments]";
        // }

    }

    private static class PropAndRange {
        public final String prop;
        public final String range;

        public PropAndRange(String prop, String range) {
            this.prop = Preconditions.checkNotNull(prop);
            this.range = Preconditions.checkNotNull(range);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((prop == null) ? 0 : prop.hashCode());
            result = prime * result + ((range == null) ? 0 : range.hashCode());
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
            PropAndRange other = (PropAndRange) obj;
            if (prop == null) {
                if (other.prop != null)
                    return false;
            } else if (!prop.equals(other.prop))
                return false;
            if (range == null) {
                return other.range == null;
            } else return range.equals(other.range);
        }

    }

}
