package cc.koosha.pac.selector;

import cc.koosha.pac.filter.DefaultFilterListParser;
import cc.koosha.pac.func.PredicateX;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;


/**
 * Special purpose ProxySelector used as Facade on top of a normal
 * ProxySelector. A wrapper that will first check the URI against a white list
 * and if it matches it will return DIRECT else it will pass the URI to an
 * delegate for inspection.
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 */
public final class BypassListProxySelector extends AbstractProxySelector {

    private final ProxySelector delegate;
    private final List<PredicateX<URI>> whiteListFilter;

    /**
     * @param whiteListFilter a list of filters for whitelist URLs.
     * @param proxySelector   the proxy selector to use.
     */
    public BypassListProxySelector(final List<PredicateX<URI>> whiteListFilter,
                                   final ProxySelector proxySelector) {

        if (whiteListFilter == null)
            throw new NullPointerException("whitelist");
        if (proxySelector == null)
            throw new NullPointerException("proxySelector");

        this.delegate = proxySelector;
        this.whiteListFilter = whiteListFilter;
    }

    /**
     * @param whiteList     a list of filters for whitelist URLs as comma/space
     *                      separated string.
     * @param proxySelector the proxy selector to use.
     */
    public BypassListProxySelector(final String whiteList,
                                   final ProxySelector proxySelector) {

        this(DefaultFilterListParser.parse(whiteList), proxySelector);
    }

    @Override
    public void connectFailed(final URI uri,
                              final SocketAddress sa,
                              final IOException ioe) {

        this.delegate.connectFailed(uri, sa, ioe);
    }

    @Override
    public List<Proxy> select(final URI uri) {

        // If in white list, use DIRECT connection.
        for (final PredicateX<URI> filter : this.whiteListFilter)
            if (filter.test(uri))
                return noProxyList();

        return this.delegate.select(uri);
    }

}
