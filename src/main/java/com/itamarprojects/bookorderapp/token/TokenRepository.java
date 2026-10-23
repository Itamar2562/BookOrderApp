package com.itamarprojects.bookorderapp.token;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TokenRepository
{
    private JdbcClient jdbcClient;

    public TokenRepository(JdbcClient jdbcClient){
        this.jdbcClient = jdbcClient;
    }


    public Integer saveToken(Token token)
    {
        return this.jdbcClient.sql("INSERT INTO Tokens (token,token_type,revoked,expired,user_id) values (?,?,?,?,?) RETURNING id")
                .params(List.of(token.getToken(),token.getTokenType().toString(),token.isRevoked(),token.isExpired(),token.getUserId()))
                .query(Integer.class).single();
    }

    public Optional<Token> findTokenById(Integer id){
        return this.jdbcClient.sql("SELECT * FROM Tokens WHERE id = :id")
                .param("id" , id)
                .query(Token.class)
                .optional();
    }

    public Optional<Token> findByTokenString(String token)
    {
        return this.jdbcClient.sql("SELECT * FROM Tokens WHERE token = :token")
                .param("token",token)
                .query(Token.class)
                .optional();
    }

    public List<Token> findAllValidTokensByUserId(UUID user_id)
    {
        return this.jdbcClient.sql("SELECT * FROM Tokens WHERE " +
                        "expired = FALSE AND revoked = FALSE AND user_id= :user_id")
                .param("user_id",user_id)
                .query(Token.class)
                .list();
    }

    public void updateTokenState(Token token){
        var updated = this.jdbcClient.sql("UPDATE Tokens SET expired = :expired, revoked = :revoked WHERE id = :id")
                .param("expired",token.isExpired())
                .param("revoked",token.isRevoked())
                .param("id",token.getId()).update();
        Assert.state(updated == 1 , "cant update token "+token.getId());
    }

    public void updateAllTokensState(List<Token> tokens){
        tokens.forEach(this::updateTokenState);
    }
}
