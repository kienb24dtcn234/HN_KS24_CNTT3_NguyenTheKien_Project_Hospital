package re.hospital.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MedicalRecordRequest {
    @NotNull(message = "Mã bệnh nhân không được để trống")
    private Long patientId;

    private Long appointmentId;
    private String diagnosis;
    private String prescription;
}
