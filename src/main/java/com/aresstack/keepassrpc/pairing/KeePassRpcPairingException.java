package com.aresstack.keepassrpc.pairing;

/**
 * Report a failed KeePassRPC pairing step with a stable reason code.
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
