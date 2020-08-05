package com.ukrainianboyz.nearly.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NoUsersFoundException extends RuntimeException{
    public static void fail(){
        throw new NoUsersFoundException();
    }
}
