package main.repository;

import main.entity.UserJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJoinRequestRepository extends JpaRepository<UserJoinRequest, Long> {
    Optional<UserJoinRequest> findByLogin(String login);
    Boolean existsByLogin(String login);
    Boolean existsBySessionId(String sessionId);
}
