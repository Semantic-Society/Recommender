package de.rwth.dbis.neologism.recommender.bioportal;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;

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

		public ArrayList<BindingsItem> getBindings() {
			return bindings;
		}
	}
	
	
	public class BindingsItem {
		private PropertyValue property;
		private PropertyValue range;
		private PropertyValue label;
		private PropertyValue comment;
			
		public PropertyValue getProperty() {
			return property;
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
	
	
	public class PropertyValue {
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
			if(getType() == null && getValue() == null && getDatatype() == null && getLang() == null) {
				return true;
			}else return false;
		}
		
	}

	

}
