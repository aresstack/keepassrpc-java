package com.aresstack.keepassrpc.pairing;

/**
 * Provide the one-time KeePassRPC pairing key shown by KeePass.
 */
public interface KeePassRpcPairingKeyProvider {
    String requestPairingKey(KeePassRpcPairingSession session);
}
