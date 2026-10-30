package com.itamarprojects.bookorderapp.auth;

import com.itamarprojects.bookorderapp.config.JwtService;
import com.itamarprojects.bookorderapp.token.Token;
import com.itamarprojects.bookorderapp.token.TokenRepository;
import com.itamarprojects.bookorderapp.token.TokenType;
import com.itamarprojects.bookorderapp.users.User;
import com.itamarprojects.bookorderapp.users.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class AuthenticationService
{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;
    private final TokenRepository tokenRepository;

    public AuthenticationService(UserRepository userRepository ,
                                 PasswordEncoder passwordEncoder ,
                                 JwtService jwtService,
                                 AuthenticationManager authManager,
                                 TokenRepository tokenRepository){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authManager = authManager;
        this.tokenRepository = tokenRepository;
    }

    public AuthenticationResponse register(RegisterRequest request)
    {
        UUID UserId = UUID.randomUUID();
        User user = new User(UserId ,
                request.firstName(),
                request.lastName(),
                request.email(),
                this.passwordEncoder.encode(request.password()),
                request.userRole(),
                request.managerId());
        if (!this.userRepository.userEmailExists(user.getEmail()))
            this.userRepository.create(user);
        else
            throw new ResponseStatusException(HttpStatus.CONFLICT,"email already exists");
        var jwtToken = this.jwtService.generateToken(user);
        var refreshToken = this.jwtService.generateRefreshToken(user);
        saveUserToken(user,jwtToken,TokenType.ACCESS);
        saveUserToken(user,refreshToken,TokenType.REFRESH);
        return new AuthenticationResponse(jwtToken,refreshToken);

    }

    public AuthenticationResponse authenticate(AuthenticationRequest request)
    {
        this.authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                        )
        );
        User user = this.userRepository.findByEmail(request.email()).orElseThrow();
        var jwtToken = this.jwtService.generateToken(user);
        var refreshToken = this.jwtService.generateRefreshToken(user);
        revokeAllUsersTokens(user);
        cleanUpTokenDb();
        saveUserToken(user,jwtToken,TokenType.ACCESS);
        saveUserToken(user,refreshToken,TokenType.REFRESH);
        return new AuthenticationResponse(jwtToken,refreshToken);
    }

    private void saveUserToken(User user, String jwtToken , TokenType tokenType){
        this.tokenRepository.saveToken(new Token(jwtToken,false,false,user.getId(),tokenType));
    }

    public void cleanUpTokenDb(){
        Random random = new Random();
        int r = random.nextInt(1,10);
        if (r==1) //10%
            this.tokenRepository.cleanUp();

    }


    private void revokeAllUsersTokensByType(User user , TokenType type){
        List<Token> validTokens = this.tokenRepository.findAllValidTokensByUserIdAndTokenType(user.getId(),type);
        if (validTokens.isEmpty())
            return;
        validTokens.forEach(token -> {
            token.setRevoked(true);
            token.setExpired(true);
        });
        this.tokenRepository.updateAllTokensState(validTokens);
    }

    private void revokeAllUsersTokens(User user){
        List<Token> validTokens = this.tokenRepository.findAllValidTokensByUserId(user.getId());
        if (validTokens.isEmpty())
            return;
        validTokens.forEach(token -> {
            token.setRevoked(true);
            token.setExpired(true);
        });
        this.tokenRepository.updateAllTokensState(validTokens);
    }

    public void refreshToken(HttpServletRequest request , HttpServletResponse response) throws IOException
    {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer "))
            throw invalidRefreshToken();

        refreshToken = authHeader.substring(7);
        try {
            userEmail = this.jwtService.extractUserName(refreshToken);
        } catch (JwtException | IllegalArgumentException exception) {
            throw invalidRefreshToken();
        }

        if (userEmail == null)
            throw invalidRefreshToken();

        var user = this.userRepository.findByEmail(userEmail)
                .orElseThrow(this::invalidRefreshToken);
        try {
            Token token = this.tokenRepository.findByTokenString(refreshToken).orElseThrow(this::invalidRefreshToken);
            if (!jwtService.isTokenValid(token, user, TokenType.REFRESH.name()))
                throw invalidRefreshToken();
        } catch (JwtException | IllegalArgumentException exception) {
            throw invalidRefreshToken();
        }

        var accessToken = this.jwtService.generateToken(user);
        revokeAllUsersTokensByType(user , TokenType.ACCESS);
        saveUserToken(user, accessToken, TokenType.ACCESS);
        var authResponse = new AuthenticationResponse(accessToken, refreshToken);
        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
    }

    private ResponseStatusException invalidRefreshToken() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
    }
}
