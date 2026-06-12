package re.hospital.model.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentRequest {
    @NotNull(message = "Mã bác sĩ không được để trống")
    private Long doctorId;

    @NotNull(message = "Thời gian khám không được để trống")
    @Future(message = "Thời gian khám phải là thời gian trong tương lai")
    private LocalDateTime appointmentTime;

    private String notes;
}
