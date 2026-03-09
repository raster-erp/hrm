package com.raster.hrm.employee;

import com.raster.hrm.employee.dto.EmployeeSearchCriteria;
import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.entity.EmploymentStatus;
import com.raster.hrm.employee.service.EmployeeSpecification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeSpecificationTest {

    @Mock
    private Root<Employee> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Path<Object> objectPath;

    @Mock
    private Path<String> stringPath;

    @Mock
    private Path<LocalDate> datePath;

    @Mock
    private Predicate predicate;

    @Mock
    private Expression<String> lowerExpression;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        when(root.get("deleted")).thenReturn(objectPath);
        when(criteriaBuilder.equal(objectPath, false)).thenReturn(predicate);
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(predicate);
    }

    @Test
    void buildSpecification_withNullCriteria_shouldOnlyFilterDeleted() {
        var criteria = new EmployeeSearchCriteria(null, null, null, null, null);

        var specification = EmployeeSpecification.buildSpecification(criteria);
        var result = specification.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result);
        verify(criteriaBuilder).equal(objectPath, false);
    }

    @Test
    void buildSpecification_withBlankName_shouldOnlyFilterDeleted() {
        var criteria = new EmployeeSearchCriteria("  ", null, null, null, null);

        var specification = EmployeeSpecification.buildSpecification(criteria);
        specification.toPredicate(root, query, criteriaBuilder);

        verify(criteriaBuilder).equal(objectPath, false);
        verify(criteriaBuilder, never()).like(any(Expression.class), any(String.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void buildSpecification_withName_shouldFilterByFirstNameOrLastName() {
        var criteria = new EmployeeSearchCriteria("John", null, null, null, null);

        when(root.<String>get("firstName")).thenReturn(stringPath);
        when(root.<String>get("lastName")).thenReturn(stringPath);
        when(criteriaBuilder.lower(stringPath)).thenReturn(lowerExpression);
        when(criteriaBuilder.like(eq(lowerExpression), eq("%john%"))).thenReturn(predicate);
        when(criteriaBuilder.or(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);

        var specification = EmployeeSpecification.buildSpecification(criteria);
        var result = specification.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result);
        verify(criteriaBuilder, org.mockito.Mockito.times(2)).like(eq(lowerExpression), eq("%john%"));
        verify(criteriaBuilder).or(any(Predicate.class), any(Predicate.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void buildSpecification_withDepartmentId_shouldFilterByDepartment() {
        var criteria = new EmployeeSearchCriteria(null, 1L, null, null, null);

        var departmentPath = (Path<Object>) org.mockito.Mockito.mock(Path.class);
        var departmentIdPath = (Path<Object>) org.mockito.Mockito.mock(Path.class);
        when(root.get("department")).thenReturn(departmentPath);
        when(departmentPath.get("id")).thenReturn(departmentIdPath);
        when(criteriaBuilder.equal(departmentIdPath, 1L)).thenReturn(predicate);

        var specification = EmployeeSpecification.buildSpecification(criteria);
        var result = specification.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result);
        verify(criteriaBuilder).equal(departmentIdPath, 1L);
    }

    @Test
    @SuppressWarnings("unchecked")
    void buildSpecification_withStatus_shouldFilterByStatus() {
        var criteria = new EmployeeSearchCriteria(null, null, "ACTIVE", null, null);

        when(root.get("employmentStatus")).thenReturn(objectPath);
        when(criteriaBuilder.equal(objectPath, EmploymentStatus.ACTIVE)).thenReturn(predicate);

        var specification = EmployeeSpecification.buildSpecification(criteria);
        var result = specification.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result);
        verify(criteriaBuilder).equal(objectPath, EmploymentStatus.ACTIVE);
    }

    @Test
    @SuppressWarnings("unchecked")
    void buildSpecification_withBlankStatus_shouldNotFilterByStatus() {
        var criteria = new EmployeeSearchCriteria(null, null, "  ", null, null);

        var specification = EmployeeSpecification.buildSpecification(criteria);
        specification.toPredicate(root, query, criteriaBuilder);

        verify(root, never()).get("employmentStatus");
    }

    @Test
    @SuppressWarnings("unchecked")
    void buildSpecification_withJoiningDateFrom_shouldFilterByDateFrom() {
        var dateFrom = LocalDate.of(2023, 1, 1);
        var criteria = new EmployeeSearchCriteria(null, null, null, dateFrom, null);

        when(root.<LocalDate>get("joiningDate")).thenReturn(datePath);
        when(criteriaBuilder.greaterThanOrEqualTo(datePath, dateFrom)).thenReturn(predicate);

        var specification = EmployeeSpecification.buildSpecification(criteria);
        var result = specification.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result);
        verify(criteriaBuilder).greaterThanOrEqualTo(datePath, dateFrom);
    }

    @Test
    @SuppressWarnings("unchecked")
    void buildSpecification_withJoiningDateTo_shouldFilterByDateTo() {
        var dateTo = LocalDate.of(2023, 12, 31);
        var criteria = new EmployeeSearchCriteria(null, null, null, null, dateTo);

        when(root.<LocalDate>get("joiningDate")).thenReturn(datePath);
        when(criteriaBuilder.lessThanOrEqualTo(datePath, dateTo)).thenReturn(predicate);

        var specification = EmployeeSpecification.buildSpecification(criteria);
        var result = specification.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result);
        verify(criteriaBuilder).lessThanOrEqualTo(datePath, dateTo);
    }

    @Test
    @SuppressWarnings("unchecked")
    void buildSpecification_withAllCriteria_shouldApplyAllFilters() {
        var dateFrom = LocalDate.of(2023, 1, 1);
        var dateTo = LocalDate.of(2023, 12, 31);
        var criteria = new EmployeeSearchCriteria("John", 1L, "ACTIVE", dateFrom, dateTo);

        when(root.<String>get("firstName")).thenReturn(stringPath);
        when(root.<String>get("lastName")).thenReturn(stringPath);
        when(criteriaBuilder.lower(stringPath)).thenReturn(lowerExpression);
        when(criteriaBuilder.like(eq(lowerExpression), eq("%john%"))).thenReturn(predicate);
        when(criteriaBuilder.or(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);

        var departmentPath = (Path<Object>) org.mockito.Mockito.mock(Path.class);
        var departmentIdPath = (Path<Object>) org.mockito.Mockito.mock(Path.class);
        when(root.get("department")).thenReturn(departmentPath);
        when(departmentPath.get("id")).thenReturn(departmentIdPath);
        when(criteriaBuilder.equal(departmentIdPath, 1L)).thenReturn(predicate);

        when(root.get("employmentStatus")).thenReturn(objectPath);
        when(criteriaBuilder.equal(objectPath, EmploymentStatus.ACTIVE)).thenReturn(predicate);

        when(root.<LocalDate>get("joiningDate")).thenReturn(datePath);
        when(criteriaBuilder.greaterThanOrEqualTo(datePath, dateFrom)).thenReturn(predicate);
        when(criteriaBuilder.lessThanOrEqualTo(datePath, dateTo)).thenReturn(predicate);

        var specification = EmployeeSpecification.buildSpecification(criteria);
        var result = specification.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result);
        verify(criteriaBuilder).equal(objectPath, false);
        verify(criteriaBuilder).or(any(Predicate.class), any(Predicate.class));
        verify(criteriaBuilder).equal(departmentIdPath, 1L);
        verify(criteriaBuilder).equal(objectPath, EmploymentStatus.ACTIVE);
        verify(criteriaBuilder).greaterThanOrEqualTo(datePath, dateFrom);
        verify(criteriaBuilder).lessThanOrEqualTo(datePath, dateTo);
    }
}
