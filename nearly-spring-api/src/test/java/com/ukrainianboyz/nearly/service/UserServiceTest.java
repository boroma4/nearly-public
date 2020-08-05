package com.ukrainianboyz.nearly.service;

import com.ukrainianboyz.nearly.db.entity.DatabaseUser;
import com.ukrainianboyz.nearly.db.repository.UserRepository;
import com.ukrainianboyz.nearly.exceptions.NoUsersFoundException;
import com.ukrainianboyz.nearly.exceptions.NotUniqueAppUserIDException;
import com.ukrainianboyz.nearly.model.requestdata.AppUserIdDataUpdate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    UserRepository userRepository;

    private final String newAppUserID = "I AM GOD";
    private final String firstUserID = "100";
    private final String firstUserName = "SASHA";
    private final AppUserIdDataUpdate appUserIdDataUpdate = new AppUserIdDataUpdate(firstUserID, newAppUserID);
    private final DatabaseUser firstUser = new DatabaseUser(firstUserID, firstUserName, "something", "123123@gmail.com", "http:/?/", 1, "sanek");
    private final DatabaseUser firstUserUpdated = new DatabaseUser(firstUserID, firstUserName, "something", "123123@gmail.com", "http:/?/", 1, newAppUserID);

    @Test
    void shouldUpdateAppUserID() {
        when(userRepository.findById(firstUserID)).thenReturn(Optional.of(firstUser));

        userService.updateAppUserID(appUserIdDataUpdate);

        verify(userRepository).save(firstUserUpdated);
    }

    @Test
    void shouldFailUpdateAppUserIDOnMissingUser() {
        when(userRepository.findById(firstUserID)).thenReturn(Optional.empty());

        assertThrows(NoUsersFoundException.class, () -> userService.updateAppUserID(appUserIdDataUpdate));
    }

    @Test
    void shouldFailUpdateAppUserIDOnExistingAppUserID() {
        when(userRepository.findById(firstUserID)).thenReturn(Optional.of(firstUser));
        doThrow(new DataIntegrityViolationException("This is test")).when(userRepository).save(firstUserUpdated);

        assertThrows(NotUniqueAppUserIDException.class, () -> userService.updateAppUserID(appUserIdDataUpdate));
    }
}