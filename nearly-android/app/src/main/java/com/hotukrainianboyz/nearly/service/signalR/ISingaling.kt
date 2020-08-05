package com.hotukrainianboyz.nearly.service.signalR

import com.hotukrainianboyz.nearly.utils.webSocket.IceCandidateMessage
import com.hotukrainianboyz.nearly.utils.webSocket.SessionDescriptionMessage




interface ISingaling {

    fun onRemoteHangUp()
    fun onOfferReceived(data: SessionDescriptionMessage)
    fun onAnswerReceived(data: SessionDescriptionMessage)
    fun onIceCandidateReceived(data: IceCandidateMessage)
    fun onHoldReceived()
    fun onResumeReceived()
}