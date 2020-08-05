package com.ukrainianboyz.nearly.functional;

import com.ukrainianboyz.nearly.model.entity.SecureUser;
import com.ukrainianboyz.nearly.db.entity.DatabaseUser;
import com.ukrainianboyz.nearly.db.entity.UserRelationship;
import com.ukrainianboyz.nearly.db.enums.Status;
import com.ukrainianboyz.nearly.db.repository.RelationshipRepository;
import com.ukrainianboyz.nearly.db.repository.UserRepository;
import com.ukrainianboyz.nearly.util.DataTransferUtils;
import com.ukrainianboyz.nearly.service.FirebaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.*;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FriendsControllerFriendsFetchingTest {

    @Resource
    private UserRepository userRepository;

    @Resource
    private RelationshipRepository relationshipRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private FirebaseService firebaseService;  //will do nothing when methods from this service are invoked

    private final String existingEndpoint = "/api/friends/allFriends/";
    private final String outgoingEndpoint = "/api/friends/outgoingFriendRequests/";
    private final String incomingEndpoint = "/api/friends/incomingFriendRequests/";

    private final DatabaseUser testUser1 = new DatabaseUser("id1", "kek", "", "kek@", null, 0, "doom");
    private final DatabaseUser testUser2 = new DatabaseUser("id2", "lol", "", "lol@", null, 1, "dam");
    private final DatabaseUser testUser3 = new DatabaseUser("id3", "gei", "", "guy@", null, 0, "drone");
    private final DatabaseUser testUser4 = new DatabaseUser("id4", "lol", "", "psa@", null, 0, "vroom");


    private final UserRelationship acceptedRelationship12 = new UserRelationship(testUser1.getUserId(), testUser2.getUserId(), testUser1.getUserName(), testUser2.getUserName(), Status.ACCEPTED);
    private final UserRelationship acceptedRelationship14 = new UserRelationship(testUser1.getUserId(), testUser4.getUserId(), testUser1.getUserName(), testUser4.getUserName(), Status.ACCEPTED);
    private final UserRelationship requestedRelationship23 = new UserRelationship(testUser2.getUserId(), testUser3.getUserId(), testUser2.getUserName(), testUser3.getUserName(), Status.REQUEST_SENT);
    private final UserRelationship blockedRelationship13 = new UserRelationship(testUser1.getUserId(), testUser3.getUserId(), testUser1.getUserName(), testUser3.getUserName(), Status.BLOCKED_BY_FIRST);


    @BeforeEach
    void init() {
        relationshipRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(testUser1);
        userRepository.save(testUser2);
        userRepository.save(testUser3);
        userRepository.save(testUser4);
        relationshipRepository.save(acceptedRelationship12);
        relationshipRepository.save(requestedRelationship23);
        relationshipRepository.save(acceptedRelationship14);
        relationshipRepository.save(blockedRelationship13);
    }

    @Test
    void can_get_valid_friends() {
        ResponseEntity<SecureUser[]> responseEntity =
                restTemplate.exchange(existingEndpoint + testUser1.getUserId(), HttpMethod.GET, null, SecureUser[].class);
        assertEquals(HttpStatus.OK,responseEntity.getStatusCode());
        List<SecureUser> users = Arrays.asList(Objects.requireNonNull(responseEntity.getBody()));
        assertEquals(DataTransferUtils.toSecureUsers(List.of(testUser2,testUser4)),users,"Wrong friends were loaded!");

        responseEntity =
                restTemplate.exchange(outgoingEndpoint + testUser2.getUserId(), HttpMethod.GET, null, SecureUser[].class);
        assertEquals(HttpStatus.OK,responseEntity.getStatusCode());
        users = Arrays.asList(Objects.requireNonNull(responseEntity.getBody()));
        assertEquals(DataTransferUtils.toSecureUsers(List.of(testUser3)),users,"Wrong outgoing requests were loaded!");

        responseEntity =
                restTemplate.exchange(incomingEndpoint + testUser3.getUserId(), HttpMethod.GET, null, SecureUser[].class);
        assertEquals(HttpStatus.OK,responseEntity.getStatusCode());
        users = Arrays.asList(Objects.requireNonNull(responseEntity.getBody()));
        assertEquals(DataTransferUtils.toSecureUsers(List.of(testUser2)),users,"Wrong outgoing requests were loaded!");
    }

    @Test
    void can_get_empty_lists() {
        ResponseEntity<SecureUser[]> responseEntity =
                restTemplate.exchange(incomingEndpoint + testUser1.getUserId(), HttpMethod.GET, null, SecureUser[].class);
        assertEquals(HttpStatus.OK,responseEntity.getStatusCode());
        List<SecureUser> users = Arrays.asList(Objects.requireNonNull(responseEntity.getBody()));
        assertEquals(List.of(),users,"Something was loaded!");

        responseEntity =
                restTemplate.exchange(existingEndpoint + testUser3.getUserId(), HttpMethod.GET, null, SecureUser[].class);
        assertEquals(HttpStatus.OK,responseEntity.getStatusCode());
        users = Arrays.asList(Objects.requireNonNull(responseEntity.getBody()));
        assertEquals(List.of(),users,"Something was loaded!");

        responseEntity =
                restTemplate.exchange(outgoingEndpoint + testUser1.getUserId(), HttpMethod.GET, null, SecureUser[].class);
        assertEquals(HttpStatus.OK,responseEntity.getStatusCode());
        users = Arrays.asList(Objects.requireNonNull(responseEntity.getBody()));
        assertEquals(List.of(),users,"Something was loaded!");
    }

    @Test
    void bad_request_is_returned_if_userId_is_invalid(){
        ResponseEntity<Void> responseEntity =
                restTemplate.exchange(incomingEndpoint + "fake", HttpMethod.GET, null, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST,responseEntity.getStatusCode());

        responseEntity = restTemplate.exchange(existingEndpoint + "fake", HttpMethod.GET, null, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST,responseEntity.getStatusCode());

        responseEntity = restTemplate.exchange(outgoingEndpoint + "fake", HttpMethod.GET, null, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST,responseEntity.getStatusCode());
    }
}
