package de.rwth.dbis.neologism.recommender;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Query {
    public final static int RESULT_LIMIT = 100;
    private static final String queryStringNameSpace = "neo://query/";
    private static final String queryStringNodeQuery = "select distinct ?queryNode where {"
            + "{		 ?queryNode ?b [] . } " + " UNION " + "{ [] ?b ?queryNode . }"
            + " FILTER STRSTARTS(str(?queryNode), \"" + queryStringNameSpace + "\") " + "} ";
    public final Model context;
    public final String queryString;
    // Integer.MAX_VALUE if unset.
    public final int limit;
    private final Object localClassNamesSync = new Object();
    private final Object contextHashSync = new Object();
    private ImmutableSet<String> localClassNames = null;
    private HashCode contextHash = null;

    public Query(Model context) {
        this(context, RESULT_LIMIT);
    }
    // TODO decide whether to add a limit to this query...

    public Query(Model context, int limit) {
        this(extractOnlyKeyWord(context), context, limit);
    }

    public Query(String query, Model context) {
        this(query, context, RESULT_LIMIT);
    }

    public Query(String query, Model context, int limit) {
        this.context = Preconditions.checkNotNull(context);

        this.queryString = query;
        this.limit = limit;
    }

    private static String extractOnlyKeyWord(Model thecontext) {

        ImmutableList<String> foundQueries = extractQueryStringFromContext(thecontext);
        if (foundQueries.size() == 0) {
            throw new Error("No queries found in context");
        } else if (foundQueries.size() > 1) {
            throw new UnsupportedOperationException("Multiple queries found in context. This is not supported yet!");
        }
        return foundQueries.get(0);
    }

    private static ImmutableList<String> extractQueryStringFromContext(Model context) {
        Builder<String> queries = new ImmutableList.Builder<>();
        org.apache.jena.query.Query query = QueryFactory.create(queryStringNodeQuery);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, context)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Resource queryResource = soln.getResource("queryNode"); // Get a result variable by name.
                String QueryResourceString = queryResource.toString();
                String queryText = QueryResourceString.substring(queryStringNameSpace.length());// this is correct

                if (queryText.length() > 0) {
                    queries.add(queryText);
                }
            }
        }
        return queries.build();
    }

    public ImmutableSet<String> getLocalClassNames() {

        if (localClassNames == null) // don't want to block here
        {
            // two or more threads might be here!!!
            synchronized (localClassNamesSync) {
                // must check again as one of the
                // blocked threads can still enter
                if (localClassNames == null) {
                    localClassNames = _getLocalClassNames();
                }
            }
        }
        return localClassNames;

    }

    private ImmutableSet<String> _getLocalClassNames() {
        if (context.isEmpty()) {
            return ImmutableSet.of();
        }
        ResIterator classes = context.listResourcesWithProperty(
                context.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                context.createResource("http://www.w3.org/2000/01/rdf-schema#Class"));

        Set<String> setClasses = new HashSet<String>();
        while (classes.hasNext()) {
            setClasses.add(classes.next().toString());
        }

        return ImmutableSet.copyOf(setClasses);
    }

    public HashCode getContextHash() {
        if (contextHash == null) // don't want to block here
        {
            // two or more threads might be here!!!
            synchronized (contextHashSync) {
                // must check again as one of the
                // blocked threads can still enter
                if (contextHash == null) {
                    contextHash = _getContextHash();
                }
            }
        }
        return contextHash;
    }

    /**
     * A hash of the model. This hash is such that it IGNORES the statements
     * containing the query string.
     * <p>
     * It is attempted that this hash is robust to permutations of the statements in
     * the model, but this is not guaranteed.
     */
    private HashCode _getContextHash() {
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
            return Hashing.combineUnordered(hashes);
        } else {
            return HashCode.fromInt(0);
        }
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
            return !t.getObject().isResource() || !t.getObject().asResource().getNameSpace().equals(queryStringNameSpace);
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
