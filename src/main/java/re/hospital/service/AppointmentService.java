package re.hospital.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import re.hospital.model.dto.request.AppointmentRequest;
import re.hospital.model.dto.response.AppointmentResponse;

import java.util.List;

public interface AppointmentService {
    AppointmentResponse createAppointment(Long patientId, AppointmentRequest request);
    Page<AppointmentResponse> getPatientAppointments(Long patientId, Pageable pageable);
    AppointmentResponse approveAppointment(Long appointmentId);
    AppointmentResponse rejectAppointment(Long appointmentId);
    AppointmentResponse completeAppointment(Long appointmentId);
    List<AppointmentResponse> getPendingAppointments(Long doctorId);
}
