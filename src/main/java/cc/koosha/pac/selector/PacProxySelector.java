package cc.koosha.pac.selector;

import cc.koosha.pac.ProxyEvaluationException;
import cc.koosha.pac.StringProvider;
import cc.koosha.pac.pac.JavaxPacScriptParser;
import cc.koosha.pac.pac.PacScriptParser;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


/**
 * ProxySelector that will use a PAC script to find an proxy for a given URI.
 *
 * @author Koosha Hosseiny, Copyright 2017
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 */
public final class PacProxySelector extends EProxySelector {

    public static final int DEFAULT_PROXY_PORT = 80;

    // private static final String PAC_PROXY = "PROXY";
    private static final String PAC_SOCKS  = "SOCKS";
    private static final String PAC_DIRECT = "DIRECT";

    private PacScriptParser pacScriptParser;

    public PacProxySelector(final StringProvider pacSource) throws ProxyEvaluationException {

        this(new JavaxPacScriptParser(pacSource));
    }

    public PacProxySelector(final PacScriptParser pacScriptParser) {

        this.pacScriptParser = pacScriptParser;
    }

    @Override
    protected List<Proxy> _select(final URI uri) {

        final String parseResult;

        try {
            parseResult = this.pacScriptParser.evaluate(uri.toString(), uri.getHost());
        }
        catch (final ProxyEvaluationException e) {
            return noProxyList();
        }

        final String[] proxyDefinitions = parseResult == null
                                          ? PAC_DIRECT.split("[;]")
                                          : parseResult.split("[;]");

        final List<Proxy> proxies = new ArrayList<>(proxyDefinitions.length);
        for (final String proxyDef : proxyDefinitions)
            if (!proxyDef.trim().isEmpty())
                proxies.add(buildProxyFromPacResult(proxyDef));

        return proxies;
    }

    /**
     * The proxy evaluator will return a proxy string. This method will take
     * this string and build a matching <code>Proxy</code> for it.
     *
     * @param pacResult the result from the PAC parser.
     *
     * @return a Proxy
     **/
    private Proxy buildProxyFromPacResult(final String pacResult) {

        if (pacResult.trim().length() < 6)
            return Proxy.NO_PROXY;

        final String proxyDef = pacResult.trim();
        if (proxyDef.toUpperCase().startsWith(PAC_DIRECT))
            return Proxy.NO_PROXY;

        // Check proxy type.
        final Proxy.Type type = proxyDef.toUpperCase().startsWith(PAC_SOCKS)
                                ? Proxy.Type.SOCKS
                                : Proxy.Type.HTTP;

        String  host = proxyDef.substring(6);
        Integer port = DEFAULT_PROXY_PORT;

        // Split port from host
        final int indexOfPort = host.indexOf(':');
        final int index2      = host.lastIndexOf(']');
        if (indexOfPort != -1 && index2 < indexOfPort) {
            port = Integer.parseInt(host.substring(indexOfPort + 1).trim());
            host = host.substring(0, indexOfPort).trim();
        }

        final SocketAddress adr = InetSocketAddress.createUnresolved(host, port);
        return new Proxy(type, adr);
    }

}
