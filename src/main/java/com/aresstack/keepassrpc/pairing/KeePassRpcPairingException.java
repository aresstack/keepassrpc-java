package com.aresstack.keepassrpc.pairing;

/**
 * Exception thrown when a KeePassRPC pairing step fails.
 * <p>
 * In addition to the human-readable message, the exception contains the endpoint
 * that failed and a stable {@link KeePassRpcPairingFailureReason} for UI and
 * diagnostic handling.
 */
public class KeePassRpcPairingException extends RuntimeException {
    private final KeePassRpcPairingFailureReason reason;
    private final KeePassRpcEndpoint endpoint;

    public KeePassRpcPairingException(KeePassRpcPairingFailureReason reason, KeePassRpcEndpoint endpoint, String message) {
        super(message);
        this.reason = reason;
        this.endpoint = endpoint;
    }

    public KeePassRpcPairingException(KeePassRpcPairingFailureReason reason, KeePassRpcEndpoint endpoint, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
        this.endpoint = endpoint;
    }

    public KeePassRpcPairingFailureReason getReason() {
        return reason;
    }

    public KeePassRpcEndpoint getEndpoint() {
        return endpoint;
    }
}
