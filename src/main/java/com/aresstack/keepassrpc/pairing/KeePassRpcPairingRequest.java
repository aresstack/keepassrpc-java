package com.aresstack.keepassrpc.pairing;

import com.aresstack.keepassrpc.config.KeePassRpcSettings;

/**
 * Immutable input for one KeePassRPC pairing run.
 * <p>
 * A request contains the endpoint, WebSocket origin, client identity, timeout
 * values, and localhost fallback behavior used while trying to connect to
 * KeePassRPC.
 */
public final class KeePassRpcPairingRequest {
    public static final String DEFAULT_ORIGIN = "chrome-extension://keepassrpc-java";
    public static final String DEFAULT_CLIENT_ID = "MainframeMate";
    public static final String DEFAULT_CLIENT_DISPLAY_NAME = "MainframeMate";
    public static final String DEFAULT_CLIENT_DESCRIPTION = "Java KeePassRPC client";

    private final KeePassRpcEndpoint endpoint;
    private final String origin;
    private final String clientId;
    private final String clientDisplayName;
    private final String clientDisplayDescription;
    private final int connectTimeoutSeconds;
    private final int responseTimeoutSeconds;
    private final boolean tryLocalhostFallbacks;

    private KeePassRpcPairingRequest(Builder builder) {
        this.endpoint = builder.endpoint == null ? KeePassRpcEndpoint.localhost() : builder.endpoint;
        this.origin = defaultIfBlank(builder.origin, DEFAULT_ORIGIN);
        this.clientId = defaultIfBlank(builder.clientId, DEFAULT_CLIENT_ID);
        this.clientDisplayName = defaultIfBlank(builder.clientDisplayName, DEFAULT_CLIENT_DISPLAY_NAME);
        this.clientDisplayDescription = defaultIfBlank(builder.clientDisplayDescription, DEFAULT_CLIENT_DESCRIPTION);
        this.connectTimeoutSeconds = positiveOrDefault(builder.connectTimeoutSeconds, 5);
        this.responseTimeoutSeconds = positiveOrDefault(builder.responseTimeoutSeconds, 10);
        this.tryLocalhostFallbacks = builder.tryLocalhostFallbacks;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static KeePassRpcPairingRequest fromSettings(KeePassRpcSettings settings) {
        if (settings == null) {
            return builder().build();
        }
        return builder()
                .host(settings.getEffectiveRpcHost())
                .port(settings.getRpcPort())
                .origin(settings.getEffectiveRpcOrigin())
                .build();
    }

    public KeePassRpcEndpoint getEndpoint() {
        return endpoint;
    }

    public String getOrigin() {
        return origin;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientDisplayName() {
        return clientDisplayName;
    }

    public String getClientDisplayDescription() {
        return clientDisplayDescription;
    }

    public int getConnectTimeoutSeconds() {
        return connectTimeoutSeconds;
    }

    public int getResponseTimeoutSeconds() {
        return responseTimeoutSeconds;
    }

    public boolean isTryLocalhostFallbacks() {
        return tryLocalhostFallbacks;
    }

    public static final class Builder {
        private KeePassRpcEndpoint endpoint;
        private String origin = DEFAULT_ORIGIN;
        private String clientId = DEFAULT_CLIENT_ID;
        private String clientDisplayName = DEFAULT_CLIENT_DISPLAY_NAME;
        private String clientDisplayDescription = DEFAULT_CLIENT_DESCRIPTION;
        private int connectTimeoutSeconds = 5;
        private int responseTimeoutSeconds = 10;
        private boolean tryLocalhostFallbacks = true;

        public Builder endpoint(KeePassRpcEndpoint endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder host(String host) {
            int port = endpoint == null ? KeePassRpcEndpoint.DEFAULT_PORT : endpoint.getPort();
            this.endpoint = new KeePassRpcEndpoint(host, port);
            return this;
        }

        public Builder port(int port) {
            String host = endpoint == null ? KeePassRpcEndpoint.DEFAULT_HOST : endpoint.getHost();
            this.endpoint = new KeePassRpcEndpoint(host, port);
            return this;
        }

        public Builder origin(String origin) {
            this.origin = origin;
            return this;
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder clientDisplayName(String clientDisplayName) {
            this.clientDisplayName = clientDisplayName;
            return this;
        }

        public Builder clientDisplayDescription(String clientDisplayDescription) {
            this.clientDisplayDescription = clientDisplayDescription;
            return this;
        }

        public Builder connectTimeoutSeconds(int connectTimeoutSeconds) {
            this.connectTimeoutSeconds = connectTimeoutSeconds;
            return this;
        }

        public Builder responseTimeoutSeconds(int responseTimeoutSeconds) {
            this.responseTimeoutSeconds = responseTimeoutSeconds;
            return this;
        }

        public Builder tryLocalhostFallbacks(boolean tryLocalhostFallbacks) {
            this.tryLocalhostFallbacks = tryLocalhostFallbacks;
            return this;
        }

        public KeePassRpcPairingRequest build() {
            return new KeePassRpcPairingRequest(this);
        }
    }

    private static String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }

    private static int positiveOrDefault(int value, int defaultValue) {
        return value > 0 ? value : defaultValue;
    }
}
