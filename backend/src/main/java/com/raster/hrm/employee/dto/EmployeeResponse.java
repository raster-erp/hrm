package com.raster.hrm.employee.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EmployeeResponse(
        Long id,
        String employeeCode,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate dateOfBirth,
        String gender,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String country,
        String zipCode,
        String emergencyContactName,
        String emergencyContactPhone,
        String emergencyContactRelationship,
        String bankName,
        String bankAccountNumber,
        String bankIfscCode,
        Long departmentId,
        String departmentName,
        Long designationId,
        String designationTitle,
        LocalDate joiningDate,
        String employmentStatus,
        String photoUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
