package re.hospital.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import re.hospital.model.entity.User;
import re.hospital.model.enums.RoleName;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Page<User> findByFullNameContainingIgnoreCase(String fullName, Pageable pageable);
    List<User> findByRoles_Name(RoleName roleName);
}
