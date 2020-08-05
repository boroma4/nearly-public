package com.hotukrainianboyz.nearly.webRTC

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.util.Log
import org.webrtc.ThreadUtils
import java.util.*


class LocalAudioManager(context: Context) {
    /**
     * AudioDevice is the names of possible audio devices that we currently
     * support.
     */
    enum class AudioDevice {
        SPEAKER_PHONE, WIRED_HEADSET, EARPIECE, BLUETOOTH, NONE
    }

    /** AudioManager state.  */
    enum class AudioManagerState {
        UNINITIALIZED, RUNNING
    }

    private val appContext: Context
    private val audioManager: AudioManager
    private var amState: AudioManagerState
    private var savedAudioMode = AudioManager.MODE_INVALID
    private var savedIsSpeakerPhoneOn = false
    private var savedIsMicrophoneMute = false
    private var hasWiredHeadset = false

    // Contains the currently selected audio device.
    // This device is changed automatically using a certain scheme where e.g.
    // a wired headset "wins" over speaker phone. It is also possible for a
    // user to explicitly select a device (and override any predefined scheme).
    // See |userSelectedAudioDevice| for details.
    private lateinit var selectedAudioDevice: AudioDevice

    // Handles all tasks related to Bluetooth headset devices.
    private val bluetoothManager: BluetoothManager

    // Contains a list of available audio devices. A Set collection is used to
    // avoid duplicate elements.
    private var audioDevices: MutableSet<AudioDevice> = HashSet()

    // Broadcast receiver for wired headset intent broadcasts.
    private val wiredHeadsetReceiver: BroadcastReceiver

    init {
        Log.d(TAG, "ctor")
        appContext = context
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        bluetoothManager = BluetoothManager(context, this)
        wiredHeadsetReceiver = WiredHeadsetReceiver()
        amState = AudioManagerState.UNINITIALIZED
    }



    fun start(){
        if (amState == AudioManagerState.RUNNING) {
            Log.e(TAG, "AudioManager is already active")
            return
        }
        Log.d(TAG, "AudioManager starts...")
        amState = AudioManagerState.RUNNING

        // Store current audio state so we can restore it when stop() is called.
        savedAudioMode = audioManager.mode
        savedIsSpeakerPhoneOn = audioManager.isSpeakerphoneOn
        savedIsMicrophoneMute = audioManager.isMicrophoneMute
        hasWiredHeadset = hasWiredHeadset()

        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        // Always disable microphone mute during a WebRTC call.
        setMicrophoneMute(false)

        // Set initial device states.
        selectedAudioDevice = AudioDevice.NONE
        audioDevices.clear()

        // Initialize and start Bluetooth if a BT device is available or initiate
        // detection of new (enabled) BT devices.
        bluetoothManager.start()

        // Do initial selection of audio device. This setting can later be changed
        // either by adding/removing a BT or wired headset
        updateAudioDeviceState()

        // Register receiver for broadcast intents related to adding/removing a
        // wired headset.
        registerReceiver(wiredHeadsetReceiver, IntentFilter(Intent.ACTION_HEADSET_PLUG))
        Log.d(TAG, "AudioManager started")
    }

    fun stop() {
        Log.d(TAG, "stop")
        ThreadUtils.checkIsOnMainThread()
        if (amState != AudioManagerState.RUNNING) {
            Log.e(TAG, "Trying to stop AudioManager in incorrect state: $amState")
            return
        }
        amState = AudioManagerState.UNINITIALIZED
        unregisterReceiver(wiredHeadsetReceiver)
        bluetoothManager.stop()

        // Restore previously stored audio states.
        setSpeakerphoneOn(savedIsSpeakerPhoneOn)
        setMicrophoneMute(savedIsMicrophoneMute)
        audioManager.mode = savedAudioMode

        Log.d(TAG, "AudioManager stopped")
    }

    /** Changes selection of the currently active audio device.  */
    private fun setAudioDeviceInternal(device: AudioDevice) {

        Log.d(TAG, "setAudioDeviceInternal(device=$device)")
        when (device) {
            AudioDevice.SPEAKER_PHONE -> setSpeakerphoneOn(true)
            AudioDevice.EARPIECE -> setSpeakerphoneOn(false)
            AudioDevice.WIRED_HEADSET -> setSpeakerphoneOn(false)
            AudioDevice.BLUETOOTH -> setSpeakerphoneOn(false)
            else -> Log.e(TAG, "Invalid audio device selection")
        }
        selectedAudioDevice = device
    }


    /** Helper method for receiver registration.  */
    private fun registerReceiver(receiver: BroadcastReceiver, filter: IntentFilter) {
        appContext.registerReceiver(receiver, filter)
    }

    /** Helper method for unregistration of an existing receiver.  */
    private fun unregisterReceiver(receiver: BroadcastReceiver) {
        appContext.unregisterReceiver(receiver)
    }

    /** Sets the speaker phone mode.  */
    private fun setSpeakerphoneOn(on: Boolean) {
        val wasOn = audioManager.isSpeakerphoneOn
        if (wasOn == on) {
            return
        }
        audioManager.isSpeakerphoneOn = on
    }

    /** Sets the microphone mute state.  */
    private fun setMicrophoneMute(on: Boolean) {
        val wasMuted = audioManager.isMicrophoneMute
        if (wasMuted == on) {
            return
        }
        audioManager.isMicrophoneMute = on
    }

    /**
     * Checks whether a wired headset is connected or not.
     * This is not a valid indication that audio playback is actually over
     * the wired headset as audio routing depends on other conditions. We
     * only use it as an early indicator (during initialization) of an attached
     * wired headset.
     */
    private fun hasWiredHeadset(): Boolean {
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_ALL)
        for (device in devices) {
            when (device.type) {
                AudioDeviceInfo.TYPE_WIRED_HEADSET -> {
                    Log.d(TAG, "hasWiredHeadset: found wired headset")
                    return true
                }
                AudioDeviceInfo.TYPE_USB_DEVICE -> {
                    Log.d(TAG, "hasWiredHeadset: found USB audio device")
                    return true
                }
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> {
                    Log.d(TAG, "hasWiredHeadset: found wired headphones")
                    return true
                }
            }
        }
        return false
    }

    /**
     * Updates list of possible audio devices and make new device selection.
     */
    fun updateAudioDeviceState() {
        Log.d(TAG, "--- updateAudioDeviceState: "
                    + "wired headset=" + hasWiredHeadset + ", "
                    + "BT state=" + bluetoothManager.bluetoothState
        )
        Log.d(TAG, "Device status: "
                    + "available=" + audioDevices + ", "
                    + "selected=" + selectedAudioDevice + ", "
        )

        if (bluetoothManager.bluetoothState === BluetoothManager.State.HEADSET_AVAILABLE || bluetoothManager.bluetoothState === BluetoothManager.State.HEADSET_UNAVAILABLE || bluetoothManager.bluetoothState === BluetoothManager.State.SCO_DISCONNECTING
        ) {
            bluetoothManager.updateDevice()
        }

        // Update the set of available audio devices.
        val newAudioDevices: MutableSet<AudioDevice> = HashSet()
        if (bluetoothManager.bluetoothState === BluetoothManager.State.SCO_CONNECTED || bluetoothManager.bluetoothState === BluetoothManager.State.SCO_CONNECTING || bluetoothManager.bluetoothState === BluetoothManager.State.HEADSET_AVAILABLE
        ) {
            newAudioDevices.add(AudioDevice.BLUETOOTH)
        }
        if (hasWiredHeadset) {
            // If a wired headset is connected, then it is the only possible option.
            newAudioDevices.add(AudioDevice.WIRED_HEADSET)
        } else {
            // No wired headset, hence the audio-device list can contain speaker
            // phone (on a tablet), or speaker phone and earpiece (on mobile phone).
            newAudioDevices.add(AudioDevice.SPEAKER_PHONE)
        }
        // Store state which is set to true if the device list has changed.
        val audioDeviceSetUpdated = audioDevices != newAudioDevices
        // Update the existing audio device set.
        audioDevices = newAudioDevices

        // Update selected audio device.
        val newAudioDevice: AudioDevice = when {
            bluetoothManager.bluetoothState === BluetoothManager.State.SCO_CONNECTED -> {
                // If a Bluetooth is connected, then it should be used as output audio
                // device. Note that it is not sufficient that a headset is available;
                // an active SCO channel must also be up and running.
                AudioDevice.BLUETOOTH
            }
            hasWiredHeadset -> AudioDevice.WIRED_HEADSET
            else -> AudioDevice.SPEAKER_PHONE
        }
        // Switch to new device but only if there has been any changes.
        if (newAudioDevice != selectedAudioDevice || audioDeviceSetUpdated) {
            // Do the required device switch.
            setAudioDeviceInternal(newAudioDevice)
            Log.d(TAG, "New device status: "
                        + "available=" + audioDevices + ", "
                        + "selected=" + newAudioDevice
            )
        }
        Log.d(TAG, "--- updateAudioDeviceState done")
    }

    /* Receiver which handles changes in wired headset availability. */
    private inner class WiredHeadsetReceiver : BroadcastReceiver() {

        private val STATE_UNPLUGGED = 0
        private val STATE_PLUGGED = 1

        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            val state = intent.getIntExtra("state", STATE_UNPLUGGED)

            hasWiredHeadset = state == STATE_PLUGGED
            updateAudioDeviceState()
        }
    }


    companion object {
        private const val TAG = "LocalAudioManager"
    }
}