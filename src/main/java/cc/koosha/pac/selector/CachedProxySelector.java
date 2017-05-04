package cc.koosha.pac.selector;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.*;
import java.util.Map.Entry;


/**
 * Implements a cache that can be used to warp it around an existing
 * ProxySelector. You can specify a maximum cache size and a "time to live" for
 * positive resolves.
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 */
public final class CachedProxySelector extends ProxySelector {

    /**
     * Define the available scopes of the cache key generation
     */
    public enum CacheScope {

        /**
         * Cache keys are generated by <code>uri.getHost()</code>.
         */
        CACHE_SCOPE_HOST,

        /**
         * Cache keys are generated by
         * <code>uri.getHost() + &quot;:&quot; + uri.getPort()</code>.
         */
        CACHE_SCOPE_HOST_PORT,

        /**
         * Cache keys are generated by <code>uri.toString()</code>.
         */
        CACHE_SCOPE_URL
    }


    private final ProxySelector delegate;

    private final HashMap<String, CacheEntry> cache = new HashMap<>();
    private final int bulkFree;
    private final int maxSize;
    private final long ttl;
    private final CacheScope cacheScope;


    private final Comparator<CacheEntry> cacheEntryByExpiryComparator = new Comparator<CacheEntry>() {
        @Override
        public int compare(final CacheEntry o1, final CacheEntry o2) {
            if (o1.expireAt < o2.expireAt)
                return -1;
            else if (o1.expireAt == o2.expireAt)
                return 0;
            else
                return 1;
        }
    };


    private final static class CacheEntry {

        final List<Proxy> result;
        final long expireAt;
        final String key;

        CacheEntry(final List<Proxy> r,
                   final long expireAt,
                   final String key) {

            this.result = Collections.unmodifiableList(new ArrayList<>(r));
            this.expireAt = expireAt;
            this.key = key;
        }

        boolean isExpired() {

            return System.nanoTime() >= this.expireAt;
        }
    }

    /**
     * @param maxSize    the max size for the cache.
     * @param ttl        the "time to live" for cache entries as amount in
     *                   milliseconds.
     * @param delegate   the delegate to use.
     * @param cacheScope the desired cache scope.
     */
    public CachedProxySelector(final int maxSize,
                               final long ttl,
                               final ProxySelector delegate,
                               final CacheScope cacheScope) {

        if (maxSize < 1)
            throw new IllegalStateException("maxSize must be >= 1: " + maxSize);
        if (ttl < 1)
            throw new IllegalStateException("ttl must be >= 1: " + ttl);

        this.maxSize = maxSize;
        this.delegate = delegate;
        this.ttl = ttl;
        this.cacheScope = cacheScope;
        this.bulkFree = maxSize <= 10 ? 1 : maxSize / 10;
    }

    @Override
    public void connectFailed(final URI uri,
                              final SocketAddress sa,
                              final IOException ioe) {

        this.delegate.connectFailed(uri, sa, ioe);
    }

    @Override
    public List<Proxy> select(final URI uri) {

        final String cacheKey;

        switch (cacheScope) {
            case CACHE_SCOPE_HOST:
                cacheKey = uri.getHost();
                break;

            case CACHE_SCOPE_HOST_PORT:
                cacheKey = uri.getHost() + ":" + uri.getPort();
                break;

            case CACHE_SCOPE_URL:
                cacheKey = uri.toString();
                break;

            default:
                throw new IllegalStateException("Unhandled CacheScope enum constant: " + cacheScope);
        }

        CacheEntry entry;

        synchronized (this.cache) {
            entry = this.cache.get(cacheKey);
        }

        if (entry == null || entry.isExpired())
            entry = new CacheEntry(
                    this.delegate.select(uri),
                    System.nanoTime() + this.ttl * 1000 * 1000,
                    cacheKey
            );

        synchronized (this.cache) {
            if (this.cache.size() >= this.maxSize) {
                final Iterator<Entry<String, CacheEntry>> it =
                        this.cache.entrySet().iterator();
                while (it.hasNext()) {
                    final Entry<String, CacheEntry> entry1 = it.next();
                    if (entry1.getValue().isExpired())
                        it.remove();
                }
            }
            if (this.cache.size() >= this.maxSize) {
                final ArrayList<CacheEntry> entries =
                        new ArrayList<>(this.cache.values());
                Collections.sort(entries, cacheEntryByExpiryComparator);
                for (int i = 0; i < bulkFree; i++)
                    cache.remove(entries.get(i).key);
            }

            this.cache.put(cacheKey, entry);
        }

        return entry.result;
    }

    public void flush() {

        synchronized (this.cache) {
            this.cache.clear();
        }
    }

}
