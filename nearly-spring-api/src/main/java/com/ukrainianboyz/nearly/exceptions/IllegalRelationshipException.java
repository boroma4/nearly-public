package com.ukrainianboyz.nearly.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class IllegalRelationshipException extends RuntimeException{
    public static void fail(){
        throw new IllegalRelationshipException();
    }
}
