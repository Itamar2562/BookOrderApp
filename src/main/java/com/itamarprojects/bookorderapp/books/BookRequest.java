package com.itamarprojects.bookorderapp.books;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;



public record BookRequest
        (
                @NotBlank
                String title,
                String author,
                @PositiveOrZero
                Integer amount,
                @NotNull
                Integer orderId
        )
{
}
