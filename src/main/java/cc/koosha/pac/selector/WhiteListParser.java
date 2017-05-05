package cc.koosha.pac.selector;

import cc.koosha.pac.func.PredicateX;

import java.net.URI;
import java.util.List;


/**
 * Interface for an white list parser. This will take an white list string and
 * parse it into a list of UriFilter rules.
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 */
public interface WhiteListParser {

    /**
     * Parses a list of host name and IP filters into UriFilter objects.
     *
     * @param whiteList the string to parse.
     *
     * @return a list of UriFilters
     */
    List<PredicateX<URI>> parseWhiteList(String whiteList);

}
