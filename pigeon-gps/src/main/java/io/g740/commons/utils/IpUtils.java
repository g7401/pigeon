package io.g740.commons.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class IpUtils {

    public static void main(String[] args) {
        try {
            getLocalHostAddresses();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getLocalHostAddresses() throws IOException {
        Enumeration<NetworkInterface> networkInterfaces = null;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
            return null;
        }

        List<String> result = new ArrayList<>();

        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                if (!inetAddress.isLoopbackAddress() && !inetAddress.isAnyLocalAddress() && !inetAddress.isLinkLocalAddress() && !inetAddress.isMulticastAddress() && inetAddress.isSiteLocalAddress()) {
                    result.add(inetAddress.getHostAddress());
                }
            }
        }

        return result;
    }

}
