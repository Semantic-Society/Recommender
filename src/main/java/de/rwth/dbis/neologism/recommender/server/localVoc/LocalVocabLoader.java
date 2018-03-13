package de.rwth.dbis.neologism.recommender.server.localVoc;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder.SetMultimapBuilder;
import com.google.common.collect.SetMultimap;

import de.rwth.dbis.neologism.recommender.Query;
import de.rwth.dbis.neologism.recommender.Recommendations;
import de.rwth.dbis.neologism.recommender.Recommendations.Label;
import de.rwth.dbis.neologism.recommender.Recommendations.Language;
import de.rwth.dbis.neologism.recommender.Recommendations.Recommendation;
import de.rwth.dbis.neologism.recommender.Recommendations.Recommendation.Builder;
import de.rwth.dbis.neologism.recommender.Recommender;

/**
 * This {@link Recommender} gives recommendations on simple String matching with
 * the local names of all classes in the specified vocabulary.
 * 
 * The search is optimized by precomputing all results. So, this is useful for
 * small vocabularies. For large vocabuaries, the memory use would become
 * unreasonable.
 * 
 * @author cochez
 *
 */
public class LocalVocabLoader implements Recommender {

	public enum PredefinedVocab implements Recommender {
		DCAT("dcat.ttl", Lang.TURTLE, "DCAT"), DUBLIN_CORE_TERMS("dcterms.ttl", Lang.TURTLE, "Dublin Core Terms");

		private final LocalVocabLoader loader;

		private PredefinedVocab(String resource, Lang lang, String ontology) {
			// res = new FileInputStream(new File(resource)); //
			InputStream res = LocalVocabLoader.class.getResourceAsStream(resource);
			if (res == null) {
				throw new Error("Hard coded resource not found. " + resource);
			}
			this.loader = new LocalVocabLoader(res, lang, ontology);
		}

		@Override
		public Recommendations recommend(Query c) {
			return this.loader.recommend(c);
		}

	}

	private final ImmutableMap<String, Recommendations> mappingTroughLocalName;
	// private final ImmutableMap<String, Recommendations> mappingTroughNamespace;
	// private final ImmutableMap<String, Recommendations> mappingTroughLabel;

	public LocalVocabLoader(InputStream source, Lang syntax, String ontology) {

		SetMultimap<String, Recommendation.Builder> localNameMap = SetMultimapBuilder.hashKeys().hashSetValues()
				.build();
		HashMap<String, Recommendation.Builder> terms = new HashMap<>();

		Dataset dataset = DatasetFactory.create();
		RDFParser.source(source).forceLang(syntax).build().parse(dataset.asDatasetGraph());

		RDFConnection conn = RDFConnectionFactory.connect(dataset);

		ResultSet rs = conn.query(
				"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT ?class ?label WHERE {{?class a rdfs:Class} UNION {[] a ?class} . "
						+ "OPTIONAL { ?class rdfs:label ?label } } ")
				.execSelect();

		if (rs.hasNext()) {

			rs.forEachRemaining(res -> {

				Resource className = res.getResource("class");

				String classURI = className.getURI();
				String localName = className.getLocalName();

				Builder builder = terms.computeIfAbsent(classURI, (URI) -> new Recommendation.Builder(ontology, URI));

				addAllsubsToMapping(localName.toLowerCase(), builder, localNameMap);

				if (res.contains("label")) {
					Literal literalLabel = res.get("label").asLiteral();
					String label = literalLabel.getString();
					String lang = literalLabel.getLanguage();
					if (lang.equals("")) {
						System.err.println("Found a label without language tag. Assuming english for '" + label + "'");
						lang = "en";
					}
					builder.addLabel(new Label(Language.forLangCode(lang), label));
					// addAllsubsToMapping(label.toLowerCase(), classURI, labelMap);
				}
			});
		}

		mappingTroughLocalName = convert(localNameMap);

	}

	private static class RecommendationComparator implements Comparator<Recommendation> {

		/**
		 * This comparator ignores the labels!
		 * 
		 * @param that
		 * @return
		 */
		@Override
		public int compare(Recommendation o1, Recommendation o2) {
			return ComparisonChain.start().compare(o1.getURI(), o2.getURI()).compare(o1.getOntology(), o2.getOntology())
					.result();
		}

	}

	private static ImmutableMap<String, Recommendations> convert(SetMultimap<String, Builder> localNameMap) {
		ImmutableMap.Builder<String, Recommendations> res = ImmutableMap.builder();
		for (Entry<String, Collection<Builder>> entry : localNameMap.asMap().entrySet()) {
			List<Recommendation> recs = new ArrayList<>(entry.getValue().size());
			for (Builder recBuilder : entry.getValue()) {
				recs.add(recBuilder.build());
			}
			recs.sort(new RecommendationComparator());
			res.put(entry.getKey(), new Recommendations(recs));
		}
		return res.build();
	}

	// private static ImmutableListMultimap<String, Recommendation>
	// convert(SortedSetMultimap<String, String> collection,
	// String ontologyName) {
	//
	// }

	private static void addAllsubsToMapping(String subsFrom, Recommendation.Builder mapTo,
			Multimap<String, Recommendation.Builder> theMap) {
		for (int i = 0; i < subsFrom.length(); i++) {
			for (int j = i + 1; j <= subsFrom.length(); j++) {
				String sub = subsFrom.substring(i, j);
				theMap.put(sub, mapTo);
			}
		}
	}

	@Override
	public Recommendations recommend(Query c) {
		return this.mappingTroughLocalName.getOrDefault(c.queryString, Recommendations.empty());
	}

	public static void main(String[] args) throws FileNotFoundException {
		// LocalVocabLoader loader = new LocalVocabLoader(new
		// FileInputStream("dcat.ttl"), Lang.TURTLE);

		// System.out.println(loader.mappingTroughLocalName.values());

		// Set<String> result = loader.mappingTroughLocalName.get("o");

		// cause loading
		PredefinedVocab a = PredefinedVocab.DCAT;

		int hc = 0;

		Stopwatch stopwatch = Stopwatch.createStarted();
		hc ^= PredefinedVocab.DCAT.recommend(new Query(null, "a")).hashCode();
		hc ^= PredefinedVocab.DCAT.recommend(new Query(null, "b")).hashCode();
		hc ^= PredefinedVocab.DCAT.recommend(new Query(null, "c")).hashCode();
		hc ^= PredefinedVocab.DCAT.recommend(new Query(null, "d")).hashCode();
		hc ^= PredefinedVocab.DCAT.recommend(new Query(null, "e")).hashCode();
		hc ^= PredefinedVocab.DCAT.recommend(new Query(null, "f")).hashCode();
		hc ^= PredefinedVocab.DCAT.recommend(new Query(null, "g")).hashCode();
		hc ^= PredefinedVocab.DCAT.recommend(new Query(null, "h")).hashCode();
		hc ^= PredefinedVocab.DCAT.recommend(new Query(null, "i")).hashCode();
		hc ^= PredefinedVocab.DCAT.recommend(new Query(null, "j")).hashCode();
		stopwatch.stop();
		System.out.println("time: " + stopwatch); // formatted string like "12.3 ms"
		System.out.println(hc);
	}

}