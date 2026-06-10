package re.hospital.model.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AppointmentResponse {
    private Long id;
    private String patientName;
    private Long patientId;
    private String doctorName;
    private Long doctorId;
    private String doctorSpecialization;
    private LocalDateTime appointmentTime;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
}
