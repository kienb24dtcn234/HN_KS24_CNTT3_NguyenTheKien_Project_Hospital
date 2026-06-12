package re.hospital.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import re.hospital.model.dto.request.MedicalRecordRequest;
import re.hospital.model.dto.response.ApiResponse;
import re.hospital.model.dto.response.AppointmentResponse;
import re.hospital.model.dto.response.MedicalRecordResponse;
import re.hospital.security.principal.CustomUserDetails;
import re.hospital.service.AppointmentService;
import re.hospital.service.MedicalRecordService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;

    @GetMapping("/appointments/pending")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getPendingAppointments(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Danh sách lịch chờ duyệt",
                appointmentService.getPendingAppointments(userDetails.getUser().getId())));
    }

    @PutMapping("/appointments/{id}/approve")
    public ResponseEntity<ApiResponse<AppointmentResponse>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Đã duyệt lịch khám",
                appointmentService.approveAppointment(id)));
    }

    @PutMapping("/appointments/{id}/reject")
    public ResponseEntity<ApiResponse<AppointmentResponse>> reject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Đã từ chối lịch khám",
                appointmentService.rejectAppointment(id)));
    }

    @PutMapping("/appointments/{id}/complete")
    public ResponseEntity<ApiResponse<AppointmentResponse>> complete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Đã hoàn thành khám",
                appointmentService.completeAppointment(id)));
    }

    @PostMapping(value = "/records", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> uploadRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute MedicalRecordRequest request,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo hồ sơ bệnh án thành công",
                        medicalRecordService.createRecord(userDetails.getUser().getId(), request, file)));
    }

    @GetMapping("/records")
    public ResponseEntity<ApiResponse<List<MedicalRecordResponse>>> getMyRecords(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Hồ sơ bệnh án bạn đã tạo",
                medicalRecordService.getRecordsByDoctor(userDetails.getUser().getId())));
    }
}
