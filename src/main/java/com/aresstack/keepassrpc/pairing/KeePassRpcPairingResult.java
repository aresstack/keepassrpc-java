package com.aresstack.keepassrpc.pairing;

/**
 * Result of a successful KeePassRPC pairing run.
 */
public final class KeePassRpcPairingResult {
    private final KeePassRpcEndpoint endpoint;
    private final String origin;
    private final String clientId;
    private final String srpKey;

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
