package com.ukrainianboyz.nearly.utils.webSocket

import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

const val COMMAND = "command"
const val CANDIDATE = "candidate"
const val HANGUP = "hangup:"
const val INIT = "init:"
const val INIT_BACKEND_COMPAT = "init"
const val DISMISS = "dismiss"
const val DISMISS_SELF = "dismiss_self"
const val HOLD = "hold"
const val RESUME = "resume"
const val CALLER_ID = "callerId"
const val FRIEND_ID = "friendId"
const val USER_ONLINE = "UserOnline"
const val USER_OFFLINE = "UserOffline"
const val USER_ON_CALL = "UserOnCall"
const val UPDATE_CALL_STATUS = "UpdateCallStatus"

open class Message(val type: String)

class SessionDescriptionMessage(sd: SessionDescription): Message(sd.type.canonicalForm()){
    val sdp: String = sd.description
}

class CommandMessage(val command: String): Message(COMMAND)

class IceCandidateMessage(iceCandidate: IceCandidate): Message(CANDIDATE){
    val label: Int = iceCandidate.sdpMLineIndex
    val id: String = iceCandidate.sdpMid
    val candidate: String = iceCandidate.sdp
}