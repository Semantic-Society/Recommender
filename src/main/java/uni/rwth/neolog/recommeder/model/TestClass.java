package uni.rwth.neolog.recommeder.model;

import uni.rwth.neolog.recommeder.model.TDBConnection;

import java.util.List;

import org.apache.jena.rdf.model.Statement;

import uni.rwth.neolog.recommeder.model.AddStatement;
public class TestClass {
	

	public static void main(String[] args) {
		
			TDBConnection tdb = new TDBConnection("/Users/shibasishdas/Documents/Workspace/NeologRecommender");
			
			AddStatement as= new AddStatement()
					;
			String URI = "https://tutorial-academy.com/2015/tdb#";
			
			String namedModel1 = "Model_German_Cars";
			String namedModel2 = "Model_US_Cars";
			
			String john = URI + "John";
			String mike = URI + "Mike";
			String bill = URI + "Bill";
			String owns = URI + "owns";
			
//			tdb = new TDBConnection("tdb");
			// named Model 1
			as.addStatement( namedModel1, john, owns, URI + "Porsche",tdb.getDs() );
			as.addStatement( namedModel1, john, owns, URI + "BMW",tdb.getDs()  );
			as.addStatement( namedModel1, mike, owns, URI + "BMW",tdb.getDs()  );
			as.addStatement( namedModel1, bill, owns, URI + "Audi",tdb.getDs()  );
			as.addStatement( namedModel1, bill, owns, URI + "BMW",tdb.getDs()  );
			
			// named Model 2
//			as.addStatement( namedModel2, john, owns, URI + "Chrysler",tdb.getDs()  );
//			as.addStatement( namedModel2, john, owns, URI + "Ford" );
//			as.addStatement( namedModel2, bill, owns, URI + "Chevrolet" );
			
			// null = wildcard search. Matches everything with BMW as object!
//			List<Statement> result = tdb.getStatements( namedModel1, null, null, URI + "BMW");
//			System.out.println( namedModel1 + " size: " + result.size() + "\n\t" + result );
			
			// null = wildcard search. Matches everything with john as subject!
//			result = tdb.getStatements( namedModel2, john, null, null);
//			System.out.println( namedModel2 + " size: " + result.size() + "\n\t" + result );
			
			// remove all statements from namedModel1
//			tdb.removeStatement( namedModel1, john, owns, URI + "Porsche" );
//			tdb.removeStatement( namedModel1, john, owns, URI + "BMW" );
//			tdb.removeStatement( namedModel1, mike, owns, URI + "BMW" );
//			tdb.removeStatement( namedModel1, bill, owns, URI + "Audi" );
//			tdb.removeStatement( namedModel1, bill, owns, URI + "BMW" );
			
//			result = tdb.getStatements( namedModel1, john, null, null);
//			System.out.println( namedModel1 + " size: " + result.size() + "\n\t" + result );
//			tdb.close();
	 
		}
}
