package com.ukrainianboyz.nearly.webRTC

import android.content.Context
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory


class PeerFactory {

    fun createFactory(context: Context, rootEglBaseContext: EglBase.Context): PeerConnectionFactory{
        //Initialize PeerConnectionFactory globals.
        val initializationOptions = PeerConnectionFactory.InitializationOptions.builder(context).createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)

        //Create a new PeerConnectionFactory instance - using Hardware encoder and decoder.
        val options = PeerConnectionFactory.Options()
        val defaultVideoEncoderFactory = DefaultVideoEncoderFactory(
            rootEglBaseContext,  /* enableIntelVp8Encoder */
            true,  /* enableH264HighProfile */
            true
        )
        val defaultVideoDecoderFactory =
            DefaultVideoDecoderFactory(rootEglBaseContext)

        return PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoEncoderFactory(defaultVideoEncoderFactory)
            .setVideoDecoderFactory(defaultVideoDecoderFactory)
            .createPeerConnectionFactory()
    }

    // TODO: figure what are these settings :D
    fun createConfig(iceServers: List<PeerConnection.IceServer>): PeerConnection.RTCConfiguration {
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        // TCP candidates are only useful when connecting to a server that supports ICE-TCP.
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        // Use ECDSA encryption.
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA

        return rtcConfig
    }
}