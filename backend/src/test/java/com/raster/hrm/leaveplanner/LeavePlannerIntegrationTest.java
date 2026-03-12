package com.raster.hrm.leaveplanner;

import com.raster.hrm.holiday.dto.HolidayRequest;
import com.raster.hrm.holiday.dto.HolidayResponse;
import com.raster.hrm.holiday.entity.HolidayType;
import com.raster.hrm.leavecalendar.dto.LeaveCalendarEntry;
import com.raster.hrm.leavecalendar.dto.TeamAvailabilityResponse;
import com.raster.hrm.leaveplan.dto.LeavePlanRequest;
import com.raster.hrm.leaveplan.dto.LeavePlanResponse;
import com.raster.hrm.leaveplan.entity.LeavePlanStatus;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
@Sql(scripts = "/leaveplanner/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/leaveplanner/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
class LeavePlannerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    // ── Holiday CRUD ────────────────────────────────────────────────────

    @Test
    @Order(1)
    void holidayCrudWorkflow() {
        // Create a holiday
        var request = new HolidayRequest("Republic Day", LocalDate.of(2026, 1, 26),
                HolidayType.PUBLIC, null, "National holiday");

        ResponseEntity<HolidayResponse> createResp = restTemplate.postForEntity(
                "/api/v1/holidays", request, HolidayResponse.class);

        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        HolidayResponse created = createResp.getBody();
        assertThat(created).isNotNull();
        assertThat(created.name()).isEqualTo("Republic Day");
        assertThat(created.type()).isEqualTo(HolidayType.PUBLIC);
        assertThat(created.active()).isTrue();

        Long holidayId = created.id();

        // Get by ID
        ResponseEntity<HolidayResponse> getResp = restTemplate.getForEntity(
                "/api/v1/holidays/" + holidayId, HolidayResponse.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResp.getBody()).isNotNull();
        assertThat(getResp.getBody().name()).isEqualTo("Republic Day");

        // Update
        var updateRequest = new HolidayRequest("Republic Day Updated", LocalDate.of(2026, 1, 26),
                HolidayType.PUBLIC, "ALL", "Updated national holiday");

        restTemplate.put("/api/v1/holidays/" + holidayId, updateRequest);

        ResponseEntity<HolidayResponse> getUpdated = restTemplate.getForEntity(
                "/api/v1/holidays/" + holidayId, HolidayResponse.class);
        assertThat(getUpdated.getBody()).isNotNull();
        assertThat(getUpdated.getBody().name()).isEqualTo("Republic Day Updated");
        assertThat(getUpdated.getBody().region()).isEqualTo("ALL");

        // Deactivate
        ResponseEntity<HolidayResponse> deactivateResp = restTemplate.exchange(
                "/api/v1/holidays/" + holidayId + "/deactivate",
                HttpMethod.PATCH, null, HolidayResponse.class);
        assertThat(deactivateResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(deactivateResp.getBody()).isNotNull();
        assertThat(deactivateResp.getBody().active()).isFalse();
    }

    @Test
    @Order(2)
    void holidayQueryByTypeAndRegion() {
        // Create regional holiday
        var regional = new HolidayRequest("Pongal", LocalDate.of(2026, 1, 14),
                HolidayType.REGIONAL, "South", "Regional harvest festival");
        restTemplate.postForEntity("/api/v1/holidays", regional, HolidayResponse.class);

        // Create another public holiday
        var publicHoliday = new HolidayRequest("Independence Day", LocalDate.of(2026, 8, 15),
                HolidayType.PUBLIC, null, "Independence day celebration");
        restTemplate.postForEntity("/api/v1/holidays", publicHoliday, HolidayResponse.class);

        // Query by type
        ResponseEntity<Map> typeResp = restTemplate.getForEntity(
                "/api/v1/holidays/type/REGIONAL?page=0&size=10", Map.class);
        assertThat(typeResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(typeResp.getBody()).isNotNull();
        List<?> typeContent = (List<?>) typeResp.getBody().get("content");
        assertThat(typeContent).isNotEmpty();

        // Query by region
        ResponseEntity<Map> regionResp = restTemplate.getForEntity(
                "/api/v1/holidays/region/South?page=0&size=10", Map.class);
        assertThat(regionResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(regionResp.getBody()).isNotNull();
        List<?> regionContent = (List<?>) regionResp.getBody().get("content");
        assertThat(regionContent).isNotEmpty();

        // Query active by date range
        ResponseEntity<List<HolidayResponse>> activeResp = restTemplate.exchange(
                "/api/v1/holidays/active?start=2026-01-01&end=2026-12-31",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<HolidayResponse>>() {});
        assertThat(activeResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(activeResp.getBody()).isNotNull();
        // Should include Pongal and Independence Day (both active). Republic Day was deactivated in test 1.
        assertThat(activeResp.getBody().size()).isGreaterThanOrEqualTo(2);
    }

    // ── Leave Plan CRUD ─────────────────────────────────────────────────

    @Test
    @Order(3)
    void leavePlanCrudWorkflow() {
        // Create a leave plan
        var request = new LeavePlanRequest(9801L, 9801L,
                LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 12),
                new BigDecimal("3.00"), "Planned family trip");

        ResponseEntity<LeavePlanResponse> createResp = restTemplate.postForEntity(
                "/api/v1/leave-plans", request, LeavePlanResponse.class);

        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        LeavePlanResponse created = createResp.getBody();
        assertThat(created).isNotNull();
        assertThat(created.employeeId()).isEqualTo(9801L);
        assertThat(created.leaveTypeName()).isEqualTo("Planner Casual Leave");
        assertThat(created.status()).isEqualTo(LeavePlanStatus.PLANNED);
        assertThat(created.numberOfDays()).isEqualByComparingTo(new BigDecimal("3.00"));

        Long planId = created.id();

        // Get by ID
        ResponseEntity<LeavePlanResponse> getResp = restTemplate.getForEntity(
                "/api/v1/leave-plans/" + planId, LeavePlanResponse.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResp.getBody()).isNotNull();
        assertThat(getResp.getBody().notes()).isEqualTo("Planned family trip");

        // Update
        var updateRequest = new LeavePlanRequest(9801L, 9801L,
                LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 14),
                new BigDecimal("5.00"), "Extended family trip");

        restTemplate.put("/api/v1/leave-plans/" + planId, updateRequest);

        ResponseEntity<LeavePlanResponse> getUpdated = restTemplate.getForEntity(
                "/api/v1/leave-plans/" + planId, LeavePlanResponse.class);
        assertThat(getUpdated.getBody()).isNotNull();
        assertThat(getUpdated.getBody().numberOfDays()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(getUpdated.getBody().notes()).isEqualTo("Extended family trip");

        // Query by employee
        ResponseEntity<Map> byEmployee = restTemplate.getForEntity(
                "/api/v1/leave-plans/employee/9801?page=0&size=10", Map.class);
        assertThat(byEmployee.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<?> employeeContent = (List<?>) byEmployee.getBody().get("content");
        assertThat(employeeContent).hasSize(1);
    }

    @Test
    @Order(4)
    void leavePlanCancelAndConvert() {
        // Create plan to cancel
        var cancelRequest = new LeavePlanRequest(9802L, 9801L,
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 2),
                new BigDecimal("2.00"), "To be cancelled");

        ResponseEntity<LeavePlanResponse> createResp = restTemplate.postForEntity(
                "/api/v1/leave-plans", cancelRequest, LeavePlanResponse.class);
        assertThat(createResp.getBody()).isNotNull();
        Long cancelPlanId = createResp.getBody().id();

        // Cancel it
        ResponseEntity<LeavePlanResponse> cancelResp = restTemplate.exchange(
                "/api/v1/leave-plans/" + cancelPlanId + "/cancel",
                HttpMethod.PATCH, null, LeavePlanResponse.class);
        assertThat(cancelResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cancelResp.getBody().status()).isEqualTo(LeavePlanStatus.CANCELLED);

        // Create plan to convert
        var convertRequest = new LeavePlanRequest(9802L, 9801L,
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 3),
                new BigDecimal("3.00"), "To be converted");

        ResponseEntity<LeavePlanResponse> createConvert = restTemplate.postForEntity(
                "/api/v1/leave-plans", convertRequest, LeavePlanResponse.class);
        assertThat(createConvert.getBody()).isNotNull();
        Long convertPlanId = createConvert.getBody().id();

        // Convert it
        ResponseEntity<LeavePlanResponse> convertResp = restTemplate.exchange(
                "/api/v1/leave-plans/" + convertPlanId + "/convert",
                HttpMethod.PATCH, null, LeavePlanResponse.class);
        assertThat(convertResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(convertResp.getBody().status()).isEqualTo(LeavePlanStatus.CONVERTED);

        // Trying to cancel a CONVERTED plan should fail
        ResponseEntity<Map> failCancel = restTemplate.exchange(
                "/api/v1/leave-plans/" + convertPlanId + "/cancel",
                HttpMethod.PATCH, null, Map.class);
        assertThat(failCancel.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ── Calendar Integration ────────────────────────────────────────────

    @Test
    @Order(5)
    void calendarEntriesAggregation() {
        // Create a holiday in March for calendar view
        var holiday = new HolidayRequest("Holi", LocalDate.of(2026, 3, 14),
                HolidayType.PUBLIC, null, "Festival of colors");
        restTemplate.postForEntity("/api/v1/holidays", holiday, HolidayResponse.class);

        // Create a leave plan in March
        var plan = new LeavePlanRequest(9802L, 9801L,
                LocalDate.of(2026, 3, 25), LocalDate.of(2026, 3, 27),
                new BigDecimal("3.00"), "Month-end break");
        restTemplate.postForEntity("/api/v1/leave-plans", plan, LeavePlanResponse.class);

        // Fetch calendar entries for March 2026
        ResponseEntity<List<LeaveCalendarEntry>> calendarResp = restTemplate.exchange(
                "/api/v1/leave-calendar/entries?start=2026-03-01&end=2026-03-31",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<LeaveCalendarEntry>>() {});

        assertThat(calendarResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<LeaveCalendarEntry> entries = calendarResp.getBody();
        assertThat(entries).isNotNull();
        // Should have: Holi (holiday), approved leave 9801 (March 20-22), planned leave for 9802 (March 25-27)
        assertThat(entries.size()).isGreaterThanOrEqualTo(3);

        // Verify types
        boolean hasHoliday = entries.stream().anyMatch(e -> "HOLIDAY".equals(e.type()));
        boolean hasLeave = entries.stream().anyMatch(e -> "LEAVE".equals(e.type()));
        boolean hasPlan = entries.stream().anyMatch(e -> "PLAN".equals(e.type()));
        assertThat(hasHoliday).isTrue();
        assertThat(hasLeave).isTrue();
        assertThat(hasPlan).isTrue();

        // Filter by department
        ResponseEntity<List<LeaveCalendarEntry>> deptResp = restTemplate.exchange(
                "/api/v1/leave-calendar/entries?start=2026-03-01&end=2026-03-31&departmentId=9800",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<LeaveCalendarEntry>>() {});
        assertThat(deptResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(deptResp.getBody()).isNotNull();

        // Filter by employee
        ResponseEntity<List<LeaveCalendarEntry>> empResp = restTemplate.exchange(
                "/api/v1/leave-calendar/entries?start=2026-03-01&end=2026-03-31&employeeId=9801",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<LeaveCalendarEntry>>() {});
        assertThat(empResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(empResp.getBody()).isNotNull();
    }

    @Test
    @Order(6)
    void teamAvailability() {
        // Get team availability for March 20-22 (when employee 9801 is on approved leave)
        ResponseEntity<List<TeamAvailabilityResponse>> availResp = restTemplate.exchange(
                "/api/v1/leave-calendar/team-availability?departmentId=9800&start=2026-03-20&end=2026-03-22",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<TeamAvailabilityResponse>>() {});

        assertThat(availResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<TeamAvailabilityResponse> availability = availResp.getBody();
        assertThat(availability).isNotNull();
        assertThat(availability).hasSize(3); // 3 days

        // On each of these days, one employee should be on leave
        for (TeamAvailabilityResponse day : availability) {
            assertThat(day.totalMembers()).isEqualTo(2); // 2 employees in dept
            assertThat(day.onLeave()).isGreaterThanOrEqualTo(1);
            assertThat(day.coveragePercentage()).isLessThan(100.0);
            assertThat(day.absentEmployees()).isNotEmpty();
        }
    }
}
