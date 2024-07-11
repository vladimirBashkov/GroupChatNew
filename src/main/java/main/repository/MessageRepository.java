package main.repository;

import main.entity.Chat;
import main.entity.Message;
import main.entity.RoleType;
import main.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Optional<Message> findById(Long id);
    List<Message> findAll();
    Message findFirstByOrderByTimeDesc();
    Long countByChat(Chat chat);
    long deleteByUser(User user);
}
