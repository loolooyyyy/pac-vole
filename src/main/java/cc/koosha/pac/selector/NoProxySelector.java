package cc.koosha.pac.selector;

import java.io.IOException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;


/**
 * This proxy selector will always return a "DIRECT" proxy.
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 */
public final class NoProxySelector extends AbstractProxySelector {

    public NoProxySelector() {
    }

    @Override
    public void connectFailed(final URI uri,
                              final SocketAddress sa,
                              final IOException ioe) {

        // Not used.
    }

    @Override
    public List<Proxy> select(final URI uri) {

        return noProxyList();
    }

}
