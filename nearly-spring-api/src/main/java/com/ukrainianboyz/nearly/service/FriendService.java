package com.ukrainianboyz.nearly.service;

import com.ukrainianboyz.nearly.db.entity.DatabaseUser;
import com.ukrainianboyz.nearly.db.entity.UserRelationship;
import com.ukrainianboyz.nearly.db.repository.RelationshipRepository;
import com.ukrainianboyz.nearly.db.repository.UserRepository;
import com.ukrainianboyz.nearly.exceptions.IllegalRelationshipException;
import com.ukrainianboyz.nearly.exceptions.WrongExistingRelationException;
import com.ukrainianboyz.nearly.model.entity.SecureUser;
import com.ukrainianboyz.nearly.db.enums.Status;
import com.ukrainianboyz.nearly.model.requestdata.RelationshipAnswer;
import com.ukrainianboyz.nearly.model.requestdata.RelationshipRequest;
import com.ukrainianboyz.nearly.model.requestdata.RequestSearchData;
import com.ukrainianboyz.nearly.util.DataTransferUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

//TODO: ADD ANNOTATIONS FOR THROWS

@Service
@RequiredArgsConstructor
public class FriendService {

    private final UserRepository userRepository;
    private final RelationshipRepository relationshipRepository;
    private final FirebaseService firebaseService;

    public void addFriend(RelationshipRequest relationshipRequest) {
        String requesterId = relationshipRequest.getRequesterId();
        String requestedId = relationshipRequest.getRequestedId();

        checkSameUser(requesterId, requestedId);
        DatabaseUser requester = userRepository.findById(requesterId)
                .orElseThrow(IllegalRelationshipException::new);

        DatabaseUser requested = userRepository.findById(requestedId)
                .orElseThrow(IllegalRelationshipException::new);

        relationshipRepository.findUserRelationship(requesterId, requestedId)
                .ifPresentOrElse(
                        relationship -> IllegalRelationshipException.fail(),
                        () -> createInvitation(requester, requested));
    }

    private void createInvitation(DatabaseUser requester, DatabaseUser requested) {
        UserRelationship relationship = new UserRelationship(requester.getUserId(), requested.getUserId(),
                requester.getUserName(), requested.getUserName(), Status.REQUEST_SENT);
        relationshipRepository.save(relationship);
        firebaseService.sendFriendRequestMessage(requested.getUserId(), requester);
    }

    public void removeAddRequest(RelationshipRequest relationshipRequest) {
        String requesterId = relationshipRequest.getRequesterId();
        String requestedId = relationshipRequest.getRequestedId();
        checkSameUser(requesterId, requestedId);

        relationshipRepository.findUserRelationship(requesterId, requestedId)
                .ifPresentOrElse(
                        relationship -> removeAddRequest(relationshipRequest, relationship),
                        IllegalRelationshipException::fail
                );
    }

    private void removeAddRequest(RelationshipRequest relationshipRequest, UserRelationship relationship) {
        if (relationship.getStatus() == Status.REQUEST_SENT
                && relationship.getRequesterId().equals(relationshipRequest.getRequesterId())) {
            relationshipRepository.delete(relationship);
            firebaseService.sendRevokeFriendRequest(relationshipRequest);
        } else {
            throw new IllegalRelationshipException();
        }
    }

    public void removeFriend(RelationshipRequest relationshipRequest) {
        String requesterId = relationshipRequest.getRequesterId();
        String requestedId = relationshipRequest.getRequestedId();
        checkSameUser(requesterId, requestedId);

        relationshipRepository.findUserRelationship(requesterId, requestedId)
                .ifPresentOrElse(
                        this::removeFriend,
                        IllegalRelationshipException::fail
                );
    }

    private void removeFriend(UserRelationship relationship) {
        if (relationship.getStatus() == Status.ACCEPTED) {
            // responder should receive a msg to remove sender from his friend list
            firebaseService.sendFriendRemovedMessage(relationship.getResponderId(),relationship.getRequesterId());
            relationshipRepository.delete(relationship);
        } else {
            throw new IllegalRelationshipException();
        }
    }

    public void blockUser(RelationshipRequest relationshipRequest) {
        String requesterId = relationshipRequest.getRequesterId();
        String requestedId = relationshipRequest.getRequestedId();
        checkSameUser(requesterId, requestedId);
        var requester = userRepository.findById(requesterId)
                .orElseThrow(IllegalRelationshipException::new);
        var requested = userRepository.findById(requestedId)
                .orElseThrow(IllegalRelationshipException::new);
        relationshipRepository.findUserRelationship(requesterId, requestedId)
                .ifPresentOrElse(
                        relationship -> blockExistingRelation(relationshipRequest, relationship),
                        () -> createBlockRelation(requester, requested));
    }

    private void createBlockRelation(DatabaseUser requester, DatabaseUser requested) {
        var relationship = DataTransferUtils.createUserRelationship(requester, requested, Status.BLOCKED_BY_FIRST);
        relationshipRepository.save(relationship);
        // no need to send any message if there was not relationship before
    }

    private void blockExistingRelation(RelationshipRequest relationshipRequest, UserRelationship relationship) {
        if (relationship.getStatus() == Status.BLOCKED_BY_FIRST
                && relationshipRequest.getRequesterId().equals(relationship.getRequesterId())) {
            throw new IllegalRelationshipException();
        } else if (relationship.getStatus() == Status.BLOCKED_BY_SECOND
                && relationshipRequest.getRequesterId().equals(relationship.getResponderId())) {
            throw new IllegalRelationshipException();
        }
        if (relationship.getStatus() == Status.BLOCKED_BY_FIRST
                && relationshipRequest.getRequesterId().equals(relationship.getResponderId())) {
            relationship.setStatus(Status.BLOCKED_BY_BOTH);
        } else if (relationship.getStatus() == Status.BLOCKED_BY_SECOND
                && relationshipRequest.getRequesterId().equals(relationship.getRequesterId())) {
            relationship.setStatus(Status.BLOCKED_BY_BOTH);
        } else if (relationship.getRequesterId().equals(relationshipRequest.getRequesterId())) {
            relationship.setStatus(Status.BLOCKED_BY_FIRST);
            // second user aka requested is blocked, send a message to him to remove requester from his lists
            firebaseService.sendFriendRemovedMessage(relationshipRequest.getRequestedId(), relationshipRequest.getRequesterId());
        } else if (relationship.getRequesterId().equals(relationshipRequest.getRequestedId())) {
            relationship.setStatus(Status.BLOCKED_BY_SECOND);
            // first user aka requester is blocked, send a message to him to remove requested from his lists
            firebaseService.sendFriendRemovedMessage(relationshipRequest.getRequesterId(), relationshipRequest.getRequestedId());
        } else {
            throw new IllegalRelationshipException();
        }
        relationshipRepository.save(relationship);
    }

    public void unblockUser(RelationshipRequest relationshipRequest) {
        String requesterId = relationshipRequest.getRequesterId();
        String requestedId = relationshipRequest.getRequestedId();
        relationshipRepository.findUserRelationship(requesterId, requestedId)
                .ifPresentOrElse(
                        relationship -> unblockUser(requesterId, relationship),
                        IllegalRelationshipException::fail);
    }

    private void unblockUser(String requesterId, UserRelationship relationship) {
        if (relationship.getStatus() == Status.BLOCKED_BY_FIRST
                && requesterId.equals(relationship.getRequesterId())) {
            relationshipRepository.delete(relationship);
        } else if (relationship.getStatus() == Status.BLOCKED_BY_SECOND
                && requesterId.equals(relationship.getResponderId())) {
            relationshipRepository.delete(relationship);
        } else {
            throw new IllegalRelationshipException();
        }
    }

    public List<SecureUser> searchUser(RequestSearchData requestSearchData) {
        int offset = requestSearchData.getOffset();
        int loadAmount = requestSearchData.getLoadAmount();
        Pageable pageable = PageRequest.of(offset, loadAmount);
        var startsWith = requestSearchData.getStartsWith();
        List<DatabaseUser> dbUsers = userRepository.findByAppUserIdStartsWithOrUserNameStartsWith(startsWith, startsWith, pageable);
        return DataTransferUtils.toSecureUsers(dbUsers);
    }

    public void respondOnFriendRequest(RelationshipAnswer relationshipAnswer) {
        String requesterId = relationshipAnswer.getRequesterId();
        String requestedId = relationshipAnswer.getRequestedId();

        checkSameUser(requesterId, requestedId);
        relationshipRepository.findUserRelationship(requesterId, requestedId)
                .ifPresentOrElse(
                        relationship -> updateRelationOnRespond(relationshipAnswer, relationship),
                        IllegalRelationshipException::fail);
    }

    private void updateRelationOnRespond(RelationshipAnswer relationshipAnswer, UserRelationship relationship) {
        if (relationship.getStatus() != Status.REQUEST_SENT) {
            throw new WrongExistingRelationException();
        }
        firebaseService.sendFriendResponse(relationshipAnswer);
        if (relationshipAnswer.getIsAccepted()) {
            relationship.setStatus(Status.ACCEPTED);
            relationshipRepository.save(relationship);
        } else {
            relationshipRepository.delete(relationship);
        }
    }


    private void checkSameUser(String requesterId, String requestedId) {
        if (requestedId.equals(requesterId)) {
            throw new IllegalRelationshipException();
        }
    }
}