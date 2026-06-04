package com.aresstack.keepassrpc.pairing;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Address of a KeePassRPC WebSocket endpoint.
 */
public final class KeePassRpcEndpoint {
    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final int DEFAULT_PORT = 12546;

    private final String host;
    private final int port;

    public KeePassRpcEndpoint(String host, int port) {
        String effectiveHost = host == null || host.trim().isEmpty() ? DEFAULT_HOST : host.trim();
        validatePort(port);
        this.host = effectiveHost;
        this.port = port;
    }

    public static KeePassRpcEndpoint localhost() {
        return new KeePassRpcEndpoint(DEFAULT_HOST, DEFAULT_PORT);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public URI toWebSocketUri() {
        try {
            return new URI("ws://" + host + ":" + port + "/");
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid KeePassRPC endpoint: " + host + ":" + port, e);
        }
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof KeePassRpcEndpoint)) {
            return false;
        }
        KeePassRpcEndpoint that = (KeePassRpcEndpoint) other;
        return port == that.port && host.equals(that.host);
    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = 31 * result + port;
        return result;
    }

    private static void validatePort(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("KeePassRPC port must be between 1 and 65535.");
        }
    }
}
