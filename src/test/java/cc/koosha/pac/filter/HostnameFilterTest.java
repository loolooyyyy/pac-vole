package cc.koosha.pac.filter;

import cc.koosha.pac.filter.HostnameFilter.Mode;
import org.testng.annotations.Test;

import java.net.URI;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


public class HostnameFilterTest {

    @Test
    public void testBeginsWithFilter1() {

        test(
                Mode.BEGINS_WITH,
                "no_proxy",
                "http://no_proxy.unit-test.invalid/",
                true
        );
    }

    @Test
    public void testBeginsWithFilter2() {

        test(
                Mode.BEGINS_WITH,
                "no_proxy",
                "http://host1.unit-test.invalid/",
                false
        );
    }

    @Test
    public void testBeginsWithFilter3() {

        test(
                Mode.BEGINS_WITH,
                "192.168.0",
                "http://192.168.0.100:81/test.data",
                true
        );
    }

    @Test
    public void testBeginsWithFilter4() {

        test(
                Mode.BEGINS_WITH,
                "192.168.0",
                "http://192.168.1.100:81/test.data",
                false
        );
    }

    @Test
    public void testBeginsWithFilter() {

        test(
                Mode.BEGINS_WITH,
                "no_proxy",
                "http://no_proxy.unit-test.invalid/",
                true
        );

        test(
                Mode.BEGINS_WITH,
                "no_proxy",
                "http://host1.unit-test.invalid/",
                false
        );
    }

    @Test
    public void testEndsWithFilter() {
        test(
                Mode.ENDS_WITH,
                ".unit-test.invalid",
                "http://no_proxy.unit-test.invalid/",
                true
        );
    }

    @Test
    public void testEndsWithFilter2() {

        test(
                Mode.ENDS_WITH,
                ".unit-test.invalid",
                "http://test.no-host.invalid:81/test.data",
                false
        );
    }

    @Test
    public void testEndsWithFilter3() {

        test(
                Mode.ENDS_WITH,
                ".100",
                "http://192.168.1.100:81/test.data",
                true
        );

    }

    @Test
    public void testWithProtocolFilter() {

        test(
                Mode.BEGINS_WITH,
                "http://192.168.0.100",
                "http://192.168.0.100:81/test.data",
                true
        );

        test(
                Mode.BEGINS_WITH,
                "http://192.168.0.100",
                "ftp://192.168.0.100:81/test.data",
                false
        );

        test(
                Mode.BEGINS_WITH,
                "http://192.168.0.100",
                "http://192.168.1.100:81/test.data",
                false
        );
    }

    private void test(final Mode mode,
                      final String matchTo,
                      final String uri,
                      final boolean assertion) {

        final boolean test = new HostnameFilter(mode, matchTo).test(URI.create(uri));

        final String msg = "\nMode:\t" + mode + "\nmatchTo:\t" + matchTo +
                "\n" + "uri:\t" + uri + "\nexpected:\t" + assertion;

        if (assertion) {
            assertTrue(test, msg);
        }
        else {
            assertFalse(test, msg);
        }
    }
}