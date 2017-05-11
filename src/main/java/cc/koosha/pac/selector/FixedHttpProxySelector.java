package cc.koosha.pac.selector;

import java.net.Proxy;


/**
 * This proxy selector is configured with a fixed proxy. This proxy will be
 * returned for all URIs passed to the select method. This implementation can be
 * used for HTTP proxy support.
 *
 * @author Koosha Hosseiny, Copyright 2017
 */
public final class FixedHttpProxySelector extends FixedProxySelector {

    /**
     * @param proxyHost the host name or IP address of the proxy to use.
     * @param proxyPort the port of the proxy.
     */
    public FixedHttpProxySelector(final String proxyHost, final int proxyPort) {

        super(Proxy.Type.HTTP, proxyHost, proxyPort);
    }

}
