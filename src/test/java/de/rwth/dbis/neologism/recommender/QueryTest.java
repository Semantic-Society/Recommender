package de.rwth.dbis.neologism.recommender;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.StringReader;
import java.util.Collections;

public class QueryTest extends TestCase {

    private static Model simpleModel() {
        Model model = ModelFactory.createDefaultModel();
        String data = "<http://ex.com#A> <http://ex.com#P1> <http://ex.com#B> .\n"
                + "<http://ex.com#B> <http://ex.com#P1> <http://ex.com#C> .\n"
                + "<http://ex.com#E> <http://ex.com#P2> <http://ex.com#F> .\n"
                + "<http://ex.com#A> <http://ex.com#P1> <http://ex.com#G> .\n"
                + "<http://ex.com#A> <http://ex.com#P1> <neo://query/bla> .\n";

        StringReader r = new StringReader(data);

        model = model.read(r, null, "N-TRIPLE");
        return model;
    }

    private static Model modelWithOneClass() {
        Model model = ModelFactory.createDefaultModel();
        String data = "<http://ex.com#A> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ."
                + "<http://ex.com#A> <http://ex.com#P1> <neo://query/bla> .\n";
        StringReader r = new StringReader(data);
        model = model.read(r, null, "N-TRIPLE");
        return model;
    }

    private static Model modelWithClasses() {
        Model model = ModelFactory.createDefaultModel();
        String data = "<http://ex.com#A> <http://ex.com#P1> <http://ex.com#B> .\n"
                + "<http://ex.com#B> <http://ex.com#P1> <http://ex.com#C> .\n"
                + "<http://ex.com#B> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> .\n"
                + "<http://ex.com#E> <http://ex.com#P2> <http://ex.com#F> .\n"
                + "<http://ex.com#A> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> .\n"
                + "<http://ex.com#A> <http://ex.com#P1> <neo://query/bla> .\n";
        StringReader r = new StringReader(data);
        model = model.read(r, null, "N-TRIPLE");
        return model;
    }

    private static Model modelWithClassesPermuted() {
        Model model = ModelFactory.createDefaultModel();
        String data = "<http://ex.com#A> <http://ex.com#P1> <http://ex.com#B> .\n"
                + "<http://ex.com#B> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> .\n"
                + "<http://ex.com#B> <http://ex.com#P1> <http://ex.com#C> .\n"
                + "<http://ex.com#E> <http://ex.com#P2> <http://ex.com#F> .\n"
                + "<http://ex.com#A> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> .\n"
                + "<http://ex.com#A> <http://ex.com#P1> <neo://query/bla> .\n";
        StringReader r = new StringReader(data);
        model = model.read(r, null, "N-TRIPLE");
        return model;
    }

    private static Model modelWithClassesAndQuery() {
        Model model = ModelFactory.createDefaultModel();
        String data = "<http://ex.com#A> <http://ex.com#P1> <http://ex.com#B> .\n"
                + "<http://ex.com#B> <http://ex.com#P1> <http://ex.com#C> .\n"
                + "<http://ex.com#B> <http://ex.com#P1> <neo://query/bla> .\n"
                + "<http://ex.com#B> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> .\n"
                + "<http://ex.com#E> <http://ex.com#P2> <http://ex.com#F> .\n"
                + "<neo://query/bla> <http://ex.com#P1> <http://ex.com#B> .\n"
                + "<http://ex.com#A> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> .\n";
        StringReader r = new StringReader(data);
        model = model.read(r, null, "N-TRIPLE");
        return model;
    }

    private static Model modelWithClassesAndThreeQueries() {
        Model model = ModelFactory.createDefaultModel();
        String data = "<http://ex.com#A> <http://ex.com#P1> <http://ex.com#B> .\n"
                + "<http://ex.com#B> <http://ex.com#P1> <http://ex.com#C> .\n"
                + "<http://ex.com#B> <http://ex.com#P1> <neo://query/bla> .\n"
                + "<http://ex.com#B> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> .\n"
                + "<http://ex.com#B> <http://ex.com#P1> <neo://query/blo> .\n"
                + "<neo://query/blu> <http://ex.com#P1> <http://ex.com#B> .\n"
                + "<http://ex.com#E> <http://ex.com#P2> <http://ex.com#F> .\n"
                + "<neo://query/bla> <http://ex.com#P1> <http://ex.com#B> .\n"
                + "<http://ex.com#A> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> .\n";
        StringReader r = new StringReader(data);
        model = model.read(r, null, "N-TRIPLE");
        return model;
    }

    public void testQueryModelStringInt_SimpleParts() {
        Model m = simpleModel();
        Query q = new Query(m, 20);
        Assert.assertEquals(20, q.limit);
        Assert.assertEquals(m, q.context);
        assertEquals("bla", q.queryString);
    }

    public void testQueryModelStringInt_ClassesEmpty() {
        Model m = simpleModel();
        Query q = new Query(m, 20);
        assertEquals("bla", q.queryString);
        assertEquals(q.getLocalClassNames(), Collections.emptySet());
    }

    public void testQueryModelStringInt_SingleClass() {
        Model m = modelWithOneClass();
        Query q = new Query(m, 20);
        assertEquals(Lists.newArrayList("A"), q.getLocalClassNames());
    }

    public void testQueryModelStringInt_Classes() {
        Model m = modelWithClasses();
        Query q = new Query(m, 20);
        assertEquals(Sets.newHashSet("A", "B"), Sets.newHashSet(q.getLocalClassNames()));
    }

    public void testQueryModelStringInt_HashForPermutations() {
        Model m1 = modelWithClasses();
        Query q1 = new Query(m1, 20);
        Model m2 = modelWithClassesPermuted();
        Query q2 = new Query(m2, 20);
        assertEquals("The hash shoulf not be modified by permutations.", q1.getContextHash(), q2.getContextHash());
    }

    public void testQueryModelStringInt_ClassesAndQuery() {
        Model m = modelWithClassesAndQuery();
        Query q = new Query(m, 20);
        assertEquals("The queries should be ignored when finding the classes from the context.",
                Sets.newHashSet("A", "B"), Sets.newHashSet(q.getLocalClassNames()));
    }

    public void testQueryModelStringInt_Query() {
        Model m = modelWithClassesAndQuery();
        Query q = new Query(m, 20);
        assertEquals("The queries should be ignored when finding the classes from the context.", "bla", q.queryString);
    }

    public void testQueryModelStringInt_ThreeQuery() {
        Model m = modelWithClassesAndThreeQueries();
        try {
            new Query(m, 20);
        } catch (UnsupportedOperationException e) {
            assertEquals("Multiple queries found in context. This is not supported yet!", e.getMessage());
            return;
        }
        throw new junit.framework.AssertionFailedError("Expected exception was not thrown.");

        //// For now, multiple queries are not supported, so testing exception rather
        //// than expecting reuslts

        // assertEquals("The queries should be ignored when finding the classes from the
        // context.", Sets.newHashSet("bla", "blo", "blu"),
        // Sets.newHashSet(q.queryStrings));
    }

    public void testQueryModelStringInt_HashWithQueries() {
        Model m1 = modelWithClasses();
        Query q1 = new Query(m1, 20);
        Model m2 = modelWithClassesAndQuery();
        Query q2 = new Query(m2, 20);
        assertEquals("The hash should not be modified by addition of statements including a query.", q1.getContextHash(), q2.getContextHash());

    }

    // public void testExtractQuery() {
    // Method method = Query.getDeclaredMethod("extractQueryStringFromContext",
    // argClasses);
    // method.setAccessible(true);
    // return method.invoke(targetObject, argObjects);
    // }
}
