package de.rwth.dbis.neologism.recommender;

public interface Recommend {
	Recommendations recommend ( QueryContext c, String query );
}
