package com.takusemba.rtmppublisher;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;

import java.io.IOException;
import java.nio.ByteBuffer;

class AudioEncoder implements Encoder {

    private final int TIMEOUT_USEC = 10000;

    private static final String AUDIO_MIME_TYPE = "audio/mp4a-latm";
    private static final int CHANNEL_COUNT = 1;

    private ByteBuffer[] inputBuffers;
    private ByteBuffer[] outputBuffers;
    private MediaCodec encoder;

    private long startedEncodingAt = 0;
    private boolean isEncoding = false;
    private AudioHandler.OnAudioEncoderStateListener listener;

    void setOnAudioEncoderStateListener(AudioHandler.OnAudioEncoderStateListener listener) {
        this.listener = listener;
    }

    /**
     * prepare the Encoder. call this before start the encoder.
     */
    void prepare(int bitrate, int sampleRate, long startStreamingAt) {
        int bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        MediaFormat audioFormat =
                MediaFormat.createAudioFormat(AUDIO_MIME_TYPE, sampleRate, CHANNEL_COUNT);
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, bufferSize);
        startedEncodingAt = startStreamingAt;
        try {
            encoder = MediaCodec.createEncoderByType(AUDIO_MIME_TYPE);
            encoder.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        encoder.start();
        inputBuffers = encoder.getInputBuffers();
        outputBuffers = encoder.getOutputBuffers();
        isEncoding = true;
        drain();
    }

    @Override
    public void stop() {
        if (isEncoding) {
            int inputBufferId = encoder.dequeueInputBuffer(TIMEOUT_USEC);
            encoder.queueInputBuffer(inputBufferId, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
        }
    }

    @Override
    public boolean isEncoding() {
        return encoder != null && isEncoding;
    }

    /**
     * enqueue recorded dada from {@link AudioRecord}
     * the data will be drained from {@link AudioEncoder#drain()}
     */
    void enqueueData(byte[] data, int length) {
        if (encoder == null) return;
        int bufferRemaining;
        long timestamp = System.currentTimeMillis() - startedEncodingAt;
        int inputBufferId = encoder.dequeueInputBuffer(TIMEOUT_USEC);
        if (inputBufferId >= 0) {
            ByteBuffer inputBuf = inputBuffers[inputBufferId];
            inputBuf.clear();
            bufferRemaining = inputBuf.remaining();
            if (bufferRemaining < length) {
                inputBuf.put(data, 0, bufferRemaining);
            } else {
                inputBuf.put(data, 0, length);
            }
            encoder.queueInputBuffer(inputBufferId, 0, inputBuf.position(), timestamp * 1000, 0);
        }
    }

    /**
     * drain data from {@link MediaCodec}.
     * keep draining inside until it stops encoding.
     * so it would be good to use another thread for this method.
     */
    private void drain() {
        HandlerThread handlerThread = new HandlerThread("AudioEncoder-drain");
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                // keep running... so use a different thread.
                while (isEncoding) {
                    int outputBufferId = encoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                    if (outputBufferId >= 0) {
                        ByteBuffer encodedData = outputBuffers[outputBufferId];
                        if (encodedData == null) {
                            continue;
                        }

                        encodedData.position(bufferInfo.offset);
                        encodedData.limit(bufferInfo.offset + bufferInfo.size);

                        byte[] data = new byte[bufferInfo.size];

                        encodedData.get(data, 0, bufferInfo.size);
                        encodedData.position(bufferInfo.offset);

                        long currentTime = System.currentTimeMillis();
                        int timestamp = (int) (currentTime - startedEncodingAt);
                        listener.onAudioDataEncoded(data, bufferInfo.size, timestamp);

                        encoder.releaseOutputBuffer(outputBufferId, false);
                    } else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        // format should not be changed
                    }
                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        //end of stream
                        break;
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
