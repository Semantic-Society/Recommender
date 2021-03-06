package de.rwth.dbis.neologism.recommender;

import com.google.common.base.Preconditions;

public class PropertiesQuery {
    public final String classIRI;

    public PropertiesQuery(String classIRI) {
        Preconditions.checkNotNull(classIRI);
        Preconditions.checkArgument(!classIRI.isEmpty(), "No empty query allowed");
        this.classIRI = classIRI;
    }

    @Override
    public int hashCode() {
        return classIRI.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PropertiesQuery other = (PropertiesQuery) obj;
        if (classIRI == null) {
            return other.classIRI == null;
        } else return classIRI.equals(other.classIRI);
    }


    @Override
    public String toString() {
        return this.classIRI;
    }

}
