package de.rwth.dbis.neologism.recommender;

import com.google.common.collect.ImmutableList;

public class PropertiesForClass {

	public final ImmutableList<PropertyWithDomain> properties;

	public PropertiesForClass(ImmutableList<PropertyWithDomain> properties) {
		this.properties = properties;
	}

	public static class Builder {
		private final ImmutableList.Builder<PropertyWithDomain> props = new ImmutableList.Builder<>();

		public Builder add(String propertyIRI, String domainClassIRI) {
			this.props.add(new PropertyWithDomain(propertyIRI, domainClassIRI));
			return this;
		}

		public Builder add(PropertyWithDomain prop) {
			this.props.add(prop);
			return this;
		}
	}

	public static class PropertyWithDomain {
		public final String propertyIRI;
		public final String domainClassIRI;

		public PropertyWithDomain(String propertyIRI, String domainClassIRI) {
			super();
			this.propertyIRI = propertyIRI;
			this.domainClassIRI = domainClassIRI;
		}

	}

}
