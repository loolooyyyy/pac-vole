package cc.koosha.pac.selector;

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
 * and if it matches it will use a proxy as provided by the delegate
 * ProxySelector else it will return DIRECT.
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 */
public final class UseWhiteListProxySelector extends AbstractProxySelector {

    private final ProxySelector delegate;
    private final List<PredicateX<URI>> whiteListFilter;

    /**
     * @param whiteList     the whitelist to use.
     * @param proxySelector the proxy selector to use.
     */
    public UseWhiteListProxySelector(final String whiteList,
                                     final ProxySelector proxySelector) {
        if (whiteList == null)
            throw new NullPointerException("whitelist");
        if (proxySelector == null)
            throw new NullPointerException("proxySelector");

        this.delegate = proxySelector;
        this.whiteListFilter = DefaultWhiteListParser.parse(whiteList);
    }

    @Override
    public void connectFailed(final URI uri,
                              final SocketAddress sa,
                              final IOException ioe) {

        this.delegate.connectFailed(uri, sa, ioe);
    }

    @Override
    public List<Proxy> select(final URI uri) {

        // If in white list, use proxy selector.
        for (final PredicateX<URI> filter : this.whiteListFilter)
            if (filter.test(uri))
                return this.delegate.select(uri);

        return noProxyList();
    }

}
