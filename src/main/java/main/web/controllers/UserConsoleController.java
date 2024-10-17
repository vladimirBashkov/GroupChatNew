package main.web.controllers;

import lombok.RequiredArgsConstructor;
import main.services.UserConsoleService;
import main.web.model.auth.RefreshTokenRequest;
import main.web.model.SimpleResponse;
import main.web.model.user.UpdateUserRequest;
import main.web.model.user.UserInfoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@PreAuthorize("hasAnyRole('USER', 'OLD', 'SIGNOR', 'ADMIN')")
@RequiredArgsConstructor
public class UserConsoleController {
    private final UserConsoleService userConsoleService;

    @PostMapping("/info")
    public ResponseEntity<UserInfoResponse> getInfoByRefreshTokenRequest(@RequestBody RefreshTokenRequest request){
        return ResponseEntity.ok(userConsoleService.getInfoByRefreshToken(request));
    }

    @PostMapping(path = "/update")
    public ResponseEntity<SimpleResponse> updateUserData(@RequestBody UpdateUserRequest request){
        return ResponseEntity.ok(new SimpleResponse(userConsoleService.updateUserData(request)));
    }

    @GetMapping(path = "/logout")
    public ResponseEntity<SimpleResponse> logoutUser(){
        return ResponseEntity.ok(new SimpleResponse(userConsoleService.logoutUser()));
    }
}
