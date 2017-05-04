package cc.koosha.pac.whitelist;

import cc.koosha.pac.func.PredicateX;
import cc.koosha.pac.ProxyUtil;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;


/**
 * Default implementation for an white list parser. This will support the most
 * common forms of filters found in white lists. The white list is a comma (or
 * space) separated list of domain names or IP addresses. The following section
 * shows some examples.
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
public final class DefaultWhiteListParser implements WhiteListParser {

    public List<PredicateX<URI>> parseWhiteList(final String whiteList) {

        return parse(whiteList);
    }

    public static List<PredicateX<URI>> parse(final String whiteList) {

        final String[] token = whiteList.split("[, ]+");
        final List<PredicateX<URI>> result = new ArrayList<>(token.length);

        for (final String aToken : token) {
            final String tkn = aToken.trim();
            if (ProxyUtil.isValidIP4Range(tkn))
                result.add(new IpRangeFilter(tkn));
            else if (tkn.endsWith("*"))
                result.add(new HostnameFilter(HostnameFilter.Mode.BEGINS_WITH, tkn.substring(0, tkn
                        .length() - 1)));
            else if (tkn.trim().startsWith("*"))
                result.add(new HostnameFilter(HostnameFilter.Mode.ENDS_WITH, tkn.substring(1)));
            else
                result.add(new HostnameFilter(HostnameFilter.Mode.ENDS_WITH, tkn));
        }

        return result;
    }

}
