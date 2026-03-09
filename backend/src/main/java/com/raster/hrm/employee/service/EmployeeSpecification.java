package com.raster.hrm.employee.service;

import com.raster.hrm.employee.dto.EmployeeSearchCriteria;
import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.entity.EmploymentStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class EmployeeSpecification {

    private EmployeeSpecification() {
    }

    public static Specification<Employee> buildSpecification(EmployeeSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("deleted"), false));

            if (criteria.name() != null && !criteria.name().isBlank()) {
                var nameLike = "%" + criteria.name().toLowerCase() + "%";
                var firstNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("firstName")), nameLike);
                var lastNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("lastName")), nameLike);
                predicates.add(criteriaBuilder.or(firstNamePredicate, lastNamePredicate));
            }

            if (criteria.departmentId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("department").get("id"), criteria.departmentId()));
            }

            if (criteria.status() != null && !criteria.status().isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        root.get("employmentStatus"), EmploymentStatus.valueOf(criteria.status())));
            }

            if (criteria.joiningDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("joiningDate"), criteria.joiningDateFrom()));
            }

            if (criteria.joiningDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("joiningDate"), criteria.joiningDateTo()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
