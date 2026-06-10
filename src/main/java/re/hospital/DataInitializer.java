package re.hospital;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import re.hospital.model.entity.Role;
import re.hospital.model.entity.User;
import re.hospital.model.enums.RoleName;
import re.hospital.repository.RoleRepository;
import re.hospital.repository. UserRepository;

import java.util.Arrays;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            Arrays.stream(RoleName.values())
                    .forEach(r -> roleRepository.save(Role.builder().name(r).build()));
        }

        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN).orElseThrow();
            userRepository.save(User.builder()
                    .username("admin").email("admin@hospital.com")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("System Administrator").active(true)
                    .roles(Set.of(adminRole)).build());
        }

        if (!userRepository.existsByUsername("doctor1")) {
            Role doctorRole = roleRepository.findByName(RoleName.ROLE_DOCTOR).orElseThrow();
            userRepository.save(User.builder()
                    .username("doctor1").email("doctor1@hospital.com")
                    .password(passwordEncoder.encode("doctor123"))
                    .fullName("Dr. Nguyen Van A").specialization("Internal Medicine")
                    .active(true).roles(Set.of(doctorRole)).build());
        }
    }
}
