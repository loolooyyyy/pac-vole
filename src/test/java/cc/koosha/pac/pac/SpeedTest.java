package cc.koosha.pac.pac;


import cc.koosha.pac.ProxyEvaluationException;
import cc.koosha.pac.StringProvider;
import org.testng.annotations.Test;

import java.util.Scanner;


@SuppressWarnings("FieldCanBeLocal")
public class SpeedTest {

    private static final boolean enabled = false;
    private static       boolean RUNNING = true;

    @Test(enabled = enabled)
    public void testScriptExecution() throws ProxyEvaluationException {

        print("begin");

        final JavaxPacScriptParser parser = new JavaxPacScriptParser(provider());

        // sum will overflow. change the while loop to for loop and divide sum
        // to number of loop count to get mean

        long sum = 0;
        long before;
        long after;

        while (RUNNING) {
            before = now();
            parser.evaluate("", "jakajdklsjdkljaskljdklsajdklasjdaskljadls.com");
            parser.evaluate("", "example.com");
            parser.evaluate("", "0hna.com");
            after = now() - before;
            sum += after;
            print(after);
        }

        print("total:\t", sum);
        System.err.println("fuck");
    }

    private String lines() {

        final String[] h = new Scanner(getClass()
                .getClassLoader()
                .getResourceAsStream("cc/koosha/pac/pac/hpHosts.txt")).useDelimiter("\\A")
                                                                      .next()
                                                                      .split("\n");

        final StringBuilder sb = new StringBuilder(h.length * 60);
        sb.append(";\nvar x = [");
        for (final String s : h) {
            if (s.charAt(0) == '#')
                continue;
            sb.append("'").append(s.substring(10)).append("', ");
        }
        sb.append("];\n\n");

        return sb.toString();
    }

    @SuppressWarnings("SpellCheckingInspection")
    private StringProvider provider() {

        final String f0 = "if(host == 'example.com') return 'PROXY localhost:8080';\n";
        final String f1 = "if(x.indexOf(host) >= 0) return 'BLOCK';\n";
        final String f2 = "return 'SHIT';\n";

        final String script = "function FindProxyForURL(url, host) {\n"
                + lines()
                + f0
                + f1
                + f2
                + "\n}";

        return new StringProvider() {
            @Override
            public String get() {
                return script;
            }
        };
    }

    private long now() {

        return System.currentTimeMillis();
    }

    private void print(final Object... o) {

        for (final Object o1 : o)
            System.out.print(o1);

        System.out.print("\n");
    }

}
