package com.raster.hrm.leavebalance.controller;

import com.raster.hrm.leavebalance.dto.BalanceAdjustmentRequest;
import com.raster.hrm.leavebalance.dto.LeaveBalanceResponse;
import com.raster.hrm.leavebalance.dto.LeaveTransactionResponse;
import com.raster.hrm.leavebalance.dto.YearEndProcessingRequest;
import com.raster.hrm.leavebalance.dto.YearEndSummaryResponse;
import com.raster.hrm.leavebalance.entity.TransactionType;
import com.raster.hrm.leavebalance.service.LeaveBalanceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leave-balances")
public class LeaveBalanceController {

    private final LeaveBalanceService leaveBalanceService;

    public LeaveBalanceController(LeaveBalanceService leaveBalanceService) {
        this.leaveBalanceService = leaveBalanceService;
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<LeaveBalanceResponse>> getBalancesByEmployee(
            @PathVariable Long employeeId,
            @RequestParam int year) {
        return ResponseEntity.ok(leaveBalanceService.getBalancesByEmployee(employeeId, year));
    }

    @GetMapping("/employee/{employeeId}/leave-type/{leaveTypeId}")
    public ResponseEntity<LeaveBalanceResponse> getBalance(
            @PathVariable Long employeeId,
            @PathVariable Long leaveTypeId,
            @RequestParam int year) {
        return ResponseEntity.ok(leaveBalanceService.getBalance(employeeId, leaveTypeId, year));
    }

    @GetMapping("/employee/{employeeId}/transactions")
    public ResponseEntity<Page<LeaveTransactionResponse>> getTransactions(
            @PathVariable Long employeeId,
            @RequestParam(required = false) Long leaveTypeId,
            @RequestParam(required = false) TransactionType transactionType,
            Pageable pageable) {
        return ResponseEntity.ok(leaveBalanceService.getTransactions(employeeId, leaveTypeId, transactionType, pageable));
    }

    @PostMapping("/adjust")
    public ResponseEntity<LeaveBalanceResponse> adjustBalance(
            @Valid @RequestBody BalanceAdjustmentRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(leaveBalanceService.adjustBalance(request));
    }

    @PostMapping("/year-end")
    public ResponseEntity<YearEndSummaryResponse> processYearEnd(
            @Valid @RequestBody YearEndProcessingRequest request) {
        return ResponseEntity.ok(leaveBalanceService.processYearEnd(request));
    }
}
