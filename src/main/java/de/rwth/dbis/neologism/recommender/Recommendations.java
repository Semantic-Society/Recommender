package de.rwth.dbis.neologism.recommender;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Recommendations {
	public class Recommendation {
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

	}

	public final ImmutableList<Recommendation> l;

	public Recommendations(List<Recommendation> l) {
		super();
		this.l = ImmutableList.copyOf(l);
	}

}
