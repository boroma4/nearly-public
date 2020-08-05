package com.ukrainianboyz.nearly.controllers;

import com.ukrainianboyz.nearly.exceptions.IllegalCallRequest;
import com.ukrainianboyz.nearly.db.enums.Status;
import com.ukrainianboyz.nearly.model.requestdata.FirebaseBasicData;
import com.ukrainianboyz.nearly.db.repository.RelationshipRepository;
import com.ukrainianboyz.nearly.service.FirebaseService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


@RestController
@RequestMapping("api/calls")
@AllArgsConstructor
public class CallController {

    @Resource
    private final RelationshipRepository relationshipRepository;

    private final FirebaseService firebaseService;

    //to be used for call initiation, notification dismissal, perhaps hangup
    @PostMapping("command")
    @ResponseStatus(value = HttpStatus.OK)
    public void sendCommand (@RequestBody FirebaseBasicData requestData) {
        String receiverId = requestData.getReceiverId(), senderId = requestData.getSenderId();
        relationshipRepository.findUserRelationship(receiverId,senderId).ifPresentOrElse(
                relationship -> {
                    if(relationship.getStatus() != Status.ACCEPTED){
                        throw new IllegalCallRequest();
                    }
                    firebaseService.sendBasicMessage(requestData);
                },
                IllegalCallRequest::fail
        );
    }
}
