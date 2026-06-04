package com.aresstack.keepassrpc.client;

/**
 * Signals that KeePassRPC could not be reached or authenticated.
 * <p>
 * This exception is used by the credential client once an application already
 * expects a paired KeePassRPC connection to be available.
 */
public class KeePassNotAvailableException extends RuntimeException {
    public KeePassNotAvailableException(String message) {
        super(message);
    }

    public KeePassNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
