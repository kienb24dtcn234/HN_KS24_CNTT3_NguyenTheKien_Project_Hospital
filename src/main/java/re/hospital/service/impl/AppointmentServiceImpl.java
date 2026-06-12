package re.hospital.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import re.hospital.exception.ConflictException;
import re.hospital.exception.ResourceNotFoundException;
import re.hospital.model.dto.request.AppointmentRequest;
import re.hospital.model.dto.response.AppointmentResponse;
import re.hospital.model.entity.Appointment;
import re.hospital.model.entity.User;
import re.hospital.model.enums.AppointmentStatus;
import re.hospital.repository.AppointmentRepository;
import re.hospital.repository.UserRepository;
import re.hospital.service.AppointmentService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AppointmentResponse createAppointment(Long patientId, AppointmentRequest request) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bệnh nhân"));
        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bác sĩ với id: " + request.getDoctorId()));

        boolean conflict = appointmentRepository.existsByDoctorAndAppointmentTimeBetweenAndStatusNot(
                doctor,
                request.getAppointmentTime().minusMinutes(30),
                request.getAppointmentTime().plusMinutes(30),
                AppointmentStatus.CANCELLED);

        if (conflict) {
            throw new ConflictException("Bác sĩ đã có lịch khám vào thời gian này");
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentTime(request.getAppointmentTime())
                .status(AppointmentStatus.PENDING)
                .notes(request.getNotes())
                .build();

        return toResponse(appointmentRepository.save(appointment));
    }

    @Override
    public Page<AppointmentResponse> getPatientAppointments(Long patientId, Pageable pageable) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bệnh nhân"));
        return appointmentRepository.findByPatientOrderByCreatedAtDesc(patient, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public AppointmentResponse approveAppointment(Long appointmentId) {
        Appointment appointment = findAppointment(appointmentId);
        validateStatus(appointment, AppointmentStatus.PENDING);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return toResponse(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentResponse rejectAppointment(Long appointmentId) {
        Appointment appointment = findAppointment(appointmentId);
        validateStatus(appointment, AppointmentStatus.PENDING);
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return toResponse(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentResponse completeAppointment(Long appointmentId) {
        Appointment appointment = findAppointment(appointmentId);
        validateStatus(appointment, AppointmentStatus.CONFIRMED);
        appointment.setStatus(AppointmentStatus.COMPLETED);
        return toResponse(appointmentRepository.save(appointment));
    }

    @Override
    public List<AppointmentResponse> getPendingAppointments(Long doctorId) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bác sĩ"));
        return appointmentRepository.findByDoctorAndStatus(doctor, AppointmentStatus.PENDING).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private Appointment findAppointment(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch khám với id: " + id));
    }

    private void validateStatus(Appointment appointment, AppointmentStatus expected) {
        if (appointment.getStatus() != expected) {
            throw new ConflictException("Trạng thái lịch khám phải là " + expected + ", hiện tại: " + appointment.getStatus());
        }
    }

    private AppointmentResponse toResponse(Appointment a) {
        return AppointmentResponse.builder()
                .id(a.getId())
                .patientName(a.getPatient().getFullName())
                .patientId(a.getPatient().getId())
                .doctorName(a.getDoctor().getFullName())
                .doctorId(a.getDoctor().getId())
                .doctorSpecialization(a.getDoctor().getSpecialization())
                .appointmentTime(a.getAppointmentTime())
                .status(a.getStatus().name())
                .notes(a.getNotes())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
