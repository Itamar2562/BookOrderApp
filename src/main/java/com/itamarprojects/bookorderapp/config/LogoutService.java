package com.itamarprojects.bookorderapp.config;

import com.itamarprojects.bookorderapp.token.Token;
import com.itamarprojects.bookorderapp.token.TokenRepository;
import com.itamarprojects.bookorderapp.token.TokenType;
import com.itamarprojects.bookorderapp.users.User;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class LogoutService implements LogoutHandler
{
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    public LogoutService(TokenRepository tokenRepository , JwtService jwtService, UserDetailsService userDetailsService){
        this.tokenRepository = tokenRepository;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void logout(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            Authentication authentication)
    {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        if (authHeader ==null || !authHeader.startsWith("Bearer "))
            return;
        jwt = authHeader.substring(7);
        //Optional<Token> StoredToken = this.tokenRepository.findByTokenString(jwt);

        String email;
        try {
            email = this.jwtService.extractUserName(jwt);
        } catch (JwtException | IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        if (email == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        Optional<Token> token = this.tokenRepository.findByTokenString(jwt);
        if(token.isEmpty())
        {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        if (!token.map(token1 ->!token1.isExpired() && !token1.isRevoked() && token1.getTokenType().equals(TokenType.ACCESS) ).get()){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        List<Token> validTokens = this.tokenRepository.findAllValidTokensByUserEmail(email);
        if (validTokens.isEmpty())
            return;
        validTokens.forEach(token2 -> {
            token2.setRevoked(true);
            token2.setExpired(true);
        });
        this.tokenRepository.updateAllTokensState(validTokens);
    }
}
