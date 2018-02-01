package com.takusemba.rtmppublisher;

import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.os.Handler;
import android.os.HandlerThread;

import java.io.IOException;

class VideoHandler implements CameraSurfaceRenderer.OnRendererStateChangedListener {

    private static final int FRAME_RATE = 30;

    /**
     * note that to use {@link VideoEncoder} and {@link VideoRenderer} from handler.
     */
    private Handler handler;
    private VideoEncoder videoEncoder;
    private VideoRenderer videoRenderer;

    interface OnVideoEncoderStateListener {
        void onVideoDataEncoded(byte[] data, int size, int timestamp);
    }

    void setOnVideoEncoderStateListener(OnVideoEncoderStateListener listener) {
        videoEncoder.setOnVideoEncoderStateListener(listener);
    }

    VideoHandler() {
        this.videoRenderer = new VideoRenderer();
        this.videoEncoder = new VideoEncoder();
        HandlerThread handlerThread = new HandlerThread("VideoHandler");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    void start(final int width, final int height, final int bitRate,
               final EGLContext sharedEglContext, final long startStreamingAt) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    videoEncoder.prepare(width, height, bitRate, FRAME_RATE, startStreamingAt);
                    videoEncoder.start();
                    videoRenderer.initialize(sharedEglContext, videoEncoder.getInputSurface());
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
            }
        });
    }

    void stop() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (videoEncoder.isEncoding()) {
                    videoEncoder.stop();
                }
                if (videoRenderer.isInitialized()) {
                    videoRenderer.release();
                }
            }
        });
    }

    @Override
    public void onSurfaceCreated(SurfaceTexture surfaceTexture) {
        // no-op
    }

    @Override
    public void onFrameDrawn(final int textureId, final float[] transform, final long timestamp) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsedTime = System.currentTimeMillis() - videoEncoder.getLastFrameEncodedAt();
                if (!videoEncoder.isEncoding() || !videoRenderer.isInitialized()
                        || elapsedTime < getFrameInterval()) {
                    return;
                }
                videoRenderer.draw(textureId, transform, timestamp);
            }
        });
    }

    private long getFrameInterval() {
        return 1000 / FRAME_RATE;
    }
}
