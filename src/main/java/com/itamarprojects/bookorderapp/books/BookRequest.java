package com.itamarprojects.bookorderapp.books;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;


public record BookRequest
        (
                @NotBlank
                @Size(max = 256)
                String title,
                @Size(max = 256)
                String author,
                @PositiveOrZero
                Integer amount,
                @NotNull
                Integer orderId
        )
{
                public BookRequest {
                        if (title != null)
                                title = title.strip();
                        if (author != null)
                                author = author.strip();
                }
        }
