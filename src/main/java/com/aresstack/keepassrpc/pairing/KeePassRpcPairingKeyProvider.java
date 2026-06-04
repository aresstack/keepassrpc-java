package com.aresstack.keepassrpc.pairing;

/**
 * Supplies the one-time pairing key shown by KeePassRPC.
 * <p>
 * Implementations can read the key from a Swing dialog, a command-line prompt,
 * an installer screen, or a test harness. The pairing service remains independent
 * from the chosen user interaction mechanism.
 */
public interface KeePassRpcPairingKeyProvider {
    /**
     * Request the one-time pairing key for the given session.
     *
     * @param session current pairing session
     * @return the one-time key shown by KeePassRPC
     */
    String requestPairingKey(KeePassRpcPairingSession session);
}
