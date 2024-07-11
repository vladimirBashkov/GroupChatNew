package main.repository;

import main.entity.UserWarning;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserWarningRepository extends JpaRepository<UserWarning, Long> {

}
