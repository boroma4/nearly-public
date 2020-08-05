package com.ukrainianboyz.nearly.model.requestdata;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@RequiredArgsConstructor
@Value
public class RelationshipAnswer {

    @NotBlank
    String requesterId;

    @NotBlank
    String requestedId;

    @NotNull
    Boolean isAccepted;
}
