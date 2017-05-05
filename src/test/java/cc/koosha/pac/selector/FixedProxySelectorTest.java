package cc.koosha.pac.selector;

import org.testng.annotations.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;

import static org.testng.Assert.assertEquals;


public class FixedProxySelectorTest {

    private final Proxy HTTP_TEST_PROXY = new Proxy(Proxy.Type.HTTP,
            InetSocketAddress.createUnresolved("http_proxy.unit-test.invalid", 8090));

    @Test
    public void testFixedProxy0() {

        final Proxy proxy = new FixedProxySelector(HTTP_TEST_PROXY)
                .select(URI.create("http://host1.unit-test.invalid/"))
                .get(0);

        assertEquals(proxy, HTTP_TEST_PROXY);
    }

    @Test
    public void testFixedProxy1() {

        final Proxy proxy = new FixedProxySelector(HTTP_TEST_PROXY)
                .select(URI.create("http://host1.unit-test.invalid/"))
                .get(0);

        assertEquals(proxy, HTTP_TEST_PROXY);
    }

}