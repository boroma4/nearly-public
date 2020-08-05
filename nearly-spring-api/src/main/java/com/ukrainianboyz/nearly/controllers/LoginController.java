package com.ukrainianboyz.nearly.controllers;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.ukrainianboyz.nearly.model.entity.UserDto;
import com.ukrainianboyz.nearly.db.entity.DatabaseUser;
import com.ukrainianboyz.nearly.service.GoogleAuthenticationService;
import com.ukrainianboyz.nearly.db.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@RestController
@RequestMapping("api/login")
@AllArgsConstructor
public class LoginController {

    @Resource
    private final UserRepository repository;
    private final GoogleAuthenticationService googleAuthenticationService;

    @GetMapping("googleToken/{token}")
    public UserDto createUser(@PathVariable("token") String idTokenString) {
        GoogleIdToken.Payload googlePayload = googleAuthenticationService.verify(idTokenString);
        DatabaseUser entity = repository.findByEmail(googlePayload.getEmail());
        if (entity == null) {
            DatabaseUser user = googleAuthenticationService.createUser(googlePayload);
            repository.save(user);
            return new  UserDto(user);
        }
        return new UserDto(entity);
    }
}

