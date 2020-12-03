package com.ukrainianboyz.nearly.service.signalR

import com.ukrainianboyz.nearly.utils.webSocket.IceCandidateMessage
import com.ukrainianboyz.nearly.utils.webSocket.SessionDescriptionMessage




interface ISingaling {

    fun onRemoteHangUp()
    fun onOfferReceived(data: SessionDescriptionMessage)
    fun onAnswerReceived(data: SessionDescriptionMessage)
    fun onIceCandidateReceived(data: IceCandidateMessage)
    fun onHoldReceived()
    fun onResumeReceived()
}