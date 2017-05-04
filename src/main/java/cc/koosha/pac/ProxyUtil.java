package cc.koosha.pac;

import java.net.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;


public final class ProxyUtil {

    private static final Pattern IP_SUB_PATTERN = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])/(\\d|([12]\\d|3[0-2]))$");

    private ProxyUtil() {

    }

    private static final List<Proxy> noProxyList =
            Collections.singletonList(Proxy.NO_PROXY);

    /**
     * Gets an unmodifiable proxy list that will have as it's only entry an
     * DIRECT proxy.
     *
     * @return a list with a DIRECT proxy in it.
     */
    public static List<Proxy> noProxyList() {

        return noProxyList;
    }

    public static boolean isScriptValid(final String script) {

        return script != null &&
                !script.trim().isEmpty() &&
                script.contains("FindProxyForURL");
    }

    /**
     * Tests if a given string is of in the correct format for an IP4 subnet
     * mask.
     *
     * @param possibleIPAddress to test for valid format.
     *
     * @return true if valid else false.
     */
    public static boolean isValidIP4Range(final String possibleIPAddress) {

        return IP_SUB_PATTERN.matcher(possibleIPAddress).matches();
    }

}
