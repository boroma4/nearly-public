package com.hotukrainianboyz.nearly.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hotukrainianboyz.nearly.R
import com.hotukrainianboyz.nearly.service.signalR.ISingaling
import com.hotukrainianboyz.nearly.service.signalR.SignalRClient
import com.hotukrainianboyz.nearly.utils.activity.turnScreenOffAndKeyguardOn
import com.hotukrainianboyz.nearly.utils.activity.turnScreenOnAndKeyguardOff
import com.hotukrainianboyz.nearly.utils.webSocket.*
import com.hotukrainianboyz.nearly.webRTC.LocalMediaStreamManager
import com.hotukrainianboyz.nearly.webRTC.PeerConnectionManager
import com.hotukrainianboyz.nearly.webRTC.PeerFactory
import com.hotukrainianboyz.nearly.viewModel.FriendsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import org.webrtc.*
import java.util.*


class VideoCallActivity : AppCompatActivity(), ISingaling {

    private val permissionRequestActivityCode = 69
    private val eglBase = EglBase.create()
    private val eglBaseContext = eglBase.eglBaseContext
    private var isActivityInitialized = false
    private val friendsViewModel: FriendsViewModel by viewModels()
    private val callStatusTextView by lazy { findViewById<TextView>(R.id.call_status_text_view) }
    private val motionLayout by lazy { findViewById<MotionLayout>(R.id.video_call_motionlayout) }
    private val localVideoView by lazy { findViewById<SurfaceViewRenderer>(R.id.local_gl_surface_view)}
    private val remoteVideoView by lazy{ findViewById<SurfaceViewRenderer>(R.id.remote_gl_surface_view)}
    private val endCallButton by lazy {findViewById<FloatingActionButton>(R.id.end_call_btn)}
    private val muteSelfButton by lazy { findViewById<FloatingActionButton>(R.id.mute_self_btn)}
    private lateinit var localStreamManager: LocalMediaStreamManager
    private lateinit var peerConnectionManager: PeerConnectionManager
    private lateinit var peerConnectionFactory : PeerConnectionFactory
    private lateinit var calleeId: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        turnScreenOnAndKeyguardOff()
        if(havePermissions()) start() else requestPermissions()
    }

    //TODO: Consider case when paused during initialization
    override fun onPause() {
        super.onPause()
         // if not true, pause is probably called for permissions
        if(isActivityInitialized) {
            localStreamManager.stopCapture()
            SignalRClient.sendHold()
        }
    }

    override fun onResume() {
        super.onResume()
        if(isActivityInitialized) {
            localStreamManager.startCapture()
            SignalRClient.sendResume()
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "OnDestroy called")
        rtcCleanup()
        turnScreenOffAndKeyguardOn()
        super.onDestroy()
    }


    /**
     * Order matters!!! checking for init in case of early finish
     */
    private fun rtcCleanup(){
        if(this::peerConnectionManager.isInitialized) peerConnectionManager.dispose()
        if(this::localStreamManager.isInitialized) localStreamManager.dispose()
        if(this::peerConnectionFactory.isInitialized) peerConnectionFactory.dispose()
        localVideoView.release()
        remoteVideoView.release()
        eglBase.release()
        SignalRClient.stopCall()
    }


    private fun start(){
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_video_call)
        peerConnectionFactory = PeerFactory().createFactory(this,eglBaseContext)

        connectViews()
        initVideoView(remoteVideoView)
        initVideoView(localVideoView)
        localVideoView.setZOrderOnTop(true)
        try{
            localStreamManager = LocalMediaStreamManager(this,eglBaseContext,peerConnectionFactory)
        }catch (e: RuntimeException){
            val msg = if(e.message == null) "error on local media stream manager init" else e.message
            Log.e(TAG,msg!!)
            finish()
        }
        localStreamManager.addSink(localVideoView)
        localVideoView.setZOrderMediaOverlay(true)

        val extras = intent.extras
        calleeId = extras?.getString(FRIEND_ID).toString()
        val isInitiator = extras?.getBoolean(IS_INITIATOR)

        if(!calleeId.isBlank() && isInitiator != null){
            // Must be called before peerConnection manager init
            if(!SignalRClient.startCall(this,calleeId,isInitiator)){
                showFailedMessageAndFinish()
            }
        }else{
            Log.e(TAG, "Activity was initialized in an illegal way, exiting")
            finish()
        }

        // caller - send invite using firebase, await for offer to be received and call initLocalPeer there
        // callee - call initLocalPeer and start RTC connection routine through web socket
        if(!SignalRClient.initiatedCall){
            initLocalPeer()
            peerConnectionManager.sendOffer()
            callStatusTextView.setText(R.string.connecting)
        }else{
            friendsViewModel.sendCallCommand(calleeId, INIT_BACKEND_COMPAT){
                showFailedMessageAndFinish()
            }
            callStatusTextView.setText(R.string.waiting_for_confirmation)
        }
        SignalRClient.sendCallStatusUpdate(true)
        isActivityInitialized = true
    }

    //functionality will be extended
    private fun hangup(){
        Log.d(TAG,"Hanging up")
        friendsViewModel.sendCallCommand(calleeId, DISMISS){ /* if failed, do nothing */  }
        SignalRClient.sendCallStatusUpdate(false)
        Log.d(TAG,"Finishing activity")
        finish()
    }

    override fun onRemoteHangUp() {
        Log.d(TAG,"Remote hangup")
        lifecycleScope.launch(Dispatchers.Main){
            callStatusTextView.setText(R.string.finishing_call)
        }
        // show some summary screen here
        hangup()
    }


    /**
     * When receive offer, create answer and send it back
     */
    override fun onOfferReceived(data: SessionDescriptionMessage) {
        val description = SessionDescription(SessionDescription.Type.OFFER, data.sdp)
        initLocalPeer()
        lifecycleScope.launch(Dispatchers.Main) {
            callStatusTextView.setText(R.string.connecting)
        }
        peerConnectionManager.setRemoteDescription(description)
        //answering the offer
        peerConnectionManager.sendAnswer()
    }
    /**
     * When an answer is received, call is almost ready to start
     */
    override fun onAnswerReceived(data: SessionDescriptionMessage) {
        val description = SessionDescription(SessionDescription.Type.fromCanonicalForm(
            data.type.toLowerCase(Locale.ROOT)), data.sdp)

        peerConnectionManager.setRemoteDescription(description)
    }

    /**
     * When got ice candidate from peer - can start the call
     */
    override fun onIceCandidateReceived(data: IceCandidateMessage) {
        peerConnectionManager.addIceCandidate(data)
    }

    override fun onHoldReceived() {
        lifecycleScope.launch(Dispatchers.Main) {
            callStatusTextView.setText(R.string.waiting_for_friend)
            motionLayout.transitionToStart()
        }
    }

    override fun onResumeReceived() {
        lifecycleScope.launch(Dispatchers.Main) { motionLayout.transitionToEnd() }
    }

    private fun initLocalPeer(){
        peerConnectionManager = PeerConnectionManager(peerConnectionFactory)
        val onRemoteStream = {ms:MediaStream -> ms.videoTracks[0].addSink(remoteVideoView) }

        val onConnected = {
            lifecycleScope.launch(Dispatchers.Main) {
                motionLayout.transitionToEnd()
            }
        }
        peerConnectionManager.initPeer(localStreamManager.mediaStream,onRemoteStream,onConnected)
    }

    private fun initVideoView(video: SurfaceViewRenderer){
        video.init(eglBaseContext, null)
        video.setEnableHardwareScaler(true)
        video.setMirror(true)
        video.visibility = View.VISIBLE
    }

    private fun connectViews(){
        endCallButton.setOnClickListener{hangup()}
        muteSelfButton.setOnClickListener {
            if(localStreamManager.toggleSelfMute()){
                muteSelfButton.setImageDrawable(this.getDrawable(R.drawable.ic_baseline_volume_off_24))
            }else{
                muteSelfButton.setImageDrawable(this.getDrawable(R.drawable.ic_baseline_volume_up_24))
            }
        }
    }

    private fun havePermissions(): Boolean{
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
            ActivityCompat.requestPermissions(this,
                listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO).toTypedArray(), permissionRequestActivityCode)
    }

    private fun showFailedMessageAndFinish(){
        lifecycleScope.launch(Dispatchers.Main){
            callStatusTextView.setText(R.string.initialization_failed)
            delay(3000)
            finish()
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestActivityCode && grantResults.size == 2
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
            && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            start()
        } else {
            finish()
        }
    }

    companion object {
        const val IS_INITIATOR = "isInitiator"
        const val TAG = "VideoCallActivity"
        fun makeIntent(ctx: Context, friendId: String, isInitiator: Boolean): Intent {
            val intent = Intent(ctx, VideoCallActivity::class.java)
            intent.putExtra(FRIEND_ID, friendId)
            intent.putExtra(IS_INITIATOR, isInitiator)
            return intent
        }
    }
}
