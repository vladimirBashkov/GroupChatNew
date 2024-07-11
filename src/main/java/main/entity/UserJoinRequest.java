package main.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "application_to_join", indexes = {
        @Index(columnList = "login"),
        @Index(columnList = "session-id")})
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserJoinRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "login")
    private String login;

    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "message_to_admin")
    private String messageToAdmin;

    @Column(name = "session-id")
    private String sessionId;
}
