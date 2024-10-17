package main.services;

import lombok.AllArgsConstructor;
import main.entity.RoleType;
import main.entity.User;
import main.security.SecurityService;
import main.web.model.auth.RefreshTokenRequest;
import main.web.model.user.UpdateUserRequest;
import main.web.model.user.UserInfoResponse;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserConsoleService {
    private final SecurityService securityService;

    public UserInfoResponse getInfoByRefreshToken(RefreshTokenRequest request){
        User user = securityService.getUserByRefreshToken(request.getRefreshToken());
        return UserInfoResponse.builder()
                .login(user.getLogin())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(RoleType::name)
                        .map((role) -> role.replaceAll("ROLE_", "")).toList())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .age(user.getAge())
                .sex(user.getSex())
                .icon(user.getIconType())
                .build();
    }

    public String updateUserData(UpdateUserRequest request){
        User user = securityService.getUserByRefreshToken(request.getRefreshToken());
        if(user.getLogin().equals(request.getLogin())){
            user.setEmail(request.getEmail());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setAge(request.getAge());
            user.setSex(request.getSex());
            securityService.updateUser(user);
            return "User data updated";
        }
        return "User data not updated";
    }

    public String logoutUser(){
        securityService.logout();
        return "See you later";
    }
}
