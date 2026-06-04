package com.aresstack.keepassrpc.client;

import com.aresstack.keepassrpc.config.KeePassRpcSettings;
import com.aresstack.keepassrpc.pairing.KeePassRpcPairingRequest;
import com.aresstack.keepassrpc.pairing.KeePassRpcPairingResult;

/**
 * Default public credential client backed by the extracted KeePassRPC protocol client.
 */
public final class DefaultKeePassRpcCredentialClient implements KeePassRpcCredentialClient {
    private final KeePassRpcClient delegate;

    public DefaultKeePassRpcCredentialClient(String host, int port, String clientId, String srpKey, String origin) {
        this.delegate = new KeePassRpcClient(host, port, clientId, srpKey, origin);
    }

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
