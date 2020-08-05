package com.ukrainianboyz.nearly.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukrainianboyz.nearly.db.entity.DatabaseUser;
import com.ukrainianboyz.nearly.db.entity.UserRelationship;
import com.ukrainianboyz.nearly.exceptions.IllegalRelationshipException;
import com.ukrainianboyz.nearly.exceptions.WrongExistingRelationException;
import com.ukrainianboyz.nearly.model.enums.FriendListType;
import com.ukrainianboyz.nearly.db.enums.Status;
import com.ukrainianboyz.nearly.model.requestdata.RelationshipAnswer;
import com.ukrainianboyz.nearly.model.requestdata.RelationshipRequest;
import com.ukrainianboyz.nearly.model.requestdata.RequestSearchData;
import com.ukrainianboyz.nearly.model.requestdata.UserFriendDataRequest;
import com.ukrainianboyz.nearly.service.FriendService;
import com.ukrainianboyz.nearly.service.SearchFriendService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FriendsController.class)
class FriendsControllerTest {

    private static final String BASE_PATH = "/api/friends";
    public static final String REMOVE_ADD_REQUEST = "/removeAddRequest";
    public static final String ADD = "/add";
    public static final String BLOCK_USER = "/blockUser";
    public static final String UNBLOCK_USER = "/unblockUser";
    public static final String SEARCH = "/search";
    public static final String RESPOND = "/respond";
    public static final String ALL_FRIENDS = "/allFriends/mockString";
    public static final String REMOVE_FRIEND = "/removeFriend";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    private FriendService friendService;

    @MockBean
    private SearchFriendService searchFriendService;

    private final String firstUserID = "100";
    private final String secondUserID = "200";
    private final String firstUserName = "SASHA";
    private final String secondUserName = "TARAS";
    private final RelationshipRequest relationshipRequest = new RelationshipRequest(firstUserID, secondUserID);
    private final RelationshipRequest missingUserRelationshipRequest = new RelationshipRequest(null, secondUserID);
    private final RelationshipRequest emptyUserRelationshipRequest = new RelationshipRequest(null, secondUserID);
    private final RelationshipAnswer relationshipAnswer = new RelationshipAnswer(firstUserID, secondUserID, true);
    private final RequestSearchData requestSearchData = new RequestSearchData("start", 0, 2);

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(friendService, searchFriendService);
    }

    @Test
    void shouldAddFriend() throws Exception {

        mockMvc.perform(
                post(BASE_PATH + ADD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(relationshipRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));

        verify(friendService).addFriend(relationshipRequest);
    }

    @Test
    void shouldReceiveBadRequestOnIllegalRelationshipExceptionAddFriend() throws Exception {
        doThrow(new IllegalRelationshipException()).when(friendService).addFriend(relationshipRequest);

        mockMvc.perform(
                post(BASE_PATH + ADD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(relationshipRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));

        verify(friendService).addFriend(relationshipRequest);
    }

    @Test
    void shouldReceiveBadRequestOnNullUserAddFriend() throws Exception {
        mockMvc.perform(
                post(BASE_PATH + ADD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingUserRelationshipRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void shouldReceiveBadRequestOnEmptyUserAddFriend() throws Exception {
        mockMvc.perform(
                post(BASE_PATH + ADD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyUserRelationshipRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void shouldRemoveAddFriendRequest() throws Exception {
        mockMvc.perform(
                post(BASE_PATH + REMOVE_ADD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(relationshipRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(friendService).removeAddRequest(relationshipRequest);
    }

    @Test
    void shouldReceiveBadRequestOnIllegalRelationshipExceptionRemoveAddRequest() throws Exception {
        doThrow(new IllegalRelationshipException()).when(friendService).removeAddRequest(relationshipRequest);

        mockMvc.perform(
                post(BASE_PATH + REMOVE_ADD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(relationshipRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));

        verify(friendService).removeAddRequest(relationshipRequest);
    }

    @Test
    void shouldReceiveBadRequestOnNullUserRemoveAddRequest() throws Exception {
        mockMvc.perform(
                post(BASE_PATH + REMOVE_ADD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingUserRelationshipRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void shouldReceiveBadRequestOnEmptyUserRemoveAddRequest() throws Exception {
        mockMvc.perform(
                post(BASE_PATH + REMOVE_ADD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyUserRelationshipRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void shouldRemoveAddFriend() throws Exception {
        mockMvc.perform(
                post(BASE_PATH + REMOVE_FRIEND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(relationshipRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(friendService).removeFriend(relationshipRequest);
    }

    @Test
    void shouldReceiveBadRequestOnIllegalRelationshipExceptionRemoveFriend() throws Exception {
        doThrow(new IllegalRelationshipException()).when(friendService).removeFriend(relationshipRequest);

        mockMvc.perform(
                post(BASE_PATH + REMOVE_FRIEND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(relationshipRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));

        verify(friendService).removeFriend(relationshipRequest);
    }

    @Test
    void shouldReceiveBadRequestOnNullUserRemoveFriend() throws Exception {
        mockMvc.perform(
                post(BASE_PATH + REMOVE_FRIEND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingUserRelationshipRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void shouldReceiveBadRequestOnEmptyUserRemoveFriend() throws Exception {
        mockMvc.perform(
                post(BASE_PATH + REMOVE_FRIEND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyUserRelationshipRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void shouldBlockUser() throws Exception {
        mockMvc.perform(
                post(BASE_PATH + BLOCK_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(relationshipRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(friendService).blockUser(relationshipRequest);
    }

    @Test
    void shouldReceiveBadRequestOnIllegalRelationshipExceptionBlockUser() throws Exception {
        doThrow(new IllegalRelationshipException()).when(friendService).blockUser(relationshipRequest);

        mockMvc.perform(
                post(BASE_PATH + BLOCK_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(relationshipRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));

        verify(friendService).blockUser(relationshipRequest);
    }

    @Test
    void shouldReceiveBadRequestOnNullUserBlockUser() throws Exception {
        mockMvc.perform(
                post(BASE_PATH + BLOCK_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingUserRelationshipRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void shouldReceiveBadRequestOnEmptyUserBlockUser() throws Exception {
        mockMvc.perform(
                post(BASE_PATH + BLOCK_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyUserRelationshipRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void shouldUnblockUser() throws Exception {
        mockMvc.perform(
                post(BASE_PATH + UNBLOCK_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(relationshipRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(friendService).unblockUser(relationshipRequest);
    }

    @Test
    void shouldReceiveBadRequestOnIllegalRelationshipExceptionUnblockUser() throws Exception {
        doThrow(new IllegalRelationshipException()).when(friendService).unblockUser(relationshipRequest);

        mockMvc.perform(
                post(BASE_PATH + UNBLOCK_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(relationshipRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));

        verify(friendService).unblockUser(relationshipRequest);
    }

    @Test
    void shouldReceiveBadRequestOnNullUserUnblockUser() throws Exception {
        mockMvc.perform(
                post(BASE_PATH + UNBLOCK_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingUserRelationshipRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void shouldReceiveBadRequestOnEmptyUserUnblockUser() throws Exception {
        mockMvc.perform(
                post(BASE_PATH + UNBLOCK_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyUserRelationshipRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void shouldSearchUser() throws Exception {
        mockMvc.perform(
                post(BASE_PATH + SEARCH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestSearchData)))
                .andExpect(status().isOk());

        verify(friendService).searchUser(requestSearchData);
    }

    @Test
    void shouldReceiveBadRequestOnLoadDataNotPositiveSearchUser() throws Exception {
        RequestSearchData requestSearchData = new RequestSearchData("start", 0, 0);

        mockMvc.perform(
                post(BASE_PATH + SEARCH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestSearchData)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void shouldReceiveBadRequestOnNegativeOffsetSearchUser() throws Exception {
        RequestSearchData requestSearchData = new RequestSearchData("start", -1, 3);

        mockMvc.perform(
                post(BASE_PATH + SEARCH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestSearchData)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void shouldReceiveBadRequestOnEmptyStartsWithSearchUser() throws Exception {
        RequestSearchData requestSearchData = new RequestSearchData("", 0, 3);

        mockMvc.perform(
                post(BASE_PATH + SEARCH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestSearchData)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void shouldReceiveBadRequestOnNullStartsWithSearchUser() throws Exception {
        RequestSearchData requestSearchData = new RequestSearchData(null, 0, 3);

        mockMvc.perform(
                post(BASE_PATH + SEARCH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestSearchData)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void shouldRespondToRequest() throws Exception {
        mockMvc.perform(
                post(BASE_PATH + RESPOND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(relationshipAnswer)))
                .andExpect(status().isAccepted())
                .andExpect(content().string(""));

        verify(friendService).respondOnFriendRequest(relationshipAnswer);
    }

    @Test
    void shouldReceiveBadRequestOnMissingRequesterIDRespondFriendRequest() throws Exception {
        RelationshipAnswer relationshipAnswer = new RelationshipAnswer("", secondUserID, true);

        mockMvc.perform(
                post(BASE_PATH + RESPOND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(relationshipAnswer)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void shouldReceiveBadRequestOnMissingRequestedIDRespondFriendRequest() throws Exception {
        RelationshipAnswer relationshipAnswer = new RelationshipAnswer("123123", "", true);

        mockMvc.perform(
                post(BASE_PATH + RESPOND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(relationshipAnswer)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void shouldReceiveBadRequestOnMissingNullIDRespondFriendRequest() throws Exception {
        RelationshipAnswer relationshipAnswer = new RelationshipAnswer(null, "12312", true);

        mockMvc.perform(
                post(BASE_PATH + RESPOND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(relationshipAnswer)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void shouldReceiveConflictOnWrongExistingRelationExceptionRespondFriendRequest() throws Exception {
        doThrow(new WrongExistingRelationException()).when(friendService).respondOnFriendRequest(relationshipAnswer);

        mockMvc.perform(
                post(BASE_PATH + RESPOND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(relationshipAnswer)))
                .andExpect(status().isConflict())
                .andExpect(content().string(""));

        verify(friendService).respondOnFriendRequest(relationshipAnswer);
    }

    @Test
    void shouldReceiveBadRequestOnIllegalRelationshipExceptionRespondFriendRequest() throws Exception {
        doThrow(new IllegalRelationshipException()).when(friendService).respondOnFriendRequest(relationshipAnswer);

        mockMvc.perform(
                post(BASE_PATH + RESPOND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(relationshipAnswer)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));

        verify(friendService).respondOnFriendRequest(relationshipAnswer);
    }

    @Test
    void shouldFindAllFriends() throws Exception {
        mockMvc.perform(
                get(BASE_PATH + ALL_FRIENDS))
                .andExpect(status().isOk());

        verify(searchFriendService).findFriends(new UserFriendDataRequest("mockString", FriendListType.EXISTING));
    }

    @Test
    void shouldFindAllOutgoingFriendRequests() throws Exception {
        String mockID = "123123";
        mockMvc.perform(
                get(BASE_PATH + "/outgoingFriendRequests/" + mockID))
                .andExpect(status().isOk());

        verify(searchFriendService).findFriends(new UserFriendDataRequest(mockID, FriendListType.OUTGOING));
    }


    @Test
    void shouldFindAllReceivedFriendRequests() throws Exception {
        String mockID = "123123";
        mockMvc.perform(
                get(BASE_PATH + "/incomingFriendRequests/" + mockID))
                .andExpect(status().isOk());

        verify(searchFriendService).findFriends(new UserFriendDataRequest(mockID, FriendListType.INCOMING));
    }

}