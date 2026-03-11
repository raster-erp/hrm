package com.raster.hrm.attendancedeviation.dto;

public record DeviationSummaryResponse(
        Long employeeId,
        String employeeCode,
        String employeeName,
        Integer lateComingCount,
        Integer earlyGoingCount,
        Integer totalDeviationMinutes,
        Integer lateComingMinutes,
        Integer earlyGoingMinutes,
        Integer warningCount,
        Integer leaveDeductionCount,
        Integer payCutCount
) {
}
