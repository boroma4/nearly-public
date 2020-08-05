package com.ukrainianboyz.nearly.controllers;


import com.ukrainianboyz.nearly.model.entity.SecureUser;
import com.ukrainianboyz.nearly.model.enums.FriendListType;
import com.ukrainianboyz.nearly.model.requestdata.RelationshipAnswer;
import com.ukrainianboyz.nearly.model.requestdata.RelationshipRequest;
import com.ukrainianboyz.nearly.model.requestdata.RequestSearchData;
import com.ukrainianboyz.nearly.model.requestdata.UserFriendDataRequest;
import com.ukrainianboyz.nearly.service.FriendService;
import com.ukrainianboyz.nearly.service.SearchFriendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("api/friends")
@RequiredArgsConstructor
@Slf4j
public class FriendsController {

    private final SearchFriendService searchFriendService;

    private final FriendService friendService;

    @PostMapping("add")
    @ResponseStatus(value = HttpStatus.CREATED)
    public void addFriend(@Valid @RequestBody RelationshipRequest relationshipRequest) {
        log.info("Adding friend - {}", relationshipRequest);
        friendService.addFriend(relationshipRequest);
    }

    @PostMapping("removeAddRequest")
    public void removeAddRequest(@Valid @RequestBody RelationshipRequest relationshipRequest) {
        friendService.removeAddRequest(relationshipRequest);
    }

    @PostMapping("removeFriend")
    public void removeFriend(@Valid @RequestBody RelationshipRequest relationshipRequest) {
        friendService.removeFriend(relationshipRequest);
    }

    @PostMapping("blockUser")
    public void blockUser(@Valid @RequestBody RelationshipRequest relationshipRequest){
        friendService.blockUser(relationshipRequest);
    }

    @PostMapping("unblockUser")
    public void unblockUser(@Valid @RequestBody RelationshipRequest relationshipRequest) {
        friendService.unblockUser(relationshipRequest);
    }

    @PostMapping("search")
    public List<SecureUser> searchUsers(@Valid @RequestBody RequestSearchData requestSearchData) {
        return friendService.searchUser(requestSearchData);
    }

    @PostMapping("respond")
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public void friendRequestRespond(@Valid @RequestBody RelationshipAnswer relationshipAnswer){
        friendService.respondOnFriendRequest(relationshipAnswer);
    }

    @GetMapping("blockedByUser/{id}")
    public List<SecureUser> findBlockedByUserUsers(@PathVariable("id") String id){
        return searchFriendService.findBlockedByUserUsers(id);
    }

    @GetMapping("allFriends/{id}")
    public List<SecureUser> findAllFriends(@PathVariable("id") String id){
        return searchFriendService.findFriends(new UserFriendDataRequest(id, FriendListType.EXISTING));
    }

    @GetMapping("outgoingFriendRequests/{id}")
    public List<SecureUser> findSentFriendRequests(@PathVariable("id") String id){
        return searchFriendService.findFriends(new UserFriendDataRequest(id, FriendListType.OUTGOING));
    }

    @GetMapping("incomingFriendRequests/{id}")
    public List<SecureUser> findReceivedFriendRequests(@PathVariable("id") String id){
        return searchFriendService.findFriends(new UserFriendDataRequest(id, FriendListType.INCOMING));
    }


}
