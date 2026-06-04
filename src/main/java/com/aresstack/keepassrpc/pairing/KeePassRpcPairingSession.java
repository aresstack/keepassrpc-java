package com.aresstack.keepassrpc.pairing;

import org.java_websocket.client.WebSocketClient;

import java.math.BigInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents an in-progress KeePassRPC pairing session.
 */
public final class KeePassRpcPairingSession implements AutoCloseable {
    private final KeePassRpcPairingRequest request;
    private final KeePassRpcEndpoint endpoint;
    private final WebSocketClient webSocketClient;
    private final BigInteger clientPrivateEphemeral;
    private final String clientPublicEphemeralHex;
    private final String salt;
    private final String serverPublicEphemeralHex;
    private final AtomicReference<String> mailbox;
    private volatile CountDownLatch latch;
    private volatile boolean closed;

    KeePassRpcPairingSession(KeePassRpcPairingRequest request,
                             KeePassRpcEndpoint endpoint,
                             WebSocketClient webSocketClient,
                             BigInteger clientPrivateEphemeral,
                             String clientPublicEphemeralHex,
                             String salt,
                             String serverPublicEphemeralHex,
                             AtomicReference<String> mailbox,
                             CountDownLatch latch) {
        this.request = request;
        this.endpoint = endpoint;
        this.webSocketClient = webSocketClient;
        this.clientPrivateEphemeral = clientPrivateEphemeral;
        this.clientPublicEphemeralHex = clientPublicEphemeralHex;
        this.salt = salt;
        this.serverPublicEphemeralHex = serverPublicEphemeralHex;
        this.mailbox = mailbox;
        this.latch = latch;
    }

    public KeePassRpcPairingRequest getRequest() {
        return request;
    }

    public KeePassRpcEndpoint getEndpoint() {
        return endpoint;
    }

    public String getOrigin() {
        return request.getOrigin();
    }

    public String getClientId() {
        return request.getClientId();
    }

    boolean isOpen() {
        return !closed && webSocketClient != null && webSocketClient.isOpen();
    }

    WebSocketClient getWebSocketClient() {
        return webSocketClient;
    }

    BigInteger getClientPrivateEphemeral() {
        return clientPrivateEphemeral;
    }

    String getClientPublicEphemeralHex() {
        return clientPublicEphemeralHex;
    }

    String getSalt() {
        return salt;
    }

    String getServerPublicEphemeralHex() {
        return serverPublicEphemeralHex;
    }

    AtomicReference<String> getMailbox() {
        return mailbox;
    }

    CountDownLatch getLatch() {
        return latch;
    }

    void resetLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void close() {
        closed = true;
        if (webSocketClient != null) {
            try {
                webSocketClient.close();
            } catch (Exception ignored) {
                // Ignore shutdown failures.
            }
        }
    }
}
