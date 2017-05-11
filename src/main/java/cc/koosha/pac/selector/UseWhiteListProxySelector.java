package cc.koosha.pac.selector;

import cc.koosha.pac.PredicateX;
import cc.koosha.pac.filter.FilterListParser;

import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;


/**
 * Special purpose ProxySelector used as Facade on top of a normal
 * ProxySelector. A wrapper that will first check the URI against a white list
 * and if it matches it will use a proxy as provided by the delegate
 * ProxySelector else it will return DIRECT.
 *
 * @author Koosha Hosseiny, Copyright 2017
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 */
public final class UseWhiteListProxySelector extends DelegatingProxySelector {

    private final List<PredicateX<URI>> filters;

    /**
     * @param whiteList     the whitelist to use.
     * @param proxySelector the proxy selector to use.
     */
    public UseWhiteListProxySelector(final ProxySelector proxySelector,
                                     final String whiteList) {

        super(proxySelector);

        if (whiteList == null)
            throw new NullPointerException("whitelist");

        this.filters = FilterListParser.parse(whiteList);
    }

    @Override
    protected List<Proxy> __select(final URI uri) {

        // If in white list, use proxy selector.
        for (final PredicateX<URI> filter : this.filters)
            if (filter.test(uri))
                return null;

        return noProxyList();
    }

}
