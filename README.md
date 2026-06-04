# keepassrpc-java

`keepassrpc-java` is intended to become a small Java 8 library for integrating Java applications with KeePass 2.x through the KeePassRPC plugin.

The current repository is an extraction from MainframeMate. It contains the KeePassRPC configuration UI and the existing pairing/client code as a starting point. The next design step is to separate the pairing workflow from Swing so that applications can pair with KeePassRPC through a clean Java API. Swing should become only one optional adapter on top of that API.

## Maven coordinates

Planned coordinates for the first Maven Central release:

```text
com.aresstack:keepassrpc-java:0.1.0-beta.1
```

The repository name and artifact ID should remain aligned:

```text
Repository:  keepassrpc-java
Group:       com.aresstack
Artifact:    keepassrpc-java
Version:     0.1.0-beta.1
Java target: Java 8
```

## Scope

This library should provide:

- KeePassRPC connection configuration
- KeePassRPC reachability checks
- KeePassRPC pairing through a UI-independent API
- secure persistence handoff for pairing results
- optional Swing components for desktop applications
- no dependency on MainframeMate global settings

The extracted Swing UI remains useful, but it should not become the core abstraction. The core abstraction should be usable from CLI tools, tests, headless services, Swing applications, JavaFX applications, or future installer/setup flows.

## Extracted source layout

```text
src/main/java/com/aresstack/keepassrpc/config/KeePassRpcSettings.java
src/main/java/com/aresstack/keepassrpc/config/KeePassRpcSettingsRepository.java
src/main/java/com/aresstack/keepassrpc/config/InMemoryKeePassRpcSettingsRepository.java
src/main/java/com/aresstack/keepassrpc/ui/KeePassRpcSettingsPanel.java
src/main/java/com/aresstack/keepassrpc/client/KeePassRpcPairingDialog.java
src/main/java/com/aresstack/keepassrpc/client/KeePassRpcClient.java
src/main/java/com/aresstack/keepassrpc/client/KeePassNotAvailableException.java
src/main/java/com/aresstack/keepassrpc/demo/KeePassRpcSettingsDemo.java
```

Original MainframeMate classes are preserved for traceability:

```text
original-mainframemate-sources/
```

## Target architecture

The intended Maven Central library should be split conceptually into three layers:

```text
Application / UI
    ↓
Pairing Use Case API
    ↓
KeePassRPC Transport / Protocol Client
```

Recommended package structure:

```text
com.aresstack.keepassrpc.api
com.aresstack.keepassrpc.pairing
com.aresstack.keepassrpc.client
com.aresstack.keepassrpc.config
com.aresstack.keepassrpc.swing
```

The package `com.aresstack.keepassrpc.swing` should contain optional Swing adapters only. Core pairing should not depend on Swing.

## Proposed API-first pairing model

This section describes the intended API. It is not implemented yet.

### Main entry point

```java
KeePassRpcPairingService pairingService = new DefaultKeePassRpcPairingService(
        keepPassRpcClient,
        pairingRepository,
        pairingObserver
);

KeePassRpcPairingResult result = pairingService.pair(
        KeePassRpcPairingRequest.builder()
                .host("127.0.0.1")
                .port(12546)
                .origin("chrome-extension://mainframemate")
                .clientName("MainframeMate")
                .build()
);
```

The use case should own the pairing process. The UI should only display progress, ask the user for confirmation when needed, and persist the final result through an application-owned repository.

### Proposed core interfaces

```java
public interface KeePassRpcPairingService {
    KeePassRpcPairingResult pair(KeePassRpcPairingRequest request);
}
```

```java
public interface KeePassRpcPairingRepository {
    KeePassRpcPairingState load();
    void save(KeePassRpcPairingState state);
}
```

```java
public interface KeePassRpcPairingObserver {
    void onPairingStarted(KeePassRpcPairingRequest request);
    void onKeePassConnectionWaiting(KeePassRpcEndpoint endpoint);
    void onKeePassUserConfirmationRequired(KeePassRpcPairingChallenge challenge);
    void onPairingSucceeded(KeePassRpcPairingResult result);
    void onPairingFailed(KeePassRpcPairingFailure failure);
}
```

```java
public interface KeePassRpcConnectionProbe {
    KeePassRpcConnectionStatus check(KeePassRpcEndpoint endpoint);
}
```

```java
public interface KeePassRpcCredentialClient extends AutoCloseable {
    void connect(KeePassRpcConnectionConfig config);
    KeePassRpcPairingResult pair(KeePassRpcPairingRequest request);
    String getUserName(String entryTitle);
    String getPassword(String entryTitle);
    String getDatabaseFileName();
}
```

### Proposed value objects

```java
public final class KeePassRpcEndpoint {
    private final String host;
    private final int port;
}
```

```java
public final class KeePassRpcPairingRequest {
    private final KeePassRpcEndpoint endpoint;
    private final String origin;
    private final String clientName;
    private final String existingSrpKey;
}
```

```java
public final class KeePassRpcPairingResult {
    private final KeePassRpcEndpoint endpoint;
    private final String origin;
    private final String clientId;
    private final String srpKey;
    private final String databaseFileName;
}
```

```java
public final class KeePassRpcPairingFailure {
    private final KeePassRpcEndpoint endpoint;
    private final KeePassRpcPairingFailureReason reason;
    private final String message;
    private final Throwable cause;
}
```

```java
public enum KeePassRpcPairingFailureReason {
    KEEPASS_NOT_RUNNING,
    KEEPASSRPC_PLUGIN_NOT_AVAILABLE,
    CONNECTION_REJECTED,
    USER_CANCELLED,
    AUTHENTICATION_FAILED,
    TIMEOUT,
    PROTOCOL_ERROR
}
```

### Why the UI must be optional

The pairing process is a use case, not a Swing concern. A Swing dialog can subscribe to `KeePassRpcPairingObserver`, but the pairing service itself should not create dialogs, show message boxes, or persist settings through Swing components.

Recommended flow:

```text
Swing button
  → collects form values
  → builds KeePassRpcPairingRequest
  → calls KeePassRpcPairingService.pair(...)
  → receives KeePassRpcPairingResult
  → writes result into KeePassRpcSettingsRepository
```

That allows the same pairing workflow to be reused by:

- Swing settings panels
- command-line setup tools
- headless integration tests
- JavaFX frontends
- future MainframeMate installers

## Proposed Swing adapter

The current `KeePassRpcPairingDialog` should later become an adapter around the core pairing API:

```java
public final class SwingKeePassRpcPairingController {
    private final KeePassRpcPairingService pairingService;
    private final KeePassRpcSettingsRepository settingsRepository;

    public KeePassRpcPairingResult startPairing(Component parent, KeePassRpcSettings settings) {
        // Build a request from settings.
        // Show progress through a dialog observer.
        // Persist the returned pairing state.
        throw new UnsupportedOperationException("Design only");
    }
}
```

The final Swing dialog should not implement the KeePassRPC protocol. It should only render state and delegate to the use case.

## Proposed dependency direction

Recommended dependency direction:

```text
swing → pairing → client → protocol/transport
config → pairing
```

Forbidden dependency direction:

```text
client → swing
pairing → swing
config → swing
```

## KeePassRPC installation overview

KeePassRPC is loaded by KeePass as a plugin. Copy the plugin files to the KeePass plugin directory, usually one of these locations:

```text
C:\Program Files\KeePass Password Safe 2\Plugins
C:\Program Files (x86)\KeePass Password Safe 2\Plugins
```

Restart KeePass after copying the plugin. Then open your `.kdbx` database and enable or verify the KeePassRPC plugin settings.

## KeePass configuration walkthrough

The screenshots below document the KeePass-side setup. They do not document the Java API. They are included so users understand the KeePass and KeePassRPC state required before pairing can work.

### 1. Open the KeePass database

KeePass asks for the master key when the database is opened. The example uses a Windows user account as one unlock factor.

![Open KeePass database master key dialog](docs/images/01-keepass-open-database-master-key.png)

After unlocking, the KeePass database is open and the stored entries are visible.

![KeePass database open](docs/images/02-keepass-database-open.png)

### 2. Verify that KeePassRPC is loaded

Open the KeePass plugin list through `Tools → Plugins...`.

![Open KeePass plugins menu](docs/images/03-keepass-tools-plugins-menu.png)

The plugin dialog should show `KeePassRPC`. The screenshot shows version `2.0.2` by `Kee Vault Ltd`.

![KeePass plugins dialog with KeePassRPC](docs/images/04-keepass-plugins-dialog.png)

### 3. Open KeePassRPC options

Open the KeePassRPC options through `Tools → KeePassRPC (Kee) Options...`.

![Open KeePassRPC options menu](docs/images/05-keepass-tools-keepassrpc-options-menu.png)

### 4. Configure the general KeePassRPC options

The general tab contains the listener port. MainframeMate uses `12546` by default. Keep the automatic save option enabled if generated or updated credentials should be persisted immediately.

![KeePassRPC general options](docs/images/06-keepassrpc-options-general.png)

### 5. Configure connection security

The connection security tab defines the KeePass security level and the minimum acceptable client security level. The screenshots use `Medium` for both sides.

![KeePassRPC connection security options](docs/images/07-keepassrpc-options-connection-security.png)

### 6. Review authorised clients

The authorised clients tab lists already paired clients. Use `Revoke` to remove old or invalid MainframeMate authorisations before pairing again.

![KeePassRPC authorised clients](docs/images/08-keepassrpc-options-authorised-clients.png)

## Extracted Swing UI

`KeePassRpcSettingsPanel` contains only the relevant KeePassRPC part from MainframeMate's security/password settings:

- access method selector with `KeePassRPC`
- KeePass database path field, disabled when RPC mode is selected
- entry title field
- RPC host and port
- SRP key field with show/hide button
- origin scheme and origin id
- random origin UUID button
- `Pairing starten...` button that opens `KeePassRpcPairingDialog`

The panel persists through `KeePassRpcSettingsRepository`, so it can be reused without depending on MainframeMate's global `SettingsHelper`.

## Demo

Run the extracted demo class after resolving dependencies:

```bash
./gradlew run
```

or launch:

```text
com.aresstack.keepassrpc.demo.KeePassRpcSettingsDemo
```

## Dependencies

The extracted pairing/client code needs:

```text
com.google.code.gson:gson:2.8.9
org.java-websocket:Java-WebSocket:1.5.2
org.slf4j:slf4j-api:1.7.32
ch.qos.logback:logback-classic:1.2.13   runtime only
```

The Swing UI itself only needs the JDK.

## Current status

This repository is a Maven Central-oriented Java library extracted from MainframeMate. The next implementation step should be the API-first pairing layer described above. After that, the Swing dialog can be refactored into a thin adapter around the API.


## Build and release automation

This repository contains two GitHub Actions workflows:

- `.github/workflows/release.yml` publishes tagged releases to Maven Central through the Sonatype Central Portal.
- `.github/workflows/chatgpt-compatible-release.yml` creates a rolling `chatgpt-compatible` release asset named `keepassrpc-java-chatgpt-compatible.zip`.

The ChatGPT-compatible ZIP contains the source tree plus a prepared Gradle dependency cache. It is verified by extracting the archive into a fresh directory, removing the executable bit from `gradlew`, and running:

```bash
bash chatgpt-build.sh
CHATGPT_FULL_GRADLE_BUILD=true bash chatgpt-build.sh
```

Required repository secrets for Maven Central publishing:

```text
CENTRAL_USERNAME
CENTRAL_PASSWORD
GPG_PRIVATE_KEY
GPG_PASSPHRASE
```

Create a Maven Central release with:

```bash
git tag v0.1.0-beta.1
git push origin v0.1.0-beta.1
```
