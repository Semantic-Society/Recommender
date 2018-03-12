package de.rwth.dbis.neologism.recommender;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Recommendations {

	public final ImmutableList<Recommendation> list;

	public Recommendations(List<Recommendation> l) {
		this.list = ImmutableList.copyOf(l);
	}

	public static class Recommendation {
		private final List<Label> labels;
		private final String URI;
		private final String ontology;

		public Recommendation(List<Label> labels, String uRI, String ontology) {
			this.labels = ImmutableList.copyOf(Preconditions.checkNotNull(labels));
			URI = Preconditions.checkNotNull(uRI);
			this.ontology = Preconditions.checkNotNull(ontology);
		}

		public List<Label> getLabel() {
			return labels;
		}

		public String getURI() {
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
			if (labels == null) {
				if (other.labels != null)
					return false;
			} else if (!labels.equals(other.labels))
				return false;
			if (ontology == null) {
				if (other.ontology != null)
					return false;
			} else if (!ontology.equals(other.ontology))
				return false;
			return true;
		}

	}

	public static class Label {
		public final Language language;
		public final String label;

		public Label(Language language, String label) {
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
			Label other = (Label) obj;
			if (label == null) {
				if (other.label != null)
					return false;
			} else if (!label.equals(other.label))
				return false;
			if (language == null) {
				if (other.language != null)
					return false;
			} else if (!language.equals(other.language))
				return false;
			return true;
		}
		
		
		
	}
	
	public static class Language {
		public final String languageCode;

		/**
		 * Create a new language. Common ones are predefined as 
		 * @param langcode
		 */
		private Language(String langcode) {
			Preconditions.checkNotNull(langcode);
			Preconditions.checkArgument(langcode.length() == 2);
			this.languageCode = langcode;
		}

		public Language forLangCode(String code) {
			if (code.equalsIgnoreCase("en")) {
				return EN;
			} else {
				return new Language(code);
			}
		}
		
		public static final Language EN = new Language("en");

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
				if (other.languageCode != null)
					return false;
			} else if (!languageCode.equals(other.languageCode))
				return false;
			return true;
		}
		
		
		
		
	}

}
