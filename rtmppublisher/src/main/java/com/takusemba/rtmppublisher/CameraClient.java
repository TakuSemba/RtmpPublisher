package com.takusemba.rtmppublisher;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import java.io.IOException;

import static android.content.Context.WINDOW_SERVICE;

class CameraClient {

  private Context context;
  private Camera camera;

  private static final int desiredHeight = 1280;
  private static final int desiredWidth = 720;

  CameraClient(Context context) {
    this.context = context;
  }

  Camera.Parameters open() {

    Camera.CameraInfo info = new Camera.CameraInfo();

    int numCameras = Camera.getNumberOfCameras();
    for (int i = 0; i < numCameras; i++) {
      Camera.getCameraInfo(i, info);
      if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
        camera = Camera.open(i);
        break;
      }
    }
    if (camera == null) {
      camera = Camera.open();
    }
    if (camera == null) {
      throw new RuntimeException("Unable to open camera");
    }

    Camera.Parameters params = camera.getParameters();

    boolean isDesiredSizeFound = false;
    for (Camera.Size size : params.getSupportedPreviewSizes()) {
      if (size.width == desiredWidth && size.height == desiredHeight) {
        params.setPreviewSize(desiredWidth, desiredHeight);
        isDesiredSizeFound = true;
      }
    }

    if (!isDesiredSizeFound) {
      Camera.Size ppsfv = params.getPreferredPreviewSizeForVideo();
      if (ppsfv != null) {
        params.setPreviewSize(ppsfv.width, ppsfv.height);
      }
    }

    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
    params.setRecordingHint(true);

    camera.setParameters(params);

    int[] fpsRange = new int[2];
    params.getPreviewFpsRange(fpsRange);

    Display display =
        ((WindowManager) context.getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

    if (display.getRotation() == Surface.ROTATION_0) {
      camera.setDisplayOrientation(90);
    } else if (display.getRotation() == Surface.ROTATION_270) {
      camera.setDisplayOrientation(180);
    }

    return params;
  }

  void startPreview(SurfaceTexture surfaceTexture) {
    try {
      camera.setPreviewTexture(surfaceTexture);
      camera.startPreview();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  void close() {
    if (camera != null) {
      camera.stopPreview();
      camera.release();
      camera = null;
    }
  }
}
