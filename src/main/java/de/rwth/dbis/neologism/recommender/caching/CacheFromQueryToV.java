package de.rwth.dbis.neologism.recommender.caching;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import de.rwth.dbis.neologism.recommender.Query;

public class CacheFromQueryToV<V>
// implements LoadingCache<Query, V>
{

	private static class QueryWrapper {
		private final Query q;

		public QueryWrapper(Query q) {
			this.q = q;
		}

		@Override
		public int hashCode() {
			return q.getContextHash().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof QueryWrapper)) {
				return false;
			}
			QueryWrapper w = (QueryWrapper)obj;
			return q.getContextHash().equals(w.q.getContextHash());
		}

	}

	private final LoadingCache<QueryWrapper, V> cache;

	public <W,Y> CacheFromQueryToV(CacheLoader<Query, V> loader) {
		cache = CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(120, TimeUnit.MINUTES) // cache will
																										// expire after
																										// 120 minutes
																										// of access
				.build(new CacheLoader<QueryWrapper, V>() { // build the cacheloader

					@Override
					public V load(QueryWrapper key) throws Exception {
						return loader.load(key.q);
					}
				});
	}

	public V get(Query key) throws ExecutionException {
		QueryWrapper w = new QueryWrapper(key);
		return this.cache.get(w);
	}

	// @Override
	// public V getIfPresent(Object key) {
	// if (!(key instanceof Query)) {
	// return null;
	// }
	// QueryWrapper w = new QueryWrapper((Query) key);
	// return this.cache.getIfPresent(w);
	// }
	//
	// @Override
	// public V get(Query key, Callable<? extends V> loader) throws
	// ExecutionException {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public ImmutableMap<Query, V> getAllPresent(Iterable<?> keys) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public void put(Query key, V value) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void putAll(Map<? extends Query, ? extends V> m) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void invalidate(Object key) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void invalidateAll(Iterable<?> keys) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void invalidateAll() {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public long size() {
	// // TODO Auto-generated method stub
	// return 0;
	// }
	//
	// @Override
	// public CacheStats stats() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public void cleanUp() {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public V get(Query key) throws ExecutionException {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public V getUnchecked(Query key) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public ImmutableMap<Query, V> getAll(Iterable<? extends Query> keys) throws
	// ExecutionException {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public V apply(Query key) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public void refresh(Query key) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public ConcurrentMap<Query, V> asMap() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	//

}
