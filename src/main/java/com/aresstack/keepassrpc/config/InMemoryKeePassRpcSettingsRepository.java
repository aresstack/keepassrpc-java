package com.aresstack.keepassrpc.config;

/**
 * Keep settings in memory for demos and tests.
 */
public class InMemoryKeePassRpcSettingsRepository implements KeePassRpcSettingsRepository {
    private KeePassRpcSettings settings;

    public InMemoryKeePassRpcSettingsRepository(KeePassRpcSettings settings) {
        this.settings = settings == null ? KeePassRpcSettings.defaults() : settings.copy();
    }

    @Override
    public KeePassRpcSettings load() {
        return settings.copy();
    }

    @Override
    public void save(KeePassRpcSettings settings) {
        this.settings = settings == null ? KeePassRpcSettings.defaults() : settings.copy();
    }
}
