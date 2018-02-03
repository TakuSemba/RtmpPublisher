package com.takusemba.rtmppublisher;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

class VideoEncoder implements Encoder {

    // H.264 Advanced Video Coding
    private static final String MIME_TYPE = "video/avc";
    // 5 seconds between I-frames
    private static final int IFRAME_INTERVAL = 5;

    private boolean isEncoding = false;
    private static final int TIMEOUT_USEC = 10000;

    private Surface inputSurface;
    private MediaCodec encoder;
    private MediaCodec.BufferInfo bufferInfo;
    private VideoHandler.OnVideoEncoderStateListener listener;
    private long lastFrameEncodedAt = 0;
    private long startStreamingAt = 0;

    void setOnVideoEncoderStateListener(VideoHandler.OnVideoEncoderStateListener listener) {
        this.listener = listener;
    }

    long getLastFrameEncodedAt() {
        return lastFrameEncodedAt;
    }

    Surface getInputSurface() {
        return inputSurface;
    }

    /**
     * prepare the Encoder. call this before start the encoder.
     */
    void prepare(int width, int height, int bitRate, int frameRate, long startStreamingAt)
            throws IOException {
        this.startStreamingAt = startStreamingAt;
        this.bufferInfo = new MediaCodec.BufferInfo();
        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);

        encoder = MediaCodec.createEncoderByType(MIME_TYPE);
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        inputSurface = encoder.createInputSurface();
    }

    @Override
    public void start() {
        encoder.start();
        isEncoding = true;
        drain();
    }

    @Override
    public void stop() {
        if (isEncoding()) {
            encoder.signalEndOfInputStream();
        }
    }

    @Override
    public boolean isEncoding() {
        return encoder != null && isEncoding;
    }

    void drain() {
        HandlerThread handlerThread = new HandlerThread("VideoEncoder-drain");
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                // keep running... so use a different thread.
                while (isEncoding) {
                    if (encoder == null) return;
                    ByteBuffer[] encoderOutputBuffers = encoder.getOutputBuffers();
                    int inputBufferId = encoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                    if (inputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

                        MediaFormat newFormat = encoder.getOutputFormat();
                        ByteBuffer sps = newFormat.getByteBuffer("csd-0");
                        ByteBuffer pps = newFormat.getByteBuffer("csd-1");
                        byte[] config = new byte[sps.limit() + pps.limit()];
                        sps.get(config, 0, sps.limit());
                        pps.get(config, sps.limit(), pps.limit());

                        listener.onVideoDataEncoded(config, config.length, 0);
                    } else {
                        if (inputBufferId > 0) {
                            ByteBuffer encodedData = encoderOutputBuffers[inputBufferId];
                            if (encodedData == null) {
                                continue;
                            }

                            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                                bufferInfo.size = 0;
                            }

                            if (bufferInfo.size != 0) {

                                encodedData.position(bufferInfo.offset);
                                encodedData.limit(bufferInfo.offset + bufferInfo.size);

                                long currentTime = System.currentTimeMillis();
                                int timestamp = (int) (currentTime - startStreamingAt);
                                byte[] data = new byte[bufferInfo.size];
                                encodedData.get(data, 0, bufferInfo.size);
                                encodedData.position(bufferInfo.offset);

                                listener.onVideoDataEncoded(data, bufferInfo.size, timestamp);
                                lastFrameEncodedAt = currentTime;
                            }
                            encoder.releaseOutputBuffer(inputBufferId, false);
                        } else if (inputBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                            continue;
                        }

                        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            break;
                        }
                    }
                }
                release();
            }
        });
    }

    private void release() {
        if (encoder != null) {
            isEncoding = false;
            encoder.stop();
            encoder.release();
            encoder = null;
        }
    }
}
