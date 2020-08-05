package com.ukrainianboyz.nearly.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class NotUniqueAppUserIDException extends RuntimeException{
}
