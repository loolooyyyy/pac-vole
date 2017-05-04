package cc.koosha.pac.selector;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This is a facade for a list of ProxySelector objects. Different
 * ProxySelectors per protocol can be registered.
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 */
public final class ProtocolDispatchSelector extends ProxySelector {

    private final Map<String, ProxySelector> selectors = new ConcurrentHashMap<>();

    private ProxySelector fallbackSelector = new NoProxySelector();

    /**
     * Sets a selector responsible for the given protocol.
     *
     * @param protocol the name of the protocol.
     * @param selector the selector to use.
     */
    public void setSelector(final String protocol, final ProxySelector selector) {

        if (protocol == null)
            throw new NullPointerException("protocol");

        if (selector == null)
            throw new NullPointerException("selector");

        this.selectors.put(protocol, selector);
    }

    /**
     * Removes the selector installed for the given protocol.
     *
     * @param protocol the protocol name.
     *
     * @return the old selector that is removed.
     */
    public ProxySelector removeSelector(final String protocol) {

        return this.selectors.remove(protocol);
    }

    /**
     * Gets the selector installed for the given protocol.
     *
     * @param protocol the protocol name.
     *
     * @return the selector for that protocol, null if none is currently set.
     */
    public ProxySelector getSelector(final String protocol) {

        return this.selectors.get(protocol);
    }

    /**
     * Sets the fallback selector that is always called when no matching
     * protocol selector was found..
     *
     * @param selector the selector to use.
     */
    public void setFallbackSelector(final ProxySelector selector) {

        if (selector == null)
            throw new NullPointerException("selector");

        this.fallbackSelector = selector;
    }

    // ________________________________________________________________________

    @Override
    public void connectFailed(final URI uri,
                              final SocketAddress sa,
                              final IOException ioe) {

        final String protocol = uri.getScheme();

        if (protocol != null && this.selectors.get(protocol) != null)
            this.selectors.get(protocol).connectFailed(uri, sa, ioe);
        else
            this.fallbackSelector.connectFailed(uri, sa, ioe);
    }

    @Override
    public List<Proxy> select(final URI uri) {

        final String protocol = uri.getScheme();

        return protocol != null && this.selectors.get(protocol) != null
               ? this.selectors.get(protocol).select(uri)
               : this.fallbackSelector.select(uri);
    }

    /**
     * Gets the size of the selector map.
     *
     * @return the size of the selector map.
     */
    public int size() {

        return this.selectors.size();
    }

}
