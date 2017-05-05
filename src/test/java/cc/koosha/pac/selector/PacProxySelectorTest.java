package cc.koosha.pac.selector;

import cc.koosha.pac.pac.NetRequest;
import cc.koosha.pac.ProxyEvaluationException;
import cc.koosha.pac.ProxyException;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URI;
import java.util.List;

import static cc.koosha.pac.pac.JavaxPacScriptParserTest.provider;
import static org.testng.Assert.assertEquals;


public class PacProxySelectorTest {

    private final Proxy HTTP_TEST_PROXY  = new Proxy(Proxy.Type.HTTP,
            InetSocketAddress.createUnresolved("http_proxy.unit-test.invalid", 8090));
    private final Proxy HTTPS_TEST_PROXY = new Proxy(Proxy.Type.HTTP,
            InetSocketAddress.createUnresolved("https_proxy.unit-test.invalid", 8091));
    private final Proxy FTP_TEST_PROXY   = new Proxy(Proxy.Type.HTTP,
            InetSocketAddress.createUnresolved("ftp_proxy.unit-test.invalid", 8092));

    private final URI HTTP_TEST_URI  = URI.create("http://host1.unit-test.invalid/");
    private final URI HTTPS_TEST_URI = URI.create("https://host1.unit-test.invalid/");
    private final URI FTP_TEST_URI   = URI.create("ftp://host1.unit-test.invalid/");

    @Test
    public void testScriptExecution() throws ProxyEvaluationException {

        List<Proxy> result = new PacProxySelector(provider("exec"))
                .select(HTTP_TEST_URI);

        assertEquals(HTTP_TEST_PROXY, result.get(0));
    }

    @Test
    public void testScriptExecution2() throws ProxyException, MalformedURLException {

        final PacProxySelector pacProxySelector = new PacProxySelector(provider("comment"));
        assertEquals(Proxy.NO_PROXY, pacProxySelector.select(HTTP_TEST_URI)
                                                     .get(0));
        assertEquals(Proxy.NO_PROXY, pacProxySelector.select(HTTPS_TEST_URI)
                                                     .get(0));
    }

    @Test
    public void testScriptMuliProxy() throws ProxyEvaluationException {

        final PacProxySelector pacProxySelector = new PacProxySelector(provider("multiProxy"));
        final List<Proxy>      result           = pacProxySelector.select(HTTP_TEST_URI);

        int size = result.size();

        final Proxy p0 = result.get(0);
        final Proxy p1 = result.get(1);

        assertEquals(2, size);
        assertEquals(new Proxy(Type.HTTP, InetSocketAddress.createUnresolved("my-proxy.com", 80)), p0);
        assertEquals(new Proxy(Type.HTTP, InetSocketAddress.createUnresolved("my-proxy2.com", 8080)), p1);
    }

    @Test
    public void testLocalIPOverride() throws ProxyEvaluationException {

        final String ip = "123.123.123.123";

        final String old =
                System.getProperty(NetRequest.OVERRIDE_LOCAL_IP, "__WAS_NOT_SET__");

        System.setProperty(NetRequest.OVERRIDE_LOCAL_IP, ip);

        try {
            final PacProxySelector pacProxySelector = new PacProxySelector(provider("localIp"));

            final Proxy result = pacProxySelector.select(HTTP_TEST_URI).get(0);

            assertEquals(result, new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(ip, 8080)));
        }
        finally {
            if (!"__WAS_NOT_SET__".equals(old))
                System.setProperty(NetRequest.OVERRIDE_LOCAL_IP, old);
        }
    }

}
