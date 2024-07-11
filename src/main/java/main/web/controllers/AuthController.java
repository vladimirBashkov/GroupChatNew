package main.web.controllers;

import lombok.RequiredArgsConstructor;
import main.exception.AlreadyExistException;
import main.repository.UserRepository;
import main.security.SecurityService;
import main.web.model.*;
import main.web.model.auth.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;
    private final SecurityService securityService;

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> authUser(@RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(securityService.authenticateUser(loginRequest));
    }

    @PostMapping("/application")
    public ResponseEntity<ApplicationToJoinResponse> addApplicationToJoinRequest(@RequestBody ApplicationToJoinRequest request){
        String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
        try {
            return ResponseEntity.ok(securityService.saveApplicationToJoin(request, sessionId));
        } catch (AlreadyExistException ex){
            return ResponseEntity.badRequest().body(new ApplicationToJoinResponse(ex.getMessage()));
        }
    }

    @PostMapping(path = "/refresh-token",  consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<RefreshTokenResponse> refreshToken(RefreshTokenRequest request){
        return ResponseEntity.ok(securityService.refreshToken(request));
    }

    @PostMapping(path = "/logout",  consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @PreAuthorize("hasAnyRole('USER', 'OLD', 'SIGNOR', 'ADMIN')")
    public ResponseEntity<SimpleResponse> logoutUser(@AuthenticationPrincipal UserDetails userDetails){
        securityService.logout();
        return ResponseEntity.ok(new SimpleResponse("User logout: " + userDetails.getUsername()));
    }
}
