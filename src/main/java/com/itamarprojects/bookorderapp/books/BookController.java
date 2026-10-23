package com.itamarprojects.bookorderapp.books;

import com.itamarprojects.bookorderapp.orders.OrderRepository;
import com.itamarprojects.bookorderapp.users.User;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import org.slf4j.Logger;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/books")
public class BookController
{
    private static final Logger log = LoggerFactory.getLogger(BookController.class);
    private BookRepository bookRepository;
    private OrderRepository orderRepository;
    public BookController(BookRepository bookRepository , OrderRepository orderRepository){
        this.bookRepository = bookRepository;
        this.orderRepository = orderRepository;
    }

    //temporary
    @GetMapping("/{book_id}")
    Book getBook(@PathVariable Integer book_id)
    {
        Optional<Book> book= this.bookRepository.getBookById(book_id);
        if (book.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Book not found");
        return book.get();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("")
    Book addBooks(@AuthenticationPrincipal User currentUser,@Valid @RequestBody BookRequest bookRequest)
    {
        UUID orderManagerId = this.orderRepository.getManagerIdByOrderId(
                bookRequest.orderId()).orElseThrow(() ->new ResponseStatusException(HttpStatus.NOT_FOUND,"Order not found"));
        if (orderManagerId.equals(currentUser.getManagerId()) || orderManagerId.equals(currentUser.getId()))
            return this.bookRepository.addBook(bookRequest,currentUser.getId());
        else
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

    }

        @PatchMapping("/{book_id}")
        Book updateBook(@AuthenticationPrincipal User currentUser, @PathVariable Integer book_id,
                        @Valid @RequestBody BookPatchRequest request)
        {
            Map<String, Object> updates = request.toUpdates();
            if (updates.isEmpty())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patch must contain at least one field");

            BookOwners bookOwners = this.bookRepository.getBookOwnersByBookId(book_id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

            if (!currentUser.getId().equals(bookOwners.managerId()) &&
                    !currentUser.getId().equals(bookOwners.workerId()))
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);

            return this.bookRepository.updateBook(book_id, updates)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
        }

        @ResponseStatus(HttpStatus.NO_CONTENT)
        @DeleteMapping("/{book_id}")
        void deleteBookById(@AuthenticationPrincipal User currentUser,  @PathVariable Integer book_id)
    {
        BookOwners bookOwners = this.bookRepository.getBookOwnersByBookId(book_id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

        if (currentUser.getId().equals(bookOwners.managerId()) || currentUser.getId().equals(bookOwners.workerId()))
            this.bookRepository.deleteBook(book_id);
        else
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
}
