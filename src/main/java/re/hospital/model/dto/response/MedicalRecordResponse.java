package re.hospital.model.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class MedicalRecordResponse {
    private Long id;
    private String patientName;
    private Long patientId;
    private String doctorName;
    private Long doctorId;
    private String diagnosis;
    private String prescription;
    private String fileUrl;
    private Long appointmentId;
    private LocalDateTime createdAt;
}
