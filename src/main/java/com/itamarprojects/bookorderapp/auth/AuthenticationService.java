package com.itamarprojects.bookorderapp.auth;

import com.itamarprojects.bookorderapp.config.JwtService;
import com.itamarprojects.bookorderapp.token.Token;
import com.itamarprojects.bookorderapp.token.TokenRepository;
import com.itamarprojects.bookorderapp.users.User;
import com.itamarprojects.bookorderapp.users.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
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
        this.userRepository.create(user);
        var jwtToken = this.jwtService.generateToken(user);
        saveUserToken(user,jwtToken);
        return new AuthenticationResponse(jwtToken);

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
        revokeAllUsersTokens(user);
        saveUserToken(user,jwtToken);
        return new AuthenticationResponse(jwtToken);
    }

    private void saveUserToken(User user, String jwtToken){
        this.tokenRepository.saveToken(new Token(jwtToken,false,false,user.getId()));
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
}
