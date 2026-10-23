package com.itamarprojects.bookorderapp.config;

import com.itamarprojects.bookorderapp.token.Token;
import com.itamarprojects.bookorderapp.token.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LogoutService implements LogoutHandler
{
    private final TokenRepository tokenRepository;

    public LogoutService(TokenRepository tokenRepository){
        this.tokenRepository = tokenRepository;
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
        Optional<Token> StoredToken = this.tokenRepository.findByTokenString(jwt);
        if (StoredToken.isPresent())
        {
            Token token =   StoredToken.get();
            token.setExpired(true);
            token.setRevoked(true);
            this.tokenRepository.updateTokenState(token);
            SecurityContextHolder.clearContext();
        }



    }
}
