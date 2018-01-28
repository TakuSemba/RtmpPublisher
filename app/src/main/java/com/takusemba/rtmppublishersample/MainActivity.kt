package com.takusemba.rtmppublishersample

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.takusemba.rtmppublisher.RtmpPublisher

class MainActivity : AppCompatActivity() {

  private val publisher = RtmpPublisher()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val glView: GLSurfaceView = findViewById(R.id.surface_view)
    publisher.initialize(this, glView)

    val toggle: Button = findViewById(R.id.toggle_publish)
    toggle.setOnClickListener {
      if (publisher.isPublishing) {
        publisher.stopPublishing()
      } else {
        publisher.startPublishing(BuildConfig.STREAMING_URL)
      }
      updateControls()
    }
  }

  override fun onResume() {
    super.onResume()
    updateControls()
  }

  private fun updateControls() {
    val toggleRelease: Button = findViewById(R.id.toggle_publish)
    toggleRelease.text = if (publisher.isPublishing) "Stop publishing" else "Start publishing"
  }
}