package com.ukrainianboyz.nearly.controllers;

import com.ukrainianboyz.nearly.model.requestdata.AppUserIdDataUpdate;
import com.ukrainianboyz.nearly.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("updateAppUserID")
    public void updateAppUserID(@Valid @RequestBody AppUserIdDataUpdate appUserIdDataUpdate) {
        userService.updateAppUserID(appUserIdDataUpdate);
    }
}
