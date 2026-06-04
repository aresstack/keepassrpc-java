package com.aresstack.keepassrpc.client;

/**
 * Signal that KeePassRPC is not reachable or rejects the current operation.
 */
public class KeePassNotAvailableException extends RuntimeException {
    public KeePassNotAvailableException(String message) {
        super(message);
    }

    public KeePassNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
