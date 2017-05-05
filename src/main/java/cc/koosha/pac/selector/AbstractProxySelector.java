package cc.koosha.pac.selector;

import java.net.Proxy;
import java.net.ProxySelector;
import java.util.Collections;
import java.util.List;


abstract class AbstractProxySelector extends ProxySelector {

    private static final List<Proxy> noProxyList =
            Collections.singletonList(Proxy.NO_PROXY);

    protected AbstractProxySelector() {

    }

    /**
     * Gets an unmodifiable proxy list that will have as it's only entry an
     * DIRECT proxy.
     *
     * @return a list with a DIRECT proxy in it.
     */
    protected static List<Proxy> noProxyList() {

        return noProxyList;
    }

}
