package com.aresstack.keepassrpc.pairing;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.LinkedHashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * Default UI-independent KeePassRPC pairing service.
 */
public class DefaultKeePassRpcPairingService implements KeePassRpcPairingService {
    private static final Logger LOG = Logger.getLogger(DefaultKeePassRpcPairingService.class.getName());
    private static final Gson GSON = new Gson();

    private static final BigInteger N = new BigInteger(
            "d4c7f8a2b32c11b8fba9581ec4ba4f1b04215642ef7355e37c0fc0443ef756ea"
          + "2c6b8eeb755a1c723027663caa265ef785b8ff6a9b35227a52d86633dbdfca43", 16);
    private static final BigInteger G = BigInteger.valueOf(2);
    private static final BigInteger K = new BigInteger("b7867f1299da8cc24ab93e08986ebc4d6a478ad0", 16);
    private static final int PROTOCOL_VERSION = (1 << 16) | (7 << 8) | 2;
    private static final String[] FEATURES = {
            "KPRPC_FEATURE_VERSION_1_6",
            "KPRPC_FEATURE_WARN_USER_WHEN_FEATURE_MISSING",
            "KPRPC_ENTRIES_WITH_NO_URL"
    };

    private final KeePassRpcPairingObserver observer;

    public DefaultKeePassRpcPairingService() {
        this(KeePassRpcPairingObservers.none());
    }

    public DefaultKeePassRpcPairingService(KeePassRpcPairingObserver observer) {
        this.observer = observer == null ? KeePassRpcPairingObservers.none() : observer;
    }

    @Override
    public KeePassRpcPairingSession startPairing(KeePassRpcPairingRequest request) {
        KeePassRpcPairingRequest effectiveRequest = request == null ? KeePassRpcPairingRequest.builder().build() : request;
        observer.onPairingStarted(effectiveRequest);

        LinkedHashSet<KeePassRpcEndpoint> candidates = candidateEndpoints(effectiveRequest);
        KeePassRpcPairingException lastFailure = null;
        for (KeePassRpcEndpoint endpoint : candidates) {
            try {
                observer.onKeePassConnectionWaiting(endpoint);
                KeePassRpcPairingSession session = startPairingWithEndpoint(effectiveRequest, endpoint);
                observer.onKeePassUserConfirmationRequired(session);
                return session;
            } catch (KeePassRpcPairingException e) {
                lastFailure = e;
                LOG.fine("KeePassRPC pairing candidate failed for " + endpoint + ": " + e.getMessage());
            }
        }

        if (lastFailure != null) {
            observer.onPairingFailed(lastFailure);
            throw lastFailure;
        }

        KeePassRpcPairingException failure = new KeePassRpcPairingException(
                KeePassRpcPairingFailureReason.KEEPASS_NOT_RUNNING,
                effectiveRequest.getEndpoint(),
                "KeePassRPC is not reachable.");
        observer.onPairingFailed(failure);
        throw failure;
    }

    @Override
    public KeePassRpcPairingResult completePairing(KeePassRpcPairingSession session, String pairingKey) {
        validateSession(session);
        String key = pairingKey == null ? "" : pairingKey.trim();
        if (key.isEmpty()) {
            throw failure(session, KeePassRpcPairingFailureReason.USER_CANCELLED, "Pairing key must not be empty.");
        }

        try {
            if (!session.isOpen()) {
                throw failure(session, KeePassRpcPairingFailureReason.CONNECTION_REJECTED,
                        "KeePassRPC WebSocket connection is no longer open.");
            }

            String sessionKey = verifyPairingKey(session, key);
            KeePassRpcPairingResult result = new KeePassRpcPairingResult(
                    session.getEndpoint(), session.getOrigin(), session.getClientId(), sessionKey);
            observer.onPairingSucceeded(result);
            return result;
        } catch (KeePassRpcPairingException e) {
            observer.onPairingFailed(e);
            throw e;
        } finally {
            session.close();
        }
    }

    @Override
    public KeePassRpcPairingResult pair(KeePassRpcPairingRequest request, KeePassRpcPairingKeyProvider keyProvider) {
        if (keyProvider == null) {
            throw new IllegalArgumentException("Pairing key provider must not be null.");
        }
        KeePassRpcPairingSession session = startPairing(request);
        try {
            String pairingKey = keyProvider.requestPairingKey(session);
            return completePairing(session, pairingKey);
        } catch (RuntimeException e) {
            session.close();
            throw e;
        }
    }

    private KeePassRpcPairingSession startPairingWithEndpoint(final KeePassRpcPairingRequest request,
                                                             final KeePassRpcEndpoint endpoint) {
        final AtomicReference<String> mailbox = new AtomicReference<String>(null);
        final AtomicReference<String> connectError = new AtomicReference<String>(null);
        final CountDownLatch latch = new CountDownLatch(1);
        final SecureRandom random = new SecureRandom();
        final BigInteger a = new BigInteger(256, random);
        final BigInteger A = G.modPow(a, N);
        final String aHex = toHex(A);

        URI uri = endpoint.toWebSocketUri();
        WebSocketClient webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                try {
                    JsonObject msg = buildSetupMessage(request);
                    JsonObject srp = new JsonObject();
                    srp.addProperty("stage", "identifyToServer");
                    srp.addProperty("I", request.getClientId());
                    srp.addProperty("A", aHex);
                    srp.addProperty("securityLevel", 2);
                    msg.add("srp", srp);
                    send(GSON.toJson(msg));
                } catch (Exception ex) {
                    connectError.set("Failed to send identifyToServer: " + ex.getMessage());
                    latch.countDown();
                }
            }

            @Override
            public void onMessage(String text) {
                mailbox.set(text);
                latch.countDown();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                LOG.fine("KeePassRPC pairing WebSocket closed: " + code + " " + reason);
            }

            @Override
            public void onError(Exception ex) {
                String detail = ex.getMessage() != null ? ex.getMessage() : ex.toString();
                connectError.set("Connection failed: " + detail);
                latch.countDown();
            }
        };

        webSocketClient.addHeader("Origin", request.getOrigin());
        webSocketClient.setConnectionLostTimeout(0);
        try {
            if (!webSocketClient.connectBlocking(request.getConnectTimeoutSeconds(), TimeUnit.SECONDS)) {
                throw new KeePassRpcPairingException(KeePassRpcPairingFailureReason.TIMEOUT, endpoint,
                        "Timeout while connecting to KeePassRPC at " + endpoint + ".");
            }
            if (!latch.await(request.getResponseTimeoutSeconds(), TimeUnit.SECONDS)) {
                throw new KeePassRpcPairingException(KeePassRpcPairingFailureReason.TIMEOUT, endpoint,
                        "Timeout while waiting for KeePassRPC identifyToClient response.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new KeePassRpcPairingException(KeePassRpcPairingFailureReason.INTERRUPTED, endpoint,
                    "Interrupted while connecting to KeePassRPC.", e);
        } catch (RuntimeException e) {
            closeQuietly(webSocketClient);
            throw e;
        }

        String error = connectError.get();
        if (error != null) {
            closeQuietly(webSocketClient);
            throw new KeePassRpcPairingException(KeePassRpcPairingFailureReason.CONNECTION_REJECTED, endpoint, error);
        }

        String responseText = mailbox.get();
        if (responseText == null) {
            closeQuietly(webSocketClient);
            throw new KeePassRpcPairingException(KeePassRpcPairingFailureReason.PROTOCOL_ERROR, endpoint,
                    "KeePassRPC did not return a response.");
        }

        try {
            JsonObject response = JsonParser.parseString(responseText).getAsJsonObject();
            if (response.has("error") && !response.get("error").isJsonNull()) {
                closeQuietly(webSocketClient);
                throw new KeePassRpcPairingException(KeePassRpcPairingFailureReason.CONNECTION_REJECTED, endpoint,
                        "KeePassRPC returned an error: " + response.get("error"));
            }
            JsonObject serverSrp = response.getAsJsonObject("srp");
            if (serverSrp == null || !serverSrp.has("s") || !serverSrp.has("B")) {
                closeQuietly(webSocketClient);
                throw new KeePassRpcPairingException(KeePassRpcPairingFailureReason.PROTOCOL_ERROR, endpoint,
                        "KeePassRPC identifyToClient response did not contain srp.s and srp.B.");
            }
            String salt = serverSrp.get("s").getAsString();
            String bRaw = serverSrp.get("B").getAsString();
            if (bRaw.length() % 2 != 0) {
                bRaw = "0" + bRaw;
            }
            BigInteger B = new BigInteger(bRaw, 16);
            if (B.mod(N).equals(BigInteger.ZERO)) {
                closeQuietly(webSocketClient);
                throw new KeePassRpcPairingException(KeePassRpcPairingFailureReason.PROTOCOL_ERROR, endpoint,
                        "KeePassRPC returned an invalid SRP value B.");
            }
            return new KeePassRpcPairingSession(request, endpoint, webSocketClient, a, aHex,
                    salt, toHex(B), mailbox, latch);
        } catch (KeePassRpcPairingException e) {
            throw e;
        } catch (Exception e) {
            closeQuietly(webSocketClient);
            throw new KeePassRpcPairingException(KeePassRpcPairingFailureReason.PROTOCOL_ERROR, endpoint,
                    "Failed to parse KeePassRPC identifyToClient response.", e);
        }
    }

    private String verifyPairingKey(KeePassRpcPairingSession session, String pairingKey) {
        try {
            BigInteger B = new BigInteger(session.getServerPublicEphemeralHex(), 16);
            BigInteger u = new BigInteger(1, sha256str(session.getClientPublicEphemeralHex()
                    + session.getServerPublicEphemeralHex()));
            if (u.equals(BigInteger.ZERO)) {
                throw failure(session, KeePassRpcPairingFailureReason.PROTOCOL_ERROR, "SRP value u is invalid.");
            }

            BigInteger x = new BigInteger(1, sha256str(session.getSalt() + pairingKey));
            BigInteger gx = G.modPow(x, N);
            BigInteger kgx = K.multiply(gx).mod(N);
            BigInteger diff = B.subtract(kgx).mod(N);
            if (diff.signum() < 0) {
                diff = diff.add(N);
            }
            BigInteger exp = session.getClientPrivateEphemeral().add(u.multiply(x));
            BigInteger S = diff.modPow(exp, N);
            String sHex = toHex(S);

            byte[] mBytes = sha256str(session.getClientPublicEphemeralHex()
                    + session.getServerPublicEphemeralHex() + sHex);
            String mHex = bytesToHex(mBytes);

            session.resetLatch(new CountDownLatch(1));
            session.getMailbox().set(null);

            JsonObject proof = buildSetupMessage(session.getRequest());
            JsonObject srpProof = new JsonObject();
            srpProof.addProperty("stage", "proofToServer");
            srpProof.addProperty("M", mHex);
            srpProof.addProperty("securityLevel", 2);
            proof.add("srp", srpProof);
            session.getWebSocketClient().send(GSON.toJson(proof));

            if (!session.getLatch().await(session.getRequest().getResponseTimeoutSeconds(), TimeUnit.SECONDS)) {
                throw failure(session, KeePassRpcPairingFailureReason.TIMEOUT,
                        "Timeout while waiting for KeePassRPC proofToClient response.");
            }

            String verifyText = session.getMailbox().get();
            if (verifyText == null) {
                throw failure(session, KeePassRpcPairingFailureReason.PROTOCOL_ERROR,
                        "KeePassRPC returned no proofToClient response.");
            }

            JsonObject verify = JsonParser.parseString(verifyText).getAsJsonObject();
            if (verify.has("error") && !verify.get("error").isJsonNull()) {
                throw failure(session, KeePassRpcPairingFailureReason.AUTHENTICATION_FAILED,
                        "KeePassRPC rejected the pairing key: " + verify.get("error"));
            }

            JsonObject verifySrp = verify.getAsJsonObject("srp");
            if (verifySrp != null && verifySrp.has("M2")) {
                String m2Received = verifySrp.get("M2").getAsString();
                byte[] expectedM2 = sha256str(session.getClientPublicEphemeralHex() + mHex.toLowerCase() + sHex);
                String expectedM2Hex = bytesToHex(expectedM2);
                if (!m2Received.equalsIgnoreCase(expectedM2Hex)) {
                    throw failure(session, KeePassRpcPairingFailureReason.AUTHENTICATION_FAILED,
                            "KeePassRPC proofToClient verification failed.");
                }
            }

            return bytesToHex(sha256str(sHex));
        } catch (KeePassRpcPairingException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw failure(session, KeePassRpcPairingFailureReason.INTERRUPTED,
                    "Interrupted while verifying KeePassRPC pairing key.", e);
        } catch (Exception e) {
            throw failure(session, KeePassRpcPairingFailureReason.INTERNAL_ERROR,
                    "Failed to verify KeePassRPC pairing key.", e);
        }
    }

    private LinkedHashSet<KeePassRpcEndpoint> candidateEndpoints(KeePassRpcPairingRequest request) {
        LinkedHashSet<KeePassRpcEndpoint> candidates = new LinkedHashSet<KeePassRpcEndpoint>();
        KeePassRpcEndpoint configured = request.getEndpoint();
        candidates.add(configured);
        if (request.isTryLocalhostFallbacks()) {
            candidates.add(new KeePassRpcEndpoint("127.0.0.1", configured.getPort()));
            candidates.add(new KeePassRpcEndpoint("localhost", configured.getPort()));
        }
        return candidates;
    }

    private static void validateSession(KeePassRpcPairingSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Pairing session must not be null.");
        }
    }

    private KeePassRpcPairingException failure(KeePassRpcPairingSession session,
                                               KeePassRpcPairingFailureReason reason,
                                               String message) {
        return failure(session, reason, message, null);
    }

    private KeePassRpcPairingException failure(KeePassRpcPairingSession session,
                                               KeePassRpcPairingFailureReason reason,
                                               String message,
                                               Throwable cause) {
        return new KeePassRpcPairingException(reason, session.getEndpoint(), message, cause);
    }

    private static void closeQuietly(WebSocketClient webSocketClient) {
        if (webSocketClient != null) {
            try {
                webSocketClient.close();
            } catch (Exception ignored) {
                // Ignore shutdown failures.
            }
        }
    }

    private static JsonObject buildSetupMessage(KeePassRpcPairingRequest request) {
        JsonObject msg = new JsonObject();
        msg.addProperty("protocol", "setup");
        msg.addProperty("version", PROTOCOL_VERSION);
        msg.addProperty("clientTypeId", request.getClientId());
        msg.addProperty("clientDisplayName", request.getClientDisplayName());
        msg.addProperty("clientDisplayDescription", request.getClientDisplayDescription());
        JsonArray features = new JsonArray();
        for (String feature : FEATURES) {
            features.add(feature);
        }
        msg.add("features", features);
        return msg;
    }

    private static String toHex(BigInteger value) {
        return value.toString(16).toUpperCase();
    }

    private static byte[] sha256str(String input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xFF));
        }
        return sb.toString();
    }
}
