package cc.koosha.pac;

import cc.koosha.pac.pac.JavaxPacScriptParser;
import cc.koosha.pac.selector.PacProxySelector;
import org.testng.annotations.Test;

import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.Arrays;
import java.util.List;


@SuppressWarnings("SpellCheckingInspection")
public class BreakpointableTest {

    private final List<String> f = Arrays.asList(
            "isIpAddress(host);",
            "if (shExpMatch(host, %example.com%)) { return %DIRECT%; }",
            "if (shExpMatch(host, %*.example.com%)) { return %DIRECT%; }",
            "if (isInNet(host, %192.168.1.0%, %255.255.255.0%)) { return %PROXY fastproxy.com:8080%; }",
            "if (host == %time.cc%) { return %DIRECT%; }",
            "return %PROXY proxifiedsomethingsomewhere.com:9991; DIRECT%;"
    );

    private JavaxPacScriptParser parser() throws ProxyEvaluationException {

        return new JavaxPacScriptParser(new StringProvider() {
            @Override
            public String get() {
                final StringBuilder sb = new StringBuilder(
                        "function FindProxyForURL(url, host) {");
                for (final String s : f)
                    sb.append(s);
                return sb.append('}').toString().replace('%', '"');
            }
        });
    }

    private ProxySelector selector() throws ProxyEvaluationException {

        return new PacProxySelector(parser());
    }

    private URI uri(final String u) {

        return URI.create(u);
    }

    @SuppressWarnings({"unused", "UnusedAssignment"})
    @Test
    public void test() throws ProxyEvaluationException {

        // evaluate = parser().evaluate("http://example.com/", "example.com");
        // evaluate = parser().evaluate("http://something.example.com/", "something.example.com");
        // evaluate = parser().evaluate("10.0.0.2", "10.0.0.2");
        // evaluate = parser().evaluate("http://blabla.net/", "blabla.net");

        final List<Proxy> select0 = selector().select(uri("http://example.com/"));

        final List<Proxy> select1 = selector().select(uri("http://something.example.com/"));

        final List<Proxy> select2 = selector().select(uri("http://192.168.1.24"));

        final List<Proxy> select3 = selector().select(uri("http://blabla.net/"));

        System.out.println("done");
    }

}