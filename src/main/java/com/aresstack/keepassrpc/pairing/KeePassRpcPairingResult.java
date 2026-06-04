package com.aresstack.keepassrpc.pairing;

/**
 * Immutable result of a successful KeePassRPC pairing run.
 * <p>
 * The SRP key returned by {@link #getSrpKey()} is the value an application should
 * persist and reuse for future KeePassRPC authentication.
 */
public final class KeePassRpcPairingResult {
    private final KeePassRpcEndpoint endpoint;
    private final String origin;
    private final String clientId;
    private final String srpKey;

    /**
     * Create a pairing result.
     *
     * @param endpoint endpoint that accepted the pairing request
     * @param origin WebSocket origin used during pairing
     * @param clientId KeePassRPC client identifier
     * @param srpKey reusable SRP key to persist
     */
    public KeePassRpcPairingResult(KeePassRpcEndpoint endpoint, String origin, String clientId, String srpKey) {
        this.endpoint = endpoint;
        this.origin = origin;
        this.clientId = clientId;
        this.srpKey = srpKey;
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

    /**
     * Return the 64-character session key that should be persisted for future KCR authentication.
     */
    public String getSrpKey() {
        return srpKey;
    }
}
