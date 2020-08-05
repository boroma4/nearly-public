package com.ukrainianboyz.nearly.service;

import com.ukrainianboyz.nearly.db.entity.DatabaseUser;
import com.ukrainianboyz.nearly.db.entity.UserRelationship;
import com.ukrainianboyz.nearly.db.repository.RelationshipRepository;
import com.ukrainianboyz.nearly.db.repository.UserRepository;
import com.ukrainianboyz.nearly.exceptions.IllegalRelationshipException;
import com.ukrainianboyz.nearly.exceptions.WrongExistingRelationException;
import com.ukrainianboyz.nearly.db.enums.Status;
import com.ukrainianboyz.nearly.model.requestdata.RelationshipAnswer;
import com.ukrainianboyz.nearly.model.requestdata.RelationshipRequest;
import com.ukrainianboyz.nearly.model.requestdata.RequestSearchData;
import com.ukrainianboyz.nearly.util.DataTransferUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FriendServiceUnitTest {

    @InjectMocks
    private FriendService friendService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private RelationshipRepository relationshipRepository;
    @Mock
    private FirebaseService firebaseService;

    private final String firstUserID = "100";
    private final String secondUserID = "200";
    private final String firstUserName = "SASHA";
    private final String secondUserName = "TARAS";
    private final RelationshipRequest relationshipRequest = new RelationshipRequest(firstUserID, secondUserID);
    private final RelationshipRequest relationshipRequestInterchaged = new RelationshipRequest(secondUserID, firstUserID);
    private final RelationshipRequest sameUserRelationshipRequest = new RelationshipRequest(firstUserID, firstUserID);
    private final RelationshipAnswer relationshipAnswer = new RelationshipAnswer(firstUserID, secondUserID, true);
    private final RequestSearchData requestSearchData = new RequestSearchData("start", 0, 2);
    private final UserRelationship requestSentRelationship = new UserRelationship(firstUserID, secondUserID, firstUserName, secondUserName, Status.REQUEST_SENT);
    private final UserRelationship blockedRelationship = new UserRelationship(firstUserID, secondUserID, firstUserName, secondUserName, Status.BLOCKED_BY_FIRST);
    private final UserRelationship blockedRelationshipBySecond = new UserRelationship(firstUserID, secondUserID, firstUserName, secondUserName, Status.BLOCKED_BY_SECOND);
    private final UserRelationship acceptedRelationship = new UserRelationship(firstUserID, secondUserID, firstUserName, secondUserName, Status.ACCEPTED);
    private final DatabaseUser firstUser = new DatabaseUser(firstUserID, firstUserName, "something", "123123@gmail.com", "http:/?/", 1,"sanek");
    private final DatabaseUser secondUser = new DatabaseUser(secondUserID, secondUserName, "something", "123123@gmail.com1111", "http:/?/", 1,"taras-batya");


    @Test
    void shouldAddFriend() {
        //given


        //when
        when(userRepository.findById(firstUserID)).thenReturn(Optional.of(firstUser));
        when(userRepository.findById(secondUserID)).thenReturn(Optional.of(secondUser));
        when(relationshipRepository.findUserRelationship(firstUserID, secondUserID)).thenReturn(Optional.empty());

        //then
        friendService.addFriend(relationshipRequest);

        //verify
        verify(relationshipRepository).save(requestSentRelationship);
        verify(firebaseService).sendFriendRequestMessage(secondUserID, firstUser);
    }

    @Test
    void shouldFailAddFriendOnSameUser() {
        Assertions.assertThrows(IllegalRelationshipException.class, () -> friendService.addFriend(sameUserRelationshipRequest));
    }

    @Test
    void shouldFailAddFriendOnMissingRequester() {
        when(userRepository.findById(firstUserID)).thenReturn(Optional.empty());

        Assertions.assertThrows(IllegalRelationshipException.class, () -> friendService.addFriend(relationshipRequest));
    }

    @Test
    void shouldFailAddFriendOnMissingRequested() {
        when(userRepository.findById(firstUserID)).thenReturn(Optional.of(firstUser));
        when(userRepository.findById(secondUserID)).thenReturn(Optional.empty());

        Assertions.assertThrows(IllegalRelationshipException.class, () -> friendService.addFriend(relationshipRequest));
    }

    @Test
    void shouldFailAddFriendOnPresentRelation() {
        when(userRepository.findById(firstUserID)).thenReturn(Optional.of(firstUser));
        when(userRepository.findById(secondUserID)).thenReturn(Optional.of(secondUser));
        when(relationshipRepository.findUserRelationship(firstUserID, secondUserID)).thenReturn(Optional.of(requestSentRelationship));

        Assertions.assertThrows(IllegalRelationshipException.class, () -> friendService.addFriend(relationshipRequest));
    }


    @Test
    void shouldRemoveAddRequest() {
        when(relationshipRepository.findUserRelationship(firstUserID, secondUserID)).thenReturn(Optional.of(requestSentRelationship));

        friendService.removeAddRequest(relationshipRequest);

        verify(relationshipRepository).delete(requestSentRelationship);
    }

    @Test
    void shouldFailRemoveAddRequestOnSameUser() {
        Assertions.assertThrows(IllegalRelationshipException.class, () -> friendService.removeAddRequest(sameUserRelationshipRequest));
    }

    @Test
    void shouldFailRemoveAddRequestOnWrongStatus() {
        var relationship = new UserRelationship(firstUserID, secondUserID, firstUserName, secondUserName, Status.ACCEPTED);

        when(relationshipRepository.findUserRelationship(firstUserID, secondUserID)).thenReturn(Optional.of(relationship));

        Assertions.assertThrows(IllegalRelationshipException.class, () -> friendService.removeAddRequest(relationshipRequest));
    }

    @Test
    void shouldFailRemoveAddRequestOnDifferentRequester() {
        var relationship = new UserRelationship(secondUserID, firstUserID, secondUserName, firstUserName, Status.REQUEST_SENT);

        when(relationshipRepository.findUserRelationship(firstUserID, secondUserID)).thenReturn(Optional.of(relationship));

        Assertions.assertThrows(IllegalRelationshipException.class, () -> friendService.removeAddRequest(relationshipRequest));
    }

    @Test
    void shouldFailRemoveAddRequestOnNotFoundRelation() {
        when(relationshipRepository.findUserRelationship(firstUserID, secondUserID)).thenReturn(Optional.empty());

        Assertions.assertThrows(IllegalRelationshipException.class, () -> friendService.removeAddRequest(relationshipRequest));
    }

    @Test
    void shouldRemoveFriend() {
        when(relationshipRepository.findUserRelationship(firstUserID, secondUserID)).thenReturn(Optional.of(acceptedRelationship));

        friendService.removeFriend(relationshipRequest);

        verify(relationshipRepository).delete(acceptedRelationship);
    }

    @Test
    void shouldNotRemoveFriendOnWrongStatus() {
        when(relationshipRepository.findUserRelationship(firstUserID, secondUserID)).thenReturn(Optional.of(requestSentRelationship));

        Assertions.assertThrows(IllegalRelationshipException.class, () -> friendService.removeFriend(relationshipRequest));
    }

    @Test
    void shouldNotRemoveFriendOnMissingRelation() {
        when(relationshipRepository.findUserRelationship(firstUserID, secondUserID)).thenReturn(Optional.empty());

        Assertions.assertThrows(IllegalRelationshipException.class, () -> friendService.removeFriend(relationshipRequest));
    }

    @Test
    void shouldBlockUserWithoutRelation() {
        when(userRepository.findById(firstUserID)).thenReturn(Optional.of(firstUser));
        when(userRepository.findById(secondUserID)).thenReturn(Optional.of(secondUser));
        when(relationshipRepository.findUserRelationship(firstUserID, secondUserID)).thenReturn(Optional.empty());

        friendService.blockUser(relationshipRequest);

        verify(relationshipRepository).save(blockedRelationship);
    }

    @Test
    void shouldBlockBySecondWithRelationBlockedByFirst () {
        var bothBlockedRelationship = new UserRelationship(firstUserID, secondUserID, firstUserName, secondUserName, Status.BLOCKED_BY_BOTH);

        when(userRepository.findById(firstUserID)).thenReturn(Optional.of(firstUser));
        when(userRepository.findById(secondUserID)).thenReturn(Optional.of(secondUser));
        when(relationshipRepository.findUserRelationship(secondUserID, firstUserID)).thenReturn(Optional.of(blockedRelationship));

        friendService.blockUser(relationshipRequestInterchaged);

        verify(relationshipRepository).save(bothBlockedRelationship);
    }

    @Test
    void shouldBlockByFirstWithRelationBlockedBySecond () {
        var blockedRelationship = new UserRelationship(firstUserID, secondUserID, firstUserName, secondUserName, Status.BLOCKED_BY_SECOND);
        var bothBlockedRelationship = new UserRelationship(firstUserID, secondUserID, firstUserName, secondUserName, Status.BLOCKED_BY_BOTH);

        when(userRepository.findById(firstUserID)).thenReturn(Optional.of(firstUser));
        when(userRepository.findById(secondUserID)).thenReturn(Optional.of(secondUser));
        when(relationshipRepository.findUserRelationship(firstUserID, secondUserID)).thenReturn(Optional.of(blockedRelationship));

        friendService.blockUser(relationshipRequest);

        verify(relationshipRepository).save(bothBlockedRelationship);
    }

    @Test
    void shouldBlockUserWithRelationByFirst() {
        when(userRepository.findById(firstUserID)).thenReturn(Optional.of(firstUser));
        when(userRepository.findById(secondUserID)).thenReturn(Optional.of(secondUser));
        when(relationshipRepository.findUserRelationship(firstUserID, secondUserID)).thenReturn(Optional.of(requestSentRelationship));

        friendService.blockUser(relationshipRequest);

        verify(relationshipRepository).save(blockedRelationship);
    }

    @Test
    void shouldBlockUserWithRelationBySecond() {
        var blockedRelationship = new UserRelationship(firstUserID, secondUserID, firstUserName, secondUserName, Status.BLOCKED_BY_SECOND);
        var relationshipRequestInterchanged = new RelationshipRequest(secondUserID, firstUserID);

        when(userRepository.findById(firstUserID)).thenReturn(Optional.of(firstUser));
        when(userRepository.findById(secondUserID)).thenReturn(Optional.of(secondUser));
        when(relationshipRepository.findUserRelationship(secondUserID, firstUserID)).thenReturn(Optional.of(requestSentRelationship));

        friendService.blockUser(relationshipRequestInterchanged);

        verify(relationshipRepository).save(blockedRelationship);
    }

    @Test
    void shouldFailBlockUserOnMissingRequester() {
        when(userRepository.findById(firstUserID)).thenReturn(Optional.empty());

        Assertions.assertThrows(IllegalRelationshipException.class, () -> friendService.blockUser(relationshipRequest));
    }

    @Test
    void shouldFailBlockUserOnMissingRequested() {
        when(userRepository.findById(firstUserID)).thenReturn(Optional.of(firstUser));
        when(userRepository.findById(secondUserID)).thenReturn(Optional.empty());

        Assertions.assertThrows(IllegalRelationshipException.class, () -> friendService.blockUser(relationshipRequest));
    }

    @Test
    void shouldFailOnExistingBlockByFirstAndRequestByFirst() {
        when(userRepository.findById(firstUserID)).thenReturn(Optional.of(firstUser));
        when(userRepository.findById(secondUserID)).thenReturn(Optional.of(secondUser));
        when(relationshipRepository.findUserRelationship(firstUserID, secondUserID)).thenReturn(Optional.of(blockedRelationship));

        Assertions.assertThrows(IllegalRelationshipException.class, () -> friendService.blockUser(relationshipRequest));
    }

    @Test
    void shouldFailOnExistingBlockBySecondAndRequestBySecond() {
        var blockedRelationship = new UserRelationship(firstUserID, secondUserID, firstUserName, secondUserName, Status.BLOCKED_BY_SECOND);

        when(userRepository.findById(firstUserID)).thenReturn(Optional.of(firstUser));
        when(userRepository.findById(secondUserID)).thenReturn(Optional.of(secondUser));
        when(relationshipRepository.findUserRelationship(secondUserID, firstUserID)).thenReturn(Optional.of(blockedRelationship));

        Assertions.assertThrows(IllegalRelationshipException.class, () -> friendService.blockUser(relationshipRequestInterchaged));
    }

    @Test
    void shouldUnblockUserByFirst() {

        when(relationshipRepository.findUserRelationship(firstUserID, secondUserID)).thenReturn(Optional.of(blockedRelationship));

        friendService.unblockUser(relationshipRequest);

        verify(relationshipRepository).delete(blockedRelationship);
    }

    @Test
    void shouldUnblockUserBySecond() {
        when(relationshipRepository.findUserRelationship(secondUserID, firstUserID)).thenReturn(Optional.of(blockedRelationshipBySecond));

        friendService.unblockUser(relationshipRequestInterchaged);

        verify(relationshipRepository).delete(blockedRelationshipBySecond);
    }

    @Test
    void shouldFailUnblockUserOnMissingRelation() {
        when(relationshipRepository.findUserRelationship(firstUserID, secondUserID)).thenReturn(Optional.empty());

        Assertions.assertThrows(IllegalRelationshipException.class, () -> friendService.unblockUser(relationshipRequest));
    }

    @Test
    void shouldFailUnblockUserOnWrongRequesterByFirst() {
        when(relationshipRepository.findUserRelationship(secondUserID, firstUserID)).thenReturn(Optional.of(blockedRelationship));

        Assertions.assertThrows(IllegalRelationshipException.class, () -> friendService.unblockUser(relationshipRequestInterchaged));
    }

    @Test
    void shouldFailUnblockUserOnWrongRequesterBySecond() {
        when(relationshipRepository.findUserRelationship(firstUserID, secondUserID)).thenReturn(Optional.of(blockedRelationshipBySecond));

        Assertions.assertThrows(IllegalRelationshipException.class, () -> friendService.unblockUser(relationshipRequest));
    }

    @Test
    void shouldFailUnblockUserOnWrongStatus() {
        when(relationshipRepository.findUserRelationship(firstUserID, secondUserID)).thenReturn(Optional.of(requestSentRelationship));

        Assertions.assertThrows(IllegalRelationshipException.class, () -> friendService.unblockUser(relationshipRequest));
    }

    @Test
    void shouldSearchUser() {
        var pageable = PageRequest.of(requestSearchData.getOffset(), requestSearchData.getLoadAmount());
        var databaseUserArrayList = new ArrayList<DatabaseUser>();
        var firstSecureUser = DataTransferUtils.toSecureUser(firstUser);
        var secondSecureUser = DataTransferUtils.toSecureUser(secondUser);
        var startsWith = requestSearchData.getStartsWith();
        databaseUserArrayList.add(firstUser);
        databaseUserArrayList.add(secondUser);

        when(userRepository.findByAppUserIdStartsWithOrUserNameStartsWith(startsWith, startsWith,  pageable)).thenReturn(databaseUserArrayList);

        var result = friendService.searchUser(requestSearchData);

        assertThat(result).hasSize(2).contains(firstSecureUser).contains(secondSecureUser);
    }

    @Test
    void shouldRespondOnFriendRequestAccepted() {
        var requestAcceptedRelationship = new UserRelationship(firstUserID, secondUserID, firstUserName, secondUserName,
                Status.ACCEPTED);

        when(relationshipRepository.findUserRelationship(firstUserID, secondUserID)).thenReturn(Optional.of(requestSentRelationship));

        friendService.respondOnFriendRequest(relationshipAnswer);

        verify(relationshipRepository).save(requestAcceptedRelationship);
    }

    @Test
    void shouldRespondOnFriendRequestDecline() {
        var relationshipAnswer = new RelationshipAnswer(firstUserID, secondUserID, false);

        when(relationshipRepository.findUserRelationship(firstUserID, secondUserID)).thenReturn(Optional.of(requestSentRelationship));

        friendService.respondOnFriendRequest(relationshipAnswer);

        verify(relationshipRepository).delete(requestSentRelationship);
    }

    @Test
    void shouldFailRespondOnFriendRequestOnWrongStatus() {
        when(relationshipRepository.findUserRelationship(firstUserID, secondUserID)).thenReturn(Optional.of(blockedRelationshipBySecond));

        Assertions.assertThrows(WrongExistingRelationException.class, () -> friendService.respondOnFriendRequest(relationshipAnswer));
    }

    @Test
    void shouldFailRespondOnFriendRequestOnMissingRelation() {
        when(relationshipRepository.findUserRelationship(firstUserID, secondUserID)).thenReturn(Optional.empty());

        Assertions.assertThrows(IllegalRelationshipException.class, () -> friendService.respondOnFriendRequest(relationshipAnswer));
    }

    @Test
    void shouldFailRespondFriendRequestOnSameUserRelation() {
        var relationshipAnswerSameUser = new RelationshipAnswer(firstUserID, firstUserID, true);

        Assertions.assertThrows(IllegalRelationshipException.class, () -> friendService.respondOnFriendRequest(relationshipAnswerSameUser));
    }
}