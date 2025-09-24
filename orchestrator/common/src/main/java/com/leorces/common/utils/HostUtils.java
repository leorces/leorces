package com.leorces.common.utils;


import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * Utility class for hostname resolution and host-related operations.
 * Provides static access to the current hostname with fallback handling.
 */
public class HostUtils {

    private static final String UNKNOWN_HOST = "unknown-host";

    /**
     * The resolved hostname of the current machine.
     * If hostname resolution fails, defaults to "unknown-host".
     */
    public static final String HOSTNAME = resolveHostname();


    /**
     * Resolves the hostname of the current machine.
     *
     * @return the hostname of the current machine, or "unknown-host" if resolution fails
     */
    private static String resolveHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return UNKNOWN_HOST;
        }
    }

}
