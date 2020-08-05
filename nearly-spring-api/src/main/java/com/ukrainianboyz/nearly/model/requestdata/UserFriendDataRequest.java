package com.ukrainianboyz.nearly.model.requestdata;

import com.ukrainianboyz.nearly.model.enums.FriendListType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
@Getter
@Value
public class UserFriendDataRequest {
    String userId;
    FriendListType listType;
}
