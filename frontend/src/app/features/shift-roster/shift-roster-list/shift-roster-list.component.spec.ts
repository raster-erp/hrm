import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ShiftRosterListComponent } from './shift-roster-list.component';
import { ShiftRosterService } from '../../../services/shift-roster.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { ShiftRosterResponse } from '../../../models/shift-roster.model';

describe('ShiftRosterListComponent', () => {
  let component: ShiftRosterListComponent;
  let fixture: ComponentFixture<ShiftRosterListComponent>;
  let shiftRosterServiceSpy: jasmine.SpyObj<ShiftRosterService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockPage: Page<ShiftRosterResponse> = {
    content: [
      {
        id: 1, employeeId: 1, employeeName: 'John Doe', employeeCode: 'EMP-001',
        shiftId: 1, shiftName: 'Morning Shift',
        effectiveDate: '2026-07-01', endDate: '2026-12-31',
        rotationPatternId: null, rotationPatternName: null,
        createdAt: '2026-01-01T00:00:00', updatedAt: '2026-03-10T10:00:00'
      }
    ],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    shiftRosterServiceSpy = jasmine.createSpyObj('ShiftRosterService', ['getAll', 'delete']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    shiftRosterServiceSpy.getAll.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        ShiftRosterListComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: ShiftRosterService, useValue: shiftRosterServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: Router, useValue: routerSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({}),
            snapshot: { paramMap: { get: () => null } }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ShiftRosterListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have displayed columns defined', () => {
    expect(component.displayedColumns.length).toBeGreaterThan(0);
  });

  it('should load rosters on init', () => {
    component.ngOnInit();
    expect(shiftRosterServiceSpy.getAll).toHaveBeenCalledWith(0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle load error', () => {
    shiftRosterServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.loadRosters();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load shift rosters');
  });

  it('should update pagination on page change', () => {
    component.onPageChange({ pageIndex: 2, pageSize: 25, length: 100 });
    expect(component.pageIndex).toBe(2);
    expect(component.pageSize).toBe(25);
  });

  it('should navigate to edit page', () => {
    component.editRoster(mockPage.content[0]);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/shift-rosters', 1, 'edit']);
  });

  it('should delete roster after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    shiftRosterServiceSpy.delete.and.returnValue(of(void 0));
    component.deleteRoster(mockPage.content[0]);
    expect(shiftRosterServiceSpy.delete).toHaveBeenCalledWith(1);
    expect(notificationServiceSpy.success).toHaveBeenCalled();
  });

  it('should not delete roster if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteRoster(mockPage.content[0]);
    expect(shiftRosterServiceSpy.delete).not.toHaveBeenCalled();
  });

  it('should handle delete error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    shiftRosterServiceSpy.delete.and.returnValue(throwError(() => new Error('fail')));
    component.deleteRoster(mockPage.content[0]);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to delete roster assignment');
  });
});
