package de.rwth.dbis.neologism.recommender;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Set;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder.SetMultimapBuilder;
import com.google.common.collect.SetMultimap;

public class LocalVocabLoader {

	enum PredefinedVocab {
		DCAT("dcat.ttl", Lang.TURTLE), DUBLIN_CORE_TERMS("dcterms.ttl", Lang.TURTLE);

		private final LocalVocabLoader loader;

		private PredefinedVocab(String resource, Lang lang) {
			InputStream res;
			try {
				// res = new FileInputStream(new File(resource)); //
				res = LocalVocabLoader.class.getResourceAsStream(resource);
				this.loader = new LocalVocabLoader(res, lang);
			} catch (Exception e) {
				throw new Error("Hard coded resource not found. " + resource, e);
			}
		}

		public Set<String> query(String q) {
			return this.loader.query(q);
		}

	}

	private final SetMultimap<String, String> mappingTroughLocalName = SetMultimapBuilder.hashKeys()
			.linkedHashSetValues().build();
	private final SetMultimap<String, String> mappingTroughNamespace = SetMultimapBuilder.hashKeys()
			.linkedHashSetValues().build();
	private final SetMultimap<String, String> mappingTroughLabel = SetMultimapBuilder.hashKeys().linkedHashSetValues()
			.build();

	public LocalVocabLoader(InputStream source, Lang lang) {
		Dataset dataset = DatasetFactory.create();
		RDFParser.source(source).forceLang(lang).build().parse(dataset.asDatasetGraph());

		RDFConnection conn = RDFConnectionFactory.connect(dataset);

		ResultSet rs = conn.query(
				"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT ?class ?label WHERE {{?class a rdfs:Class} UNION {[] a ?class} . "
						+ "OPTIONAL { ?class rdfs:label ?label } }")
				.execSelect();

		rs.forEachRemaining(res -> {

			Resource className = res.getResource("class");
			String classURI = className.getURI();
			String localName = className.getLocalName();
			String namespace = className.getNameSpace();

			addAllsubsToMapping(localName.toLowerCase(), classURI, mappingTroughLocalName);
			addAllsubsToMapping(namespace.toLowerCase(), classURI, mappingTroughNamespace);

			if (res.contains("label")) {
				String label = res.get("label").asLiteral().getString();
				addAllsubsToMapping(label.toLowerCase(), classURI, mappingTroughLabel);
			}
		});
	}

	public Set<String> query(String q) {
		//FIXME: currently only local name
		return mappingTroughLocalName.get(q);
		
	}

	private static void addAllsubsToMapping(String subsFrom, String mapTo, Multimap<String, String> theMap) {
		for (int i = 0; i < subsFrom.length(); i++) {
			for (int j = i + 1; j <= subsFrom.length(); j++) {
				String sub = subsFrom.substring(i, j);
				theMap.put(sub, mapTo);
			}
		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		// LocalVocabLoader loader = new LocalVocabLoader(new
		// FileInputStream("dcat.ttl"), Lang.TURTLE);

		// System.out.println(loader.mappingTroughLocalName.values());

		// Set<String> result = loader.mappingTroughLocalName.get("o");
		System.out.println(PredefinedVocab.DCAT.query("a"));
	}

}
