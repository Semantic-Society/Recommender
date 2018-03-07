package uni.rwth.neolog.recommeder.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Selector;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

public class GetStatements {
	
	public List<Statement> getStatements( String modelName, String subject, String property, String object, Dataset ds )
	{
		List<Statement> results = new ArrayList<Statement>();
			
		Model model = null;
			
		ds.begin( ReadWrite.READ );
		try
		{
			model = ds.getNamedModel( modelName );
				
			Selector selector = 
					new SimpleSelector(( subject != null ) ? model.createResource( subject ) : (Resource) null,
						( property != null ) ? model.createProperty( property ) : (Property) null,
						( object != null ) ? model.createResource( object ) : (RDFNode) null);
				
			StmtIterator it = model.listStatements( selector );
			{
				while( it.hasNext() )
				{
					Statement stmt = it.next(); 
					results.add( stmt );
				}
			}
				
			ds.commit();
		}
		finally
		{
			if( model != null ) model.close();
			ds.end();
			model.close();
		}
			
		return results;
	}

}
