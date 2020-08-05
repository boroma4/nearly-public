package com.ukrainianboyz.nearly.functional;

import com.ukrainianboyz.nearly.db.entity.DatabaseUser;
import com.ukrainianboyz.nearly.db.entity.UserRelationship;
import com.ukrainianboyz.nearly.db.enums.Status;
import com.ukrainianboyz.nearly.model.requestdata.RelationshipAnswer;
import com.ukrainianboyz.nearly.model.requestdata.RelationshipRequest;
import com.ukrainianboyz.nearly.db.repository.RelationshipRepository;
import com.ukrainianboyz.nearly.db.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

//TODO: delete extends with in all tests
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FriendsControllerRemoveAddTest {

    @Resource
    private UserRepository userRepository;

    @Resource
    private RelationshipRepository relationshipRepository;

    @Autowired
    private TestRestTemplate restTemplate;



    private static final DatabaseUser testUser1 = new DatabaseUser("id1", "kek", "", "kek@", null, 0, "fek");
    private static final DatabaseUser testUser2 = new DatabaseUser("id2", "lol", "", "lol@", null, 1, "fak");

    private static final String removeAddEndpoint = "/api/friends/removeAddRequest";
    private static final String confirmAdd = "/api/friends/respond";

    //login user(real user) ->
    @BeforeEach
    void init() {
        relationshipRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(testUser1);
        userRepository.save(testUser2);
        relationshipRepository.save(new UserRelationship(testUser1.getUserId(), testUser2.getUserId(), "something1", "something2",
                Status.REQUEST_SENT));
    }

    @Test
    void add_request_can_be_removed_by_first() {
        RelationshipRequest rr = new RelationshipRequest(testUser1.getUserId(), testUser2.getUserId());
        HttpEntity<RelationshipRequest> requestEntity = new HttpEntity<>(rr);
        ResponseEntity<Void> response = restTemplate.exchange((removeAddEndpoint), HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status was not \"OK\"!");
        assertNull(response.getBody(), "Response has a body! (Should not)");

        assertFalse(relationshipRepository.findUserRelationship(testUser1.getUserId(), testUser2.getUserId()).isPresent(),
                "Relationship still exists!");
    }

    @Test
    void add_request_cannot_be_removed_by_other() {
        //interchange users, second user cannot delete request if it was send by first
        RelationshipRequest rr = new RelationshipRequest(testUser2.getUserId(), testUser1.getUserId());
        HttpEntity<RelationshipRequest> requestEntity = new HttpEntity<>(rr);
        ResponseEntity<Void> response = restTemplate.exchange((removeAddEndpoint), HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Status was not \"BAD_REQUEST\"");
        assertNull(response.getBody(), "Response has a body! (Should not)");

        assertNotNull(relationshipRepository.findUserRelationship(testUser1.getUserId(), testUser2.getUserId()),
                "Relationship got deleted");
    }

    @Test
    void removing_add_request_nonexistent_user() {
        RelationshipRequest rr = new RelationshipRequest(testUser1.getUserId(), "fake");
        HttpEntity<RelationshipRequest> requestEntity = new HttpEntity<>(rr);
        ResponseEntity<Void> response = restTemplate.exchange((removeAddEndpoint), HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Removed non-existent user.");

        rr = new RelationshipRequest("fake", testUser1.getUserId());
        requestEntity = new HttpEntity<>(rr);
        response = restTemplate.exchange((removeAddEndpoint), HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Removed non-existent user.");

        rr = new RelationshipRequest("fake", null);
        requestEntity = new HttpEntity<>(rr);
        response = restTemplate.exchange((removeAddEndpoint), HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Removed non-existent user.");
    }

    @Test
    void removing_add_existent_friend() {
        RelationshipAnswer ra = new RelationshipAnswer(testUser1.getUserId(), testUser2.getUserId(), true);
        HttpEntity<RelationshipAnswer> requestEntity = new HttpEntity<>(ra);
        restTemplate.exchange((confirmAdd), HttpMethod.POST, requestEntity, Void.class);

        RelationshipRequest rr = new RelationshipRequest(testUser2.getUserId(), testUser1.getUserId());
        HttpEntity<RelationshipRequest> requestEntityRR = new HttpEntity<>(rr);
        ResponseEntity<Void> response = restTemplate.exchange((removeAddEndpoint), HttpMethod.POST, requestEntityRR, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Deleted adding, while users are confirmed friends");
    }




}