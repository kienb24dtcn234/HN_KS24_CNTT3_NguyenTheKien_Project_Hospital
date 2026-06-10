package re.hospital.service;

import org.springframework.web.multipart.MultipartFile;
import re.hospital.model.dto.request.MedicalRecordRequest;
import re.hospital.model.dto.response.MedicalRecordResponse;

import java.util.List;

public interface MedicalRecordService {
    MedicalRecordResponse createRecord(Long doctorId, MedicalRecordRequest request, MultipartFile file);
    List<MedicalRecordResponse> getRecordsByPatient(Long patientId);
    List<MedicalRecordResponse> getRecordsByDoctor(Long doctorId);
}
