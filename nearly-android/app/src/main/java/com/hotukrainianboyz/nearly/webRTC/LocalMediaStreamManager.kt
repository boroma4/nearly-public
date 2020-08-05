package com.hotukrainianboyz.nearly.webRTC

import android.content.Context
import org.webrtc.*
import java.util.*


/**
 * High level controller for local audio and video
 */
class LocalMediaStreamManager(context: Context, eglBaseContext: EglBase.Context,private val peerConnectionFactory: PeerConnectionFactory) {

    private val videoCapturer: VideoCapturer = FrontCameraCapturerFactory().create(context) ?: throw RuntimeException("No front facing camera discovered")
    private val localAudioManager: LocalAudioManager = LocalAudioManager(context)
    private val height = 720
    private val width = 1280
    private val fps = 30
    private var videoManager: LocalVideoManager
    private var isMuted = false
    val mediaStream: MediaStream

    private lateinit var localAudioTrack: AudioTrack

    init{
        videoManager = LocalVideoManager(videoCapturer,height,width,fps)
        videoManager.init(peerConnectionFactory,eglBaseContext,context)
        mediaStream = peerConnectionFactory.createLocalMediaStream(UUID.randomUUID().toString())
        videoManager.addTrackToStream(mediaStream)
        initAudio()
    }

    fun addSink(surface: SurfaceViewRenderer){
        getVideoTrack().addSink(surface)
    }

    private fun getVideoTrack(): VideoTrack {
        return mediaStream.videoTracks[0]
    }

    fun toggleSelfMute(): Boolean {
        isMuted = !isMuted
        localAudioTrack.setEnabled(!isMuted)
        return isMuted
    }


    fun startCapture(){
        videoManager.startCapture()
        localAudioManager.start()
        localAudioTrack.setEnabled(true)
    }
    fun stopCapture(){
        videoManager.stopCapture()
        localAudioTrack.setEnabled(false)
        localAudioManager.stop()
    }
    fun dispose(){
        videoManager.dispose()
    }

    private fun initAudio(){
        val audioConstraints = MediaConstraints()
        val audioSource = peerConnectionFactory.createAudioSource(audioConstraints)
        localAudioTrack = peerConnectionFactory.createAudioTrack(UUID.randomUUID().toString(), audioSource)
        mediaStream.addTrack(localAudioTrack)
        videoManager.startCapture()
    }
}