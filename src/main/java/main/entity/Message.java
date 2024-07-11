package main.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.entity.User;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "messages")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @Column(name = "time")
    private LocalDateTime time;

    @Column(name = "message", columnDefinition = "TEXT(10000)")
    private String message;

    @Column(name = "message-id-reply")
    private long messageIdReply;
}
