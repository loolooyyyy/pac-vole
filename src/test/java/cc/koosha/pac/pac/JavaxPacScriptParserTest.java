package cc.koosha.pac.pac;

import cc.koosha.pac.ProxyEvaluationException;
import cc.koosha.pac.StringProvider;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URI;


public class JavaxPacScriptParserTest {

    private final URI HTTP_TEST_URI = URI.create("http://host1.unit-test.invalid/");

    @SuppressWarnings("SpellCheckingInspection")
    public static StringProvider provider(final String which) {

        final String script;

        switch (which) {
            case "exec":
                script = "function FindProxyForURL(url, host) {" +
                        "return \"PROXY http_proxy.unit-test.invalid:8090\";" +
                        "}";
                break;

            case "comment":
                script = "// Test comments in scripts\n" +
                        "\n" +
                        "function FindProxyForURL(url, host) {\n" +
                        "\n" +
                        "  /*\n" +
                        "   * This is a multiline comment\n" +
                        "   */\n" +
                        "\n" +
                        "  return \"DIRECT\"; // This returns always DIRECT\n" +
                        "}\n";
                break;

            case "weekDay":
                script = "// Test weekday functions\n" +
                        "\n" +
                        "function FindProxyForURL(url, host) {\n" +
                        "  weekdayRange(\"MON\");\n" +
                        "  weekdayRange(\"MON\", \"GMT\");\t\n" +
                        "  weekdayRange(\"FRI\", \"MON\");\n" +
                        "  weekdayRange(\"MON\", \"WED\", \"GMT\");\n" +
                        "  \t\n" +
                        "  return \"DIRECT\";\n" +
                        "}\n";
                break;

            case "dateRange":
                script = "// Test date range functions\n" +
                        "\n" +
                        "function FindProxyForURL(url, host) {\n" +
                        "  dateRange(1, 30);\n" +
                        "  dateRange(\"JUN\", \"JUL\");\t\n" +
                        "  dateRange(2008, 2009);\t\n" +
                        "  dateRange(\"JUN\", \"JUL\", \"GMT\");\t\n" +
                        "  dateRange(1, \"JUN\", 2008, 30, \"JUL\", 2099, \"GMT\");\t\n" +
                        "  \t\n" +
                        "  return \"DIRECT\";\n" +
                        "}\n";
                break;

            case "timeRange":
                script = "// Test weekday functions\n" +
                        "\n" +
                        "function FindProxyForURL(url, host) {\n" +
                        "  timeRange(12);\n" +
                        "  timeRange(11, 16);\t\n" +
                        "  timeRange(10, 30, 17, 30, \"gmt\");\n" +
                        "  timeRange(10, 30, 00, 17, 30, 30, \"GMT\");\n" +
                        "  timeRange(19, 9);\n" +
                        "  \t\n" +
                        "  return \"DIRECT\";\n" +
                        "}\n";
                break;

            case "returnTypes":
                script = "// Test that we have JS data types: e.g string and not java.lang.String\n" +
                        "\n" +
                        "function FindProxyForURL(url, host) {\n" +
                        "  return typeof dnsDomainLevels(host) + \" \" +\n" +
                        "         typeof isResolvable(host) + \" \" +\n" +
                        "         typeof myIpAddress();\n" +
                        "}\n";
                break;

            case "multiProxy":
                script = "function FindProxyForURL(url, host)\n" +
                        "{\n" +
                        "    return \"PROXY  my-proxy.com:80 ; PROXY my-proxy2.com: 8080; \";\n" +
                        "}\n";
                break;

            case "localIp":
                script = "\n" +
                        "function FindProxyForURL(url, host) {\n" +
                        "  return \"PROXY \"+ myIpAddress()+\":8080\";\n" +
                        "}\n";
                break;

            default:
                throw new IllegalArgumentException("unknown script: " + which);
        }

        return new StringProvider() {
            @Override
            public String get() {
                return script;
            }
        };
    }

    @Test
    public void testScriptExecution() throws ProxyEvaluationException {

        new JavaxPacScriptParser(provider("exec"))
                .evaluate(HTTP_TEST_URI.toString(), "host1.unit-test.invalid");
    }

    @Test
    public void testCommentsInScript() throws ProxyEvaluationException {

        new JavaxPacScriptParser(provider("comment"))
                .evaluate(HTTP_TEST_URI.toString(), "host1.unit-test.invalid");
    }

    /**
     * Test deactivated because it will not run in Java 1.5 and time based test
     * are unstable
     */
    @Test
    // @Ignore
    public void testScriptWeekDayScript() throws ProxyEvaluationException {

        new JavaxPacScriptParser(provider("weekDay"))
                .evaluate(HTTP_TEST_URI.toString(), "host1.unit-test.invalid");
    }

    /**
     * Test deactivated because it will not run in Java 1.5 and time based test
     * are unstable
     */
    @Test
    // @Ignore
    public void testDateRangeScript() throws ProxyEvaluationException {

        new JavaxPacScriptParser(provider("dateRange"))
                .evaluate(HTTP_TEST_URI.toString(), "host1.unit-test.invalid");
    }

    /**
     * Test deactivated because it will not run in Java 1.5 and time based test
     * are unstable
     */
    @Test
    // @Ignore
    public void testTimeRangeScript() throws ProxyEvaluationException {

        String range = provider("timeRange").get();
        new RuntimeException(range).printStackTrace();

        new JavaxPacScriptParser(provider("timeRange"))
                .evaluate(HTTP_TEST_URI.toString(), "host1.unit-test.invalid");
    }

    @Test
    public void methodsShouldReturnJsStrings() throws ProxyEvaluationException {

        final String actual = new JavaxPacScriptParser(provider("returnTypes"))
                .evaluate(HTTP_TEST_URI.toString(), "host1.unit-test.invalid");

        Assert.assertEquals(actual, "number boolean string");
    }

}