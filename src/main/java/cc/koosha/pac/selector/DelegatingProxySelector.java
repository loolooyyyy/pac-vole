package cc.koosha.pac.selector;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;


public abstract class DelegatingProxySelector extends EProxySelector {

    private final ProxySelector delegate;

    protected DelegatingProxySelector(final ProxySelector delegate) {

        if (delegate == null)
            throw new NullPointerException("delegate proxy selector");

        this.delegate = delegate;
    }

    protected final ProxySelector getDelegate() {

        return this.delegate;
    }

    @Override
    public void connectFailed(final URI uri, final SocketAddress sa, final IOException ioe) {

        this.getDelegate().connectFailed(uri, sa, ioe);
    }

    @Override
    protected final List<Proxy> _select(final URI uri) {

        final List<Proxy> proxies = this.__select(uri);
        return proxies == null ? getDelegate().select(uri) : proxies;
    }

    abstract protected List<Proxy> __select(URI uri);

}
