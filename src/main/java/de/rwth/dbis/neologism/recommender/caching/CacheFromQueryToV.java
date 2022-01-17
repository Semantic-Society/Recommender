package de.rwth.dbis.neologism.recommender.caching;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.rwth.dbis.neologism.recommender.Query;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class CacheFromQueryToV<V>
// implements LoadingCache<Query, V>
{

    private final LoadingCache<QueryWrapper, V> cache;

    public <W, Y> CacheFromQueryToV(CacheLoader<Query, V> loader) {

        // cache will expire after 120 minutes of access
        cache = CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(120, TimeUnit.MINUTES)
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
            QueryWrapper w = (QueryWrapper) obj;
            return q.getContextHash().equals(w.q.getContextHash());
        }

    }
}
