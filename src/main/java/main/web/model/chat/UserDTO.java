package main.web.model.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String login;
    private String email;
    private List<String> roles;
    private String firstName;
    private String lastName;
    private String sex;
    private Long age;
    private Long blockTime;
    private String reasonOfBlocking;
    private String startOfBlocking;
    private Long iconType;
}
