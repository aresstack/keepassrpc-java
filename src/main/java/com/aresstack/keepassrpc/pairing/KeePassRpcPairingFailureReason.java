package com.aresstack.keepassrpc.pairing;

/**
 * Stable categories for KeePassRPC pairing failures.
 * <p>
 * These values are intended for UI status handling, retry decisions, and tests.
 */
public enum KeePassRpcPairingFailureReason {
    KEEPASS_NOT_RUNNING,
    KEEPASSRPC_PLUGIN_NOT_AVAILABLE,
    CONNECTION_REJECTED,
    USER_CANCELLED,
    AUTHENTICATION_FAILED,
    TIMEOUT,
    PROTOCOL_ERROR,
    INTERRUPTED,
    INTERNAL_ERROR
}
