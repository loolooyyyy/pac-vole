package cc.koosha.pac.pac;

import cc.koosha.pac.ProxyEvaluationException;
import cc.koosha.pac.func.StringProvider;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.reflect.Method;


/**
 * PAC parser using the JVM's bundled JavaScript engine or the provided engine.
 * <p>
 * More information about PAC can be found there:
 * <p>
 * <a href="http://en.wikipedia.org/wiki/Proxy_auto-config">Wikipedia: PAC</a>
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 */
public final class JavaxPacScriptParser implements PacScriptParser {

    static final String SCRIPT_METHODS_OBJECT = "__pacutil";

    private final StringProvider scriptSource;
    private final ScriptEngine engine;

    public JavaxPacScriptParser(final StringProvider scriptSource) throws ProxyEvaluationException {

        this(
                scriptSource,
                new ScriptEngineManager().getEngineByMimeType("text/javascript"),
                new DefaultNetRequest()
        );
    }

    public JavaxPacScriptParser(final StringProvider scriptSource,
                                final ScriptEngine engine,
                                final NetRequest netRequest) throws ProxyEvaluationException {

        this.scriptSource = scriptSource;
        this.engine = engine;

        engine.put(SCRIPT_METHODS_OBJECT, new DefaultPacScriptMethods(netRequest));

        for (final Method method : ScriptMethods.class.getMethods()) {
            final String name = method.getName();
            final int args = method.getParameterTypes().length;
            final StringBuilder toEval = new StringBuilder(name).append(" = function(");

            for (int i = 0; i < args; i++) {
                if (i > 0)
                    toEval.append(",");
                toEval.append("arg").append(i);
            }

            toEval.append(") {return ");

            final StringBuilder functionCall1 = new StringBuilder();
            functionCall1.append(SCRIPT_METHODS_OBJECT)
                         .append(".")
                         .append(name)
                         .append("(");
            for (int i = 0; i < args; i++) {
                if (i > 0)
                    functionCall1.append(",");
                functionCall1.append("arg").append(i);
            }
            functionCall1.append(")");
            String functionCall = functionCall1.toString();

            // If return type is java.lang.String convert it to a JS string
            if (String.class.isAssignableFrom(method.getReturnType()))
                functionCall = "String(" + functionCall + ")";

            toEval.append(functionCall).append("; }");

            try {
                engine.eval(toEval.toString());
            }
            catch (final ScriptException e) {
                throw new ProxyEvaluationException(e);
            }
        }
    }

    public String evaluate(final String url,
                           final String host) throws ProxyEvaluationException {

        try {
            final String evalMethod = String.format(
                    " ;FindProxyForURL (\"%s\",\"%s\")", url, host);
            final String script = this.scriptSource.get() + evalMethod;
            return (String) this.engine.eval(script);
        }
        catch (final Exception e) {
            throw new ProxyEvaluationException(e);
        }
    }

    public static boolean isScriptValid(final String script) {

        return script != null &&
                !script.trim().isEmpty() &&
                script.contains("FindProxyForURL");
    }

}
