package com.takusemba.rtmppublishersample

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.widget.*
import com.takusemba.rtmppublisher.Publisher
import com.takusemba.rtmppublisher.PublisherListener

class MainActivity : AppCompatActivity(), PublisherListener {

    private lateinit var publisher: Publisher
    private lateinit var glView: GLSurfaceView
    private lateinit var container: RelativeLayout
    private lateinit var button: Button
    private lateinit var label: TextView

    private val handler = Handler()
    private var thread: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        glView = findViewById(R.id.surface_view)
        container = findViewById(R.id.container)
        button = findViewById(R.id.toggle_publish)
        label = findViewById(R.id.live_label)


        publisher = Publisher.Builder(this)
                .setGlView(glView)
                .setUrl(BuildConfig.STREAMING_URL)
                .setSize(Publisher.Builder.DEFAULT_WIDTH, Publisher.Builder.DEFAULT_HEIGHT)
                .setAudioBitrate(Publisher.Builder.DEFAULT_AUDIO_BITRATE)
                .setVideoBitrate(Publisher.Builder.DEFAULT_VIDEO_BITRATE)
                .setCameraMode(Publisher.Builder.DEFAULT_MODE)
                .setListener(this)
                .build()

        button.setOnClickListener {
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

    override fun onStarted() {
        Toast.makeText(this, R.string.started_publishing, Toast.LENGTH_SHORT)
                .apply { setGravity(Gravity.CENTER, 0, 0) }
                .run { show() }
        updateControls()
        startCounting()
    }

    override fun onStopped() {
        Toast.makeText(this, R.string.stopped_publishing, Toast.LENGTH_SHORT)
                .apply { setGravity(Gravity.CENTER, 0, 0) }
                .run { show() }
        updateControls()
        stopCounting()
    }

    override fun onDisconnected() {
        Toast.makeText(this, R.string.disconnected_publishing, Toast.LENGTH_SHORT)
                .apply { setGravity(Gravity.CENTER, 0, 0) }
                .run { show() }
        updateControls()
        stopCounting()
    }

    override fun onFailedToConnect() {
        Toast.makeText(this, R.string.failed_publishing, Toast.LENGTH_SHORT)
                .apply { setGravity(Gravity.CENTER, 0, 0) }
                .run { show() }
        updateControls()
        stopCounting()
    }

    private fun updateControls() {
        button.text = getString(if (publisher.isPublishing) R.string.stop_publishing else R.string.start_publishing)
    }

    private fun startCounting() {
        label.text = getString(R.string.publishing_label, 0L.format(), 0L.format())
        label.visibility = View.VISIBLE
        val startedAt = System.currentTimeMillis()
        var updatedAt = System.currentTimeMillis()
        thread = Thread {
            while (true) {
                val current = System.currentTimeMillis()
                if (current - updatedAt > 1000) {
                    updatedAt = current
                    handler.post {
                        val diff = current - startedAt
                        val second = diff / 1000
                        val min = second / 60
                        label.text = getString(R.string.publishing_label, min.format(), second.format())
                    }
                }
            }
        }
        thread?.start()
    }

    private fun stopCounting() {
        label.text = ""
        label.visibility = View.GONE
        thread?.interrupt()
    }

    private fun Long.format(): String {
        return String.format("%02d", this)
    }
}