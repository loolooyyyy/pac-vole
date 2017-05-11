package cc.koosha.pac.selector;

import java.net.Proxy;
import java.net.URI;
import java.util.List;


/**
 * This proxy selector will always return a "DIRECT" proxy.
 *
 * @author Koosha Hosseiny, Copyright 2017
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 */
public final class NoProxySelector extends EProxySelector {

    public NoProxySelector() {
    }

    @Override
    protected List<Proxy> _select(final URI uri) {

        return noProxyList();
    }

}
