package de.rwth.dbis.neologism.recommender;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

public class PropertiesForClass {

	public final ImmutableList<PropertyWithRange> properties;

	public PropertiesForClass(ImmutableList<PropertyWithRange> properties) {
		this.properties = properties;
	}

	public static class Builder {
		private final ImmutableList.Builder<PropertyWithRange> props = new ImmutableList.Builder<>();

		public Builder add(String propertyIRI, String domainClassIRI) {
			this.props.add(new PropertyWithRange(propertyIRI, domainClassIRI));
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

		public PropertyWithRange(String propertyIRI, String rangeClassIRI) {
			super();
			this.propertyIRI = propertyIRI;
			this.rangeClassIRI = rangeClassIRI;
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
