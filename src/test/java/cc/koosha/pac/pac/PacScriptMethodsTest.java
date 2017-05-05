package cc.koosha.pac.pac;

import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Calendar;

import static org.testng.Assert.*;


/**
 * Tests for the global PAC script methods that are used as context inside of
 * the scripts.
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 */
public class PacScriptMethodsTest {

    private final URI HTTP_TEST_URI = URI.create("http://host1.unit-test.invalid/");

    /**
     * Get a methods implementation with a calendar for date and time base tests
     * set to a hardcoded data. Current date for all tests is: 15. December 1994
     * 12:00.00 its a Thursday
     */
    private DefaultPacScriptMethods buildParser() {

        final DefaultPacScriptMethods result =
                new DefaultPacScriptMethods(new DefaultNetRequest());

        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 1994);
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 15);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        try {
            final Field time =
                    result.getClass().getDeclaredField("currentTime");
            time.setAccessible(true);
            time.set(result, cal);
        }
        catch (final IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @Test
    public void testDnsDomainIs() {

        assertTrue(buildParser().dnsDomainIs(
                "host1.unit-test.invalid", "unit-test.invalid"));
    }

    @Test
    public void testDnsDomainLevels() {

        assertEquals(2, buildParser().dnsDomainLevels(
                HTTP_TEST_URI.toString()));
    }

    @Test
    public void testDnsResolve() throws UnknownHostException {

        final InetAddress adr = Inet4Address.getLocalHost();
        assertEquals(adr.getHostAddress(), buildParser().dnsResolve(adr.getHostName()));
    }

    @Test
    public void testIsInNet() {

        assertTrue(buildParser().isInNet(
                "192.168.0.122", "192.168.0.0", "255.255.255.0"));
    }

    @Test
    public void testIsInNet2() {

        assertTrue(buildParser().isInNet(
                "10.13.75.47", "10.13.72.0", "255.255.252.0"));
    }

    @Test
    public void testIsInNetExForIP4() {

        // isInNetEx(host, "198.95.249.79/32");
        // true if the IP address of host matches exactly 198.95.249.79
        assertTrue(buildParser().isInNetEx(
                "198.95.249.79", "198.95.249.79/32"));

        assertFalse(buildParser().isInNetEx(
                "198.95.249.80", "198.95.249.79/32"));

        // isInNetEx(host, "198.95.0.0/16");
        // true if the IP address of the host matches 198.95.*.*
        assertTrue(buildParser().isInNetEx(
                "198.95.249.79", "198.95.0.0/16"));
        assertTrue(buildParser().isInNetEx(
                "198.95.249.80", "198.95.0.0/16"));

        assertFalse(buildParser().isInNetEx(
                "198.96.249.80", "198.95.0.0/16"));
    }

    @Test
    public void testIsPlainHostName() {

        assertFalse(buildParser().isPlainHostName("host1.unit-test.invalid"));
        assertTrue(buildParser().isPlainHostName("host1"));
    }

    @Test
    public void testIsResolvable() throws UnknownHostException {

        final InetAddress adr = Inet4Address.getLocalHost();
        assertTrue(buildParser().isResolvable(adr.getHostName()));
    }

    @Test
    public void testLocalHostOrDomainIs() {

        assertTrue(buildParser().localHostOrDomainIs(
                "host1.unit-test.invalid", "host1.unit-test.invalid"));
    }

    @Test
    public void testMyIpAddress() {

        final String myIP = buildParser().myIpAddress();
        assertFalse("127.0.0.1".equals(myIP));
        assertFalse("".equals(myIP));
        assertNotNull(myIP);
    }

    @Test
    public void testShExpMatch() {

        assertTrue(buildParser().shExpMatch("host1.unit-test.invalid", "host1.unit-test.*"));
        assertTrue(buildParser().shExpMatch("host1.unit-test.invalid", "*.unit-test.invalid"));
        assertTrue(buildParser().shExpMatch("host1.unit-test.invalid", "*.unit*.invalid"));

        assertFalse(buildParser().shExpMatch("202.310.65.6", "10.*"));
        assertFalse(buildParser().shExpMatch("202.310.65.6", "*.65"));
    }

    @Test
    public void testWeekdayRange() {

        assertTrue(buildParser().weekdayRange("MON", "SUN", "GMT"));
        assertTrue(buildParser().weekdayRange("SUN", "SAT", null));

        assertFalse(buildParser().weekdayRange("MON", "WED", null));
    }

    @Test
    public void testDateRange() {

        assertTrue(buildParser().dateRange(15, "undefined", "undefined",
                "undefined", "undefined", "undefined", "undefined"));

        assertTrue(buildParser().dateRange(15, "DEC", "undefined", "undefined",
                "undefined", "undefined", "undefined"));

        assertTrue(buildParser().dateRange(15, "DEC", 1994, "undefined",
                "undefined", "undefined", "undefined"));

        assertTrue(buildParser().dateRange(15, 17, "undefined", "undefined",
                "undefined", "undefined", "undefined"));

        assertTrue(buildParser().dateRange("OCT", "JAN", "undefined",
                "undefined", "undefined", "undefined", "undefined"));

        assertTrue(buildParser().dateRange(1994, 1994, "undefined",
                "undefined", "undefined", "undefined", "undefined"));

        assertTrue(buildParser().dateRange(1, "DEC", 1994, 1, "JAN", 1995, "GTM"));

        assertFalse(buildParser().dateRange(16, "DEC", 1994, 1, "JAN", 1995, "GTM"));
    }

    @Test
    public void testTimeRange() {

        assertTrue(buildParser().timeRange(12, "undefined", "undefined",
                "undefined", "undefined", "undefined", "undefined"));

        assertTrue(buildParser().timeRange(11, 13, "undefined", "undefined",
                "undefined", "undefined", "undefined"));

        assertTrue(buildParser().timeRange(11, 13, "gmt", "undefined",
                "undefined", "undefined", "undefined"));

        assertTrue(buildParser().timeRange(11, 30, 13, 30, "undefined",
                "undefined", "undefined"));

        assertTrue(buildParser().timeRange(11, 30, 15, 13, 30, 15, "undefined"));

        assertTrue(buildParser().timeRange(11, 30, 15, 13, 30, 15, "GMT"));

        assertFalse(buildParser().timeRange(12, 50, 0, 9, 30, 0, "GMT"));
    }

}
