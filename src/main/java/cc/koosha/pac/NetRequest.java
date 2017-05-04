package cc.koosha.pac;

import java.net.InetAddress;


public interface NetRequest {

    InetAddress tryGet(String host);

    String dnsResolve(String host, String defaultValue);

    byte[] tryGetAddress(String host);

    InetAddress[] tryGetAllByName(String host);

    boolean hasOverrideLocalIp();

    String getOverrideLocalIp();

    String getLocalAddressOfType(Class<? extends InetAddress> cl);
}
