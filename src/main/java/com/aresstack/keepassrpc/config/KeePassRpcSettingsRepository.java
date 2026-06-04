package com.aresstack.keepassrpc.config;

/**
 * Persist KeePassRPC settings outside the Swing component.
 */
public interface KeePassRpcSettingsRepository {
    KeePassRpcSettings load();
    void save(KeePassRpcSettings settings);
}
