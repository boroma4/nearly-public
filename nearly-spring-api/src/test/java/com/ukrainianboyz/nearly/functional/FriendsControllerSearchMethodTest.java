package com.ukrainianboyz.nearly.functional;

import com.ukrainianboyz.nearly.model.entity.SecureUser;
import com.ukrainianboyz.nearly.db.entity.DatabaseUser;
import com.ukrainianboyz.nearly.model.requestdata.RequestSearchData;
import com.ukrainianboyz.nearly.db.repository.UserRepository;
import com.ukrainianboyz.nearly.util.DataTransferUtils;
import org.assertj.core.util.Arrays;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FriendsControllerSearchMethodTest {

    @Resource
    private UserRepository userRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    private final String searchEndpoint = "/api/friends/search";

    private final DatabaseUser testUser1 = new DatabaseUser("id2", "2", "", "k", null, 0,"kek");
    private final DatabaseUser testUser2 = new DatabaseUser("id1", "1", "", "ke", null, 0, "mek");
    private final DatabaseUser testUser3 = new DatabaseUser("id3", "3", "", "kek@", null, 0, "vek");
    private final DatabaseUser testUser4 = new DatabaseUser("id4", "4", "", "dik", null, 0, "rek");
    private final List<SecureUser> expectedUsers = DataTransferUtils.toSecureUsers(List.of(testUser1));


    @BeforeEach
    void init(){
        userRepository.deleteAll();
        userRepository.save(testUser1);
        userRepository.save(testUser2);
        userRepository.save(testUser3);
        userRepository.save(testUser4);
    }

    @Test
    void can_search_users(){
        RequestSearchData rsd = new RequestSearchData("k",0,3);
        HttpEntity<RequestSearchData> requestEntity = new HttpEntity<>(rsd);
        ResponseEntity<SecureUser[]> response = restTemplate.exchange((searchEndpoint), HttpMethod.POST, requestEntity, SecureUser[].class);
        assertEquals(HttpStatus.OK,response.getStatusCode());
        SecureUser[] users = response.getBody();

        assertNotNull(users,"No response!");
        assertEquals(1, users.length, "incorrect number of users was returned!");
        assertEquals(expectedUsers,Arrays.asList(users),"Incorrect users were returned!");
    }

    @Test
    void search_bounds(){
        // offset above count
        RequestSearchData rsd = new RequestSearchData("k",5,3);
        HttpEntity<RequestSearchData> requestEntity = new HttpEntity<>(rsd);
        ResponseEntity<SecureUser[]> response = restTemplate.exchange((searchEndpoint), HttpMethod.POST, requestEntity, SecureUser[].class);
        assertEquals(HttpStatus.OK,response.getStatusCode());
        SecureUser[] users = response.getBody();
        assertNotNull(users,"No response!");
        assertEquals(0, users.length, "incorrect number of users was returned!");

        // load amount exceeds count
        rsd = new RequestSearchData("k",0,5);
        requestEntity = new HttpEntity<>(rsd);
        response = restTemplate.exchange((searchEndpoint), HttpMethod.POST, requestEntity, SecureUser[].class);
        assertEquals(HttpStatus.OK,response.getStatusCode());
        users = response.getBody();
        assertNotNull(users,"No response!");
        assertEquals(1, users.length, "incorrect number of users was returned!");
        assertEquals(expectedUsers,Arrays.asList(users),"Incorrect users were returned!");
    }

    @Test
    void search_exceptions(){
        RequestSearchData rsd = new RequestSearchData("k",-69,3);
        HttpEntity<RequestSearchData> requestEntity = new HttpEntity<>(rsd);
        ResponseEntity<Void> response = restTemplate.exchange((searchEndpoint), HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST,response.getStatusCode(),"Bad request was not returned when received illegal arg");

        rsd = new RequestSearchData("k",2,-4);
        requestEntity = new HttpEntity<>(rsd);
        response = restTemplate.exchange((searchEndpoint), HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST,response.getStatusCode(),"Bad request was not returned when received illegal arg");

        rsd = new RequestSearchData("k",-32,-4);
        requestEntity = new HttpEntity<>(rsd);
        response = restTemplate.exchange((searchEndpoint), HttpMethod.POST, requestEntity, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST,response.getStatusCode(),"Bad request was not returned when received illegal arg");
    }
}
