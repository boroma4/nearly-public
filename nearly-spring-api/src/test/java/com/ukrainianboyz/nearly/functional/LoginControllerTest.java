package com.ukrainianboyz.nearly.functional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.ukrainianboyz.nearly.model.entity.UserDto;
import com.ukrainianboyz.nearly.exceptions.IllegalTokenException;
import com.ukrainianboyz.nearly.db.entity.DatabaseUser;
import com.ukrainianboyz.nearly.db.repository.UserRepository;
import com.ukrainianboyz.nearly.service.GoogleAuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import javax.annotation.Resource;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LoginControllerTest {

    @Resource
    private UserRepository userRepository;

    @MockBean
    private GoogleAuthenticationService googleAuthenticationService;

    @Autowired
    private TestRestTemplate restTemplate;
    
    private final DatabaseUser testUser = new DatabaseUser("a","b","c","d","e",0,"ooo");
    // email must be same
    private final DatabaseUser wrongUser = new DatabaseUser("w","b","p","d","e",0,"aaaa");
    private final UserDto testDto = new UserDto(testUser);
    private final String testToken = "token";
    private final String googleEndpoint = "/api/login/googleToken/";

    @Mock
    GoogleIdToken.Payload mockPayload;

    @BeforeEach
    void init(){
        userRepository.deleteAll(); // just in case
    }

    @Test
    void google_token_auth_is_handled_correctly(){
        //mocking google service interactions
        Mockito.when(googleAuthenticationService.verify(testToken)).thenReturn(mockPayload);
        Mockito.when(mockPayload.getEmail()).thenReturn(testUser.getEmail());
        Mockito.when(googleAuthenticationService.createUser(mockPayload)).thenReturn(testUser);

        //making a "request" to the endpoint, checking response
        ResponseEntity<UserDto> response = restTemplate.exchange((googleEndpoint + testToken), HttpMethod.GET,null, UserDto.class);
        assertNotNull(response,"There was no response...");
        UserDto user = response.getBody();
        assertNotNull(user,"There was not response body...");
        String message = String.format("id=%s;name=%s;email=%s;", user.getUserId(), user.getEmail(), user.getUserName());
        assertEquals(testDto,user,"Returned user was not equal to one provided by service... " + message);

        //check if user was saved after logging in first time
        Optional<DatabaseUser> savedUserEntity = userRepository.findById(testUser.getUserId());
        assertTrue(savedUserEntity.isPresent(),"User was not saved to the DB...");
        assertEquals(testUser, savedUserEntity.get(),"Saved user was incorrect...");

        //verifying that if user with the email already exists, it is not overwritten
        Mockito.when(googleAuthenticationService.createUser(mockPayload)).thenReturn(wrongUser);

        //check that old user is returned
        response = restTemplate.exchange((googleEndpoint + testToken), HttpMethod.GET,null, UserDto.class);
        assertNotNull(response,"There was no response...");
        user = response.getBody();
        assertNotNull(user,"There was not response body...");
        assertEquals(testDto,user,"Existing user was overwritten... " + message);

        //check that old user is still in the db and new one was not created
        savedUserEntity = userRepository.findById(testUser.getUserId());
        assertTrue(savedUserEntity.isPresent(),"User was not saved to the DB...");
        assertEquals(testUser, savedUserEntity.get(),"Saved user was incorrect...");
        assertFalse(userRepository.findById(wrongUser.getUserId()).isPresent());

    }

    @Test
    void endpoint_returns_bad_request_if_token_is_invalid() {

        //because we already that service throws correctly separately, we can safely mock it here
        Mockito.when(googleAuthenticationService.verify(testToken)).thenThrow(IllegalTokenException.class);

        ResponseEntity<DatabaseUser> entity = restTemplate.exchange((googleEndpoint + testToken), HttpMethod.GET,null, DatabaseUser.class);
        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode(),"Bad request was not returned...");
    }
}