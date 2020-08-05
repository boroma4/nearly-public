package com.ukrainianboyz.nearly.service;

import com.ukrainianboyz.nearly.model.entity.SecureUser;
import com.ukrainianboyz.nearly.db.entity.DatabaseUser;
import com.ukrainianboyz.nearly.util.DataTransferUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataTransferUtilsTest {

    private final DatabaseUser testUser1 = new DatabaseUser("id1", "1", "", "d", null, 0,"wroom");
    private final DatabaseUser testUser2 = new DatabaseUser("id2", "2", "", "k", null, 0, "wroos");
    private final SecureUser testSecureUser1 = new SecureUser("id1", "1", "", null, 0, "wroom");
    private final SecureUser testSecureUser2 = new SecureUser("id2", "2", "",  null, 0, "wroos");

    @Test
    void to_secure_users_works() {
        List<SecureUser> expected = List.of(testSecureUser1,testSecureUser2);
        List<SecureUser> actual = DataTransferUtils.toSecureUsers(List.of(testUser1,testUser2));
        assertEquals(expected,actual,"Users were converted incorrectly!");
    }

    @Test
    void to_secure_user_works() {
        SecureUser actual = DataTransferUtils.toSecureUser(testUser1);
        assertEquals(testSecureUser1,actual,"User was converted incorrectly!");
    }
}