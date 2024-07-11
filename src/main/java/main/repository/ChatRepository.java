package main.repository;

import main.entity.Chat;
import main.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    Optional<Chat> findByName(String name);
    Optional<Chat> findById(Long id);
    List<Chat> findByUsers(User user);
    Boolean existsByName(String name);
}
