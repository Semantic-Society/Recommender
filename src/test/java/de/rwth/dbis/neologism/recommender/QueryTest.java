package de.rwth.dbis.neologism.recommender;

import java.io.StringReader;
import java.util.Collections;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import junit.framework.Assert;
import junit.framework.TestCase;

public class QueryTest extends TestCase {

	private static Model simpleModel() {
		Model model = (Model) ModelFactory.createDefaultModel();
		String data = "<http://ex.com#A> <http://ex.com#P1> <http://ex.com#B> .\n"
				+ "<http://ex.com#B> <http://ex.com#P1> <http://ex.com#C> .\n"
				+ "<http://ex.com#E> <http://ex.com#P2> <http://ex.com#F> .\n"
				+ "<http://ex.com#A> <http://ex.com#P1> <http://ex.com#G> .\n";
		StringReader r = new StringReader(data);

		model = model.read(r, null, "N-TRIPLE");
		return model;
	}

	private static Model modelWithOneClass() {
		Model model = (Model) ModelFactory.createDefaultModel();
		String data = "<http://ex.com#A> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> .\n";
		StringReader r = new StringReader(data);
		model = model.read(r, null, "N-TRIPLE");
		return model;
	}

	private static Model modelWithClasses() {
		Model model = (Model) ModelFactory.createDefaultModel();
		String data = "<http://ex.com#A> <http://ex.com#P1> <http://ex.com#B> .\n"
				+ "<http://ex.com#B> <http://ex.com#P1> <http://ex.com#C> .\n"
				+ "<http://ex.com#B> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> .\n"
				+ "<http://ex.com#E> <http://ex.com#P2> <http://ex.com#F> .\n"
				+ "<http://ex.com#A> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> .\n";
		StringReader r = new StringReader(data);
		model = model.read(r, null, "N-TRIPLE");
		return model;
	}

	public void testQueryModelStringInt_SimpleParts() {
		Model m = simpleModel();
		Query q = new Query(m, "bla", 20);
		Assert.assertEquals(20, q.limit);
		Assert.assertEquals("bla", q.queryString);
		Assert.assertEquals(m, q.context);
	}

	public void testQueryModelStringInt_ClassesEmpty() {
		Model m = simpleModel();
		Query q = new Query(m, "bla", 20);
		assertEquals(q.localClassNames, Collections.emptyList());
	}

	public void testQueryModelStringInt_SingleClass() {
		Model m = modelWithOneClass();
		Query q = new Query(m, "bla", 20);
		assertEquals(Lists.newArrayList("A"), q.localClassNames);
	}

	public void testQueryModelStringInt_Classes() {
		Model m = modelWithClasses();
		Query q = new Query(m, "bla", 20);
		assertEquals(Sets.newHashSet("A", "B"), Sets.newHashSet(q.localClassNames));
	}

	private static Model modelWithClassesAndQuery() {
		Model model = (Model) ModelFactory.createDefaultModel();
		String data = "<http://ex.com#A> <http://ex.com#P1> <http://ex.com#B> .\n"
				+ "<http://ex.com#B> <http://ex.com#P1> <http://ex.com#C> .\n"
				+ "<http://ex.com#B> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> .\n"
				+ "<http://ex.com#E> <http://ex.com#P2> <http://ex.com#F> .\n"
				+ "<http://ex.com#A> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> .\n";
		StringReader r = new StringReader(data);
		model = model.read(r, null, "N-TRIPLE");
		return model;
	}

	public void testQueryModelStringInt_ClassesAndQuery() {
		Model m = modelWithClassesAndQueries();
		Query q = new Query(m, "bla", 20);
		assertEquals("The queries should be ignored when finding the classes from the context.", Sets.newHashSet("A", "B"), Sets.newHashSet(q.localClassNames));
	}
	
	
	private static Model modelWithClassesPermuted() {
		Model model = (Model) ModelFactory.createDefaultModel();
		String data = "<http://ex.com#A> <http://ex.com#P1> <http://ex.com#B> .\n"
				+ "<http://ex.com#B> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> .\n"
				+ "<http://ex.com#B> <http://ex.com#P1> <http://ex.com#C> .\n"
				+ "<http://ex.com#E> <http://ex.com#P2> <http://ex.com#F> .\n"
				+ "<http://ex.com#A> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> .\n";
		StringReader r = new StringReader(data);
		model = model.read(r, null, "N-TRIPLE");
		return model;
	}

	public void testQueryModelStringInt_HashForPermutations() {
		Model m1 = modelWithClasses();
		Query q1 = new Query(m1, "bla", 20);
		Model m2 = modelWithClassesPermuted();
		Query q2 = new Query(m2, "bla", 20);
		assertTrue("The hash shoulf not be modified by permutations.", q1.contextHash.equals(q2.contextHash));
	}

	private static Model modelWithClassesAndQueries() {
		Model model = (Model) ModelFactory.createDefaultModel();
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

	public void testQueryModelStringInt_HashWithQueries() {
		Model m1 = modelWithClasses();
		Query q1 = new Query(m1, "bla", 20);
		Model m2 = modelWithClassesAndQueries();
		Query q2 = new Query(m2, "bla", 20);
		assertTrue("The hash should not be modified by addition of statements including a query.", q1.contextHash.equals(q2.contextHash));
	}
}
