package com.takusemba.rtmppublisher;

/**
 * Encoder to encode audio or video data
 */
interface Encoder {

    /**
     * start the Encoder
     */
    void start();

    /**
     * stop the Encoder
     */
    void stop();

    /**
     * @return if the Encoder is encoding.
     */
    boolean isEncoding();
}
