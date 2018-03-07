package uni.rwth.neolog.recommeder.model;

import org.apache.jena.query.Dataset;
import org.apache.jena.tdb.TDBFactory;

public class TDBConnection 
{
	@SuppressWarnings("unused")
	private Dataset ds;
	private String path;
	
	public TDBConnection( String path )
	{
		this.path=path;
		ds = TDBFactory.createDataset( path );
	}

	public Dataset getDs() {
		return ds;
	}

	public void setDs(Dataset ds) {
		this.ds = ds;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
