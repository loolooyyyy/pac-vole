package cc.koosha.pac.selector;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Implements a fallback selector to warp it around an existing ProxySelector.
 * This will remove proxies from a list of proxies and implement an automatic
 * retry mechanism.
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 */
public final class ProxyListFallbackSelector extends ProxySelector {

    // Retry a unresponsive proxy after 10 minutes per default.
    private static final int DEFAULT_RETRY_DELAY = 1000 * 60 * 10;

    private final ProxySelector delegate;
    private final ConcurrentHashMap<SocketAddress, Long> failedDelayCache;
    private final long retryAfterMs;

    /**
     * Constructor
     *
     * @param delegate the delegate to use.
     */
    public ProxyListFallbackSelector(final ProxySelector delegate) {

        this(DEFAULT_RETRY_DELAY, delegate);
    }

    /**
     * @param retryAfterMs the "retry delay" as amount of milliseconds.
     * @param delegate     the delegate to use.
     */
    public ProxyListFallbackSelector(final long retryAfterMs,
                                     final ProxySelector delegate) {

        this.failedDelayCache = new ConcurrentHashMap<>();
        this.delegate = delegate;
        this.retryAfterMs = retryAfterMs;
    }

    @Override
    public void connectFailed(final URI uri,
                              final SocketAddress sa,
                              final IOException ioe) {

        this.failedDelayCache.put(sa, System.currentTimeMillis());
    }

    @Override
    public List<Proxy> select(final URI uri) {

        cleanupCache();
        final List<Proxy> proxyList = this.delegate.select(uri);
        return filterUnresponsiveProxiesFromList(proxyList);
    }

    /**
     * Cleanup the entries from the cache that are no longer unresponsive.
     */
    private void cleanupCache() {

        final Iterator<Entry<SocketAddress, Long>> it =
                this.failedDelayCache.entrySet().iterator();

        while (it.hasNext()) {
            final Entry<SocketAddress, Long> e = it.next();
            final Long lastFailTime = e.getValue();
            if (retryDelayHasPassedBy(lastFailTime))
                it.remove();
        }
    }

    /**
     * Filters out proxies that are not responding.
     *
     * @param proxyList a list of proxies to test.
     *
     * @return the filtered list.
     */
    private List<Proxy> filterUnresponsiveProxiesFromList(final List<Proxy> proxyList) {

        if (this.failedDelayCache.isEmpty())
            return proxyList;

        final List<Proxy> result = new ArrayList<>(proxyList.size());
        for (final Proxy proxy : proxyList)
            if (isDirect(proxy) || isNotUnresponsive(proxy))
                result.add(proxy);

        return result;
    }

    /**
     * Checks if the given proxy is representing a direct connection.
     *
     * @param proxy to inspect.
     *
     * @return true if it is direct else false.
     */
    private boolean isDirect(final Proxy proxy) {

        return Proxy.NO_PROXY.equals(proxy);
    }

    /**
     * Tests that a given proxy is not "unresponsive".
     *
     * @param proxy to test.
     *
     * @return true if not unresponsive.
     */
    private boolean isNotUnresponsive(final Proxy proxy) {

        final Long lastFailTime = this.failedDelayCache.get(proxy.address());
        return retryDelayHasPassedBy(lastFailTime);
    }

    /**
     * Checks if the retry delay has passed.
     *
     * @return true if the delay has passed.
     */
    private boolean retryDelayHasPassedBy(final Long lastFailTime) {

        return lastFailTime == null ||
                lastFailTime + this.retryAfterMs < System.currentTimeMillis();
    }

}
