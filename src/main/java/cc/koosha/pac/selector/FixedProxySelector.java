package cc.koosha.pac.selector;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.util.Collections;
import java.util.List;


/**
 * This proxy selector is configured with a fixed proxy. This proxy will be
 * returned for all URIs passed to the select method.
 *
 * @author Koosha Hosseiny, Copyright 2017
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 */
public class FixedProxySelector extends EProxySelector {

    private final List<Proxy> proxyList;

    /**
     * @param proxy the proxy to use.
     */
    public FixedProxySelector(final Proxy proxy) {

        this.proxyList = Collections.singletonList(proxy);
    }

    public FixedProxySelector(final Proxy.Type proxyType,
                              final String host,
                              final int port) {

        this(new Proxy(proxyType, InetSocketAddress.createUnresolved(host, port)));
    }

    @Override
    protected final List<Proxy> _select(final URI uri) {

        return this.proxyList;
    }

}
