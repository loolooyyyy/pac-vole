package cc.koosha.pac.pac;

import java.net.InetAddress;


/**
 * Any request made to network is handled by an implementation of this
 * interface. Thus clients can exactly control what information leaves the app
 * while parsing a pac file.
 *
 * @author Koosha Hosseiny, Copyright 2017
 */
@SuppressWarnings("SameParameterValue")
public interface NetRequest {

    String OVERRIDE_LOCAL_IP
            = "com.btr.proxy.pac.overrideLocalIP";

    /**
     * @see ScriptMethods#isResolvable(String)
     */
    InetAddress tryGet(String host);

    /**
     * @see ScriptMethods#dnsResolve(String)
     */
    String dnsResolve(String host, String defaultValue);

    /**
     * @see ScriptMethods#isInNetEx(String, String)
     * @see ScriptMethods#isInNetEx(String, String)
     * @see ScriptMethods#sortIpAddressList(String)
     */
    byte[] tryGetAddress(String host);

    /**
     * @see ScriptMethods#dnsResolveEx(String) (String, String)
     */
    InetAddress[] tryGetAllByName(String host);

    /**
     * @see ScriptMethods#myIpAddress()
     * @see ScriptMethods#myIpAddressEx()
     */
    String getLocalAddressOfType(Class<? extends InetAddress> cl);

}
