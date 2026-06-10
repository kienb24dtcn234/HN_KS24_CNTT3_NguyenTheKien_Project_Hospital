package re.hospital.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import re.hospital.exception.ConflictException;
import re.hospital.exception.ResourceNotFoundException;
import re.hospital.model.dto.request.UserUpdateRequest;
import re.hospital.model.dto.response.UserResponse;
import re.hospital.model.entity.User;
import re.hospital.model.enums.RoleName;
import re.hospital.repository.UserRepository;
import re.hospital.service.UserService;
import re.hospital.model.dto.request.CreateUserRequest;
import re.hospital.repository.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Set;
import re.hospital.model.entity.Role;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toUserResponse);
    }

    @Override
    public UserResponse getUserById(Long id) {
        return toUserResponse(findUserById(id));
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = findUserById(id);

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ConflictException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getDateOfBirth() != null) user.setDateOfBirth(request.getDateOfBirth());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (request.getSpecialization() != null) user.setSpecialization(request.getSpecialization());
        if (request.getActive() != null) user.setActive(request.getActive());

        return toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new ConflictException("Username already exists");
        if (userRepository.existsByEmail(request.getEmail()))
            throw new ConflictException("Email already exists");

        Set<Role> roles = request.getRoles().stream()
                .map(roleName -> roleRepository.findByName(RoleName.valueOf(roleName))
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName)))
                .collect(Collectors.toSet());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .specialization(request.getSpecialization())
                .active(true)
                .roles(roles)
                .build();

        return toUserResponse(userRepository.save(user));
    }


    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = findUserById(id);
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public Page<UserResponse> searchUsers(String name, Pageable pageable) {
        return userRepository.findByFullNameContainingIgnoreCase(name, pageable).map(this::toUserResponse);
    }

    @Override
    public List<UserResponse> getDoctors() {
        return userRepository.findByRoles_Name(RoleName.ROLE_DOCTOR).stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .dateOfBirth(user.getDateOfBirth())
                .address(user.getAddress())
                .specialization(user.getSpecialization())
                .active(user.getActive())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet()))
                .build();
    }
}
