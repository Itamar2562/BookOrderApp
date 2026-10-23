package com.itamarprojects.bookorderapp.auth;

import com.itamarprojects.bookorderapp.config.LogoutService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController
{
    private final AuthenticationService authenticationService;
    private final LogoutService logoutService;

    public AuthenticationController(AuthenticationService authenticationService,
                                    LogoutService logoutService){
        this.authenticationService = authenticationService;
        this.logoutService = logoutService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest request)
    {
        return ResponseEntity.ok(this.authenticationService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request)
    {
        return ResponseEntity.ok(this.authenticationService.authenticate(request));

    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request,
                       @NonNull HttpServletResponse response,
                       Authentication authentication)
    {
        this.logoutService.logout(request,response,authentication);
    }
}
