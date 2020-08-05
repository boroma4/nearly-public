package com.ukrainianboyz.nearly.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.ukrainianboyz.nearly.exceptions.IllegalTokenException;
import com.ukrainianboyz.nearly.db.entity.DatabaseUser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class GoogleAuthenticationServiceTest {

    private final GoogleAuthenticationService googleAuthenticationService = new GoogleAuthenticationService();

    private final DatabaseUser testUser = new DatabaseUser("a","b","c","d","e",0,"wat");

    @Test
    void authorize_throws_on_expired_or_invalid_token() {
        String token = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImZiOGNhNWI3ZDhkOWE1YzZjNjc4ODA3MWU4NjZjNmM0MGYzZmMxZjkiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI4NjgwMDU3MzMyMzMtOTloa2g3dGJjZWRucXNzMGVnb29pNTUwZ2cwZTNmNGguYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI4NjgwMDU3MzMyMzMtNmQ0ZmR0bDhucGZ2Mzk5NjdwMGpobHZoYnZwYzlhNGouYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTExMDY2MTI1NjE1ODQxNzE1NDciLCJlbWFpbCI6ImJvaGRhbi5yb21hc2hjaGVua29AZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJCb2hkYW4gUm9tYXNoY2hlbmtvIiwicGljdHVyZSI6Imh0dHBzOi8vbGg1Lmdvb2dsZXVzZXJjb250ZW50LmNvbS8td29ZSGE0WmQ1bWMvQUFBQUFBQUFBQUkvQUFBQUFBQUFCQTAvQU1adXVjbnJhaGdYN0lhaVVaS1RvSFl1d2t3cXRPMWxaZy9zOTYtYy9waG90by5qcGciLCJnaXZlbl9uYW1lIjoiQm9oZGFuIiwiZmFtaWx5X25hbWUiOiJSb21hc2hjaGVua28iLCJsb2NhbGUiOiJlbi1HQiIsImlhdCI6MTU5MTEyNzA3MCwiZXhwIjoxNTkxMTMwNjcwfQ.oiij5e5wKYHKblEWTVusSWum0BXttxD_lMvL7eQII8sm8sy5OF_KYaMglh5gtMDqaDQchZIUzqlgQ5_bA7gtW22MQVLs1iVRecveLh2DQi_hAuFXEvnbrgwHjgdxfdfi6SsriPrkIsyo-63fEo5wVj2oe_LMLXmrnP9s1TGDEaegj21qgbXhPu3-aj3ZoWW3CyNl_xuR0ErS2F1_J6aGtpxzTmpQk0_-wlKQTSs-nnLHoQPqaZhnAVB_hucqj7RN3JvYs9yYvGrhC5PWiOfHBqYHkCIf7U-N7fmYqmpZdP5Dl3gWAD6MwAbfAyyevkD3jgsBoh1FJP5Sb4Cd9vMrjw";
        Assertions.assertThrows(IllegalTokenException.class, () -> googleAuthenticationService.verify(token));
        Assertions.assertThrows(IllegalTokenException.class, () -> googleAuthenticationService.verify(""));
        Assertions.assertThrows(IllegalTokenException.class, () -> googleAuthenticationService.verify("test"));
        Assertions.assertThrows(IllegalTokenException.class, () -> googleAuthenticationService.verify(null));
    }

    @Test
    void create_user_takes_correct_fields(){
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.set("name",testUser.getUserName());
        payload.setEmail(testUser.getEmail());
        payload.set("picture",testUser.getImageUrl());

        DatabaseUser user = googleAuthenticationService.createUser(payload);

        assertEquals(testUser.getEmail(),user.getEmail(),"Email mismatch...");
        assertEquals(testUser.getUserName(),user.getUserName(),"Name mismatch...");
        assertEquals(testUser.getImageUrl(),user.getImageUrl(),"Image url mismatch...");
        assertEquals(0, (int) user.getStatusIndicator(), "Status was not 0 on creation");
        assertTrue(user.getUserBio().isEmpty(),"Bio was not empty string...");
        assertTrue(user.getAppUserId().isEmpty(),"AppUserId was not empty string...");
    }
}