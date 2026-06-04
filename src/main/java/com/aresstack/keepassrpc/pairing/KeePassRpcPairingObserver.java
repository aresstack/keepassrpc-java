package com.aresstack.keepassrpc.pairing;

/**
 * Observes progress and failures of the KeePassRPC pairing workflow.
 * <p>
 * UI adapters can use this interface to update status labels, enable buttons,
 * or show diagnostics without embedding the protocol implementation into the UI.
 */
public interface KeePassRpcPairingObserver {
    /**
     * Called before the service starts trying candidate endpoints.
     *
     * @param request effective pairing request
     */
    void onPairingStarted(KeePassRpcPairingRequest request);

    /**
     * Called before the service connects to one KeePassRPC endpoint.
     *
     * @param endpoint endpoint being tried
     */
    void onKeePassConnectionWaiting(KeePassRpcEndpoint endpoint);

    /**
     * Called after the initial protocol step succeeded and KeePass should show a key.
     *
     * @param session open pairing session waiting for user confirmation
     */
    void onKeePassUserConfirmationRequired(KeePassRpcPairingSession session);

    /**
     * Called after the one-time key was accepted and the reusable SRP key was derived.
     *
     * @param result successful pairing result
     */
    void onPairingSucceeded(KeePassRpcPairingResult result);

    /**
     * Called when the pairing workflow fails.
     *
     * @param failure failure with endpoint and stable reason code
     */
    void onPairingFailed(KeePassRpcPairingException failure);
}
