package cc.koosha.pac.selector;

import cc.koosha.pac.ProxyEvaluationException;
import cc.koosha.pac.func.StringProvider;
import cc.koosha.pac.pac.JavaxPacScriptParser;
import cc.koosha.pac.pac.PacScriptParser;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


/*****************************************************************************
 * ProxySelector that will use a PAC script to find an proxy for a given URI.
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/
public final class PacProxySelector extends AbstractProxySelector {

    public static final int DEFAULT_PROXY_PORT = 80;

    // private static final String PAC_PROXY = "PROXY";
    private static final String PAC_SOCKS = "SOCKS";
    private static final String PAC_DIRECT = "DIRECT";

    private PacScriptParser pacScriptParser;

    private static volatile boolean enabled = true;

    public PacProxySelector(final StringProvider pacSource) throws ProxyEvaluationException {

        this.pacScriptParser = new JavaxPacScriptParser(pacSource);
    }

    public PacProxySelector(final PacScriptParser pacScriptParser) {

        this.pacScriptParser = pacScriptParser;
    }

    /*************************************************************************
     * Can be used to enable / disable the proxy selector. If disabled it will
     * return DIRECT for all urls.
     *
     * @param enable
     *            the new status to set.
     ************************************************************************/
    public static void setEnabled(boolean enable) {
        enabled = enable;
    }

    /*************************************************************************
     * Checks if the selector is currently enabled.
     *
     * @return true if enabled else false.
     ************************************************************************/
    public static boolean isEnabled() {
        return enabled;
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {

        // Not used.
    }

    @Override
    public List<Proxy> select(final URI uri) {

        if (uri == null)
            throw new NullPointerException("uri");

        // Fix for Java 1.6.16+ where we get a infinite loop because
        // URL.connect(Proxy.NO_PROXY) does not work as expected.
        if (!enabled)
            return noProxyList();

        try {
            final String parseResult = this.pacScriptParser.evaluate(
                    uri.toString(), uri.getHost());

            final String[] proxyDefinitions = parseResult == null
                                              ? PAC_DIRECT.split("[;]")
                                              : parseResult.split("[;]");

            final List<Proxy> proxies = new ArrayList<>(proxyDefinitions.length);
            for (final String proxyDef : proxyDefinitions)
                if (!proxyDef.trim().isEmpty())
                    proxies.add(buildProxyFromPacResult(proxyDef));

            return proxies;
        }
        catch (final ProxyEvaluationException e) {
            return noProxyList();
        }
    }

    /*************************************************************************
     * The proxy evaluator will return a proxy string. This method will take
     * this string and build a matching <code>Proxy</code> for it.
     *
     * @param pacResult
     *            the result from the PAC parser.
     * @return a Proxy
     ************************************************************************/
    private Proxy buildProxyFromPacResult(String pacResult) {
        if (pacResult.trim().length() < 6)
            return Proxy.NO_PROXY;

        String proxyDef = pacResult.trim();
        if (proxyDef.toUpperCase().startsWith(PAC_DIRECT))
            return Proxy.NO_PROXY;

        // Check proxy type.
        final Proxy.Type type = proxyDef.toUpperCase().startsWith(PAC_SOCKS)
                                ? Proxy.Type.SOCKS
                                : Proxy.Type.HTTP;

        String host = proxyDef.substring(6);
        Integer port = DEFAULT_PROXY_PORT;

        // Split port from host
        final int indexOfPort = host.indexOf(':');
        final int index2 = host.lastIndexOf(']');
        if (indexOfPort != -1 && index2 < indexOfPort) {
            port = Integer.parseInt(host.substring(indexOfPort + 1).trim());
            host = host.substring(0, indexOfPort).trim();
        }

        final SocketAddress adr = InetSocketAddress.createUnresolved(host, port);
        return new Proxy(type, adr);
    }

}
