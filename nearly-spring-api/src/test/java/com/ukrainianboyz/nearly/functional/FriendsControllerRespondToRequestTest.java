package com.ukrainianboyz.nearly.functional;


import com.ukrainianboyz.nearly.db.entity.DatabaseUser;
import com.ukrainianboyz.nearly.db.entity.UserRelationship;
import com.ukrainianboyz.nearly.db.enums.Status;
import com.ukrainianboyz.nearly.model.requestdata.RelationshipAnswer;
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
public class FriendsControllerRespondToRequestTest {

    @Resource
    private UserRepository userRepository;

    @Resource
    private RelationshipRepository relationshipRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private FirebaseService firebaseService;  //will do nothing when methods from this service are invoked

    private final String endpoint = "/api/friends/respond";


    private final DatabaseUser testUser1 = new DatabaseUser("id1", "kek", "", "kek@", null, 0,"fek");
    private final DatabaseUser testUser2 = new DatabaseUser("id2", "lol", "", "lol@", null, 1, "gek");
    private final DatabaseUser testUser3 = new DatabaseUser("id3", "gei", "", "guy@", null, 0, "mek");
    private final DatabaseUser testUser4 = new DatabaseUser("id4", "lol", "", "psa@", null, 0, "dek");


    private final UserRelationship acceptedRelationship14 = new UserRelationship(testUser1.getUserId(), testUser4.getUserId(), testUser1.getUserName(), testUser4.getUserName(), Status.ACCEPTED);
    private final UserRelationship requestedRelationship23 = new UserRelationship(testUser2.getUserId(), testUser3.getUserId(), testUser2.getUserName(), testUser3.getUserName(), Status.REQUEST_SENT);
    private final UserRelationship requestedRelationship24 = new UserRelationship(testUser2.getUserId(), testUser4.getUserId(), testUser2.getUserName(), testUser4.getUserName(), Status.REQUEST_SENT);
    private final UserRelationship blockedRelationship13 = new UserRelationship(testUser1.getUserId(), testUser3.getUserId(), testUser1.getUserName(), testUser3.getUserName(), Status.BLOCKED_BY_FIRST);


    @BeforeEach
    void init() {
        relationshipRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(testUser1);
        userRepository.save(testUser2);
        userRepository.save(testUser3);
        userRepository.save(testUser4);
        relationshipRepository.save(requestedRelationship23);
        relationshipRepository.save(acceptedRelationship14);
        relationshipRepository.save(requestedRelationship24);
        relationshipRepository.save(blockedRelationship13);
    }

    @Test
    void can_accept_or_decline_friend(){
        RelationshipAnswer ra = new RelationshipAnswer(testUser2.getUserId(), testUser3.getUserId(),true);
        HttpEntity<RelationshipAnswer> requestEntity = new HttpEntity<>(ra);
        ResponseEntity<Void> response = restTemplate.exchange((endpoint), HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.ACCEPTED,response.getStatusCode());
        UserRelationship savedRelationship =  relationshipRepository.findUserRelationship(testUser2.getUserId(), testUser3.getUserId()).get();
        assertNotNull(savedRelationship);
        assertEquals(Status.ACCEPTED,savedRelationship.getStatus(),"Relationship status was not updated");

        ra = new RelationshipAnswer(testUser2.getUserId(), testUser4.getUserId(),false);
        requestEntity = new HttpEntity<>(ra);
        response = restTemplate.exchange((endpoint), HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.ACCEPTED,response.getStatusCode());
        assertFalse(relationshipRepository.findUserRelationship(testUser2.getUserId(), testUser4.getUserId()).isPresent(),
                "Relationship was not deleted after declining request.");
    }

    @Test
    void no_relationship_returns_bad_request(){
        RelationshipAnswer ra = new RelationshipAnswer(testUser1.getUserId(), testUser2.getUserId(),true);
        HttpEntity<RelationshipAnswer> requestEntity = new HttpEntity<>(ra);
        ResponseEntity<Void> response = restTemplate.exchange((endpoint), HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST,response.getStatusCode());
    }

    @Test
    void wrong_relationship_returns_conflict(){
        RelationshipAnswer ra = new RelationshipAnswer(testUser1.getUserId(), testUser3.getUserId(),false);
        HttpEntity<RelationshipAnswer> requestEntity = new HttpEntity<>(ra);
        ResponseEntity<Void> response = restTemplate.exchange((endpoint), HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.CONFLICT,response.getStatusCode());
    }

    @Test
    void wrong_user_data_returns_bad_request(){
        RelationshipAnswer ra = new RelationshipAnswer("kek", testUser3.getUserId(),false);
        HttpEntity<RelationshipAnswer> requestEntity = new HttpEntity<>(ra);
        ResponseEntity<Void> response = restTemplate.exchange((endpoint), HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST,response.getStatusCode());
    }


}
