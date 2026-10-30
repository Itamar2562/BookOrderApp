package com.itamarprojects.bookorderapp.config;

import com.itamarprojects.bookorderapp.token.Token;
import com.itamarprojects.bookorderapp.token.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService
{
    private static final String TOKEN_TYPE = "token_type";

    private final String secretKey;
    private final long jwtExpiration;
    private final long refreshExpiration;

    public JwtService(@Value("${jwt.secret_key}") String secretKey,
                      @Value("${access_token_expiration}") long jwtExpiration,
                      @Value("${refresh_token_expiration}") long refreshExpiration)
        {
        this.secretKey = secretKey;
        this.jwtExpiration=jwtExpiration;
        this.refreshExpiration=refreshExpiration;
        }

    public String extractUserName(String token)
    {
        return extractClaims(token , Claims::getSubject);
    }

    public <T> T extractClaims(String token , Function<Claims , T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails){
        return buildToken(Map.of(TOKEN_TYPE, TokenType.ACCESS.name()), userDetails , this.jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails){
        return buildToken(Map.of(TOKEN_TYPE, TokenType.REFRESH.name()),userDetails,this.refreshExpiration);
    }

    public String buildToken(
            Map<String , Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() +expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(Token token , UserDetails userDetails , String tokenType){
        final String username = extractUserName(token.getToken());
        Claims claims = extractAllClaims(token.getToken());
        return tokenType.equals(claims.get(TOKEN_TYPE , String.class)) &&
                username.equals(userDetails.getUsername()) && !isTokenExpired(token.getToken()) && !token.isRevoked()
                && !token.isExpired();
    }

    private boolean isTokenExpired(String token)
    {
        boolean e = extractExpiration(token).before(new Date());
        System.out.println(e);
        return e;
    }

    private Date extractExpiration(String token)
    {
        return extractClaims(token , Claims::getExpiration);
    }

    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build().parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey()
    {
        byte[] keyBytes = Decoders.BASE64.decode(this.secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
