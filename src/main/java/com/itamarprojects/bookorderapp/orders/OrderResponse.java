package com.itamarprojects.bookorderapp.orders;

import com.itamarprojects.bookorderapp.books.Book;

import java.util.List;

public record OrderResponse
        (
            Order order,
            List<Book> books
)
{ }
