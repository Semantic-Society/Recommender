package de.rwth.dbis.neologism.recommender.bioportal;

import java.util.Comparator;

public class OntologyComparator implements Comparator<BioportalOntology> {
    public OntologyComparator() {
    }

    public int compare(BioportalOntology o1, BioportalOntology o2) {

        if (o1.getFinalScore() < o2.getFinalScore())
            return -1;
        else if (o1.getFinalScore() == o2.getFinalScore())
            return 0;
        else
            return 1;
    }
}