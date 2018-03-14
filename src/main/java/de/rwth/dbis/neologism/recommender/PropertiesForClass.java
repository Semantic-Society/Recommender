package de.rwth.dbis.neologism.recommender;

import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import de.rwth.dbis.neologism.recommender.Recommendations.StringLiteral;

public class PropertiesForClass {

	public final ImmutableList<PropertyWithRange> properties;

	public PropertiesForClass(List<PropertyWithRange> properties) {
		this.properties = ImmutableList.copyOf(properties);
	}

	public static class Builder {
		private final ImmutableList.Builder<PropertyWithRange> props = new ImmutableList.Builder<>();

		public Builder add(String propertyIRI, String domainClassIRI, List<StringLiteral> labels,
				List<StringLiteral> comments) {
			this.props.add(new PropertyWithRange(propertyIRI, domainClassIRI,labels, comments));
			return this;
		}

		public Builder add(PropertyWithRange prop) {
			this.props.add(prop);
			return this;
		}

		public PropertiesForClass build() {
			return new PropertiesForClass(this.props.build());
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

		@Override
		public String toString() {
			return "PropertyWithRange [propertyIRI=" + propertyIRI + ", rangeClassIRI=" + rangeClassIRI + "]";
		}

	}

	private static Joiner j = Joiner.on('\n');

	@Override
	public String toString() {
		return j.join(this.properties);
	}

}
