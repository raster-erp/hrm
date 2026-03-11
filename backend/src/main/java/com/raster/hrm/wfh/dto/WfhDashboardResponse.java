package com.raster.hrm.wfh.dto;

public record WfhDashboardResponse(
        Long employeeId,
        String employeeCode,
        String employeeName,
        int totalRequests,
        int approvedRequests,
        int pendingRequests,
        int rejectedRequests,
        boolean checkedInToday
) {
}
