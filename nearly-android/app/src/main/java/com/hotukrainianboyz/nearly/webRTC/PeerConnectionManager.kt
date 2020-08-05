package com.hotukrainianboyz.nearly.webRTC

import android.util.Log
import com.hotukrainianboyz.nearly.service.signalR.SignalRClient
import com.hotukrainianboyz.nearly.utils.observer.CustomPeerConnectionObserver
import com.hotukrainianboyz.nearly.utils.observer.CustomSdpObserver
import com.hotukrainianboyz.nearly.utils.webSocket.IceCandidateMessage
import kotlinx.coroutines.Job
import org.webrtc.*


class PeerConnectionManager(
    private val peerConnectionFactory: PeerConnectionFactory
) {
    private val peerIceServers = mutableListOf<PeerConnection.IceServer>()
    private val loggerSdpObserver = CustomSdpObserver()
    private lateinit var localPeer: PeerConnection

    /**
     * Init connection and add local stream
     */
    fun initPeer( localStream: MediaStream,onRemoteStream:(MediaStream) -> Unit, onConnected: () -> Job){
        peerIceServers.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer())

        initLocalPeer(onRemoteStream,onConnected)
        localPeer.addStream(localStream)
    }

    /**
     *  Disposal
     */
    fun dispose(){
        localPeer.dispose()
    }

    fun sendOffer(){
        val sdpConstraints = MediaConstraints()
        sdpConstraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        sdpConstraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        Log.d(TAG,"Callee starting RTC, sending offer")
        localPeer.createOffer(object : CustomSdpObserver() {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                super.onCreateSuccess(sessionDescription)
                localPeer.setLocalDescription(CustomSdpObserver(), sessionDescription)
                SignalRClient.sendDescription(sessionDescription)
            }
        }, sdpConstraints)
    }

    fun sendAnswer(){
        localPeer.createAnswer(object : CustomSdpObserver() {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                super.onCreateSuccess(sessionDescription)
                localPeer.setLocalDescription(loggerSdpObserver, sessionDescription)
                SignalRClient.sendDescription(sessionDescription)
            }
        }, MediaConstraints())
    }

    fun setRemoteDescription(description: SessionDescription){
        localPeer.setRemoteDescription(loggerSdpObserver, description)
    }

    fun addIceCandidate(data: IceCandidateMessage){
        localPeer.addIceCandidate(IceCandidate(data.id, data.label, data.candidate))
    }

    private fun initLocalPeer(onRemoteStream:(MediaStream) -> Unit, onConnected:()->Job){
        val config = PeerFactory().createConfig(peerIceServers)
        val lp = peerConnectionFactory.createPeerConnection(
            config, object : CustomPeerConnectionObserver() {
                override fun onIceCandidate(iceCandidate: IceCandidate) {
                    super.onIceCandidate(iceCandidate)
                    SignalRClient.sendIceCandidate(iceCandidate)
                }
                override fun onAddStream(mediaStream: MediaStream) {
                    super.onAddStream(mediaStream)
                    Log.d(TAG,"got stream")
                    onRemoteStream(mediaStream)
                }

                override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
                    super.onIceConnectionChange(iceConnectionState)
                    if(iceConnectionState == PeerConnection.IceConnectionState.CONNECTED){
                        onConnected()
                    }
                }
            }
        )
        if (lp != null) localPeer = lp else throw RuntimeException("shit hit the fan while initializing local peer")
    }

    companion object{
        private const val TAG = "PeerConnectionManager"
    }
}