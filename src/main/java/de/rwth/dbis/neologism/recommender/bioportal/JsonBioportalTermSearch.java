package de.rwth.dbis.neologism.recommender.bioportal;

import java.util.ArrayList; 

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class JsonBioportalTermSearch {
	private int page;
	private int pageCount;
	private int totalCount;
	private int prevPage;
	private int nextPage;
	private JsonObject links;
	private ArrayList<SearchCollectionItem> collection;
	
	public int getPage() {
		return page;
	}
	public int getPageCount() {
		return pageCount;
	}
	public int getTotalCount() {
		return totalCount;
	}
	public int getPrevPage() {
		return prevPage;
	}
	public int getNextPage() {
		return nextPage;
	}
	public JsonObject getLinks() {
		return links;
	}
	public ArrayList<SearchCollectionItem> getCollection() {
		return collection;
	}
	@Override
	public String toString() {
		return "SearchedItem [page=" + page + ", pageCount=" + pageCount + ", totalCount=" + totalCount + ", prevPage="
				+ prevPage + ", nextPage=" + nextPage + ", links=" + links + ", collection=" + collection + "]";
	}
	
	public static class SearchCollectionItem {
		private String prefLabel;
		private ArrayList<String> synonym;
		private ArrayList<String> definition;
		private ArrayList<String> cui;
		private ArrayList<String> T167;
		private ArrayList<String> semanticType;
		private boolean obsolete;
		private String matchType;
		private String ontologyType;
		private boolean provisional;
		@SerializedName("@id")
		private String id;
		private String type;
		private TermLinks links;
		private JsonObject context;
		
		public String getPrefLabel() {
			return prefLabel;
		}

		public ArrayList<String> getSynonym() {
			return synonym;
		}


		public ArrayList<String> getDefinition() {
			return definition;
		}


		public ArrayList<String> getCui() {
			return cui;
		}


		public ArrayList<String> getT167() {
			return T167;
		}


		public ArrayList<String> getSemanticType() {
			return semanticType;
		}


		public boolean isObsolete() {
			return obsolete;
		}


		public String getMatchType() {
			return matchType;
		}


		public String getOntologyType() {
			return ontologyType;
		}


		public boolean isProvisional() {
			return provisional;
		}


		public String getId() {
			return id;
		}


		public String getType() {
			return type;
		}


		public TermLinks getLinks() {
			return links;
		}


		public JsonObject getContext() {
			return context;
		}


		@Override
		public String toString() {
			return "SearchCollectionItem [prefLabel=" + prefLabel + ", synonym=" + synonym + ", definition=" + definition
					+ ", cui=" + cui + ", T167=" + T167 + ", semanticType=" + semanticType + ", obsolete=" + obsolete
					+ ", matchType=" + matchType + ", ontologyType=" + ontologyType + ", provisional=" + provisional
					+ ", id=" + id + ", type=" + type + ", links=" + links + ", context=" + context + "]";
		}
		
		public static class TermLinks {
			
			private String self;
			private String ontology;
			private String children;
			private String parents;
			private String descendants;
			private String ancestors;
			private String instances;
			private String tree;
			private String notes;
			private String mappings;
			private String ui;		
			@SerializedName("@context")
			private Object context;
			
			
			public String getSelf() {
				return self;
			}
			public String getOntology() {
				return ontology;
			}
			public String getChildren() {
				return children;
			}
			public String getParents() {
				return parents;
			}
			public String getDescendants() {
				return descendants;
			}
			public String getAncestors() {
				return ancestors;
			}
			public String getInstances() {
				return instances;
			}
			public String getTree() {
				return tree;
			}
			public String getNotes() {
				return notes;
			}
			public String getMappings() {
				return mappings;
			}
			public String getUi() {
				return ui;
			}
			public Object getContext() {
				return context;
			}
			@Override
			public String toString() {
				return "TermLinks [self=" + self + ", ontology=" + ontology + ", children=" + children + ", parents=" + parents
						+ ", descendants=" + descendants + ", ancestors=" + ancestors + ", instances=" + instances + ", tree="
						+ tree + ", notes=" + notes + ", mappings=" + mappings + ", ui=" + ui + ", context=" + context + "]";
			}
		}


	}


}
