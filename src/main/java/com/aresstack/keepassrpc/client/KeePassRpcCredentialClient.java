package com.aresstack.keepassrpc.client;

/**
 * Credential access API for an already paired KeePassRPC connection.
 * <p>
 * Implementations connect to KeePassRPC using a previously persisted SRP key.
 * Callers should use try-with-resources or call {@link #close()} explicitly.
 */
public interface KeePassRpcCredentialClient extends AutoCloseable {
    /**
     * Open the WebSocket connection and authenticate with KeePassRPC.
     */
    void connect();

    /**
     * Read the username for the first KeePass entry matching the given title.
     *
     * @param entryTitle KeePass entry title
     * @return username or {@code null} if no matching entry was found
     */
    String getUserName(String entryTitle);

    /**
     * Read the password for the first KeePass entry matching the given title.
     *
     * @param entryTitle KeePass entry title
     * @return password or {@code null} if no matching entry was found
     */
    String getPassword(String entryTitle);

    /**
     * Query the filename of the active KeePass database.
     *
     * @return database filename or an empty string if KeePassRPC does not return it
     */
    String getDatabaseFileName();

    /**
     * Add a new login entry to KeePass.
     *
     * @param title entry title
     * @param userName username
     * @param password password
     * @param url associated URL
     */
    void addLogin(String title, String userName, String password, String url);

    /**
     * Update an existing login entry by title.
     *
     * @param title entry title
     * @param userName new username
     * @param password new password
     */
    void updateLogin(String title, String userName, String password);

    /**
     * Return a textual representation of entries visible through KeePassRPC.
     *
     * @return implementation-specific entry listing
     */
    String listEntries();

    /**
     * Close the underlying WebSocket connection.
     */
    @Override
    void close();
}
