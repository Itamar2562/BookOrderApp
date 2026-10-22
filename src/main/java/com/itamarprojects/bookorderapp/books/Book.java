package com.itamarprojects.bookorderapp.books;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.sql.Timestamp;
import java.util.UUID;


public record Book
        (
    @Positive
    Integer id,
    @NotBlank
    String title,
    String author,
    @PositiveOrZero
    Integer amount,
    Timestamp createdAt,

    @NotNull
    UUID workerId,
    @NotNull
    Integer orderId
)
{ }
