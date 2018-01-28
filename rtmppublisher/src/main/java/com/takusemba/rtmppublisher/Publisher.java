package com.takusemba.rtmppublisher;

import android.arch.lifecycle.Lifecycle;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;

interface Publisher {

  /**
   * initialize the Publisher.
   * after the initialization, this will display a preview from camera.
   * GLSurfaceView is needed to publish video data with RTMP.
   *
   * @param activity this need to be {@link AppCompatActivity} to use {@link Lifecycle}
   * @param glView GLSurfaceView to show display.
   */
  void initialize(AppCompatActivity activity, GLSurfaceView glView);

  /**
   * start publishing video and audio data
   *
   * @param url url to stream data.
   */
  void startPublishing(String url);

  /**
   * start publishing video and audio data
   *
   * @param url see {@link Publisher#startPublishing(String)}
   * @param width width of the video frame
   * @param height height of the video frame
   */
  void startPublishing(String url, int width, int height);

  /**
   * start publishing video and audio data
   *
   * @param url see {@link Publisher#startPublishing(String)}
   * @param width see {@link Publisher#startPublishing(String, int, int)}
   * @param height see {@link Publisher#startPublishing(String, int, int)}
   * @param audioBitrate audio bitrate used to stream audio data
   * @param videoBitrate video bitrate used to stream video data
   */
  void startPublishing(String url, final int width, final int height, final int audioBitrate,
      final int videoBitrate);

  /**
   * stop publishing video and audio data.
   */
  void stopPublishing();

  /**
   * @return if the Publisher is publishing data.
   */
  boolean isPublishing();
}
