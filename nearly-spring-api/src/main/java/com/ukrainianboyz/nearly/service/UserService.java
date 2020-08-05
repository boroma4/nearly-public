package com.ukrainianboyz.nearly.service;

import com.ukrainianboyz.nearly.db.repository.UserRepository;
import com.ukrainianboyz.nearly.exceptions.NoUsersFoundException;
import com.ukrainianboyz.nearly.exceptions.NotUniqueAppUserIDException;
import com.ukrainianboyz.nearly.model.requestdata.AppUserIdDataUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void updateAppUserID(AppUserIdDataUpdate appUserIdDataUpdate) {
        try {
            userRepository.findById(appUserIdDataUpdate.getUserID())
                    .ifPresentOrElse(databaseUser ->
                            {
                                databaseUser.setAppUserId(appUserIdDataUpdate.getAppUserID());
                                userRepository.save(databaseUser);
                            },
                            NoUsersFoundException::fail
                    );
        } catch (DataIntegrityViolationException e){
            throw new NotUniqueAppUserIDException();
        }
    }
}