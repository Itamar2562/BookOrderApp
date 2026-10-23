package com.itamarprojects.bookorderapp.books;


import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.LinkedHashMap;
import java.util.Map;

public record BookPatchRequest
        (
                @Size(min = 1, max = 256)
                String title,

                @Size(max = 256)
                String author,

                @PositiveOrZero
                Integer amount
        ) {
    public BookPatchRequest {
        if (title != null)
            title = title.strip();
        if (author != null)
            author = author.strip();
    }

    public Map<String, Object> toUpdates()
    {
        Map<String, Object> fields = new LinkedHashMap<>();
        if (this.title !=null)
            fields.put("title",this.title);
        if (this.author !=null)
            fields.put("author",this.author);
        if (this.amount != null)
            fields.put("amount",this.amount);
        return fields;
    }



}