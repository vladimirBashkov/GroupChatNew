package main.web.model.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UJRBasicDataRequest {
    private String login;
    private Long joinId;
}
