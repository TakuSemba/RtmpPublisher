package com.takusemba.rtmppublisher;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import net.butterflytv.rtmp_client.RTMPMuxer;

class Muxer {

    private static final int MSG_SEND_VIDEO = 0;
    private static final int MSG_SEND_AUDIO = 1;

    private Handler handler;

    private RTMPMuxer rtmpMuxer = new RTMPMuxer();
    private PublisherListener listener;

    void setOnMuxerStateListener(PublisherListener listener) {
        this.listener = listener;
    }

    boolean open(String url, int width, int height) {
        HandlerThread handlerThread = new HandlerThread("Muxer");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_SEND_VIDEO: {
                        if (isConnected()) {
                            rtmpMuxer.writeVideo((byte[]) msg.obj, 0, msg.arg1, msg.arg2);
                        } else {
                            if (listener != null) listener.onDisconnected();
                        }
                        break;
                    }
                    case MSG_SEND_AUDIO: {
                        if (isConnected()) {
                            rtmpMuxer.writeAudio((byte[]) msg.obj, 0, msg.arg1, msg.arg2);
                        } else {
                            if (listener != null) listener.onDisconnected();
                        }
                        break;
                    }
                }
                return false;
            }
        });
        rtmpMuxer.open(url, width, height);
        return rtmpMuxer.isConnected() == 1;
    }

    void sendVideo(byte[] data, int length, int timestamp) {
        Message message = handler.obtainMessage(MSG_SEND_VIDEO, data);
        message.arg1 = length;
        message.arg2 = timestamp;
        handler.sendMessage(message);
    }

    void sendAudio(final byte[] data, final int length, final int timestamp) {
        Message message = handler.obtainMessage(MSG_SEND_AUDIO, data);
        message.arg1 = length;
        message.arg2 = timestamp;
        handler.sendMessage(message);
    }

    void close() {
        rtmpMuxer.close();
    }

    boolean isConnected() {
        return rtmpMuxer.isConnected() == 1;
    }
}
