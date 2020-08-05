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
class FriendsControllerUnblockTest {

    @Resource
    private UserRepository userRepository;

    @Resource
    private RelationshipRepository relationshipRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    private static final DatabaseUser testUser1 = new DatabaseUser("id1", "kek", "", "kek@", null, 0,"wot");
    private static final DatabaseUser testUser2 = new DatabaseUser("id2", "lol", "", "lol@", null, 1, "watt");

    private static final String FAKE = "fake";
    private static final String UNBLOCK_USER_ENDPOINT = "/api/friends/unblockUser";

    @BeforeEach
    void init() {
        relationshipRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(testUser1);
        userRepository.save(testUser2);
    }

    @Test
    void cannot_unblock_nonexistent_block() {
        RelationshipRequest rr = new RelationshipRequest(testUser1.getUserId(), testUser2.getUserId());
        HttpEntity<RelationshipRequest> requestEntity = new HttpEntity<>(rr);
        ResponseEntity<Void> response = restTemplate.exchange((UNBLOCK_USER_ENDPOINT), HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Unblocked non-existent relation");
    }

    @Test
    void cannot_unblock_yourself() {
        RelationshipRequest rr = new RelationshipRequest(testUser1.getUserId(), testUser1.getUserId());
        HttpEntity<RelationshipRequest> requestEntity = new HttpEntity<>(rr);
        ResponseEntity<Void> response = restTemplate.exchange((UNBLOCK_USER_ENDPOINT), HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Unblocked non-existent relation");
    }

    @Test
    void cannot_unblock_fake_user() {
        RelationshipRequest rr = new RelationshipRequest(testUser1.getUserId(), FAKE);
        HttpEntity<RelationshipRequest> requestEntity = new HttpEntity<>(rr);
        ResponseEntity<Void> response = restTemplate.exchange((UNBLOCK_USER_ENDPOINT), HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Unblocked non-existent user");
    }

    @Test
    void unblock_blocked_user() {
        relationshipRepository.save(new UserRelationship(testUser1.getUserId(), testUser2.getUserId(),
                "first", "second", Status.BLOCKED_BY_SECOND));
        RelationshipRequest rr = new RelationshipRequest(testUser2.getUserId(), testUser1.getUserId());
        HttpEntity<RelationshipRequest> requestEntity = new HttpEntity<>(rr);
        ResponseEntity<Void> response = restTemplate.exchange((UNBLOCK_USER_ENDPOINT), HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Did not unblock user");
    }

    @Test
    void first_cannot_unblock_himself() {
        relationshipRepository.save(new UserRelationship(testUser1.getUserId(), testUser2.getUserId(),
                "first", "second", Status.BLOCKED_BY_SECOND));
        RelationshipRequest rr = new RelationshipRequest(testUser1.getUserId(), testUser2.getUserId());
        HttpEntity<RelationshipRequest> requestEntity = new HttpEntity<>(rr);
        ResponseEntity<Void> response = restTemplate.exchange((UNBLOCK_USER_ENDPOINT), HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Unblocked user by himself whom was blocked by other");
    }
}