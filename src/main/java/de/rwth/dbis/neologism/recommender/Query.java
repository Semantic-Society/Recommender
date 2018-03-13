package de.rwth.dbis.neologism.recommender;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Selector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import com.google.common.collect.ImmutableList;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class Query {
	public final Model context;
	public final String queryString;
	// Integer.MAX_VALUE if unset.
	public final int limit;

	public final ImmutableList<String> localClassNames;

	/**
	 * A hash of the model. This hash is such that it IGNORES the statements
	 * containing the query string.
	 * 
	 * It is attempted that this hash is robust to permutations of the statements in
	 * the model, but this is not guaranteed.
	 */
	public final HashCode contextHash;

	public Query(Model context, String queryString) {
		this(context, queryString, Integer.MAX_VALUE);
	}

	public Query(Model context, String queryString, int limit) {
		this.context = context;
		this.queryString = queryString;
		this.limit = limit;

		// get all local names
		ResIterator classes = context.listResourcesWithProperty(
				context.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
				context.createResource("http://www.w3.org/2000/01/rdf-schema#Class"));

		ImmutableList.Builder<String> b = ImmutableList.builder();
		while (classes.hasNext()) {
			Resource clazz = classes.next();
			if (!clazz.getNameSpace().startsWith("neo://query/")) {
				String localName = clazz.getLocalName();
				b.add(localName);
			}
		}
		this.localClassNames = b.build();

		// make a hash of the model:

		StmtIterator statements = context.listStatements(new IgnoreQueryStringStatements());

		List<HashCode> hashes = new ArrayList<>();
		HashFunction hashfunction = Hashing.goodFastHash(64);
		while (statements.hasNext()) {
			Statement statement = statements.next();

			HashCode hash = hashfunction.hashString(statement.getSubject().toString() + '\0'
					+ statement.getPredicate().toString() + '\0' + statement.getObject().toString(),
					StandardCharsets.UTF_8);
			hashes.add(hash);
		}
		contextHash = Hashing.combineUnordered(hashes);

	}

	private static final String queryStringNameSpace = "neo://query/";

	private static class IgnoreQueryStringStatements implements Selector {

		@Override
		public boolean test(Statement t) {

			if (t.getSubject().getNameSpace().equals(queryStringNameSpace)) {
				return false;
			}
			if (t.getPredicate().getNameSpace().equals(queryStringNameSpace)) {
				return false;
			}
			if (t.getObject().isResource() && t.getObject().asResource().getNameSpace().equals(queryStringNameSpace)) {
				return false;
			}
			return true;
		}

		@Override
		public boolean isSimple() {
			return false;
		}

		@Override
		public Resource getSubject() {
			return null;
		}

		@Override
		public Property getPredicate() {
			return null;
		}

		@Override
		public RDFNode getObject() {
			return null;
		}
	}

}
