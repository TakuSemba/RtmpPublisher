package com.takusemba.rtmppublisher;

import android.opengl.EGLContext;

class Streamer
        implements VideoHandler.OnVideoEncoderStateListener, AudioHandler.OnAudioEncoderStateListener {

    private VideoHandler videoHandler;
    private AudioHandler audioHandler;
    private Muxer muxer = new Muxer();

    Streamer(VideoHandler videoHandler, AudioHandler audioHandler) {
        this.videoHandler = videoHandler;
        this.audioHandler = audioHandler;
    }

    boolean open(String url, int width, int height) {
        return muxer.open(url, width, height);
    }

    void startStreaming(EGLContext context, int width, int height, int audioBitrate,
                        int videoBitrate) {
        if (muxer.isConnected()) {
            long startStreamingAt = System.currentTimeMillis();
            videoHandler.setOnVideoEncoderStateListener(this);
            audioHandler.setOnAudioEncoderStateListener(this);
            videoHandler.start(width, height, videoBitrate, context, startStreamingAt);
            audioHandler.start(audioBitrate, startStreamingAt);
        } else {
            throw new IllegalStateException("url is not opened");
        }
    }

    void stopStreaming() {
        videoHandler.stop();
        audioHandler.stop();
        muxer.close();
    }

    boolean isStreaming() {
        return muxer.isConnected();
    }

    @Override
    public void onVideoDataEncoded(byte[] data, int size, int timestamp) {
        muxer.sendVideo(data, size, timestamp);
    }

    @Override
    public void onAudioDataEncoded(byte[] data, int size, int timestamp) {
        muxer.sendAudio(data, size, timestamp);
    }
}
