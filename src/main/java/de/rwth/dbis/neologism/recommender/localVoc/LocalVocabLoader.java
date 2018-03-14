package de.rwth.dbis.neologism.recommender.localVoc;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
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
import com.google.common.hash.Hashing;

import de.rwth.dbis.neologism.recommender.PropertiesForClass;
import de.rwth.dbis.neologism.recommender.PropertiesQuery;
import de.rwth.dbis.neologism.recommender.Query;
import de.rwth.dbis.neologism.recommender.Recommendations;
import de.rwth.dbis.neologism.recommender.Recommendations.Language;
import de.rwth.dbis.neologism.recommender.Recommendations.Recommendation;
import de.rwth.dbis.neologism.recommender.Recommendations.Recommendation.Builder;
import de.rwth.dbis.neologism.recommender.Recommendations.StringLiteral;
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

	@Override
	public String getRecommenderName() {
		return this.name;
	}

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

		@Override
		public String getRecommenderName() {
			return this.loader.getRecommenderName();
		}

		@Override
		public PropertiesForClass getPropertiesForClass(PropertiesQuery q) {
			return this.loader.getPropertiesForClass(q);
		}
	}

	private final ImmutableMap<String, Recommendations> mappingTroughLocalName;
	// private final ImmutableMap<String, Recommendations> mappingTroughNamespace;
	// private final ImmutableMap<String, Recommendations> mappingTroughLabel;

	private final ImmutableMap<String, PropertiesForClass> propertiesForClasses;
	private final String name;

	private final Recommendations EMPTY;

	public LocalVocabLoader(InputStream source, Lang syntax, String ontology) {

		this.name = LocalVocabLoader.class.getName() + ontology
				+ Hashing.sha256().hashString(ontology, StandardCharsets.UTF_8).toString();

		this.EMPTY = new Recommendations(Collections.emptyList(), this.name);

		Dataset dataset = DatasetFactory.create();
		RDFParser.source(source).forceLang(syntax).build().parse(dataset.asDatasetGraph());

		RDFConnection conn = RDFConnectionFactory.connect(dataset);

		mappingTroughLocalName = precomputeClassRecommendations(ontology, conn, this.name);

		propertiesForClasses = precomputeProperties(conn);

		conn.close();
		dataset.close();
	}

	private static ImmutableMap<String, Recommendations> precomputeClassRecommendations(String ontology,
			RDFConnection conn, String recommenderName) {
		SetMultimap<String, Recommendation.Builder> localNameMap = SetMultimapBuilder.hashKeys().hashSetValues()
				.build();
		HashMap<String, Recommendation.Builder> terms = new HashMap<>();

		ResultSet rs = conn.query(
				"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT ?class ?label ?comment WHERE {{?class a rdfs:Class} UNION {[] a ?class} . "
						+ "OPTIONAL { ?class rdfs:label ?label } OPTIONAL {?class rdfs:comment ?comment} } ")
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
					builder.addLabel(new StringLiteral(Language.forLangCode(lang), label));
					// addAllsubsToMapping(label.toLowerCase(), classURI, labelMap);
				}

				if (res.contains("comment")) {
					Literal literalComment = res.get("comment").asLiteral();
					String literalCommentString = literalComment.getString();
					String lang = literalComment.getLanguage();
					if (lang.equals("")) {
						System.err.println("Found a label without language tag. Assuming english for '"
								+ literalCommentString + "'");
						lang = "en";
					}
					builder.addComment(new StringLiteral(Language.forLangCode(lang), literalCommentString));
					// addAllsubsToMapping(label.toLowerCase(), classURI, labelMap);
				}

			});
		}
		return convert(localNameMap, recommenderName);
	}

	private static ImmutableMap<String, PropertiesForClass> precomputeProperties(RDFConnection conn) {

		// query all classes:

		ResultSet rsAllClasses = conn.query(
				"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT distinct ?class  WHERE {{?class a rdfs:Class} UNION {[] a ?class}} ")
				.execSelect();

		List<String> classes = new ArrayList<>();

		if (rsAllClasses.hasNext()) {
			rsAllClasses.forEachRemaining(res -> {
				classes.add(res.get("class").toString());
			});
		}

		ImmutableMap.Builder<String, PropertiesForClass> mapBuilder = ImmutableMap.builder();

		for (String aClass : classes) {

			String query = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
					+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
					+ "SELECT DISTINCT ?p ?range ?label ?comment " + "WHERE{" + "?p a rdf:Property."
					+ "?p rdfs:domain <" + aClass + ">." + "?p rdfs:range ?range."
					+ "OPTIONAL{ ?p rdfs:label ?label } OPTIONAL{ ?p rdfs:comment ?comment }" + "}";

			ResultSet rs = conn.query(query).execSelect();

			if (rs.hasNext()) {
				PropertiesForClass.Builder builder = new PropertiesForClass.Builder();

				rs.forEachRemaining(res -> {
					builder.addFromQuerySolution(res);
				});
				PropertiesForClass props = builder.build();
				mapBuilder.put(aClass, props);
			}
		}

		return mapBuilder.build();
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

	private static ImmutableMap<String, Recommendations> convert(SetMultimap<String, Builder> localNameMap,
			String recommenderName) {
		ImmutableMap.Builder<String, Recommendations> res = ImmutableMap.builder();
		for (Entry<String, Collection<Builder>> entry : localNameMap.asMap().entrySet()) {
			List<Recommendation> recs = new ArrayList<>(entry.getValue().size());
			for (Builder recBuilder : entry.getValue()) {
				recs.add(recBuilder.build());
			}
			recs.sort(new RecommendationComparator());
			res.put(entry.getKey(), new Recommendations(recs, recommenderName));
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
		return this.mappingTroughLocalName.getOrDefault(c.queryString, EMPTY);
	}

	public static void main(String[] args) throws FileNotFoundException {
		// LocalVocabLoader loader = new LocalVocabLoader(new
		// FileInputStream("dcat.ttl"), Lang.TURTLE);

		// System.out.println(loader.mappingTroughLocalName.values());

		// Set<String> result = loader.mappingTroughLocalName.get("o");

		// cause loading
		// PredefinedVocab a = PredefinedVocab.DCAT;

		int hc = 0;

		Model model = (Model) ModelFactory.createDefaultModel();
		String data = "<http://ex.com#A> <http://ex.com#P1> <http://ex.com#B> .\n"
				+ "<http://ex.com#B> <http://ex.com#P1> <http://ex.com#C> .\n"
				+ "<http://ex.com#B> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> .\n"
				+ "<http://ex.com#E> <http://ex.com#P2> <http://ex.com#F> .\n"
				+ "<http://ex.com#A> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> .\n"
				+ "<http://ex.com#A> <http://ex.com#P1> <neo://query/bla> .\n";
		StringReader r = new StringReader(data);
		model = model.read(r, null, "N-TRIPLE");

		Stopwatch stopwatch = Stopwatch.createStarted();
		System.out.println(PredefinedVocab.DCAT.recommend(new Query(model)));
		stopwatch.stop();
		System.out.println("time: " + stopwatch); // formatted string like "12.3 ms"
		System.out.println(hc);
	}

	@Override
	public PropertiesForClass getPropertiesForClass(PropertiesQuery q) {
		return propertiesForClasses.getOrDefault(q.classIRI, PropertiesForClass.EMPTY);
	}

}
