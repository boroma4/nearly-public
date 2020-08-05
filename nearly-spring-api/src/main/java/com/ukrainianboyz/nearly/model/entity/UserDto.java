package com.ukrainianboyz.nearly.model.entity;


import com.ukrainianboyz.nearly.db.entity.DatabaseUser;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class UserDto {

    String UserId;

    String UserName;

    String UserBio;

    String Email;

    String ImageUrl;

    String AppUserId;

    public UserDto(DatabaseUser dbUser){
        UserId = dbUser.getUserId();
        UserName = dbUser.getUserName();
        UserBio = dbUser.getUserBio();
        Email = dbUser.getEmail();
        ImageUrl = dbUser.getImageUrl();
        AppUserId = dbUser.getAppUserId();
    }

}
