package uni.rwth.neolog.recommeder.model;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;

public class AddStatement {
	
	public void addStatement( String modelName, String subject, String property, String object, Dataset ds )
	{
		Model model = null;
		
		ds.begin( ReadWrite.WRITE );
		try
		{
			model = ds.getNamedModel( modelName );
			
			Statement stmt = model.createStatement
							 ( 	
								model.createResource( subject ), 
								model.createProperty( property ), 
								model.createResource( object ) 
							 );
			
			model.add( stmt );
			ds.commit();
		}
		finally
		{
			if( model != null ) model.close();
			ds.end();
		}
	}

}
