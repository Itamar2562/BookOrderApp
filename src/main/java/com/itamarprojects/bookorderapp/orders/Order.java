package com.itamarprojects.bookorderapp.orders;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.sql.Timestamp;
import java.util.UUID;


public record Order
        (
                @Positive
                Integer id,

                Timestamp createdAt,
                Timestamp finishedAt,

                OrderStatus status,

                @NotNull
                UUID managerId
        )
{ }
