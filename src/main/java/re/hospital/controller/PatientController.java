package re.hospital.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import re.hospital.model.dto.request.AppointmentRequest;
import re.hospital.model.dto.response.ApiResponse;
import re.hospital.model.dto.response.AppointmentResponse;
import re.hospital.model.dto.response.MedicalRecordResponse;
import re.hospital.security.principal.CustomUserDetails;
import re.hospital.service.AppointmentService;
import re.hospital.service.MedicalRecordService;
import re.hospital.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patient")
@RequiredArgsConstructor
public class PatientController {

    private final AppointmentService appointmentService;
    private final UserService userService;
    private final MedicalRecordService medicalRecordService;

    @GetMapping("/doctors")
    public ResponseEntity<ApiResponse<?>> getDoctors() {
        return ResponseEntity.ok(ApiResponse.success("Danh sách bác sĩ", userService.getDoctors()));
    }

    @PostMapping("/appointments")
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đặt lịch khám thành công",
                        appointmentService.createAppointment(userDetails.getUser().getId(), request)));
    }

    @GetMapping("/appointments")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getMyAppointments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Lịch sử khám bệnh",
                appointmentService.getPatientAppointments(
                        userDetails.getUser().getId(), PageRequest.of(page, size))));
    }

    @GetMapping("/records")
    public ResponseEntity<ApiResponse<List<MedicalRecordResponse>>> getMyRecords(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Hồ sơ bệnh án của bạn",
                medicalRecordService.getRecordsByPatient(userDetails.getUser().getId())));
    }
}
