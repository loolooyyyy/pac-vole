package cc.koosha.pac.filter;

import cc.koosha.pac.PredicateX;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static cc.koosha.pac.filter.HostnameFilter.Mode.BEGINS_WITH;
import static cc.koosha.pac.filter.HostnameFilter.Mode.ENDS_WITH;


/**
 * Will support the most common forms of filters found in lists. The list is a
 * comma (or space) separated list of domain names or IP addresses. The
 * following section shows some examples:
 * <p>
 * <ul>
 *     <li>
 *         <b>{@code .mynet.com}</b> filters all host names ending with
 *         <em>.mynet.com</em>.
 *     </li>
 *     <li>
 *         <b>{@code *.mynet.com}</b> filters all host names ending with
 *         <em>.mynet.com</em>.
 *     </li>
 *     <li>
 *         <b>{@code www.mynet.*}</b> filters all host names starting with
 *         <em>www.mynet.</em>.
 *     </li>
 *     <li>
 *         <b>{@code 123.12.32.1}</b> filters the IP <em>123.12.32.1</em>.
 *     </li>
 *     <li>
 *         <b>{@code 123.12.32.1/255}</b> filters the IP range.
 *     </li>
 *     <li>
 *         <b>{@code http://www.mynet.com}</b> Filters only HTTP protocol not
 *         FTP and no HTTPS.
 *     </li>
 * </ul>
 * <p>
 * Example of a list:
 * <p>
 * {@code .mynet.com, *.my-other-net.org, 123.55.23.222, 123.55.23.0/24}
 * <p>
 * Some info about this topic can be found on
 * <a href="http://kb.mozillazine.org/No_proxy_for">Mozillazine: No proxy for</a>
 * and
 * <a href="http://technet.microsoft.com/en-us/library/dd361953.aspx">Microsot</a>.
 *
 * <pr>
 * Note that this implementation does not cover all variations of all browsers
 * but should cover the most used formats.
 *
 * @author Koosha Hosseiny, Copyright 2017
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 */
@SuppressWarnings("SpellCheckingInspection")
public final class FilterListParser {

    private static final Pattern IP_SUB_PATTERN = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])/(\\d|([12]\\d|3[0-2]))$");

    public static List<PredicateX<URI>> parse(final String list) {

        final String[]              token  = list.split("[, ]+");
        final List<PredicateX<URI>> result = new ArrayList<>(token.length);

        for (final String each : token) {
            final String tkn = each.trim();

            final PredicateX<URI> filter;

            if (IP_SUB_PATTERN.matcher(tkn).matches())
                filter = new IpRangeFilter(tkn);
            else if (tkn.endsWith("*"))
                filter = new HostnameFilter(BEGINS_WITH, tkn.substring(0, tkn.length() - 1));
            else if (tkn.startsWith("*"))
                filter = new HostnameFilter(ENDS_WITH, tkn.substring(1));
            else
                filter = new HostnameFilter(ENDS_WITH, tkn);

            result.add(filter);
        }

        return result;
    }

}
