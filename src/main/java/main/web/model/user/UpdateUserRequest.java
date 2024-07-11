package main.web.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserRequest {
    private String login;
    private String email;
    private List<String> roles;
    private String firstName;
    private String lastName;
    private Long age;
    private String sex;
    private String warn;
    private Long icon;
    private String refreshToken;
}