package com.itamarprojects.bookorderapp.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepository
{
    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);
    private final JdbcClient jdbcClient;

    public UserRepository(JdbcClient jdbcClient){
        this.jdbcClient = jdbcClient;
    }

    public List<User> findAll(){
        return this.jdbcClient.sql("SELECT * FROM Users").query(User.class).list();
    }

    public Optional<User> findById(UUID id){
        return this.jdbcClient.sql("SELECT * FROM Users WHERE id = :id").param("id",id)
                .query(User.class).optional();
    }

    public Optional<User> findByEmail(String email)
    {
    return this.jdbcClient.sql("SELECT * FROM Users WHERE email = :email").param("email",email)
            .query(User.class).optional();
}

    public List<User> findAllManagers(){
        return this.jdbcClient.sql("SELECT * from Users Where user_role = 'MANAGER'").query(User.class).list();
    }

    public void create(User user)
    {
        var updated = this.jdbcClient.sql("INSERT INTO Users(id,manager_id,first_name,last_name,email,password ,user_role) VALUES (?,?,?,?,?,?,?)"
                ).params(Arrays.asList(user.getId(),user.getManagerId(),user.getFirstName(),user.getLastName(),user.getEmail(),user.getPassword() ,  user.getRole().toString()))
                .update();

        Assert.state(updated == 1, "failed to create user " + user.getFirstName() + user.getLastName());
    }

    public void update(User user, Integer id){
        var updated = this.jdbcClient.sql("UPDATE Users SET first_name = ? , last_name = ? , password = ? where id = ?")
                .params(List.of(user.getFirstName(),user.getLastName(),user.getPassword() , id)).update();

        Assert.state(updated == 1, "failed to create user " + user.getFirstName() + user.getLastName());
    }

    public void delete(UUID id){
        var updated = this.jdbcClient.sql("DELETE FROM Users WHERE id = :id").param("id" , id).update();
        Assert.state(updated == 1, "failed to delete user with id "+id.toString() );

    }

    public int count() {
        return this.jdbcClient.sql("SELECT * FROM Users").query().listOfRows().size();
    }

    public void saveAll(List<User> users){
        users.forEach(this::create);
    }

    public void updateWorkersManager(UUID userId, UUID managerId)
    {
        var updated = this.jdbcClient.sql("UPDATE Users SET manager_id = ? WHERE id = ?").params(List.of(managerId,userId)).
                update();
        Assert.state(updated == 1 , String.format("failed to update manager %s for user %s",managerId,userId));
    }

    public Boolean isManager(UUID id)
    {
        return !this.jdbcClient.sql("SELECT * FROM Users WHERE id = ? AND user_role = ?").params(List.of(id, UserRole.MANAGER.toString())).query()
                .listOfRows().isEmpty();
    }
}
