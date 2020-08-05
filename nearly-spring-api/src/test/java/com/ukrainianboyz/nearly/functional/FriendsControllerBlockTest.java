package com.ukrainianboyz.nearly.functional;

import com.ukrainianboyz.nearly.db.entity.DatabaseUser;
import com.ukrainianboyz.nearly.db.entity.UserRelationship;
import com.ukrainianboyz.nearly.db.enums.Status;
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

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FriendsControllerBlockTest {

    @Resource
    private UserRepository userRepository;

    @Resource
    private RelationshipRepository relationshipRepository;

    @Autowired
    private TestRestTemplate restTemplate;


    private static final DatabaseUser testUser1 = new DatabaseUser("id1", "kek", "", "kek@", null, 0,"dude");
    private static final DatabaseUser testUser2 = new DatabaseUser("id2", "lol", "", "lol@", null, 1,"bro");

    private static final String FAKE = "fake";
    private static final String BLOCK_USER_ENDPOINT = "/api/friends/blockUser";

    @BeforeEach
    void init() {
        relationshipRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(testUser1);
        userRepository.save(testUser2);
    }

    @Test
    void block_user_no_relation() {
        RelationshipRequest rr = new RelationshipRequest(testUser1.getUserId(), testUser2.getUserId());
        HttpEntity<RelationshipRequest> requestEntity = new HttpEntity<>(rr);
        ResponseEntity<Void> response = restTemplate.exchange((BLOCK_USER_ENDPOINT), HttpMethod.POST, requestEntity, Void.class);
        assertNotNull(relationshipRepository.findUserRelationship(testUser1.getUserId(), testUser2.getUserId()),
                "User was not blocked");
    }

    @Test
    void cannot_block_non_existent_user() {
        RelationshipRequest rr = new RelationshipRequest(testUser1.getUserId(), FAKE);
        HttpEntity<RelationshipRequest> requestEntity = new HttpEntity<>(rr);
        ResponseEntity<Void> response = restTemplate.exchange((BLOCK_USER_ENDPOINT), HttpMethod.POST, requestEntity, Void.class);
        assertFalse(relationshipRepository.findUserRelationship(testUser1.getUserId(), FAKE).isPresent(),
                "User was not blocked");
    }

    @Test
    void response_test() {
        RelationshipRequest rr = new RelationshipRequest(testUser1.getUserId(), testUser2.getUserId());
        HttpEntity<RelationshipRequest> requestEntity = new HttpEntity<>(rr);
        ResponseEntity<Void> response = restTemplate.exchange((BLOCK_USER_ENDPOINT), HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status was not \"OK\"!");
    }

    @Test
    void cannot_block_yourself() {
        RelationshipRequest rr = new RelationshipRequest(testUser1.getUserId(), testUser1.getUserId());
        HttpEntity<RelationshipRequest> requestEntity = new HttpEntity<>(rr);
        ResponseEntity<Void> response = restTemplate.exchange((BLOCK_USER_ENDPOINT), HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Status was not \"BAD_REQUEST\"!");
    }
}