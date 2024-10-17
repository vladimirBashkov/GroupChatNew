package main.services;

import lombok.RequiredArgsConstructor;
import main.entity.RoleType;
import main.entity.User;
import main.entity.UserJoinRequest;
import main.exception.EntityNotFoundException;
import main.repository.UserJoinRequestRepository;
import main.repository.UserRepository;
import main.security.SecurityService;
import main.web.model.SimpleIdRequest;
import main.web.model.admin.UJRBasicDataRequest;
import main.web.model.admin.UpdateUserRolesRequest;
import main.web.model.admin.UserJoinRequestDTO;
import main.web.model.auth.CreateUserRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminConsoleService {
    private final UserRepository userRepository;
    private final UserJoinRequestRepository userJoinRequestRepository;
    private final SecurityService securityService;
    private final String JOIN_REQUEST_NOT_EXIST = "Join request not exist";
    private final String USER_CREATED = "User created";
    private final String ERROR_IN_DATA = "Error in data";
    private final String LAST_ADMIN_WARN = "You are the last ADMIN. Cancelled!!!";
    private final String ROLES_UPDATED = "Roles updated";
    private final String EMPTY_ROLES_LIST_WARN = "Roles are empty";
    private final String USER_NOT_EXIST_WARN = "User not exist";



    public List<UserJoinRequestDTO> getAllUsersJoin(){
        return userJoinRequestRepository.findAll().stream()
                .map(ujr -> UserJoinRequestDTO.builder()
                        .id(ujr.getId())
                        .login(ujr.getLogin())
                        .message(ujr.getMessageToAdmin())
                        .email(ujr.getEmail())
                        .build())
                .toList();
    }

    public String addNewUser(UJRBasicDataRequest request){
        Long joinId = request.getJoinId();
        String login = request.getLogin();
        UserJoinRequest userJoinRequest;
        if(userJoinRequestRepository.existsById(joinId)){
            userJoinRequest = userJoinRequestRepository.findById(joinId).get();
        } else return JOIN_REQUEST_NOT_EXIST;
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
            deleteUserJoin(request);
            return USER_CREATED;
        } else return ERROR_IN_DATA;
    }

    @Transactional
    public String deleteUserJoin (UJRBasicDataRequest request){
        Long joinId = request.getJoinId();
        String login = request.getLogin();
        UserJoinRequest userJoinRequest;
        if(userJoinRequestRepository.existsById(joinId)){
            userJoinRequest = userJoinRequestRepository.findById(joinId).get();
        } else return JOIN_REQUEST_NOT_EXIST;
        userJoinRequestRepository.delete(userJoinRequest);
        return "Request from " + login + " deleted!!!";
    }

    public String updateUserRoles(UpdateUserRolesRequest request){
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
                    return LAST_ADMIN_WARN;
                }
                user.setRoles(roles);
                securityService.updateUser(user);
                return ROLES_UPDATED;
            } else return EMPTY_ROLES_LIST_WARN;
        } else return USER_NOT_EXIST_WARN;
    }

    @Transactional
    public String deleteUser(SimpleIdRequest request){
        if(userRepository.existsById(request.getId())){
            User user = userRepository.findById(request.getId()).get();
            if(user.getRoles().contains(RoleType.ROLE_ADMIN) && securityService.checkLastAdmin()){
                return LAST_ADMIN_WARN;
            }
            securityService.deleteUser(user);
            return "User - " + user.getLogin() + " deleted";
        } else throw new EntityNotFoundException(USER_NOT_EXIST_WARN);
    }
}
