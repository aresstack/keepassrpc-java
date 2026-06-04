package com.aresstack.keepassrpc.config;

/**
 * In-memory {@link KeePassRpcSettingsRepository} implementation for demos and tests.
 * <p>
 * Production applications should provide their own durable and appropriately
 * protected persistence mechanism.
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
