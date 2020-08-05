package com.ukrainianboyz.nearly.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.ukrainianboyz.nearly.exceptions.IllegalTokenException;
import com.ukrainianboyz.nearly.db.entity.DatabaseUser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@AllArgsConstructor
@Service
public class GoogleAuthenticationService {


    private final HttpTransport transport = new NetHttpTransport();
    private final JacksonFactory jsonFactory = new JacksonFactory();
    private final String CLIENT_ID = "xx";

    public GoogleIdToken.Payload verify(String idTokenString) {

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(CLIENT_ID))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) return idToken.getPayload();
        } catch (Exception ignored) {
        }

        System.out.println("Invalid ID token.");
        throw new IllegalTokenException();
    }


    //consider remaking to convert to db user
    public DatabaseUser createUser(GoogleIdToken.Payload payload) {

        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");

            /*
             * information that is also given by google but not used at the moment

               boolean emailVerified = payload.getEmailVerified();
               String locale = (String) payload.get("locale");
               String givenName = (String) payload.get("given_name");
             */
        UUID uuid = UUID.randomUUID();
        return new DatabaseUser(uuid.toString(), name, "", email, pictureUrl, 0, "");
    }

}
