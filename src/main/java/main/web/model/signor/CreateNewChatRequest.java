package main.web.model.signor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNewChatRequest {
    private String chatName;
    private List<UserToChatDTO> usersList;
}
