package com.ukrainianboyz.nearly.service;

import com.ukrainianboyz.nearly.exceptions.IllegalRelationshipException;
import com.ukrainianboyz.nearly.exceptions.NoUsersFoundException;
import com.ukrainianboyz.nearly.model.entity.SecureUser;
import com.ukrainianboyz.nearly.db.entity.DatabaseUser;
import com.ukrainianboyz.nearly.db.entity.UserRelationship;
import com.ukrainianboyz.nearly.db.enums.Status;
import com.ukrainianboyz.nearly.model.requestdata.UserFriendDataRequest;
import com.ukrainianboyz.nearly.db.repository.RelationshipRepository;
import com.ukrainianboyz.nearly.db.repository.UserRepository;
import com.ukrainianboyz.nearly.util.DataTransferUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchFriendService {

    private final UserRepository userRepository;

    private final RelationshipRepository relationshipRepository;

    public List<SecureUser> findBlockedByUserUsers (String userId){
        var userRelationships = relationshipRepository.findBlockedByUser(userId);
        List<String> blockedUserIds = new ArrayList<>();
        for (UserRelationship userRelationship : userRelationships){
            if (userRelationship.getRequesterId().equals(userId)){
                blockedUserIds.add(userRelationship.getResponderId());
            } else {
                blockedUserIds.add(userRelationship.getRequesterId());
            }
        }
        var databaseUsers = userRepository.findAllById(blockedUserIds);
        return DataTransferUtils.toSecureUsers(databaseUsers);
    }

    public List<SecureUser> findFriends(UserFriendDataRequest userFriendDataRequest) {

        String userId = userFriendDataRequest.getUserId();
        Optional<DatabaseUser> optionalUser = userRepository.findById(userId);
        doesUserExist(optionalUser);
        List<UserRelationship> userRelationshipList = new ArrayList<>();

        switch (userFriendDataRequest.getListType()) {
            case EXISTING:
                userRelationshipList = relationshipRepository.findByRequesterIdOrResponderIdAndStatus(userId, Status.ACCEPTED);
                break;

            case INCOMING:
                userRelationshipList = relationshipRepository.findByResponderIdAndStatus(userId, Status.REQUEST_SENT);
                break;

            case OUTGOING:
                userRelationshipList = relationshipRepository.findByRequesterIdAndStatus(userId, Status.REQUEST_SENT);
                break;
        }
        List<String> idsToGet = userRelationshipList.stream()
                .map(relationship -> relationship.getOtherId(userId))
                .collect(Collectors.toList());
        List<DatabaseUser> users = userRepository.findAllById(idsToGet);
        return DataTransferUtils.toSecureUsers(users);
    }


    public void doesUserExist(Optional<DatabaseUser> optionalRequester) {
        if (!optionalRequester.isPresent()) {
            throw new IllegalRelationshipException();
        }
    }
}
