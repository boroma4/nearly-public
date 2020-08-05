package com.ukrainianboyz.nearly.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukrainianboyz.nearly.db.entity.DatabaseUser;
import com.ukrainianboyz.nearly.exceptions.NoUsersFoundException;
import com.ukrainianboyz.nearly.exceptions.NotUniqueAppUserIDException;
import com.ukrainianboyz.nearly.model.requestdata.AppUserIdDataUpdate;
import com.ukrainianboyz.nearly.service.FriendService;
import com.ukrainianboyz.nearly.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {


    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService userService;

    private static final String UPDATE_APP_USER_ID = "/updateAppUserID";
    private static final String BASE_PATH = "/api/user";
    private static final String newAppUserID = "I AM GOD";
    private final String firstUserID = "100";
    private final String firstUserName = "SASHA";
    private final AppUserIdDataUpdate appUserIdDataUpdate = new AppUserIdDataUpdate(firstUserID, newAppUserID);
    private final AppUserIdDataUpdate appUserIdDataUpdateMissingUserID = new AppUserIdDataUpdate(null, newAppUserID);
    private final AppUserIdDataUpdate appUserIdDataUpdateEmptyUserID = new AppUserIdDataUpdate("", newAppUserID);
    private final AppUserIdDataUpdate appUserIdDataUpdateMissingUpdateAppID = new AppUserIdDataUpdate(firstUserID, null);
    private final AppUserIdDataUpdate appUserIdDataUpdateEmptyUpdateAppID = new AppUserIdDataUpdate(firstUserID, "");
    private final DatabaseUser firstUser = new DatabaseUser(firstUserID, firstUserName, "something", "123123@gmail.com", "http:/?/", 1, "sanek");
    private final DatabaseUser firstUserUpdated = new DatabaseUser(firstUserID, firstUserName, "something", "123123@gmail.com", "http:/?/", 1, newAppUserID);

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(userService);
    }

    @Test
    void shouldReceiveOkOnUpdateAppUserID() throws Exception {
        mockMvc.perform(
                put(BASE_PATH + UPDATE_APP_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appUserIdDataUpdate)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(userService).updateAppUserID(appUserIdDataUpdate);
    }

    @Test
    void shouldReceiveNotFoundOnMissingUserAppUserIDUpdate() throws Exception {
        doThrow(new NoUsersFoundException()).when(userService).updateAppUserID(appUserIdDataUpdate);

        mockMvc.perform(
                put(BASE_PATH + UPDATE_APP_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appUserIdDataUpdate)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));

        verify(userService).updateAppUserID(appUserIdDataUpdate);
    }

    @Test
    void shouldReceiveConflictOnExistingIDAppUserIDUpdate() throws Exception {
        doThrow(new NotUniqueAppUserIDException()).when(userService).updateAppUserID(appUserIdDataUpdate);

        mockMvc.perform(
                put(BASE_PATH + UPDATE_APP_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appUserIdDataUpdate)))
                .andExpect(status().isConflict())
                .andExpect(content().string(""));

        verify(userService).updateAppUserID(appUserIdDataUpdate);
    }

    @Test
    void shouldReceiveBadRequestOnNullUserId() throws Exception {
        mockMvc.perform(
                put(BASE_PATH + UPDATE_APP_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appUserIdDataUpdateMissingUserID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void shouldReceiveBadRequestOnNullAppUserIDUpdate() throws Exception {
        mockMvc.perform(
                put(BASE_PATH + UPDATE_APP_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appUserIdDataUpdateMissingUpdateAppID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void shouldReceiveBadRequestOnEmptyUserId() throws Exception {
        mockMvc.perform(
                put(BASE_PATH + UPDATE_APP_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appUserIdDataUpdateEmptyUserID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void shouldReceiveBadRequestOnEmptyAppUserIDUpdate() throws Exception {
        mockMvc.perform(
                put(BASE_PATH + UPDATE_APP_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appUserIdDataUpdateEmptyUpdateAppID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }
}