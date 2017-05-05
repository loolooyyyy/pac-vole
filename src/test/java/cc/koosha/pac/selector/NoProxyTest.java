package cc.koosha.pac.selector;

import org.testng.annotations.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;

import static org.testng.Assert.assertEquals;


public class NoProxyTest {

    private final Proxy HTTP_TEST_PROXY = new Proxy(Proxy.Type.HTTP,
            InetSocketAddress.createUnresolved("http_proxy.unit-test.invalid", 8090));

    private static final URI HTTP_TEST_URI = URI.create("http://host1.unit-test.invalid/");

    @Test
    public void testWhiteList() {

        final BypassListProxySelector ps = new BypassListProxySelector(
                "no_prox.*",
                new FixedProxySelector(HTTP_TEST_PROXY)
        );

        assertEquals(
                new FixedProxySelector(HTTP_TEST_PROXY).select(HTTP_TEST_URI)
                                                       .get(0),
                ps.select(HTTP_TEST_URI).get(0)
        );

        final BypassListProxySelector ps1 = new BypassListProxySelector(
                "no_proxy.*",
                new FixedProxySelector(HTTP_TEST_PROXY)
        );

        assertEquals(
                new FixedProxySelector(HTTP_TEST_PROXY).select(HTTP_TEST_URI)
                                                       .get(0),
                ps1.select(HTTP_TEST_URI).get(0)
        );

    }

    @Test
    public void testWhiteList2() {

        final BypassListProxySelector ps = new BypassListProxySelector(
                "*.unit-test.invalid",
                new FixedProxySelector(HTTP_TEST_PROXY)
        );

        assertEquals(ps.select(HTTP_TEST_URI).get(0), Proxy.NO_PROXY);
    }

    @Test
    public void testWhiteList3() {

        final String u0 = "http://localhost:65/getDocument";
        final String u1 = "http://127.0.0.1:65/getDocument";

        final BypassListProxySelector ps = new BypassListProxySelector(
                "*.unit-test.invalid, localhost, 127.0.0.1",
                new FixedProxySelector(HTTP_TEST_PROXY)
        );

        assertEquals(ps.select(URI.create(u0)).get(0), Proxy.NO_PROXY);
        assertEquals(ps.select(URI.create(u1)).get(0), Proxy.NO_PROXY);
    }

    @Test
    public void testWhiteList4() {

        final BypassListProxySelector ps = new BypassListProxySelector(
                "*.unit-test.invalid, ",
                new FixedProxySelector(HTTP_TEST_PROXY)
        );

        assertEquals(ps.select(HTTP_TEST_URI).get(0), Proxy.NO_PROXY);
    }

    @Test
    public void testWhiteList5() throws URISyntaxException {

        final String u0 = "http://localhost:65/getDocument";
        final String u1 = "http://127.0.0.1:65/getDocument";

        final BypassListProxySelector ps = new BypassListProxySelector(
                "*.unit-test.invalid localhost 127.0.0.1",
                new FixedProxySelector(HTTP_TEST_PROXY)
        );

        assertEquals(ps.select(new URI(u0)).get(0), Proxy.NO_PROXY);
        assertEquals(ps.select(new URI(u1)).get(0), Proxy.NO_PROXY);
    }

    @Test
    public void testIpRange() throws URISyntaxException {

        final String u0 = "http://192.168.0.100:81/test.data";
        final String u1 = "http://192.168.1.100:81/test.data";

        final BypassListProxySelector ps = new BypassListProxySelector(
                "192.168.0.0/24",
                new FixedProxySelector(HTTP_TEST_PROXY)
        );

        assertEquals(Proxy.NO_PROXY, ps.select(new URI(u0)).get(0));
        assertEquals(
                new FixedProxySelector(HTTP_TEST_PROXY).select(HTTP_TEST_URI)
                                                       .get(0),
                ps.select(new URI(u1)).get(0)
        );
    }

    @Test
    public void ipRangeShouldNotMatchHttp() {

        final String u0 = "http://192.168.0.100:81/test.data";

        final BypassListProxySelector ps = new BypassListProxySelector(
                "http://192.*",
                new FixedProxySelector(HTTP_TEST_PROXY)
        );

        assertEquals(ps.select(URI.create(u0)).get(0), Proxy.NO_PROXY);
    }

}
