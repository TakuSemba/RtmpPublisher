package com.takusemba.rtmppublisher;

import android.os.Handler;
import android.os.HandlerThread;

class AudioHandler implements AudioRecorder.OnAudioRecorderStateChangedListener {

    private static final int SAMPLE_RATE = 44100;

    /**
     * note that to use {@link AudioEncoder} and {@link AudioRecorder} from handler
     */
    private Handler handler;
    private AudioEncoder audioEncoder;
    private AudioRecorder audioRecorder;

    interface OnAudioEncoderStateListener {
        void onAudioDataEncoded(byte[] data, int size, int timestamp);
    }

    void setOnAudioEncoderStateListener(AudioHandler.OnAudioEncoderStateListener listener) {
        audioEncoder.setOnAudioEncoderStateListener(listener);
    }

    AudioHandler() {
        audioEncoder = new AudioEncoder();
        audioRecorder = new AudioRecorder(SAMPLE_RATE);
        audioRecorder.setOnAudioRecorderStateChangedListener(this);

        HandlerThread handlerThread = new HandlerThread("VideoHandler");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    void start(final int bitrate, final long startStreamingAt) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                audioEncoder.prepare(bitrate, SAMPLE_RATE, startStreamingAt);
                audioEncoder.start();
                audioRecorder.start();
            }
        });
    }

    void stop() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (audioRecorder.isRecording()) {
                    audioRecorder.stop();
                }
                if (audioEncoder.isEncoding()) {
                    audioEncoder.stop();
                }
            }
        });
    }

    @Override
    public void onAudioRecorded(final byte[] data, final int length) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                audioEncoder.enqueueData(data, length);
            }
        });
    }
}
