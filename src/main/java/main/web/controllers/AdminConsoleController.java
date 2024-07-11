package main.web.controllers;

import lombok.RequiredArgsConstructor;
import main.entity.Status;
import main.entity.User;
import main.entity.UserJoinRequest;
import main.entity.RoleType;
import main.exception.EntityNotFoundException;
import main.repository.MessageRepository;
import main.repository.UserJoinRequestRepository;
import main.repository.UserRepository;
import main.security.SecurityService;
import main.web.model.SimpleIdRequest;
import main.web.model.SimpleResponse;
import main.web.model.admin.*;
import main.web.model.auth.CreateUserRequest;
import main.web.model.chat.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminConsoleController {
    private final UserRepository userRepository;
    private final UserJoinRequestRepository userJoinRequestRepository;
    private final SecurityService securityService;

    @GetMapping("/join-request")
    public ResponseEntity<List<UserJoinRequestDTO>> getAllUserJoinRequests(){
        return ResponseEntity.ok(userJoinRequestRepository.findAll().stream()
                .map(ujr -> UserJoinRequestDTO.builder()
                        .id(ujr.getId())
                        .login(ujr.getLogin())
                        .message(ujr.getMessageToAdmin())
                        .email(ujr.getEmail())
                        .build())
                .toList());
    }

    @PostMapping("/add-user")
    public ResponseEntity<SimpleResponse> addNewUserRequest(@RequestBody UJRBasicDataRequest request){
        Long joinId = request.getJoinId();
        String login = request.getLogin();
        UserJoinRequest userJoinRequest;
        if(userJoinRequestRepository.existsById(joinId)){
             userJoinRequest = userJoinRequestRepository.findById(joinId).get();
        } else return ResponseEntity.ok(new SimpleResponse("Join request not exist"));
        if(userJoinRequest.getLogin().equals(login)){
            Set<RoleType> roles = new HashSet<>();
            roles.add(RoleType.ROLE_USER);
            CreateUserRequest createUserRequest = CreateUserRequest.builder()
                    .login(userJoinRequest.getLogin())
                    .email(userJoinRequest.getEmail())
                    .roles(roles)
                    .password(userJoinRequest.getPassword())
                    .build();
            securityService.register(createUserRequest);
            deleteUserJoinRequest(request);
            return ResponseEntity.ok(new SimpleResponse("User created"));
        } else return ResponseEntity.ok(new SimpleResponse("Error in data"));
    }

    @Transactional
    @PostMapping("/delete-request")
    public ResponseEntity<SimpleResponse> deleteUserJoinRequest(@RequestBody UJRBasicDataRequest request){
        Long joinId = request.getJoinId();
        String login = request.getLogin();
        UserJoinRequest userJoinRequest;
        if(userJoinRequestRepository.existsById(joinId)){
            userJoinRequest = userJoinRequestRepository.findById(joinId).get();
        } else return ResponseEntity.ok(new SimpleResponse("Join request not exist"));
        userJoinRequestRepository.delete(userJoinRequest);
        return ResponseEntity.ok(new SimpleResponse("Request from " + login + " deleted!!!"));
    }

    @PostMapping("/user/roles")
    public ResponseEntity<SimpleResponse> updateUserRolesRequest(@RequestBody UpdateUserRolesRequest request){
        if(userRepository.existsById(request.getId())){
            User user = userRepository.findById(request.getId()).get();
            if(!request.getRoles().isEmpty()){
                Set<RoleType> roles = new HashSet<>();
                ArrayList<String> rolesRequest = new ArrayList<>(request.getRoles());
                for (int i = 0; i < rolesRequest.size(); i++) {
                    if(rolesRequest.get(i).compareTo(RoleType.ROLE_USER.name()
                            .replaceAll("ROLE_", "")) == 0){
                        roles.add(RoleType.ROLE_USER);
                    }
                    if(rolesRequest.get(i).compareTo(RoleType.ROLE_OLD.name()
                            .replaceAll("ROLE_", "")) == 0){
                        roles.add(RoleType.ROLE_OLD);
                    }
                    if(rolesRequest.get(i).compareTo(RoleType.ROLE_SIGNOR.name()
                            .replaceAll("ROLE_", "")) == 0){
                        roles.add(RoleType.ROLE_SIGNOR);
                    }
                    if(rolesRequest.get(i).compareTo(RoleType.ROLE_ADMIN.name()
                            .replaceAll("ROLE_", "")) == 0){
                        roles.add(RoleType.ROLE_ADMIN);
                    }
                }
                if(user.getRoles().contains(RoleType.ROLE_ADMIN) &&
                        !roles.contains(RoleType.ROLE_ADMIN) && securityService.checkLastAdmin()){
                    return ResponseEntity.ok(new SimpleResponse("You are the last ADMIN. Cancelled!!!"));
                }
                user.setRoles(roles);
                securityService.updateUser(user);
                return ResponseEntity.ok(new SimpleResponse("Roles updated"));
            } else return ResponseEntity.ok(new SimpleResponse("Roles are empty"));
        } else return ResponseEntity.ok(new SimpleResponse("User not exist"));
    }

    @Transactional
    @PostMapping("/user/delete")
    public ResponseEntity<SimpleResponse> deleteUserRequest(@RequestBody SimpleIdRequest request){
        if(userRepository.existsById(request.getId())){
            User user = userRepository.findById(request.getId()).get();
            if(user.getRoles().contains(RoleType.ROLE_ADMIN) && securityService.checkLastAdmin()){
                return ResponseEntity.ok(new SimpleResponse("You are the last ADMIN. Cancelled!!!"));
            }
            securityService.deleteUser(user);
            return ResponseEntity.ok(new SimpleResponse("User - " + user.getLogin() + " deleted"));
        } else throw new EntityNotFoundException("User not exist");
    }
}
