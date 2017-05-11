package cc.koosha.pac.selector;


import cc.koosha.pac.filter.FilterListParser;
import cc.koosha.pac.PredicateX;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.List;

import static org.testng.Assert.*;


public class FilterListParserTest {

    private static List<PredicateX<URI>> parse(final String whitelist) {

        return FilterListParser.parse(whitelist);
    }

    @Test
    public void shouldAllowAllPrefix() {

        final String whitelist = "*.mynet.com";
        final String u0        = "http://rossi.mynet.com";
        final String u1        = "http://rossi.mynet.com.test";

        final PredicateX<URI> filter = parse(whitelist).get(0);

        assertTrue(filter.test(URI.create(u0)));
        assertFalse(filter.test(URI.create(u1)));
    }

    @Test
    public void shouldAllowAllPostfix() {

        final String whitelist = "mynet.*";

        final String u0 = "http://rossi.mynet.com";
        final String u1 = "http://mynet.junit.test";

        final PredicateX<URI> filter = parse(whitelist).get(0);

        assertFalse(filter.test(URI.create(u0)));
        assertTrue(filter.test(URI.create(u1)));
    }

    @Test
    public void shouldSplitMultipleEntries() {

        final String whitelist = "*.mynet.com; *.rossi.invalid; junit*";
        assertEquals(3, parse(whitelist).size());
    }

    @Test
    public void shouldAllowIpRange() {

        final String whitelist = "192.168.0.0/24";

        final String u0 = "http://192.168.0.1";
        final String u1 = "http://192.168.0.11";
        final String u2 = "http://rossi.mynet.com";
        final String u3 = "http://145.5.5.1";

        final PredicateX<URI> filter = parse(whitelist).get(0);

        assertTrue(filter.test(URI.create(u0)));
        assertTrue(filter.test(URI.create(u1)));
        assertFalse(filter.test(URI.create(u2)));
        assertFalse(filter.test(URI.create(u3)));
    }

    @Test
    public void shouldHandleInvalidWithoutException() {

        final String whitelist = "http://10.*.*.*";

        final String u0 = "http://10.0.0.1";

        final PredicateX<URI> filter = parse(whitelist).get(0);

        assertFalse(filter.test(URI.create(u0)));
    }

    //
    //
    // .mynet.com - Filters all host names ending with .mynet.com
    // * *.mynet.com - Filters all host names ending with .mynet.com
    // * www.mynet.* - Filters all host names starting with www.mynet.
    // * 123.12.32.1 - Filters the IP 123.12.32.1
    // * 123.12.32.1/255 - Filters the IP range
    // * http://www.mynet.com
    //

}
