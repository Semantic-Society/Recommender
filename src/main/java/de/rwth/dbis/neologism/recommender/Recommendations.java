package de.rwth.dbis.neologism.recommender;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Recommendations {

	public final ImmutableList<Recommendation> list;

	public Recommendations(List<Recommendation> l) {
		this.list = ImmutableList.copyOf(l);
	}

	private static Joiner j = Joiner.on('\n');

	@Override
	public String toString() {
		return j.join(this.list);
	}

	public static class Recommendation {
		/**
		 * Values for rdfs:Label
		 */
		private final List<StringLiteral> labels;
		/**
		 * Values for rdfs:Comment
		 */
		private final List<StringLiteral> comments;
		private final String URI;
		private final String ontology;

		public Recommendation(String uRI,  String ontology, List<StringLiteral> labels, List<StringLiteral> comments) {
			
			this.comments = ImmutableList.copyOf(Preconditions.checkNotNull(comments));
			this.labels = ImmutableList.copyOf(Preconditions.checkNotNull(labels));
			URI = Preconditions.checkNotNull(uRI);
			this.ontology = Preconditions.checkNotNull(ontology);
		}

		public List<StringLiteral> getLabel() {
			return labels;
		}

		public List<StringLiteral> getComments() {
			return comments;
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
				if (other.ontology != null)
					return false;
			} else if (!ontology.equals(other.ontology))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return this.ontology + '\t' + this.URI + '\t' + this.labels + '\t' + this .comments;
		}

		public static class Builder {

			private final String ontology;
			private final String URI;

			private final Set<StringLiteral> labels;
			private final Set<StringLiteral> comments;

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

			public Recommendation build() {
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
				if (other.language != null)
					return false;
			} else if (!language.equals(other.language))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return this.label + '@' + this.language;
		}

	}

	public static class Language {
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

		@Override
		public String toString() {
			return this.languageCode;
		}

	}

	private static final Recommendations EMPTY = new Recommendations(Collections.emptyList());

	public static Recommendations empty() {
		return EMPTY;
	}

}
