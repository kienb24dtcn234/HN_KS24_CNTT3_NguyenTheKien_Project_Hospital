package re.hospital.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import re.hospital.exception.ResourceNotFoundException;
import re.hospital.model.dto.request.MedicalRecordRequest;
import re.hospital.model.dto.response.MedicalRecordResponse;
import re.hospital.model.entity.Appointment;
import re.hospital.model.entity.MedicalRecord;
import re.hospital.model.entity.User;
import re.hospital.repository.AppointmentRepository;
import re.hospital.repository.MedicalRecordRepository;
import re.hospital.repository.UserRepository;
import re.hospital.service.CloudinaryService;
import re.hospital.service.MedicalRecordService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public MedicalRecordResponse createRecord(Long doctorId, MedicalRecordRequest request, MultipartFile file) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bác sĩ"));
        User patient = userRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bệnh nhân với id: " + request.getPatientId()));

        MedicalRecord record = MedicalRecord.builder()
                .doctor(doctor)
                .patient(patient)
                .diagnosis(request.getDiagnosis())
                .prescription(request.getPrescription())
                .build();

        if (request.getAppointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch khám"));
            record.setAppointment(appointment);
        }

        if (file != null && !file.isEmpty()) {
            String fileUrl = cloudinaryService.uploadFile(file);
            record.setFileUrl(fileUrl);
        }

        return toResponse(medicalRecordRepository.save(record));
    }

    @Override
    public List<MedicalRecordResponse> getRecordsByPatient(Long patientId) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bệnh nhân"));
        return medicalRecordRepository.findByPatientOrderByCreatedAtDesc(patient).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MedicalRecordResponse> getRecordsByDoctor(Long doctorId) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bác sĩ"));
        return medicalRecordRepository.findByDoctorOrderByCreatedAtDesc(doctor).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private MedicalRecordResponse toResponse(MedicalRecord r) {
        return MedicalRecordResponse.builder()
                .id(r.getId())
                .patientName(r.getPatient().getFullName())
                .patientId(r.getPatient().getId())
                .doctorName(r.getDoctor().getFullName())
                .doctorId(r.getDoctor().getId())
                .diagnosis(r.getDiagnosis())
                .prescription(r.getPrescription())
                .fileUrl(r.getFileUrl())
                .appointmentId(r.getAppointment() != null ? r.getAppointment().getId() : null)
                .createdAt(r.getCreatedAt())
                .build();
    }
}
