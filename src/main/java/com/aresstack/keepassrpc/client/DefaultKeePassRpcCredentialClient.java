package com.aresstack.keepassrpc.client;

import com.aresstack.keepassrpc.config.KeePassRpcSettings;
import com.aresstack.keepassrpc.pairing.KeePassRpcPairingRequest;
import com.aresstack.keepassrpc.pairing.KeePassRpcPairingResult;

/**
 * Default {@link KeePassRpcCredentialClient} implementation backed by the
 * KeePassRPC protocol client.
 * <p>
 * Create instances from persisted settings or directly from a pairing result.
 */
public final class DefaultKeePassRpcCredentialClient implements KeePassRpcCredentialClient {
    private final KeePassRpcClient delegate;

    /**
     * Create a credential client from explicit connection and authentication values.
     *
     * @param host KeePassRPC host
     * @param port KeePassRPC port
     * @param clientId paired client identifier
     * @param srpKey persisted SRP key returned by pairing
     * @param origin WebSocket origin accepted by KeePassRPC
     */
    public DefaultKeePassRpcCredentialClient(String host, int port, String clientId, String srpKey, String origin) {
        this.delegate = new KeePassRpcClient(host, port, clientId, srpKey, origin);
    }

    /**
     * Create a credential client from stored settings.
     *
     * @param settings settings containing endpoint, origin, and SRP key
     * @return credential client configured from the settings
     */
    public static DefaultKeePassRpcCredentialClient fromSettings(KeePassRpcSettings settings) {
        if (settings == null) {
            settings = KeePassRpcSettings.defaults();
        }
        return new DefaultKeePassRpcCredentialClient(
                settings.getEffectiveRpcHost(),
                settings.getRpcPort(),
                KeePassRpcPairingRequest.DEFAULT_CLIENT_ID,
                settings.getRpcKey(),
                settings.getEffectiveRpcOrigin());
    }

    /**
     * Create a credential client directly from a successful pairing result.
     *
     * @param result pairing result containing endpoint, origin, client ID, and SRP key
     * @return credential client configured from the pairing result
     */
    public static DefaultKeePassRpcCredentialClient fromPairingResult(KeePassRpcPairingResult result) {
        if (result == null) {
            throw new IllegalArgumentException("Pairing result must not be null.");
        }
        return new DefaultKeePassRpcCredentialClient(
                result.getEndpoint().getHost(),
                result.getEndpoint().getPort(),
                result.getClientId(),
                result.getSrpKey(),
                result.getOrigin());
    }

    @Override
    public void connect() {
        delegate.connect();
    }

    @Override
    public String getUserName(String entryTitle) {
        return delegate.getUserName(entryTitle);
    }

    @Override
    public String getPassword(String entryTitle) {
        return delegate.getPassword(entryTitle);
    }

    @Override
    public String getDatabaseFileName() {
        return delegate.getDatabaseFileName();
    }

    @Override
    public void addLogin(String title, String userName, String password, String url) {
        delegate.addLogin(title, userName, password, url);
    }

    @Override
    public void updateLogin(String title, String userName, String password) {
        delegate.updateLogin(title, userName, password);
    }

    @Override
    public String listEntries() {
        return delegate.listEntries();
    }

    @Override
    public void close() {
        delegate.close();
    }
}
