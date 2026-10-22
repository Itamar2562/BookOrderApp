package com.itamarprojects.bookorderapp.books;

import java.util.UUID;

public record BookOwners
        (
                UUID workerId,
                UUID managerId
        )
{ }
