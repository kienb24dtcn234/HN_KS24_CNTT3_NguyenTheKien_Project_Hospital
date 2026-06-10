package re.hospital.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import re.hospital.model.dto.request.CreateUserRequest;
import re.hospital.model.dto.request.UserUpdateRequest;
import re.hospital.model.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    Page<UserResponse> getAllUsers(Pageable pageable);
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UserUpdateRequest request);
    void deleteUser(Long id);
    Page<UserResponse> searchUsers(String name, Pageable pageable);
    List<UserResponse> getDoctors();
    UserResponse createUser(CreateUserRequest request);
}
