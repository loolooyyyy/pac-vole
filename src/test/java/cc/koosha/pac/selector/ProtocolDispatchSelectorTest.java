package cc.koosha.pac.selector;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;


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

    private static ProtocolDispatchSelector getPs() {

        return new ProtocolDispatchSelector(new ProxySelector() {
            @Override
            public List<Proxy> select(final URI uri) {
                return null;
            }

            @Override
            public void connectFailed(final URI uri, final SocketAddress sa, final IOException ioe) {

            }
        });
    }

    @BeforeMethod
    public void setup() {

        this.ps = getPs();
        this.ps.setSelectorForProtocol("http", new FixedProxySelector(HTTP_TEST_PROXY));
        this.ps.setSelectorForProtocol("https", new FixedProxySelector(HTTPS_TEST_PROXY));
        this.ps.setSelectorForProtocol("ftp", new FixedProxySelector(FTP_TEST_PROXY));
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
    public void testRemove() throws NoSuchMethodException,
                                    InvocationTargetException,
                                    IllegalAccessException {

        final ProtocolDispatchSelector px       = getPs();
        final FixedProxySelector       selector = new FixedProxySelector(HTTP_TEST_PROXY);

        final Method get =
                px.getClass().getDeclaredMethod("_get", String.class);
        get.setAccessible(true);

        px.setSelectorForProtocol("http", selector);

        assertSame(get.invoke(px, "http"), selector);
        assertSame(px.removeSelectorForProtocol("http"), selector);
        assertEquals(px.size(), 0);
    }

    @Test
    public void testFallback() {

        final ProtocolDispatchSelector px       = getPs();
        final FixedProxySelector       selector = new FixedProxySelector(HTTP_TEST_PROXY);
        px.setFallbackSelector(selector);

        assertEquals(px.select(HTTP_TEST_URI).get(0), HTTP_TEST_PROXY);
    }

}