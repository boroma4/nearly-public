package com.ukrainianboyz.nearly.util;

import com.ukrainianboyz.nearly.db.entity.DatabaseUser;
import com.ukrainianboyz.nearly.db.entity.UserRelationship;
import com.ukrainianboyz.nearly.model.entity.SecureUser;
import com.ukrainianboyz.nearly.db.enums.Status;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class DataTransferUtils {

    public List<SecureUser> toSecureUsers (List<DatabaseUser> users){
        List<SecureUser> scUsers = new ArrayList<>();
        for (DatabaseUser user : users){
            scUsers.add(new SecureUser(user.getUserId(), user.getUserName(), user.getUserBio(),
                    user.getImageUrl(), user.getStatusIndicator(), user.getAppUserId()));
        }
        return scUsers;
    }

    public SecureUser toSecureUser (DatabaseUser user){
        return new SecureUser(user.getUserId(), user.getUserName(), user.getUserBio(),
                user.getImageUrl(), user.getStatusIndicator(), user.getAppUserId());
    }

    public UserRelationship createUserRelationship(DatabaseUser requester, DatabaseUser requested, Status status){
        return UserRelationship.builder()
                .requesterId(requester.getUserId())
                .requesterName(requester.getUserName())
                .responderId(requested.getUserId())
                .responderName(requested.getUserName())
                .status(status)
                .build();
    }
}
