package com.itamarprojects.bookorderapp.token;

import java.util.UUID;

public class Token
{
    private Integer id;

    private String token;
    private TokenType tokenType;

    private boolean revoked;

    private boolean expired;
    private UUID userId;

    public Token(String token , boolean revoked, boolean expired , UUID userId , TokenType tokenType)
    {
        this.token = token;
        this.tokenType = tokenType;
        this.revoked = revoked;
        this.expired = expired;
        this.userId = userId;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public boolean isRevoked(){
        return this.revoked;
    }
    public boolean isExpired(){
        return this.expired;
    }

    public Integer getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return "Token{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", tokenType=" + tokenType +
                ", revoked=" + revoked +
                ", expired=" + expired +
                ", userId=" + userId +
                '}';
    }
}
