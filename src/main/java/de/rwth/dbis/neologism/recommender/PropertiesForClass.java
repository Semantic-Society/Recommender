package de.rwth.dbis.neologism.recommender;

import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;


public class PropertiesForClass {
	
	public final ImmutableList<Property> list;
	
	public PropertiesForClass(List<Property> l) {
		this.list = ImmutableList.copyOf(l);
	}

	private static Joiner j = Joiner.on('\n');

	@Override
	public String toString() {
		return j.join(this.list);
	}
	
	public static class Property {
		
		private final String URI;
		private final String range;

		public Property(String URI, String range) {
			this.URI = Preconditions.checkNotNull(URI);
			this.range = Preconditions.checkNotNull(range);
		}
		
		public String getURI() {
			return URI;
		}
		
		public String getRange() {
			return range;
		}

		@Override
		public String toString() {
			return this.URI + "\t "+ this.range;
		}

	}

	
}
