package com.ukrainianboyz.nearly.model.requestdata;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import javax.validation.constraints.NotBlank;

@RequiredArgsConstructor
@Value
public class AppUserIdDataUpdate {
    @NotBlank
    String userID;

    @NotBlank
    String appUserID;
}
