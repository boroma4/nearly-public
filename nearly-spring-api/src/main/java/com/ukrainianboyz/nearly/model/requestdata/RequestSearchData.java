package com.ukrainianboyz.nearly.model.requestdata;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Getter
@RequiredArgsConstructor
@Value
public class RequestSearchData {

    @NotBlank
    String startsWith;

    @PositiveOrZero
    int offset;

    @Positive
    int loadAmount;
}
