package cc.koosha.pac.pac;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;


/**
 * @author Koosha Hosseiny, Copyright 2017
 */
public final class DefaultNetRequest implements NetRequest {

    @Override
    public InetAddress tryGet(final String host) {

        try {
            return InetAddress.getByName(host);
        }
        catch (final UnknownHostException e) {
            return null;
        }
    }

    @Override
    public String dnsResolve(final String host, final String defaultValue) {

        try {
            return InetAddress.getByName(host).getHostAddress();
        }
        catch (final UnknownHostException e) {
            return defaultValue;
        }
    }

    @Override
    public byte[] tryGetAddress(final String host) {

        try {
            return InetAddress.getByName(host).getAddress();
        }
        catch (final UnknownHostException e) {
            return null;
        }
    }

    @Override
    public InetAddress[] tryGetAllByName(final String host) {

        try {
            return InetAddress.getAllByName(host);
        }
        catch (final UnknownHostException e) {
            return null;
        }
    }

    @Override
    public String getLocalAddressOfType(final Class<? extends InetAddress> cl) {

        final String overrideIP = System.getProperty(OVERRIDE_LOCAL_IP);
        if (overrideIP != null && !overrideIP.trim().isEmpty())
            return System.getProperty(OVERRIDE_LOCAL_IP);

        final Enumeration<NetworkInterface> interfaces;

        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                final NetworkInterface current = interfaces.nextElement();
                if (!current.isUp() || current.isLoopback() || current.isVirtual())
                    continue;
                final Enumeration<InetAddress> addresses = current.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    final InetAddress adr = addresses.nextElement();
                    if (cl.isInstance(adr))
                        return adr.getHostAddress();
                }
            }
            return "";
        }
        catch (final IOException e) {
            return "";
        }
    }

}
