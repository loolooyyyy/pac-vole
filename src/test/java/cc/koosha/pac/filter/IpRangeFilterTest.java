package cc.koosha.pac.filter;

import org.testng.annotations.Test;

import java.net.URI;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


public class IpRangeFilterTest {

    @Test
    public void testIpRangeFilter() {

        test(
                "192.168.0.0/24",
                "http://192.168.0.100:81/test.data",
                true
        );

        test(
                "192.168.0.0/24",
                "http://192.168.1.100:81/test.data",
                false
        );
    }

    @SuppressWarnings("SameParameterValue")
    private void test(final String matchTo,
                      final String uri,
                      final boolean assertion) {

        final boolean test = new IpRangeFilter(matchTo).test(URI.create(uri));

        if (assertion) {
            assertTrue(test);
        }
        else {
            assertFalse(test);
        }
    }

}