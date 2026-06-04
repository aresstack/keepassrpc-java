package com.aresstack.keepassrpc.config;

/**
 * Loads and saves KeePassRPC settings outside the optional Swing component.
 * <p>
 * Applications should provide their own implementation for files, databases,
 * encrypted preference stores, or test fixtures.
 */
public interface KeePassRpcSettingsRepository {
    /**
     * Load the current settings.
     *
     * @return settings instance, never {@code null}
     */
    KeePassRpcSettings load();

    /**
     * Persist the supplied settings.
     *
     * @param settings settings to persist
     */
    void save(KeePassRpcSettings settings);
}
