package com.itamarprojects.bookorderapp.books;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class BookRepository
{
    private JdbcClient jdbcClient;

    public BookRepository(JdbcClient jdbcClient){
        this.jdbcClient = jdbcClient;
    }

    public Optional<Book> getBookById(Integer bookId)
    {
        return this.jdbcClient.sql("SELECT * FROM Books WHERE id = :id").param("id",bookId).query(Book.class)
                .optional();
    }

    public Book addBook(BookRequest bookRequest , UUID workerId)
    {
        Timestamp createdAt = Timestamp.valueOf(LocalDate.now().atStartOfDay());
        Integer bookId = this.jdbcClient.sql("INSERT INTO Books (title,author,amount,created_at,worker_id,order_id)" +
                "VALUES (?,?,?,?,?,?) RETURNING id").params(List.of(bookRequest.title(),bookRequest.author(),bookRequest.amount(),createdAt,
               workerId,bookRequest.orderId())).query(Integer.class).single();

       return new Book(bookId,bookRequest.title(),bookRequest.author(),bookRequest.amount(),createdAt,workerId,bookRequest.orderId());
    }


    public List<Book> getBooksByOrderId(Integer orderId)
    {
        return this.jdbcClient.sql("SELECT * FROM Books WHERE order_id = :id").param("id",orderId)
                .query(Book.class).list();
    }

    public void deleteBook(Integer id) {
        var updated = this.jdbcClient.sql("DELETE FROM Books WHERE id = :id").param("id", id).update();

        Assert.state(updated == 1 , "cant remove book " + id);
    }

    public Optional<BookOwners> getBookOwnersByBookId(Integer id){
       return jdbcClient.sql("SELECT Books.worker_id, Orders.manager_id FROM Books INNER JOIN Orders ON Books.Order_id = Orders.id WHERE Books.id =:id")
               .param("id",id).query(BookOwners.class).optional();
    }
}
