package re.hospital.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import re.hospital.model.entity.MedicalRecord;
import re.hospital.model.entity.User;
import java.util.List;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByPatientOrderByCreatedAtDesc(User patient);
    List<MedicalRecord> findByDoctorOrderByCreatedAtDesc(User doctor);
}
