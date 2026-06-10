package re.hospital.model.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserUpdateRequest {
    private String fullName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String address;
    private String specialization;
    private Boolean active;
}
