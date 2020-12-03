package com.ukrainianboyz.nearly.webRTC

import android.content.Context
import android.util.Log
import org.webrtc.*
import java.util.*


/**
 * Local video controller for starting, stopping etc
 */
class LocalVideoManager(private val capturer: VideoCapturer, private val width: Int, private val height: Int, private val fps: Int)  {

    private val TAG: String = LocalVideoManager::class.java.simpleName

    private var isCapturing = false
    private lateinit var source: VideoSource
    private lateinit var track: VideoTrack


     fun init(factory: PeerConnectionFactory, eglBaseContext: EglBase.Context, context: Context) {
        Log.d(TAG, "init local video")
        val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext)
        source = factory.createVideoSource(capturer.isScreencast)
        capturer.initialize(surfaceTextureHelper, context, source.capturerObserver)
        startCapture()
        track = factory.createVideoTrack(UUID.randomUUID().toString(), source)
        track.setEnabled(true)
    }

     fun addTrackToStream(stream: MediaStream) {
        Log.d(TAG, "attach local video track to stream")
        stream.addTrack(track)
    }

     fun startCapture() {
        if (!isCapturing) {
            capturer.startCapture(width, height, fps)
            isCapturing = true
        }
    }

     fun stopCapture() {
        if (isCapturing) {
            isCapturing = false
            try {
                capturer.stopCapture()
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
        }
    }

     fun dispose() {
        stopCapture()
        capturer.dispose()
        source.dispose()
    }
}