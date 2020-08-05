package com.ukrainianboyz.nearly.model.requestdata;


import lombok.Value;

import javax.validation.constraints.NotBlank;

@Value
public class RelationshipRequest {

    @NotBlank
    String requesterId;
    @NotBlank
    String requestedId;
}