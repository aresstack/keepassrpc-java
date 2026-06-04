package com.aresstack.keepassrpc.pairing;

/**
 * Observe UI-independent KeePassRPC pairing progress.
 */
public interface KeePassRpcPairingObserver {
    void onPairingStarted(KeePassRpcPairingRequest request);

    void onKeePassConnectionWaiting(KeePassRpcEndpoint endpoint);

    void onKeePassUserConfirmationRequired(KeePassRpcPairingSession session);

    void onPairingSucceeded(KeePassRpcPairingResult result);

    void onPairingFailed(KeePassRpcPairingException failure);
}
