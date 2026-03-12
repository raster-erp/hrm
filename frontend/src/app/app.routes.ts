import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'employees', pathMatch: 'full' },
  {
    path: 'employees',
    loadChildren: () => import('./features/employee/employee.routes').then(m => m.EMPLOYEE_ROUTES)
  },
  {
    path: 'departments',
    loadChildren: () => import('./features/department/department.routes').then(m => m.DEPARTMENT_ROUTES)
  },
  {
    path: 'designations',
    loadChildren: () => import('./features/designation/designation.routes').then(m => m.DESIGNATION_ROUTES)
  },
  {
    path: 'contracts',
    loadChildren: () => import('./features/contract/contract.routes').then(m => m.CONTRACT_ROUTES)
  },
  {
    path: 'credentials',
    loadChildren: () => import('./features/credential/credential.routes').then(m => m.CREDENTIAL_ROUTES)
  },
  {
    path: 'id-cards',
    loadChildren: () => import('./features/id-card/id-card.routes').then(m => m.ID_CARD_ROUTES)
  },
  {
    path: 'uniforms',
    loadChildren: () => import('./features/uniform/uniform.routes').then(m => m.UNIFORM_ROUTES)
  },
  {
    path: 'transfers',
    loadChildren: () => import('./features/transfer/transfer.routes').then(m => m.TRANSFER_ROUTES)
  },
  {
    path: 'promotions',
    loadChildren: () => import('./features/promotion/promotion.routes').then(m => m.PROMOTION_ROUTES)
  },
  {
    path: 'separations',
    loadChildren: () => import('./features/separation/separation.routes').then(m => m.SEPARATION_ROUTES)
  },
  {
    path: 'devices',
    loadChildren: () => import('./features/device/device.routes').then(m => m.DEVICE_ROUTES)
  },
  {
    path: 'attendance-punches',
    loadChildren: () => import('./features/attendance/attendance.routes').then(m => m.ATTENDANCE_ROUTES)
  },
  {
    path: 'shifts',
    loadChildren: () => import('./features/shift/shift.routes').then(m => m.SHIFT_ROUTES)
  },
  {
    path: 'rotation-patterns',
    loadChildren: () => import('./features/rotation-pattern/rotation-pattern.routes').then(m => m.ROTATION_PATTERN_ROUTES)
  },
  {
    path: 'shift-rosters',
    loadChildren: () => import('./features/shift-roster/shift-roster.routes').then(m => m.SHIFT_ROSTER_ROUTES)
  },
  {
    path: 'overtime-policies',
    loadChildren: () => import('./features/overtime-policy/overtime-policy.routes').then(m => m.OVERTIME_POLICY_ROUTES)
  },
  {
    path: 'overtime-records',
    loadChildren: () => import('./features/overtime-record/overtime-record.routes').then(m => m.OVERTIME_RECORD_ROUTES)
  },
  {
    path: 'attendance-deviations',
    loadChildren: () => import('./features/attendance-deviation/attendance-deviation.routes').then(m => m.ATTENDANCE_DEVIATION_ROUTES)
  },
  {
    path: 'attendance-regularization',
    loadChildren: () => import('./features/attendance-regularization/attendance-regularization.routes').then(m => m.ATTENDANCE_REGULARIZATION_ROUTES)
  },
  {
    path: 'wfh-requests',
    loadChildren: () => import('./features/wfh-request/wfh-request.routes').then(m => m.WFH_REQUEST_ROUTES)
  },
  {
    path: 'attendance-reports',
    loadChildren: () => import('./features/attendance-report/attendance-report.routes').then(m => m.ATTENDANCE_REPORT_ROUTES)
  },
  {
    path: 'leave-types',
    loadChildren: () => import('./features/leave-type/leave-type.routes').then(m => m.LEAVE_TYPE_ROUTES)
  },
  {
    path: 'leave-policies',
    loadChildren: () => import('./features/leave-policy/leave-policy.routes').then(m => m.LEAVE_POLICY_ROUTES)
  },
  {
    path: 'leave-policy-assignments',
    loadChildren: () => import('./features/leave-policy-assignment/leave-policy-assignment.routes').then(m => m.LEAVE_POLICY_ASSIGNMENT_ROUTES)
  },
  {
    path: 'leave-applications',
    loadChildren: () => import('./features/leave-application/leave-application.routes').then(m => m.LEAVE_APPLICATION_ROUTES)
  },
];
