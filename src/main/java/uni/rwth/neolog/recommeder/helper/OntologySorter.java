package uni.rwth.neolog.recommender.helper;

import java.util.Comparator;

public class OntologySorter implements Comparator<OntologyOutput>{
	public OntologySorter(){}
	
	public int compare(OntologyOutput o1, OntologyOutput o2) {
		
	    if(o1.getFinalScore()<o2.getFinalScore())
	    	return -1;
	    else if(o1.getFinalScore()==o2.getFinalScore())
	    	return 0;
	    else 
	    	return 1;
	}
}