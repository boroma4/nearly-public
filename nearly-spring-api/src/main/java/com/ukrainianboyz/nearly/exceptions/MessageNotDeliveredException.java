package com.ukrainianboyz.nearly.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.EXPECTATION_FAILED)
public class MessageNotDeliveredException extends RuntimeException {
}
