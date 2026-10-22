package com.itamarprojects.bookorderapp.orders;
import com.itamarprojects.bookorderapp.users.User;
import org.springframework.cglib.core.Local;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class OrderRepository
{
    private JdbcClient jdbcClient;

    public OrderRepository(JdbcClient jdbcClient){
        this.jdbcClient = jdbcClient;
    }

    public Optional<Order> getOrderById(Integer orderId)
    {
        return this.jdbcClient.sql("SELECT * FROM Orders WHERE id = :id").param("id",orderId).query(Order.class)
                .optional();
    }

    public Order addOrder(UUID managerId)
    {
        Timestamp createdAt = Timestamp.valueOf(LocalDate.now().atStartOfDay());

        Integer orderId = this.jdbcClient.sql("INSERT INTO Orders (created_at,finished_at,status,manager_id)" +
                "VALUES (:createdAt,NULL,:status,:managerId) RETURNING id ")
                .param("createdAt",createdAt)
                .param("status",OrderStatus.ONGOING.toString())
                .param("managerId",managerId)
                .query(Integer.class).single();
        return new Order(orderId, createdAt,null, OrderStatus.ONGOING,managerId);
    }

    public void deleteOrder(Integer id){
        var updated = this.jdbcClient.sql("DELETE FROM Orders WHERE id = :id").param("id",id).update();

        Assert.state(updated == 1 , "cant delete order "+id);
    }

    public Optional<UUID> getManagerIdByOrderId(Integer id){
        return this.jdbcClient.sql("SELECT manager_id FROM Orders Where id = :id")
                .param("id",id).query(UUID.class).optional();
    }


}
