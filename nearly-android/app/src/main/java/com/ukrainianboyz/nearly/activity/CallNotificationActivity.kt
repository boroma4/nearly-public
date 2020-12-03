package com.ukrainianboyz.nearly.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.ukrainianboyz.nearly.ApplicationPrefs
import com.ukrainianboyz.nearly.R
import com.ukrainianboyz.nearly.dao.FriendsDao
import com.ukrainianboyz.nearly.database.AppRoomDatabase
import com.ukrainianboyz.nearly.service.signalR.SignalRClient
import com.ukrainianboyz.nearly.utils.activity.turnScreenOffAndKeyguardOn
import com.ukrainianboyz.nearly.utils.activity.turnScreenOnAndKeyguardOff
import com.ukrainianboyz.nearly.utils.image.ImageUtils
import com.ukrainianboyz.nearly.utils.json.JsonUtils
import com.ukrainianboyz.nearly.utils.preferences.stringLiveData
import com.ukrainianboyz.nearly.utils.webSocket.CALLER_ID
import com.ukrainianboyz.nearly.utils.webSocket.DISMISS_SELF
import com.ukrainianboyz.nearly.viewModel.CallNotificationViewModel
import com.ukrainianboyz.nearly.viewModel.FriendsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.properties.Delegates


/**
 * Here we can assume that if user received this activity, he is logged in and subscribed to notifications
 */
class CallNotificationActivity : AppCompatActivity() {

    private val callerNameTextView by lazy { findViewById<TextView>(R.id.call_notification_username) }
    private val callerAvatarImageView by lazy { findViewById<ImageView>(R.id.call_notification_user_avatar) }
    private val acceptButton by lazy { findViewById<Button>(R.id.accept_call) }
    private val declineButton by lazy { findViewById<Button>(R.id.decline_call) }
    private val friendsViewModel: FriendsViewModel by viewModels()
    private var wasInForeground by Delegates.notNull<Boolean>()
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var callerId: String
    private lateinit var friendsDao : FriendsDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getExtras()
        turnScreenOnAndKeyguardOff()
        connectToSignalRAsync()
        setContentView(R.layout.activity_call_notification)
        setupAudioAndVibration()
        setupNotificationData()
        connectActions()
        // if caller cancelled call
        CallNotificationViewModel.inComingCalls.observe(this, Observer { notifications ->
            Log.d(TAG, "Got a change")
            if (!notifications.contains(callerId)) close()
        })
    }

    override fun onDestroy() {
        turnScreenOffAndKeyguardOn()
        CallNotificationViewModel.removeCall(callerId)
        mediaPlayer.release()
        super.onDestroy()
    }

    private fun getExtras() {
        intent.extras?.let { extras ->
            extras.getString(CALLER_ID)?.let {
                callerId = it
            } ?: run {
                Log.d(TAG, "Call notification activity started illegally, exiting..")
                close()
            }
            wasInForeground = extras.getBoolean(WAS_IN_FOREGROUND)
        } ?: run {
            Log.d(TAG, "Call notification activity started illegally, exiting..")
            close()
        }
    }

    private fun setupAudioAndVibration() {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mediaPlayer = MediaPlayer.create(this, R.raw.ringtone)

        when (am.ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> { }
            AudioManager.RINGER_MODE_VIBRATE -> vibrate()
            AudioManager.RINGER_MODE_NORMAL -> {
                playSound()
                vibrate()
            }
        }
    }

    private fun playSound() {
        mediaPlayer.isLooping = true
        mediaPlayer.start()
    }

    private fun vibrate() {
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        lifecycleScope.launch(Dispatchers.Default) {
            while (true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(
                        VibrationEffect.createOneShot(
                            VIBRATION_DURATION_MS,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } else {
                    @Suppress("Deprecation")
                    v.vibrate(VIBRATION_DURATION_MS)
                }
                delay(VIBRATION_DELAY_MS)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupNotificationData() {
        friendsDao = AppRoomDatabase.getDatabase(this).friendsDao()
        lifecycleScope.launch(Dispatchers.Default) {
            val friend = friendsDao.getFriendById(callerId)
            friend?.let {
                launch(Dispatchers.Main) {
                    ImageUtils.loadCircularImage(it.imageUrl, callerAvatarImageView)
                    callerNameTextView.text = "${it.name} ${getString(R.string.calling)}"
                }
            } ?: close()
        }

    }

    private fun connectActions() {
        acceptButton.setOnClickListener {
            val intent = VideoCallActivity.makeIntent(this, callerId, false)
            friendsViewModel.sendCallCommand(callerId, DISMISS_SELF) { /* if failed, do nothing */ }
            startActivity(intent)
            finish()
        }
        declineButton.setOnClickListener {
            friendsViewModel.sendCallCommand(callerId, DISMISS_SELF)
            SignalRClient.sendHangup(callerId)
            close()
        }
    }

    private fun connectToSignalRAsync() {
        val prefs = ApplicationPrefs.encryptedPrefs
        val userLiveData = prefs.stringLiveData(ApplicationPrefs.USER_KEY, "")
        lifecycleScope.launch(Dispatchers.IO) {
            userLiveData.value?.let {
                val id = JsonUtils.jsonToUser(it).id
                SignalRClient.connectToHub(applicationContext, id)
            } ?: run {
                close()    // if somehow there is no user, finish activity
            }
        }
    }

    private fun close() {
        if (wasInForeground) finish() else finishAffinity()
    }

    companion object {
        private const val TAG = "CallNotification"
        private const val WAS_IN_FOREGROUND = "wasInFg"
        private const val VIBRATION_DURATION_MS = 500L
        private const val VIBRATION_DELAY_MS = 1500L

        fun makeIntent(context: Context, callerId: String, wasInForeground: Boolean): Intent {
            val intent = Intent(context, CallNotificationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(CALLER_ID, callerId)
            intent.putExtra(WAS_IN_FOREGROUND, wasInForeground)
            return intent
        }
    }
}