# KeePassRPC Settings Extract

This ZIP contains the extracted KeePassRPC settings and pairing UI from MainframeMate.

Scope of this extraction:

- keep only the security settings part that is relevant when the password access method is `KeePassRPC`
- include a standalone Swing panel for configuration and pairing
- include the KeePassRPC pairing dialog and client classes copied from MainframeMate
- include the original MainframeMate source files for traceability
- document the KeePass/KeePassRPC setup with the attached screenshots

The extracted UI is intentionally small and can later be moved into a dedicated module or reused inside a new settings dialog.

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

Original MainframeMate classes are preserved under:

```text
original-mainframemate-sources/
```

## KeePassRPC installation overview

KeePassRPC is loaded by KeePass as a plugin. Copy the plugin files to the KeePass plugin directory, usually one of these locations:

```text
C:\Program Files\KeePass Password Safe 2\Plugins
C:\Program Files (x86)\KeePass Password Safe 2\Plugins
```

Restart KeePass after copying the plugin. Then open your `.kdbx` database and enable or verify the KeePassRPC plugin settings.

## 1. Open the KeePass database

KeePass asks for the master key when the database is opened. The example uses a Windows user account as one unlock factor.

![Open KeePass database master key dialog](docs/images/01-keepass-open-database-master-key.png)

After unlocking, the KeePass database is open and the stored entries are visible.

![KeePass database open](docs/images/02-keepass-database-open.png)

## 2. Verify that KeePassRPC is loaded

Open the KeePass plugin list through `Tools → Plugins...`.

![Open KeePass plugins menu](docs/images/03-keepass-tools-plugins-menu.png)

The plugin dialog should show `KeePassRPC`. The screenshot shows version `2.0.2` by `Kee Vault Ltd`.

![KeePass plugins dialog with KeePassRPC](docs/images/04-keepass-plugins-dialog.png)

## 3. Open KeePassRPC options

Open the KeePassRPC options through `Tools → KeePassRPC (Kee) Options...`.

![Open KeePassRPC options menu](docs/images/05-keepass-tools-keepassrpc-options-menu.png)

## 4. Configure the general KeePassRPC options

The general tab contains the listener port. MainframeMate uses `12546` by default. Keep the automatic save option enabled if generated or updated credentials should be persisted immediately.

![KeePassRPC general options](docs/images/06-keepassrpc-options-general.png)

## 5. Configure connection security

The connection security tab defines the KeePass security level and the minimum acceptable client security level. The screenshots use `Medium` for both sides.

![KeePassRPC connection security options](docs/images/07-keepassrpc-options-connection-security.png)

## 6. Review authorised clients

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

The project is currently an extraction ZIP, not yet a polished library. The next step should be to decide whether this becomes a small reusable module inside MainframeMate or a separate helper package.

## Dependencies

The extracted pairing/client code needs:

```text
com.google.code.gson:gson:2.8.9
org.java-websocket:Java-WebSocket:1.5.2
org.slf4j:slf4j-api:1.7.32
ch.qos.logback:logback-classic:1.2.13   runtime only
```

The Swing UI itself only needs the JDK.
