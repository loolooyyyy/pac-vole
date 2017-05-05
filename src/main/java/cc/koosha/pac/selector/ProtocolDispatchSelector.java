package cc.koosha.pac.selector;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This is a facade for a list of ProxySelector objects. Different
 * ProxySelectors per protocol can be registered.
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 */
public final class ProtocolDispatchSelector extends AbstractProxySelector {

    private final Map<String, ProxySelector> selectors = new HashMap<>();

    private ProxySelector fallbackSelector = new NoProxySelector();

    private ProxySelector _get(final String protocol) {

        if (protocol == null)
            return this.fallbackSelector;

        synchronized (this.selectors) {
            final ProxySelector selector = this.selectors.get(protocol);
            return selector == null ? this.fallbackSelector : selector;
        }
    }

    private ProxySelector _remove(final String protocol) {

        synchronized (this.selectors) {
            return this.selectors.remove(protocol);
        }
    }

    private void _add(final String protocol,
                      final ProxySelector selector) {

        synchronized (this.selectors) {
            this.selectors.put(protocol, selector);
        }
    }

    public ProtocolDispatchSelector(final ProxySelector fallbackSelector) {

        if(fallbackSelector == null)
            throw new NullPointerException("fallbackSelector");

        this.fallbackSelector = fallbackSelector;
    }


    /**
     * Sets a selector responsible for the given protocol.
     *
     * @param protocol the name of the protocol.
     * @param selector the selector to use.
     */
    public void setSelectorForProtocol(final String protocol, final ProxySelector selector) {

        if (protocol == null)
            throw new NullPointerException("protocol");

        if (selector == null)
            throw new NullPointerException("selector");

        this._add(protocol, selector);
    }

    /**
     * Removes the selector installed for the given protocol.
     *
     * @param protocol the protocol name.
     *
     * @return the old selector that is removed.
     */
    public ProxySelector removeSelectorForProtocol(final String protocol) {

        return this._remove(protocol);
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

        _get(uri.getScheme()).connectFailed(uri, sa, ioe);
    }

    @Override
    public List<Proxy> select(final URI uri) {

        return _get(uri.getScheme()).select(uri);
    }

    /**
     * Gets the size of the selector map.
     *
     * @return the size of the selector map.
     */
    public int size() {

        synchronized (this.selectors) {
            return this.selectors.size();
        }
    }

}
