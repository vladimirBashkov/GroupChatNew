package main.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "chats", indexes = {
        @Index(columnList = "name")})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    private List<Message> messages = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "chats")
    List<User> users = new ArrayList<>();

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Chat)) return false;
        Chat o = (Chat) obj;
        return o.getName().equals(this.getName());
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
