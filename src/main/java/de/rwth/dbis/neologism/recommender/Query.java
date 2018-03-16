package de.rwth.dbis.neologism.recommender;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Selector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class Query {
	public final Model context;
	public final String queryString;
	// Integer.MAX_VALUE if unset.
	public final int limit;

	public final ImmutableList<String> localClassNames;

	public final static int RESULT_LIMIT = 100;

	/**
	 * A hash of the model. This hash is such that it IGNORES the statements
	 * containing the query string.
	 * 
	 * It is attempted that this hash is robust to permutations of the statements in
	 * the model, but this is not guaranteed.
	 */
	public final HashCode contextHash;

	public Query(Model context) {
		this(context, RESULT_LIMIT);
	}

	public Query(Model context, int limit) {
		this.context = Preconditions.checkNotNull(context);

		ImmutableList<String> foundQueries = extractQueryStringFromContext(context);
		if (foundQueries.size() == 0) {
			throw new Error("No queries found in context");
		} else if (foundQueries.size() > 1) {
			throw new UnsupportedOperationException("Multiple queries found in context. This is not supported yet!");
		}

		this.queryString = foundQueries.get(0);
		this.limit = limit;

		// get all local names
		ResIterator classes = context.listResourcesWithProperty(
				context.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
				context.createResource("http://www.w3.org/2000/01/rdf-schema#Class"));

		ImmutableList.Builder<String> b = ImmutableList.builder();
		while (classes.hasNext()) {
			Resource clazz = classes.next();
			if (!clazz.getNameSpace().startsWith(Query.queryStringNameSpace)) {
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
		if (hashes.size() > 0) {
		contextHash = Hashing.combineUnordered(hashes);
		} else {
			contextHash = HashCode.fromInt(0);
		}
		System.out.println(contextHash);
	}

	private static final String queryStringNameSpace = "neo://query/";

	private static final String queryStringNodeQuery = "select distinct ?queryNode where {"
			+ "{		 ?queryNode ?b [] . } " + " UNION " + "{ [] ?b ?queryNode . }"
			+ " FILTER STRSTARTS(str(?queryNode), \"" + queryStringNameSpace + "\") " + "} ";
	// TODO decide whether to add a limit to this query...

	private static ImmutableList<String> extractQueryStringFromContext(Model context) {
		Builder<String> queries = new ImmutableList.Builder<>();
		org.apache.jena.query.Query query = QueryFactory.create(queryStringNodeQuery);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, context)) {
			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				QuerySolution soln = results.nextSolution();
				Resource queryResource = soln.getResource("queryNode"); // Get a result variable by name.
				String QueryResourceString = queryResource.toString();
				String queryText = QueryResourceString.substring(queryStringNameSpace.length());//this is correct
				
				if (queryText.length() > 0) {
					queries.add(queryText);
				}
			}
		}
		return queries.build();
	}

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
