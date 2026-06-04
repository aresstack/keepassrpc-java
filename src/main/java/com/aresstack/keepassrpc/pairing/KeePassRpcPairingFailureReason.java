package com.aresstack.keepassrpc.pairing;

/**
 * Categorize failures that can happen during KeePassRPC pairing.
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
