package com.takusemba.rtmppublishersample

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.Snackbar.LENGTH_SHORT
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import com.takusemba.rtmppublisher.Publisher
import com.takusemba.rtmppublisher.PublisherListener

class MainActivity : AppCompatActivity(), PublisherListener {

    private lateinit var publisher: Publisher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val glView: GLSurfaceView = findViewById(R.id.surface_view)

        publisher = Publisher.Builder(this)
                .setGlView(glView)
                .setUrl(BuildConfig.STREAMING_URL)
                .setSize(Publisher.Builder.DEFAULT_WIDTH, Publisher.Builder.DEFAULT_HEIGHT)
                .setAudioBitrate(Publisher.Builder.DEFAULT_AUDIO_BITRATE)
                .setVideoBitrate(Publisher.Builder.DEFAULT_VIDEO_BITRATE)
                .setCameraMode(Publisher.Builder.DEFAULT_MODE)
                .setListener(this)
                .build()

        findViewById<Button>(R.id.toggle_publish).setOnClickListener {
            if (publisher.isPublishing) {
                publisher.stopPublishing()
            } else {
                publisher.startPublishing()
            }
        }

        findViewById<ImageView>(R.id.toggle_camera).setOnClickListener {
            publisher.switchCamera()
        }
    }

    override fun onResume() {
        super.onResume()
        updateControls()
    }

    private fun updateControls() {
        val publishButton: Button = findViewById(R.id.toggle_publish)
        publishButton.text = getString(if (publisher.isPublishing)
            R.string.stop_publishing else R.string.start_publishing)
    }

    override fun onStarted() {
        Snackbar.make(findViewById<RelativeLayout>(R.id.container), R.string.started_publishing, LENGTH_SHORT).show()
        updateControls()
    }

    override fun onStopped() {
        Snackbar.make(findViewById<RelativeLayout>(R.id.container), R.string.stopped_publishing, LENGTH_SHORT).show()
        updateControls()
    }

    override fun onDisconnected() {
        Snackbar.make(findViewById<RelativeLayout>(R.id.container), R.string.disconnected_publishing, LENGTH_SHORT).show()
        updateControls()
    }

    override fun onFailedToConnect() {
        Snackbar.make(findViewById<RelativeLayout>(R.id.container), R.string.failed_publishing, LENGTH_SHORT).show()
        updateControls()
    }
}