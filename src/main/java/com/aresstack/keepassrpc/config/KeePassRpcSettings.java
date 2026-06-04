package com.aresstack.keepassrpc.config;

import com.aresstack.keepassrpc.pairing.KeePassRpcPairingRequest;
import com.aresstack.keepassrpc.pairing.KeePassRpcPairingResult;

import java.io.File;

/**
 * Mutable settings object for KeePassRPC configuration.
 * <p>
 * The class is intentionally simple so applications can map it to their own
 * persistence format. It can also create a {@link com.aresstack.keepassrpc.pairing.KeePassRpcPairingRequest}
 * and apply a successful {@link com.aresstack.keepassrpc.pairing.KeePassRpcPairingResult}.
 */
public class KeePassRpcSettings {
    public static final String DEFAULT_RPC_HOST = "127.0.0.1";

    private String databasePath = "";
    private String entryTitle = "KeePassRPC Java";
    private String accessMethod = "RPC";
    private String rpcHost = DEFAULT_RPC_HOST;
    private int rpcPort = 12546;
    private String rpcKey = "";
    private String rpcOriginScheme = "chrome-extension://";
    private String rpcOriginId = "keepassrpc-java";

    public static KeePassRpcSettings defaults() {
        KeePassRpcSettings settings = new KeePassRpcSettings();
        settings.databasePath = defaultDatabasePath();
        return settings;
    }

    public String getDatabasePath() {
        return databasePath;
    }

    public void setDatabasePath(String databasePath) {
        this.databasePath = safe(databasePath);
    }

    public String getEntryTitle() {
        return entryTitle;
    }

    public void setEntryTitle(String entryTitle) {
        this.entryTitle = safe(entryTitle);
    }

    public String getAccessMethod() {
        return accessMethod;
    }

    public void setAccessMethod(String accessMethod) {
        this.accessMethod = safe(accessMethod).isEmpty() ? "RPC" : accessMethod;
    }

    public String getRpcHost() {
        return rpcHost;
    }

    public void setRpcHost(String rpcHost) {
        this.rpcHost = safe(rpcHost).isEmpty() ? DEFAULT_RPC_HOST : rpcHost.trim();
    }

    public int getRpcPort() {
        return rpcPort;
    }

    public void setRpcPort(int rpcPort) {
        this.rpcPort = rpcPort;
    }

    public String getRpcKey() {
        return rpcKey;
    }

    public void setRpcKey(String rpcKey) {
        this.rpcKey = safe(rpcKey);
    }

    public String getRpcOriginScheme() {
        return rpcOriginScheme;
    }

    public void setRpcOriginScheme(String rpcOriginScheme) {
        this.rpcOriginScheme = safe(rpcOriginScheme).isEmpty() ? "chrome-extension://" : rpcOriginScheme.trim();
    }

    public String getRpcOriginId() {
        return rpcOriginId;
    }

    public void setRpcOriginId(String rpcOriginId) {
        this.rpcOriginId = safe(rpcOriginId).isEmpty() ? "keepassrpc-java" : rpcOriginId.trim();
    }

    public String getEffectiveRpcHost() {
        return safe(rpcHost).isEmpty() ? DEFAULT_RPC_HOST : rpcHost.trim();
    }

    public String getEffectiveRpcOrigin() {
        return getRpcOriginScheme() + getRpcOriginId();
    }

    public boolean isRpcAccessMethod() {
        return "RPC".equalsIgnoreCase(accessMethod);
    }

    public KeePassRpcPairingRequest toPairingRequest() {
        return KeePassRpcPairingRequest.fromSettings(this);
    }

    public void applyPairingResult(KeePassRpcPairingResult result) {
        if (result == null) {
            return;
        }
        setAccessMethod("RPC");
        setRpcHost(result.getEndpoint().getHost());
        setRpcPort(result.getEndpoint().getPort());
        setRpcKey(result.getSrpKey());
        applyOrigin(result.getOrigin());
    }

    public KeePassRpcSettings withPairingResult(KeePassRpcPairingResult result) {
        KeePassRpcSettings copy = copy();
        copy.applyPairingResult(result);
        return copy;
    }

    public KeePassRpcSettings copy() {
        KeePassRpcSettings copy = new KeePassRpcSettings();
        copy.databasePath = databasePath;
        copy.entryTitle = entryTitle;
        copy.accessMethod = accessMethod;
        copy.rpcHost = rpcHost;
        copy.rpcPort = rpcPort;
        copy.rpcKey = rpcKey;
        copy.rpcOriginScheme = rpcOriginScheme;
        copy.rpcOriginId = rpcOriginId;
        return copy;
    }

    private void applyOrigin(String origin) {
        String safeOrigin = safe(origin).trim();
        if (safeOrigin.isEmpty()) {
            return;
        }
        int separator = safeOrigin.indexOf("://");
        if (separator < 0) {
            setRpcOriginScheme("chrome-extension://");
            setRpcOriginId(safeOrigin);
            return;
        }
        setRpcOriginScheme(safeOrigin.substring(0, separator + 3));
        setRpcOriginId(safeOrigin.substring(separator + 3));
    }

    private static String defaultDatabasePath() {
        String userProfile = System.getenv("USERPROFILE");
        if (userProfile == null || userProfile.trim().isEmpty()) {
            return "";
        }
        return userProfile + File.separator + "Documents" + File.separator + "Database.kdbx";
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
