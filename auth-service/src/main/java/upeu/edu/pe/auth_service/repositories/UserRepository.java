package upeu.edu.pe.auth_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import upeu.edu.pe.auth_service.entities.UserEntity;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
}