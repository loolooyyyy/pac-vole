package cc.koosha.pac.whitelist;

import cc.koosha.pac.func.PredicateX;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;


/**
 * Filters an URI by inspecting it's IP address is in a given range. The range
 * as must be defined in CIDR notation. e.g. 192.0.2.1/24,
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 */
public final class IpRangeFilter implements PredicateX<URI> {

    private final byte[] matchTo;
    private final int numOfBits;

    /**
     * @param matchTo the match subnet in CIDR notation.
     */
    public IpRangeFilter(final String matchTo) {

        final String[] parts = matchTo.split("/");
        if (parts.length != 2)
            throw new IllegalArgumentException("IP range is not valid:" + matchTo);

        try {
            final InetAddress address = InetAddress.getByName(parts[0].trim());
            this.matchTo = address.getAddress();
        }
        catch (final UnknownHostException e) {
            throw new IllegalArgumentException("IP range is not valid:" + matchTo);
        }

        this.numOfBits = Integer.parseInt(parts[1].trim());
    }

    @Override
    public boolean test(final URI uri) {

        if (uri == null || uri.getHost() == null)
            return false;

        try {
            final InetAddress address = InetAddress.getByName(uri.getHost());
            final byte[] addr = address.getAddress();

            // Comparing IP6 against IP4?
            if (addr.length != this.matchTo.length)
                return false;

            int bit = 0;
            for (int nibble = 0; nibble < addr.length; nibble++)
                for (int nibblePos = 7; nibblePos >= 0; nibblePos--) {
                    final int mask = 1 << nibblePos;
                    if ((this.matchTo[nibble] & mask) != (addr[nibble] & mask))
                        return false;
                    bit++;
                    if (bit >= this.numOfBits)
                        return true;
                }

        }
        catch (final UnknownHostException e) {
            // In this case we can not get the IP do not match.
        }

        return false;
    }

}
