package main.web.model.signor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockUserRequest {
    private Long id;
    private String login;
    private Long blockTime;
    private String reasonOfBlocking;
}
