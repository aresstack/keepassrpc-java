package com.aresstack.keepassrpc.pairing;

/**
 * UI-independent KeePassRPC pairing use case.
 */
public interface KeePassRpcPairingService {
    /**
     * Start pairing and keep the underlying WebSocket open until the key is completed or the session is closed.
     */
    KeePassRpcPairingSession startPairing(KeePassRpcPairingRequest request);

    /**
     * Complete pairing using the one-time key displayed by KeePass.
     */
    KeePassRpcPairingResult completePairing(KeePassRpcPairingSession session, String pairingKey);

    /**
     * Convenience flow for non-Swing callers that can provide the one-time key through a callback.
     */
    KeePassRpcPairingResult pair(KeePassRpcPairingRequest request, KeePassRpcPairingKeyProvider keyProvider);
}
