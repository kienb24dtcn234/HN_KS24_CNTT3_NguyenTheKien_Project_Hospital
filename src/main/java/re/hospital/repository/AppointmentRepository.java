package re.hospital.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import re.hospital.model.entity.Appointment;
import re.hospital.model.entity.User;
import re.hospital.model.enums.AppointmentStatus;
import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Page<Appointment> findByPatientOrderByCreatedAtDesc(User patient, Pageable pageable);
    List<Appointment> findByDoctorAndStatus(User doctor, AppointmentStatus status);
    boolean existsByDoctorAndAppointmentTimeBetweenAndStatusNot(
            User doctor, LocalDateTime start, LocalDateTime end, AppointmentStatus status);
}
