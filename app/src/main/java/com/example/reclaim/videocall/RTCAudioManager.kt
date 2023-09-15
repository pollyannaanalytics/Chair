package com.example.reclaim.videocall

import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log

import org.jetbrains.annotations.Nullable
import org.webrtc.ThreadUtils

class RTCAudioManager(context: Context) {

    private var audioManager: AudioManager? = null
    private var apprtcContext: Context? = null
    private var amState: AudioManagerState? = null
    private var useSpeakerphone: String? = null
    private var savedIsSpeakerPhoneOn = false
    private var savedAudioMode = AudioManager.MODE_INVALID


    // Broadcast receiver for wired headset intent broadcasts.
    private var wiredHeadsetReceiver: BroadcastReceiver? = null


    // name of possible audio devices currently support.
    enum class AudioDevice {
        SPEAKER_PHONE, WIRED_HEADSET, EARPIECE, NONE
    }

    companion object {
        private const val TAG = "AppRTCAudioManager"
        private val SPEAKPHONE_AUTO = "auto"
        private val SPEAKERPHONE_TRUE = "true"
        private val SPEAKERPHONE_FALSE = "false"

        fun create(context: Context): RTCAudioManager{
            return RTCAudioManager(context)
        }
    }


    // AudioManager state
    enum class AudioManagerState{
        UNINITIALIZED, PREINITIALIZED, RUNNING
    }


    // device change event
    interface AudioManagerEvents{
        // Callback fired once audio device is changed or list of available audio devices changed.
        fun onAudioDeviceChanged(
            selectedAudioDevice: AudioDevice?, availableAudioDevice: Set<AudioDevice?>?
        )
    }

    fun start(audioManagerEvents: AudioManagerEvents?) {
        Log.d(TAG, "start")
        ThreadUtils.checkIsOnMainThread()
        if (amState == AudioManagerState.RUNNING) {
            Log.e(TAG, "AudioManager is already active")
            return
        }
//        else if (amState == AudioManagerState.UNINITIALIZED) {
//            preInitAudio()
//        }
        // TODO perhaps call new method called preInitAudio() here if UNINITIALIZED.
        Log.d(TAG, "AudioManager starts...")
        this.audioManagerEvents = audioManagerEvents
        amState = AudioManagerState.RUNNING

        // Store current audio state so we can restore it when stop() is called.
        if(audioManager !== null){
            savedAudioMode = audioManager!!.mode!!
            savedIsSpeakerPhoneOn = audioManager!!.isSpeakerphoneOn
            savedIsMicrophoneMute = audioManager!!.isMicrophoneMute
            hasWiredHeadset = hasWiredHeadset() == true
        }


        // Create an AudioManager.OnAudioFocusChangeListener instance.
        audioFocusChangeListener =
            AudioManager.OnAudioFocusChangeListener { focusChange ->

                // Called on the listener to notify if the audio focus for this listener has been changed.
                // The |focusChange| value indicates whether the focus was gained, whether the focus was lost,
                // and whether that loss is transient, or whether the new focus holder will hold it for an
                // unknown amount of time.

                val typeOfChange: String
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_GAIN -> typeOfChange = "AUDIOFOCUS_GAIN"
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT -> typeOfChange =
                        "AUDIOFOCUS_GAIN_TRANSIENT"

                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE -> typeOfChange =
                        "AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE"

                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK -> typeOfChange =
                        "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK"

                    AudioManager.AUDIOFOCUS_LOSS -> typeOfChange = "AUDIOFOCUS_LOSS"
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> typeOfChange =
                        "AUDIOFOCUS_LOSS_TRANSIENT"

                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> typeOfChange =
                        "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK"

                    else -> typeOfChange = "AUDIOFOCUS_INVALID"
                }
                Log.d(TAG, "onAudioFocusChange: $typeOfChange")
            }

        // Request audio playout focus (without ducking) and install listener for changes in focus.
        val result = audioManager?.requestAudioFocus(
            audioFocusChangeListener,
            AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
        )
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d(TAG, "Audio focus request granted for VOICE_CALL streams")
        } else {
            Log.e(TAG, "Audio focus request failed")
        }

        // Start by setting MODE_IN_COMMUNICATION as default audio mode. It is
        // required to be in this mode when playout and/or recording starts for
        // best possible VoIP performance.
        audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION

        // Always disable microphone mute during a WebRTC call.
        setMicrophoneMute(false)

        // Set initial device states.
        userSelectedAudioDevice = AudioDevice.NONE
        selectedAudioDevice = AudioDevice.NONE
        audioDevices.clear()

        // Do initial selection of audio device. This setting can later be changed
        // either by adding/removing a BT or wired headset or by covering/uncovering
        // the proximity sensor.
        updateAudioDeviceState()

        // Register receiver for broadcast intents related to adding/removing a
        // wired headset.
        registerReceiver(wiredHeadsetReceiver, IntentFilter(Intent.ACTION_HEADSET_PLUG))
        Log.d(TAG, "AudioManager started")
    }

    private fun setMicrophoneMute(on: Boolean) {
        val wasMuted = audioManager?.isMicrophoneMute
        if(wasMuted == on){
            return
        }
        audioManager?.isMicrophoneMute = on

    }

    private fun registerReceiver(receiver: BroadcastReceiver?, filter: IntentFilter) {
        apprtcContext?.registerReceiver(receiver, filter)
    }

    fun stop() {
        Log.d(TAG, "stop")
        ThreadUtils.checkIsOnMainThread()
        if (amState != AudioManagerState.RUNNING) {
            Log.e(
                TAG,
                "Trying to stop AudioManager in incorrect state: $amState"
            )
            return
        }
        amState = AudioManagerState.UNINITIALIZED
        unregisterReceiver(wiredHeadsetReceiver)

        // Restore previously stored audio states.
        setSpeakerphoneOn(savedIsSpeakerPhoneOn)
        setMicrophoneMute(savedIsMicrophoneMute)
        audioManager?.mode = savedAudioMode

        // Abandon audio focus. Gives the previous focus owner, if any, focus.
        audioManager?.abandonAudioFocus(audioFocusChangeListener)
        audioFocusChangeListener = null
        Log.d(TAG, "Abandoned audio focus for VOICE_CALL streams")

        audioManagerEvents = null
        Log.d(TAG, "AudioManager stopped")
    }

    private fun unregisterReceiver(receiver: BroadcastReceiver?) {
        apprtcContext?.unregisterReceiver(receiver)
    }


    @Nullable
    private var audioManagerEvents: AudioManagerEvents? = null
    private var saveAudioMode = AudioManager.MODE_INVALID
    private var saveIsSpeakerPhoneOn = false
    private var savedIsMicrophoneMute = false
    private var hasWiredHeadset = false


    private var defaultAudioDevice: AudioDevice? = null

    private var selectedAudioDevice: AudioDevice? = null
    private var userSelectedAudioDevice: AudioDevice? = null


    // Contains a list of Available audio devices
        // A Set collection is used to avoid duplicate elements.
    private var audioDevices: MutableSet<AudioDevice?> = HashSet()



    // Callback method for changes in audio focus.
    @Nullable
    private var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener? = null


    private inner class WiredHeadsetReceiver(): BroadcastReceiver(){

        private val STATE_UNPLUGGED = 0
        private val STATE_PLUGGED = 1
        private val HAS_NO_MIC = 0
        private val HAS_MIC = 1
        override fun onReceive(context: Context?, intent: Intent?) {
            val state = intent?.getIntExtra("state", STATE_UNPLUGGED)
            val microphone = intent?.getIntExtra("microphone", HAS_NO_MIC)
            val name = intent?.getStringExtra("name")
            Log.d(TAG, "WiredHeadsetReceiver.onReceive"
                    + ": " + "a=" + intent?.action.toString() + ", s=" +
                    (if (state == STATE_UNPLUGGED) "unplugged" else "plugged").toString()
                    + ", m=" + (if (microphone == HAS_MIC) "mic" else "no mic").toString()
                    + ", n=" + name.toString() + ", sb=" + isInitialStickyBroadcast)
            hasWiredHeadset = (state == STATE_PLUGGED)
            updateAudioDeviceState()
        }

    }


    private fun hasEarpiece(): Boolean? {
        return apprtcContext?.packageManager?.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)

    }

    fun updateAudioDeviceState(){
        ThreadUtils.checkIsOnMainThread()
        Log.d(
            TAG, ("--- updateAudioDeviceState: "
                    + "wired headset=" + hasWiredHeadset)
        )
        Log.d(
            TAG, ("Device status: "
                    + "available=" + audioDevices + ", "
                    + "selected="  + ", "
                    + "user selected=" + selectedAudioDevice)
        )

        // update the set of available audio devices
        var newAudioDevices : MutableSet<AudioDevice?> = HashSet()
        if(hasWiredHeadset){
            // If a wired headset is connected, then it is the onl
            newAudioDevices.add(AudioDevice.WIRED_HEADSET)
        }else{
            // No wired headset, hence the audio-device list can contain speaker
            newAudioDevices.add(AudioDevice.SPEAKER_PHONE)
            if(hasEarpiece() == true){
                newAudioDevices.add(AudioDevice.EARPIECE)
            }

        }

        // Store state which is set to true if the device list has changed.
        var audioDeviceSetUpdated = audioDevices != newAudioDevices

        audioDevices = newAudioDevices

        if (hasWiredHeadset && userSelectedAudioDevice == AudioDevice.SPEAKER_PHONE) {
            // If user selected speaker phone, but then plugged wired headset then make
            // wired headset as user selected device.
            userSelectedAudioDevice = AudioDevice.WIRED_HEADSET
        }
        if (!hasWiredHeadset && userSelectedAudioDevice == AudioDevice.WIRED_HEADSET) {
            // If user selected wired headset, but then unplugged wired headset then make
            // speaker phone as user selected device.
            userSelectedAudioDevice = AudioDevice.SPEAKER_PHONE
        }

        // update selected audio devices
        val newAudioDevice: AudioDevice?
        if(hasWiredHeadset){
            // If a wired headset is connected, but Bluetooth is not, then wired headset is used as
            // audio device.
            newAudioDevice = AudioDevice.WIRED_HEADSET
        }else{
            newAudioDevice = defaultAudioDevice
        }

        if(newAudioDevice != selectedAudioDevice || audioDeviceSetUpdated){
            setAudioDeviceInternal(newAudioDevice)
            Log.d(TAG, ("New device status: "
                    + "available=" + audioDevices + ", "
                    + "selected=" + newAudioDevice)
            )
            if(audioManagerEvents != null){
                // Notify a listening client that audio device has been changed.
                audioManagerEvents!!.onAudioDeviceChanged(selectedAudioDevice, audioDevices)
            }
        }
        Log.d(TAG, "--- updateAudioDeviceState DONE")
    }

    private fun setAudioDeviceInternal(device: RTCAudioManager.AudioDevice?) {
        Log.d(TAG, "setAudioDeviceInternal(device=$device)")
        if(audioDevices.contains(device)){
            when(device){
                AudioDevice.SPEAKER_PHONE -> setSpeakerphoneOn(true)
                AudioDevice.EARPIECE -> setSpeakerphoneOn(false)
                AudioDevice.WIRED_HEADSET -> setSpeakerphoneOn(false)

                else -> Log.e(TAG, "Invalid audio device selection")
            }
        }

        selectedAudioDevice = device
    }

    @Deprecated("")
    private fun hasWiredHeadset(): Boolean? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return audioManager?.isWiredHeadsetOn
        } else {
            val devices = audioManager?.getDevices(AudioManager.GET_DEVICES_INPUTS)
            if (devices != null) {
                for (device: AudioDeviceInfo in devices) {
                    val type = device.type
                    if (type == AudioDeviceInfo.TYPE_WIRED_HEADSET) {
                        Log.d(TAG, "hasWiredHeadset: found wired headset")
                        return true
                    } else if (type == AudioDeviceInfo.TYPE_USB_DEVICE) {
                        Log.d(TAG, "hasWiredHeadset: found USB audio device")
                        return true
                    }
                }
            }
            return false
        }
    }

    private fun setSpeakerphoneOn(on: Boolean) {
        val wasOn = audioManager?.isSpeakerphoneOn
        if(wasOn == on){
            return
        }
        audioManager?.isSpeakerphoneOn = on
    }

    fun selectAudioDevice(device: AudioDevice){
        ThreadUtils.checkIsOnMainThread()
        if(!audioDevices.contains(device)){
            Log.e(TAG, "Can not select $device from available $audioDevices")
        }
        userSelectedAudioDevice = device
        updateAudioDeviceState()
    }

    fun setDefaultAudioDevice(defaultDevice: AudioDevice?){
        ThreadUtils.checkIsOnMainThread()
        when(defaultDevice){
            AudioDevice.SPEAKER_PHONE -> defaultDevice
            AudioDevice.EARPIECE -> if(hasEarpiece() == true){
                defaultAudioDevice = defaultDevice
            }else{
                defaultAudioDevice = AudioDevice.SPEAKER_PHONE
            }
            else -> Log.e(TAG, "Invalid default audio")

        }
        Log.d(TAG, "setDefaultAudioDevice(device=$defaultAudioDevice)")
        updateAudioDeviceState()
    }

    init {
        Log.d(TAG, "ctor")
        ThreadUtils.checkIsOnMainThread()
        apprtcContext = context
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        wiredHeadsetReceiver = WiredHeadsetReceiver()
        amState = AudioManagerState.UNINITIALIZED

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        useSpeakerphone = sharedPreferences.getString(
            "speakerphone_preference",
            "auto"
        )

        Log.d(TAG, "useSpeakerphone: $useSpeakerphone")

        if((useSpeakerphone == SPEAKERPHONE_FALSE)){
            defaultAudioDevice = AudioDevice.EARPIECE
        }else{
            defaultAudioDevice = AudioDevice.SPEAKER_PHONE
        }
        Log.d(TAG, "defaultAudioDevice: $defaultAudioDevice")

    }


}