package main.web.model.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.entity.RoleType;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationToJoinRequest {
    private String login;
    private String password;
    private String email;
    private String message;
}
