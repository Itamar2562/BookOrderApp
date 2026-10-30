package com.itamarprojects.bookorderapp.orders;

import com.itamarprojects.bookorderapp.books.Book;
import com.itamarprojects.bookorderapp.books.BookRepository;
import com.itamarprojects.bookorderapp.users.User;
import com.itamarprojects.bookorderapp.users.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import org.slf4j.Logger;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController
{
    private static final Logger log = LoggerFactory.getLogger(com.itamarprojects.bookorderapp.orders.OrderController.class);
    private OrderRepository orderRepository;
    private BookRepository bookRepository;

    public OrderController(OrderRepository orderRepository , BookRepository bookRepository){
        this.orderRepository = orderRepository;
        this.bookRepository = bookRepository;
    }

    @GetMapping("/{order_id}")
    OrderResponse getOrder(@AuthenticationPrincipal User currentUser,@PathVariable Integer order_id)
    {
        Order order = this.orderRepository.getOrderById(order_id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));

        if(currentUser.getId().equals(order.managerId()) || (currentUser.getManagerId()!=null && currentUser.getManagerId().equals(order.managerId())))
        {
            List<Book> books = bookRepository.getBooksByOrderId(order_id);
            return new OrderResponse(order,books);
        }
        else
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Order not found");
    }

    @PostMapping("")
    OrderResponse addOrder(@AuthenticationPrincipal User currentUser)
    {
        Order order = this.orderRepository.addOrder(currentUser.getId());
        return new OrderResponse(order, new ArrayList<>());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{order_id}")
    void deleteOrder(@AuthenticationPrincipal User currentUser,@PathVariable Integer order_id)
    {
        UUID OrderOwner = this.orderRepository.getManagerIdByOrderId(order_id)
                .orElseThrow(() ->new ResponseStatusException(HttpStatus.NOT_FOUND,"Order not found"));
        if (!OrderOwner.equals(currentUser.getId()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Order not found");
        this.orderRepository.deleteOrder(order_id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/{order_id}")
    void finishOrder(@AuthenticationPrincipal User currentUser , @PathVariable Integer order_id){
        UUID OrderOwner = this.orderRepository.getManagerIdByOrderId(order_id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Order not found"));
        if (!OrderOwner.equals(currentUser.getId()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Order not found");
       this.orderRepository.finishOrder(order_id);



    }

}
