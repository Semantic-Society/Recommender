package de.rwth.dbis.neologism.recommender.bioportal;

import de.rwth.dbis.neologism.recommender.bioportal.JsonBioportalTermSearch.SearchCollectionItem;

import java.util.Comparator;

public class SearchCollectionItemComparator implements Comparator<SearchCollectionItem> {

	public int compare(SearchCollectionItem s1, SearchCollectionItem s2) {

		if (s1.getPrefLabel() == null) {
			if (s2.getPrefLabel() == null)
				return 0;
			else
				return 1;
		}

		if (s2.getPrefLabel() == null)
			return -1;

		if (s1.getPrefLabel().length() < s2.getPrefLabel().length())
			return -1;
		else if (s1.getPrefLabel().length() == s2.getPrefLabel().length())
			return 0;
		else
			return 1;
	}
}
