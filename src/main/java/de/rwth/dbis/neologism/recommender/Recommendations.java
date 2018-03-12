package de.rwth.dbis.neologism.recommender;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;

public class Recommendations {

	public final ImmutableList<Recommendation> list;

	public Recommendations(List<Recommendation> l) {
		this.list = ImmutableList.copyOf(l);
	}

	public static class Recommendation {
		private final String label;
		private final String URI;
		private final String ontology;

		public Recommendation(String label, String uRI, String ontology) {
			this.label = Preconditions.checkNotNull(label);
			URI = Preconditions.checkNotNull(uRI);
			this.ontology = Preconditions.checkNotNull(ontology);
		}

		public String getLabel() {
			return label;
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
			result = prime * result + ((label == null) ? 0 : label.hashCode());
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
			if (label == null) {
				if (other.label != null)
					return false;
			} else if (!label.equals(other.label))
				return false;
			if (ontology == null) {
				if (other.ontology != null)
					return false;
			} else if (!ontology.equals(other.ontology))
				return false;
			return true;
		}

		public int compareTo(Recommendation that) {
			return ComparisonChain.start().compare(this.label, that.label).compare(this.URI, that.URI)
					.compare(this.ontology, that.ontology).result();
		}

	}

}
