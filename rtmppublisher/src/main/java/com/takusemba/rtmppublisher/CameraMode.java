package com.takusemba.rtmppublisher;

import android.hardware.Camera;

public enum CameraMode {
    FRONT, BACK;

    int getId() {
        switch (this) {
            case FRONT:
                return Camera.CameraInfo.CAMERA_FACING_FRONT;
            case BACK:
                return Camera.CameraInfo.CAMERA_FACING_BACK;
            default:
                return Camera.CameraInfo.CAMERA_FACING_BACK;
        }
    }

    CameraMode swap() {
        switch (this) {
            case FRONT:
                return BACK;
            case BACK:
                return FRONT;
            default:
                throw new IllegalStateException("mode is not set");
        }
    }
}
