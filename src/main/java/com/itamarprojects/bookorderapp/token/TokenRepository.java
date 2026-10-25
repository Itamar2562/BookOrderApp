package com.itamarprojects.bookorderapp.token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TokenRepository
{
    private static final Logger log = LoggerFactory.getLogger(TokenRepository.class);
    private final JdbcClient jdbcClient;

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

    public List<Token> findAllValidTokensByUserIdAndTokenType(UUID user_id ,TokenType tokenType )
    {
        return this.jdbcClient.sql("SELECT * FROM Tokens WHERE " +
                        "expired = FALSE AND revoked = FALSE AND user_id= :user_id AND Token_type = :token_type")
                .param("user_id",user_id)
                .param("token_type",tokenType.name())
                .query(Token.class)
                .list();
    }

    public List<Token> findAllValidTokensByUserEmail(String email)
    {
        return this.jdbcClient.sql("SELECT Tokens.id,Tokens.token,Tokens.token_type,Tokens.revoked , Tokens.expired , Tokens.user_id" +
                        " FROM Tokens INNER JOIN Users ON Tokens.user_id = Users.id WHERE " +
                        "expired = FALSE AND revoked = FALSE AND email= :email")
                .param("email",email)
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

    public List<Token> getAll()
    {
        return this.jdbcClient.sql("SELECT * FROM Tokens").query(Token.class).list();
    }

    public void cleanUp()
    {
        int updated = this.jdbcClient.sql("DELETE FROM Tokens WHERE expired = TRUE OR revoked = TRUE").update();
        log.info("cleaned up {} Tokens",updated);
    }
}
