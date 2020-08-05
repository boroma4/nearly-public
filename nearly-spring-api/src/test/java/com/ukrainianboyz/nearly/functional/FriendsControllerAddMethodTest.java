package com.ukrainianboyz.nearly.functional;

import com.ukrainianboyz.nearly.db.entity.DatabaseUser;
import com.ukrainianboyz.nearly.db.entity.UserRelationship;
import com.ukrainianboyz.nearly.db.enums.Status;
import com.ukrainianboyz.nearly.model.requestdata.RelationshipRequest;
import com.ukrainianboyz.nearly.db.repository.RelationshipRepository;
import com.ukrainianboyz.nearly.db.repository.UserRepository;
import com.ukrainianboyz.nearly.service.FirebaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FriendsControllerAddMethodTest {

    @Resource
    private UserRepository userRepository;

    @Resource
    private RelationshipRepository relationshipRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private FirebaseService firebaseService;  //will do nothing when methods from this service are invoked

    private final DatabaseUser testUser1 = new DatabaseUser("id1", "kek", "", "kek@", null, 0,"dude");
    private final DatabaseUser testUser2 = new DatabaseUser("id2", "lol", "", "lol@", null, 1,"bro");

    private final String addEndpoint = "/api/friends/add";

    @BeforeEach
    void init() {
        relationshipRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(testUser1);
        userRepository.save(testUser2);
    }

    @Test
    void friend_can_be_added() {
        RelationshipRequest rr = new RelationshipRequest(testUser1.getUserId(), testUser2.getUserId());
        HttpEntity<RelationshipRequest> requestEntity = new HttpEntity<>(rr); //httpEnity is body and headers for POST request
        ResponseEntity<Void> response = restTemplate.exchange((addEndpoint), HttpMethod.POST, requestEntity, Void.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Status was not \"Created\"!");
        assertNull(response.getBody(), "Response had a body!");

        UserRelationship savedRelationship = relationshipRepository.findUserRelationship(testUser1.getUserId(), testUser2.getUserId()).get();
        assertNotNull(savedRelationship, "Relationship was not found in the DB!");
        assertEquals(savedRelationship.getRequesterId(), testUser1.getUserId(), "Requester Id was incorrect!");
        assertEquals(savedRelationship.getResponderId(), testUser2.getUserId(), "Responder Id was incorrect!");
        assertEquals(savedRelationship.getRequesterName(), testUser1.getUserName(), "Requester name was incorrect!");
        assertEquals(savedRelationship.getResponderName(), testUser2.getUserName(), "Responder name was incorrect!");
        assertEquals(savedRelationship.getStatus(), Status.REQUEST_SENT, "Status was not \"SENT_REQUEST_FIRST_TO_SECOND \"");
    }

    @Test
    void adding_yourself_returns_bad_request() {
        RelationshipRequest rr = new RelationshipRequest(testUser1.getUserId(), testUser1.getUserId());
        HttpEntity<RelationshipRequest> requestEntity = new HttpEntity<>(rr); //httpEnity is body and headers for POST request
        ResponseEntity<Void> response = restTemplate.exchange((addEndpoint), HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Adding yourself was allowed...");
    }

    @Test
    void adding_nonexistent_users_returns_bad_request() {
        RelationshipRequest rr = new RelationshipRequest(testUser1.getUserId(), "fake");
        HttpEntity<RelationshipRequest> requestEntity = new HttpEntity<>(rr); //httpEnity is body and headers for POST request
        ResponseEntity<Void> response = restTemplate.exchange((addEndpoint), HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Adding fake user was allowed...");

        rr = new RelationshipRequest("fake", testUser2.getUserId());
        requestEntity = new HttpEntity<>(rr); //httpEnity is body and headers for POST request
        response = restTemplate.exchange((addEndpoint), HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Adding fake user was allowed...");

        rr = new RelationshipRequest(null, "fake");
        requestEntity = new HttpEntity<>(rr); //httpEnity is body and headers for POST request
        response = restTemplate.exchange((addEndpoint), HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Adding fake user was allowed...");
    }

    @Test
    void cannot_add_user_if_relationship_exists() {
        UserRelationship relationship = new UserRelationship(testUser1.getUserId(), testUser2.getUserId(), testUser1.getUserName(), testUser2.getUserName(), Status.BLOCKED_BY_FIRST);
        relationshipRepository.save(relationship);

        RelationshipRequest rr = new RelationshipRequest(testUser1.getUserId(), testUser2.getUserId());
        HttpEntity<RelationshipRequest> requestEntity = new HttpEntity<>(rr); //httpEnity is body and headers for POST request
        ResponseEntity<Void> response = restTemplate.exchange((addEndpoint), HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Adding fake user was allowed...");
    }
}