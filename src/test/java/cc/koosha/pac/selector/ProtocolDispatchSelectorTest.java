package cc.koosha.pac.selector;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;


public class ProtocolDispatchSelectorTest {

    private final Proxy HTTP_TEST_PROXY  = new Proxy(Proxy.Type.HTTP,
            InetSocketAddress.createUnresolved("http_proxy.unit-test.invalid", 8090));
    private final Proxy HTTPS_TEST_PROXY = new Proxy(Proxy.Type.HTTP,
            InetSocketAddress.createUnresolved("https_proxy.unit-test.invalid", 8091));
    private final Proxy FTP_TEST_PROXY   = new Proxy(Proxy.Type.HTTP,
            InetSocketAddress.createUnresolved("ftp_proxy.unit-test.invalid", 8092));

    private final URI HTTP_TEST_URI  = URI.create("http://host1.unit-test.invalid/");
    private final URI HTTPS_TEST_URI = URI.create("https://host1.unit-test.invalid/");
    private final URI FTP_TEST_URI   = URI.create("ftp://host1.unit-test.invalid/");

    private ProtocolDispatchSelector ps;

    @BeforeClass
    public void setup() {

        this.ps = new ProtocolDispatchSelector();
        this.ps.setSelector("http", new FixedProxySelector(HTTP_TEST_PROXY));
        this.ps.setSelector("https", new FixedProxySelector(HTTPS_TEST_PROXY));
        this.ps.setSelector("ftp", new FixedProxySelector(FTP_TEST_PROXY));
    }

    @Test
    public void testDispatchHttp() {

        assertEquals(ps.select(HTTP_TEST_URI).get(0), HTTP_TEST_PROXY);
    }

    @Test
    public void testDispatchHttps() {

        assertEquals(ps.select(HTTPS_TEST_URI).get(0), HTTPS_TEST_PROXY);
    }

    @Test
    public void testDispatchFtp() {

        assertEquals(ps.select(FTP_TEST_URI).get(0), FTP_TEST_PROXY);
    }

    @Test
    public void testRemove() {

        final ProtocolDispatchSelector px       = new ProtocolDispatchSelector();
        final FixedProxySelector       selector = new FixedProxySelector(HTTP_TEST_PROXY);

        px.setSelector("http", selector);
        assertEquals(px.getSelector("http"), selector);

        px.removeSelector("http");
        assertNull(px.getSelector("http"));
    }

    @Test
    public void testFallback() {

        final ProtocolDispatchSelector px       = new ProtocolDispatchSelector();
        final FixedProxySelector       selector = new FixedProxySelector(HTTP_TEST_PROXY);
        px.setFallbackSelector(selector);

        assertEquals(px.select(HTTP_TEST_URI).get(0), HTTP_TEST_PROXY);
    }

}