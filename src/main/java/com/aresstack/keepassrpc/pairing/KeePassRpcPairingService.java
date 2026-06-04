package com.aresstack.keepassrpc.pairing;

/**
 * UI-independent use case for pairing a Java application with KeePassRPC.
 * <p>
 * Implementations open a KeePassRPC WebSocket connection, perform the initial SRP
 * identification step, wait for the user-visible one-time pairing key, and return
 * the SRP key that the application must persist for future authenticated requests.
 */
public interface KeePassRpcPairingService {
    /**
     * Start a pairing session and keep the underlying WebSocket open until the caller
     * completes or closes the session.
     *
     * @param request endpoint, origin, client name, and timeout settings
     * @return an open pairing session waiting for the one-time key shown by KeePass
     * @throws KeePassRpcPairingException if KeePassRPC is not reachable or rejects the request
     */
    KeePassRpcPairingSession startPairing(KeePassRpcPairingRequest request);

    /**
     * Complete an open pairing session using the one-time key displayed by KeePass.
     *
     * @param session open session returned by {@link #startPairing(KeePassRpcPairingRequest)}
     * @param pairingKey one-time key shown by KeePassRPC
     * @return reusable pairing result containing the SRP key to persist
     * @throws KeePassRpcPairingException if the key is invalid or the protocol exchange fails
     */
    KeePassRpcPairingResult completePairing(KeePassRpcPairingSession session, String pairingKey);

    /**
     * Run the complete pairing flow with an application-provided key callback.
     *
     * @param request endpoint, origin, client name, and timeout settings
     * @param keyProvider callback that obtains the one-time key from a UI, CLI, or setup flow
     * @return reusable pairing result containing the SRP key to persist
     */
    KeePassRpcPairingResult pair(KeePassRpcPairingRequest request, KeePassRpcPairingKeyProvider keyProvider);
}
