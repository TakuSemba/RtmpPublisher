package com.takusemba.rtmppublisher;

public interface PublisherListener {

    /**
     * Called when {@link Publisher} started publishing
     */
    void onStarted();

    /**
     * Called when {@link Publisher} stopped publishing
     */
    void onStopped();

    /**
     * Called when stream is disconnected
     */
    void onDisconnected();

    /**
     * Called when failed to connect
     */
    void onFailedToConnect();

}
