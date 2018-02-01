package com.takusemba.rtmppublisher;

import android.opengl.EGLContext;
import android.view.Surface;

import com.takusemba.rtmppublisher.gles.EglCore;
import com.takusemba.rtmppublisher.gles.FullFrameRect;
import com.takusemba.rtmppublisher.gles.Texture2dProgram;
import com.takusemba.rtmppublisher.gles.WindowSurface;

class VideoRenderer {

    private WindowSurface inputWindowSurface;
    private EglCore eglCore;
    private FullFrameRect fullScreen;

    void initialize(EGLContext sharedContext, Surface encoderSurface) {
        eglCore = new EglCore(sharedContext, EglCore.FLAG_RECORDABLE);
        inputWindowSurface = new WindowSurface(eglCore, encoderSurface, true);
        inputWindowSurface.makeCurrent();
        fullScreen = new FullFrameRect(new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT));
    }

    void draw(final int textureId, final float[] transform, final long timestampNanos) {
        fullScreen.drawFrame(textureId, transform);
        inputWindowSurface.setPresentationTime(timestampNanos);
        inputWindowSurface.swapBuffers();
    }

    void release() {
        if (inputWindowSurface != null) {
            inputWindowSurface.release();
            inputWindowSurface = null;
        }
        if (fullScreen != null) {
            fullScreen.release(false);
            fullScreen = null;
        }
        if (eglCore != null) {
            eglCore.release();
            eglCore = null;
        }
    }

    boolean isInitialized() {
        return inputWindowSurface != null && fullScreen != null && eglCore != null;
    }
}
