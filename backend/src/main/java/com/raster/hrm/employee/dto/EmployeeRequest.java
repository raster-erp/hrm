package com.raster.hrm.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EmployeeRequest(
        @NotBlank(message = "Employee code is required")
        @Size(max = 20, message = "Employee code must not exceed 20 characters")
        String employeeCode,

        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name must not exceed 50 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name must not exceed 50 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 100, message = "Email must not exceed 100 characters")
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
        Long designationId,
        LocalDate joiningDate,
        String employmentStatus
) {
}
