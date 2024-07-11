package main.web.controllers;

import lombok.RequiredArgsConstructor;
import main.entity.RoleType;
import main.entity.User;
import main.security.SecurityService;
import main.web.model.auth.RefreshTokenRequest;
import main.web.model.SimpleResponse;
import main.web.model.user.UpdateUserRequest;
import main.web.model.user.UserInfoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@PreAuthorize("hasAnyRole('USER', 'OLD', 'SIGNOR', 'ADMIN')")
@RequiredArgsConstructor
public class UserController {
    private final SecurityService securityService;

    @PostMapping("/info")
    public ResponseEntity<UserInfoResponse> getInfoByRefreshToken(@RequestBody RefreshTokenRequest request){
        User user = securityService.getUserByRefreshToken(request.getRefreshToken());
        return ResponseEntity.ok(UserInfoResponse.builder()
                        .login(user.getLogin())
                        .email(user.getEmail())
                        .roles(user.getRoles().stream().map(RoleType::name)
                            .map((role) -> role.replaceAll("ROLE_", "")).toList())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .age(user.getAge())
                        .sex(user.getSex())
                        .icon(user.getIconType())
                .build());
    }

    @PostMapping(path = "/update")
    public ResponseEntity<SimpleResponse> updateUserData(@RequestBody UpdateUserRequest request){
        User user = securityService.getUserByRefreshToken(request.getRefreshToken());
        if(user.getLogin().equals(request.getLogin())){
            user.setEmail(request.getEmail());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setAge(request.getAge());
            user.setSex(request.getSex());
            securityService.updateUser(user);
            return ResponseEntity.ok(new SimpleResponse("User updated"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SimpleResponse("Wrong request"));
    }

    @GetMapping(path = "/logout")
    public ResponseEntity<SimpleResponse> logoutUser(){
        securityService.logout();
        return ResponseEntity.ok(new SimpleResponse("See you later"));
    }
}
