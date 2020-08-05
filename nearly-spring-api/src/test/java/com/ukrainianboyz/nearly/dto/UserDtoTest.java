package com.ukrainianboyz.nearly.dto;

import com.ukrainianboyz.nearly.db.entity.DatabaseUser;
import com.ukrainianboyz.nearly.model.entity.UserDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserDtoTest {

    private DatabaseUser testUser = new DatabaseUser("a","b","c","d","e",0,"kik");
    private UserDto testDto = new UserDto("a","b","c","d","e","kik");

    @Test
    void dto_is_created_correctly(){
        assertEquals(testDto, new UserDto(testUser));
    }

}