package com.ukrainianboyz.nearly.functional;

import com.ukrainianboyz.nearly.db.entity.DatabaseUser;
import com.ukrainianboyz.nearly.db.entity.UserRelationship;
import com.ukrainianboyz.nearly.db.enums.Status;
import com.ukrainianboyz.nearly.model.requestdata.FirebaseBasicData;
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
class CallControllerTest {

    private final String initEndpoint = "/api/calls/command";

    @Resource
    private UserRepository userRepository;

    @Resource
    private RelationshipRepository relationshipRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private FirebaseService firebaseService;  //will do nothing when methods from this service are invoked

    private final DatabaseUser testUser1 = new DatabaseUser("id1", "kek", "", "kek@", null, 0,"bim");
    private final DatabaseUser testUser2 = new DatabaseUser("id2", "lol", "", "lol@", null, 1, "bom");
    private final DatabaseUser testUser3 = new DatabaseUser("id3", "gei", "", "guy@", null, 0, "bam");

    private final UserRelationship validRelationship = new UserRelationship(testUser1.getUserId(), testUser2.getUserId(), testUser1.getUserName(), testUser2.getUserName(), Status.ACCEPTED);
    private final UserRelationship invalidRelationship = new UserRelationship(testUser2.getUserId(), testUser3.getUserId(), testUser2.getUserName(), testUser3.getUserName(), Status.REQUEST_SENT);

    @BeforeEach
    void init() {
        relationshipRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(testUser1);
        userRepository.save(testUser2);
        userRepository.save(testUser3);
        relationshipRepository.save(validRelationship);
        relationshipRepository.save(invalidRelationship);
    }

    @Test
    void init_call_func() {
        FirebaseBasicData request = new FirebaseBasicData(testUser1.getUserId(), testUser2.getUserId(),"kek",false);
        HttpEntity<FirebaseBasicData> requestEntity = new HttpEntity<>(request);
        ResponseEntity<Void> response = restTemplate.exchange(initEndpoint, HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status was not \"OK\"!");
        assertNull(response.getBody(), "Response had a body!");

        request  = new FirebaseBasicData(testUser1.getUserId(), testUser3.getUserId(),"kek",false);
        requestEntity = new HttpEntity<>(request);
        response = restTemplate.exchange(initEndpoint, HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Status was not \"Bad request\"!");
        assertNull(response.getBody(), "Response had a body!");

        request = new FirebaseBasicData(testUser2.getUserId(), testUser3.getUserId(),"kek",false);
        requestEntity = new HttpEntity<>(request);
        response = restTemplate.exchange(initEndpoint, HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Status was not \"Bad request\"!");
        assertNull(response.getBody(), "Response had a body!");

        request  = new FirebaseBasicData("fake", testUser3.getUserId(),"kek",false);
        requestEntity = new HttpEntity<>(request);
        response = restTemplate.exchange(initEndpoint, HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Status was not \"Bad request\"!");
        assertNull(response.getBody(), "Response had a body!");

        request  = new FirebaseBasicData("fake", null,"kek",false);
        requestEntity = new HttpEntity<>(request);
        response = restTemplate.exchange(initEndpoint, HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Status was not \"Bad request\"!");
        assertNull(response.getBody(), "Response had a body!");
    }
}