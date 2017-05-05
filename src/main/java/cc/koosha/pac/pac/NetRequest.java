package cc.koosha.pac.pac;

import java.net.InetAddress;


public interface NetRequest {

    String OVERRIDE_LOCAL_IP
            = "com.btr.proxy.pac.overrideLocalIP";

    InetAddress tryGet(String host);

    String dnsResolve(String host, String defaultValue);

    byte[] tryGetAddress(String host);

    InetAddress[] tryGetAllByName(String host);

    boolean hasOverrideLocalIp();

    String getOverrideLocalIp();

    String getLocalAddressOfType(Class<? extends InetAddress> cl);
}
