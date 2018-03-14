package de.rwth.dbis.neologism.recommender.bioportal;

import java.util.ArrayList;

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
		private PropertyValue p;
		private PropertyValue r;

		public PropertyValue getP() {
			return p;
		}
		
		public PropertyValue getR() {
			return r;
		}

	}
	
	
	public class PropertyValue {
		private String type;
		private String value;
		
		public String getType() {
			return type;
		}
		public String getValue() {
			return value;
		}
		
	}

	

}
