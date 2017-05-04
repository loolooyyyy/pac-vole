package cc.koosha.pac.whitelist;

import cc.koosha.pac.func.PredicateX;

import java.net.URI;


/**
 * Tests if a host name of a given URI matches some criteria.
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 */
public final class HostnameFilter implements PredicateX<URI> {

    public enum Mode {
        BEGINS_WITH,
        ENDS_WITH,
        REGEX
    }

    private static final String PROTOCOL_ENDING = "://";

    private final String matchTo;
    private final String protocolFilter;
    private final Mode mode;

    public HostnameFilter(final Mode mode, final String matchTo) {

        this.mode = mode;

        final int protocolIndex = matchTo.indexOf(PROTOCOL_ENDING);

        if (protocolIndex == -1) {
            this.matchTo = matchTo.toLowerCase();
            this.protocolFilter = null;
        }
        else {
            this.protocolFilter = matchTo.substring(0, protocolIndex);
            this.matchTo = matchTo.substring(protocolIndex + PROTOCOL_ENDING.length());
        }
    }

    @Override
    public boolean test(final URI uri) {

        if (uri == null || uri.getAuthority() == null)
            return false;

        // If protocol does NOT match.
        if (!(this.protocolFilter == null ||
                uri.getScheme() == null ||
                uri.getScheme().equalsIgnoreCase(this.protocolFilter)))
            return false;

        String host = uri.getAuthority();

        // Strip away port take special care for IP6.
        final int index = host.indexOf(':');
        final int index2 = host.lastIndexOf(']');
        if (index != -1 && index2 < index)
            host = host.substring(0, index);

        switch (this.mode) {
            case BEGINS_WITH:
                return host.toLowerCase().startsWith(this.matchTo);
            case ENDS_WITH:
                return host.toLowerCase().endsWith(this.matchTo);
            case REGEX:
                return host.toLowerCase().matches(this.matchTo);
            default:
                return false;
        }
    }

}
