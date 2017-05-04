package cc.koosha.pac.pac;

import cc.koosha.pac.ProxyEvaluationException;


/**
 * Common interface for PAC script parsers.
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 */
public interface PacScriptParser {

    /**
     * Evaluates the given URL and host against the PAC script.
     *
     * @param url  the URL to evaluate.
     * @param host the host name part of the URL.
     *
     * @return the script result.
     *
     * @throws ProxyEvaluationException on execution error.
     */
    String evaluate(String url, String host) throws ProxyEvaluationException;

}
