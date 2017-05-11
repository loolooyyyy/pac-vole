package cc.koosha.pac.selector;

import java.net.Proxy;


/**
 * This proxy selector is configured with a fixed proxy. This proxy will be
 * returned for all URIs passed to the select method. This implementation can be
 * used for SOCKS 4 and 5 proxy support.
 *
 * @author Koosha Hosseiny, Copyright 2017
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 */
public final class FixedSocksProxySelector extends FixedProxySelector {

    /**
     * @param proxyHost the host name or IP address of the proxy to use.
     * @param proxyPort the port of the proxy.
     */
    public FixedSocksProxySelector(final String proxyHost, final int proxyPort) {

        super(Proxy.Type.SOCKS, proxyHost, proxyPort);
    }

}
