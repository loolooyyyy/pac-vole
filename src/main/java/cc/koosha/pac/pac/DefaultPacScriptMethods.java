package cc.koosha.pac.pac;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.*;


/**
 * Implementation of PAC JavaScript functions.
 * <p>
 * This class does not make any external network request by itself, but the
 * {@link NetRequest} object provided to it may.
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 */
public final class DefaultPacScriptMethods implements ScriptMethods {

    private static final String VERSION = "1.0";

    private static final BigInteger HIGH_32_INT =
            new BigInteger(new byte[]{-1, -1, -1, -1});

    private static final BigInteger HIGH_128_INT = new BigInteger(new byte[]{
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            });

    private final static String GMT = "GMT";

    private final static List<String> DAYS = Collections.unmodifiableList(
            Arrays.asList(
                    "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"));

    private final static List<String> MONTH = Collections.unmodifiableList(
            Arrays.asList(
                    "JAN", "FEB", "MAR", "APR",
                    "MAY", "JUN", "JUL", "AUG",
                    "SEP", "OCT", "NOV", "DEC"));

    private final Comparator<byte[]> c = new Comparator<byte[]>() {
        public int compare(final byte[] b1, final byte[] b2) {
            return b1.length != b2.length
                   ? b2.length - b1.length
                   : new BigInteger(b1).compareTo(new BigInteger(b2));
        }
    };
    private final NetRequest netRequest;

    // Used by unit tests
    @SuppressWarnings("unused")
    private Calendar currentTime;

    public DefaultPacScriptMethods(final NetRequest netRequest) {

        this.netRequest = netRequest;
    }

    public boolean isPlainHostName(final String host) {

        return !host.contains(".");
    }

    public boolean dnsDomainIs(final String host, final String domain) {

        return host.endsWith(domain);
    }

    public boolean localHostOrDomainIs(final String host, final String domain) {

        return domain.startsWith(host);
    }

    public boolean isResolvable(final String host) {

        return netRequest.tryGet(host) != null;
    }

    public boolean isInNet(String host,
                           final String pattern,
                           final String mask) {

        host = dnsResolve(host);
        if (host == null || host.length() == 0)
            return false;

        final long lHost    = parseIpAddressToLong(host);
        final long lPattern = parseIpAddressToLong(pattern);
        final long lMask    = parseIpAddressToLong(mask);

        return (lHost & lMask) == lPattern;
    }

    public String dnsResolve(final String host) {

        return netRequest.dnsResolve(host, "");
    }

    public String myIpAddress() {

        return netRequest.getLocalAddressOfType(Inet4Address.class);
    }

    public int dnsDomainLevels(final String host) {

        int count    = 0;
        int startPos = 0;

        while ((startPos = host.indexOf(".", startPos + 1)) > -1)
            count++;

        return count;
    }

    public boolean shExpMatch(final String str, final String shexp) {

        final StringTokenizer tokenizer = new StringTokenizer(shexp, "*");
        int                   startPos  = 0;

        while (tokenizer.hasMoreTokens()) {
            final String token = tokenizer.nextToken();
            final int    temp  = str.indexOf(token, startPos);

            // Must start with first token
            if (startPos == 0 && !shexp.startsWith("*") && temp != 0)
                return false;

            // Last one ends with last token
            if (!tokenizer.hasMoreTokens() && !shexp.endsWith("*") && !str.endsWith(token))
                return false;

            if (temp == -1) {
                return false;
            }
            else {
                startPos = temp + token.length();
            }
        }

        return true;
    }

    public boolean weekdayRange(final String wd1,
                                final String wd2,
                                final String gmt) {

        final boolean  useGmt = GMT.equalsIgnoreCase(wd2) || GMT.equalsIgnoreCase(gmt);
        final Calendar cal    = getCurrentTime(useGmt);

        final int currentDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
        final int from = DAYS.indexOf(wd1 == null
                                      ? null
                                      : wd1.toUpperCase());
        int to = DAYS.indexOf(wd2 == null
                              ? null
                              : wd2.toUpperCase());
        if (to == -1)
            to = from;

        return to < from
               ? currentDay >= from || currentDay <= to
               : currentDay >= from && currentDay <= to;
    }

    public boolean dateRange(final Object day1,
                             final Object month1,
                             final Object year1,
                             final Object day2,
                             final Object month2,
                             final Object year2,
                             final Object gmt) {

        // Guess the parameter meanings.
        final Map<String, Integer> params = new HashMap<>();
        parseDateParam(params, day1);
        parseDateParam(params, month1);
        parseDateParam(params, year1);
        parseDateParam(params, day2);
        parseDateParam(params, month2);
        parseDateParam(params, year2);
        parseDateParam(params, gmt);

        // Get current date
        final boolean  useGmt  = params.get("gmt") != null;
        final Calendar cal     = getCurrentTime(useGmt);
        final Date     current = cal.getTime();

        // Build the "from" date
        if (params.get("day1") != null)
            cal.set(Calendar.DAY_OF_MONTH, params.get("day1"));
        if (params.get("month1") != null)
            cal.set(Calendar.MONTH, params.get("month1"));
        if (params.get("year1") != null)
            cal.set(Calendar.YEAR, params.get("year1"));

        final Date from = cal.getTime();

        // Build the "to" date
        if (params.get("day2") != null)
            cal.set(Calendar.DAY_OF_MONTH, params.get("day2"));
        if (params.get("month2") != null)
            cal.set(Calendar.MONTH, params.get("month2"));
        if (params.get("year2") != null)
            cal.set(Calendar.YEAR, params.get("year2"));

        Date to = cal.getTime();

        // Need to increment to the next month?
        if (to.before(from)) {
            cal.add(Calendar.MONTH, +1);
            to = cal.getTime();
        }

        // Need to increment to the next year?
        if (to.before(from)) {
            cal.add(Calendar.YEAR, +1);
            cal.add(Calendar.MONTH, -1);
            to = cal.getTime();
        }

        return current.compareTo(from) >= 0 && current.compareTo(to) <= 0;
    }

    // TODO is this method correct? the instance of sequence.
    public boolean timeRange(final Object hour1,
                             final Object min1,
                             final Object sec1,
                             final Object hour2,
                             final Object min2,
                             final Object sec2,
                             final Object gmt) {

        final boolean useGmt = GMT.equalsIgnoreCase(String.valueOf(min1)) ||
                GMT.equalsIgnoreCase(String.valueOf(sec1)) ||
                GMT.equalsIgnoreCase(String.valueOf(min2)) ||
                GMT.equalsIgnoreCase(String.valueOf(gmt));

        final Calendar cal = getCurrentTime(useGmt);
        cal.set(Calendar.MILLISECOND, 0);
        final Date current = cal.getTime();
        final Date from;
        Date       to;

        if (sec2 instanceof Number) {
            cal.set(Calendar.HOUR_OF_DAY, ((Number) hour1).intValue());
            cal.set(Calendar.MINUTE, ((Number) min1).intValue());
            cal.set(Calendar.SECOND, ((Number) sec1).intValue());
            from = cal.getTime();

            cal.set(Calendar.HOUR_OF_DAY, ((Number) hour2).intValue());
            cal.set(Calendar.MINUTE, ((Number) min2).intValue());
            cal.set(Calendar.SECOND, ((Number) sec2).intValue());
            to = cal.getTime();
        }
        else if (hour2 instanceof Number) {
            cal.set(Calendar.HOUR_OF_DAY, ((Number) hour1).intValue());
            cal.set(Calendar.MINUTE, ((Number) min1).intValue());
            cal.set(Calendar.SECOND, 0);
            from = cal.getTime();

            cal.set(Calendar.HOUR_OF_DAY, ((Number) sec1).intValue());
            cal.set(Calendar.MINUTE, ((Number) hour2).intValue());
            cal.set(Calendar.SECOND, 59);
            to = cal.getTime();
        }
        else if (min1 instanceof Number) {
            cal.set(Calendar.HOUR_OF_DAY, ((Number) hour1).intValue());
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            from = cal.getTime();

            cal.set(Calendar.HOUR_OF_DAY, ((Number) min1).intValue());
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            to = cal.getTime();
        }
        else {
            cal.set(Calendar.HOUR_OF_DAY, ((Number) hour1).intValue());
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            from = cal.getTime();

            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            to = cal.getTime();
        }

        if (to.before(from)) {
            cal.setTime(to);
            cal.add(Calendar.DATE, +1);
            to = cal.getTime();
        }

        return current.compareTo(from) >= 0 && current.compareTo(to) <= 0;
    }

    public boolean isResolvableEx(final String host) {

        return isResolvable(host);
    }

    public boolean isInNetEx(final String ipOrHost, final String cidr) {

        if (ipOrHost == null || cidr == null || ipOrHost.isEmpty() || cidr.isEmpty())
            return false;

        // Split CIDR, usually written like 2000::/64"
        final String[] cidrParts = cidr.split("/");
        if (cidrParts.length != 2)
            return false;

        final String cidrRange = cidrParts[0];
        final int    cidrBits  = Integer.parseInt(cidrParts[1]);

        final byte[] addressBytes = netRequest.tryGetAddress(ipOrHost);
        if (addressBytes == null)
            return false;

        final byte[] rangeBytes = netRequest.tryGetAddress(cidrRange);
        if (rangeBytes == null)
            return false;

        final BigInteger ip = new BigInteger(addressBytes);
        final BigInteger mask = addressBytes.length == 4
                                ? HIGH_32_INT.shiftLeft(32 - cidrBits)
                                : HIGH_128_INT.shiftLeft(128 - cidrBits);


        final BigInteger range  = new BigInteger(rangeBytes);
        final BigInteger lowIP  = range.and(mask);
        final BigInteger highIP = lowIP.add(mask.not());

        return lowIP.compareTo(ip) <= 0 && highIP.compareTo(ip) >= 0;
    }

    public String dnsResolveEx(final String host) {

        final StringBuilder result = new StringBuilder();

        final InetAddress[] list = netRequest.tryGetAllByName(host);
        if (list == null)
            return result.toString();

        for (final InetAddress inetAddress : list) {
            result.append(inetAddress.getHostAddress());
            result.append("; ");
        }

        return result.toString();
    }

    public String myIpAddressEx() {

        return netRequest.getLocalAddressOfType(Inet6Address.class);
    }

    public String sortIpAddressList(final String ipAddressList) {

        if (ipAddressList == null || ipAddressList.trim().isEmpty())
            return "";

        final String[] ipAddressToken = ipAddressList.split(";");

        final TreeMap<byte[], String> sorting = new TreeMap<>(c);

        for (final String ip : ipAddressToken) {
            final String cleanIP = ip.trim();
            final byte[] addr    = netRequest.tryGetAddress(cleanIP);

            if (addr == null)
                return "";

            sorting.put(
                    addr,
                    cleanIP
            );
        }

        final StringBuilder result = new StringBuilder();
        for (final String ip : sorting.values()) {
            if (result.length() > 0)
                result.append(";");
            result.append(ip);
        }

        return result.toString();
    }

    public String getClientVersion() {

        return VERSION;
    }


    private long parseIpAddressToLong(final String address) {

        final String[] parts = address.split("\\.");

        long result = 0;
        long shift  = 24;

        for (final String part : parts) {
            result |= (Long.parseLong(part) << shift);
            shift -= 8;
        }

        return result;
    }

    private void parseDateParam(final Map<String, Integer> params,
                                final Object value) {

        if (value instanceof Number) {
            final int n = ((Number) value).intValue();
            if (n <= 31) {
                // Its a day
                if (params.get("day1") == null)
                    params.put("day1", n);
                else
                    params.put("day2", n);
            }
            else {
                // Its a year
                if (params.get("year1") == null)
                    params.put("year1", n);
                else
                    params.put("year2", n);
            }
        }

        if (value instanceof String) {
            final int n = MONTH.indexOf(((String) value).toUpperCase());
            if (n > -1) {
                // Its a month
                if (params.get("month1") == null)
                    params.put("month1", n);
                else
                    params.put("month2", n);
            }
            else if (GMT.equalsIgnoreCase(String.valueOf(value)))
                params.put("gmt", 1);
        }
    }

    private Calendar getCurrentTime(final boolean useGmt) {

        // Used by unit tests
        if (this.currentTime != null)
            return (Calendar) this.currentTime.clone();

        final TimeZone zone = useGmt
                              ? TimeZone.getTimeZone(GMT)
                              : TimeZone.getDefault();

        return Calendar.getInstance(zone);
    }

}
