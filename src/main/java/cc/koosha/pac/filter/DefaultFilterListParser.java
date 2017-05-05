package cc.koosha.pac.filter;

import cc.koosha.pac.filter.HostnameFilter;
import cc.koosha.pac.filter.IpRangeFilter;
import cc.koosha.pac.func.FunctionX;
import cc.koosha.pac.func.PredicateX;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static cc.koosha.pac.filter.HostnameFilter.Mode.BEGINS_WITH;
import static cc.koosha.pac.filter.HostnameFilter.Mode.ENDS_WITH;


/**
 * Default implementation for an list parser. This will support the most
 * common forms of filters found in lists. The list is a comma (or space)
 * separated list of domain names or IP addresses. The following section shows
 * some examples.
 * <p>
 * .mynet.com - Filters all host names ending with .mynet.com *.mynet.com -
 * Filters all host names ending with .mynet.com www.mynet.* - Filters all host
 * names starting with www.mynet. 123.12.32.1 - Filters the IP 123.12.32.1
 * 123.12.32.1/255 - Filters the IP range http://www.mynet.com - Filters only
 * HTTP protocol not FTP and no HTTPS
 * <p>
 * Example of a list:
 * <p>
 * .mynet.com, *.my-other-net.org, 123.55.23.222, 123.55.23.0/24
 * <p>
 * Some info about this topic can be found here:
 * http://kb.mozillazine.org/No_proxy_for
 * http://technet.microsoft.com/en-us/library/dd361953.aspx
 * <p>
 * Note that this implementation does not cover all variations of all browsers
 * but should cover the most used formats.
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 */
public final class DefaultFilterListParser implements FunctionX<String, List<PredicateX<URI>>> {

    private static final Pattern IP_SUB_PATTERN = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])/(\\d|([12]\\d|3[0-2]))$");


    @Override
    public List<PredicateX<URI>> apply(final String list) {

        return parse(list);
    }

    public static List<PredicateX<URI>> parse(final String list) {

        final String[] token = list.split("[, ]+");
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
