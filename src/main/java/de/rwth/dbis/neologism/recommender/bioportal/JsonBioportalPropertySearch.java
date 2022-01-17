package de.rwth.dbis.neologism.recommender.bioportal;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class JsonBioportalPropertySearch {

    private Object head;
    private Results results;

    public Object getHead() {
        return head;
    }

    public Results getResults() {
        return results;
    }


    public static class Results {

        private ArrayList<BindingsItem> bindings;

        public List<BindingsItem> getBindings() {
            return bindings;
        }
    }

    public static class PropertyValue {
        private String type;
        private String value;
        private String datatype;
        @SerializedName("xml:lang")
        private String lang;

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }

        public String getDatatype() {
            return datatype;
        }

        public String getLang() {
            return lang;
        }

        public boolean isEmpty() {
            return getType() == null && getValue() == null && getDatatype() == null && getLang() == null;
        }

    }

    public class BindingsItem {
        private PropertyValue p;
        private PropertyValue range;
        private PropertyValue label;
        private PropertyValue comment;

        public PropertyValue getP() {
            return p;
        }

        public PropertyValue getRange() {
            return range;
        }

        public PropertyValue getLabel() {
            return label;
        }

        public PropertyValue getComment() {
            return comment;
        }

    }


}
