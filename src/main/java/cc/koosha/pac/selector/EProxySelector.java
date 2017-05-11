package cc.koosha.pac.selector;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;


/**
 * Extended ProxySelector
 *
 * @author Koosha Hosseiny, Copyright 2017
 */
public abstract class EProxySelector extends ProxySelector {

    private static final List<Proxy> noProxyList =
            Collections.singletonList(Proxy.NO_PROXY);

    private volatile boolean enabled = true;

    protected EProxySelector() {

    }

    /**
     * Can be used to enable / disable the proxy selector. If disabled it will
     * return DIRECT for all urls.
     *
     * @param enable the new status to set.
     */
    public final void setEnabled(boolean enable) {

        this.enabled = enable;
    }

    /**
     * Checks if the selector is currently enabled.
     *
     * @return true if enabled else false.
     */
    public final boolean isEnabled() {

        return this.enabled;
    }

    @Override
    public void connectFailed(final URI uri, final SocketAddress sa, final IOException ioe) {

        // Stub do nothing.
    }

    @Override
    public final List<Proxy> select(final URI uri) {

        if (uri == null)
            throw new NullPointerException("uri");

        // Fix for Java 1.6.16+ where we get a infinite loop because
        // URL.connect(Proxy.NO_PROXY) does not work as expected.
        if (!enabled)
            return noProxyList();

        final List<Proxy> proxies = this._select(uri);

        return proxies;
    }

    protected abstract List<Proxy> _select(URI uri);

    /**
     * Checks if the given proxy is representing a direct connection.
     *
     * @param proxy to inspect.
     *
     * @return true if it is direct else false.
     */
    protected final boolean isDirect(final Proxy proxy) {

        return Proxy.NO_PROXY.equals(proxy);
    }

    /**
     * Gets an unmodifiable proxy list that will have as it's only entry an
     * DIRECT proxy.
     *
     * @return a list with a DIRECT proxy in it.
     */
    protected List<Proxy> noProxyList() {

        return noProxyList;
    }

}
