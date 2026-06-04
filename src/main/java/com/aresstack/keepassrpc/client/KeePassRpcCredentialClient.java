package com.aresstack.keepassrpc.client;

/**
 * Public credential API for an already paired KeePassRPC connection.
 */
public interface KeePassRpcCredentialClient extends AutoCloseable {
    void connect();

    String getUserName(String entryTitle);

    String getPassword(String entryTitle);

    String getDatabaseFileName();

    void addLogin(String title, String userName, String password, String url);

    void updateLogin(String title, String userName, String password);

    String listEntries();

    @Override
    void close();
}
