import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { RotationPatternListComponent } from './rotation-pattern-list.component';
import { RotationPatternService } from '../../../services/rotation-pattern.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { RotationPatternResponse } from '../../../models/rotation-pattern.model';

describe('RotationPatternListComponent', () => {
  let component: RotationPatternListComponent;
  let fixture: ComponentFixture<RotationPatternListComponent>;
  let rotationPatternServiceSpy: jasmine.SpyObj<RotationPatternService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockPage: Page<RotationPatternResponse> = {
    content: [
      {
        id: 1, name: 'Weekly Rotation', description: 'Rotates weekly',
        rotationDays: 7, shiftSequence: '1,2,3,1,2,3,1',
        createdAt: '2026-01-01T00:00:00', updatedAt: '2026-03-10T10:00:00'
      }
    ],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    rotationPatternServiceSpy = jasmine.createSpyObj('RotationPatternService', ['getAll', 'delete']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    rotationPatternServiceSpy.getAll.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        RotationPatternListComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: RotationPatternService, useValue: rotationPatternServiceSpy },
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

    fixture = TestBed.createComponent(RotationPatternListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have displayed columns defined', () => {
    expect(component.displayedColumns.length).toBeGreaterThan(0);
  });

  it('should load patterns on init', () => {
    component.ngOnInit();
    expect(rotationPatternServiceSpy.getAll).toHaveBeenCalledWith(0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle load error', () => {
    rotationPatternServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.loadPatterns();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load rotation patterns');
  });

  it('should update pagination on page change', () => {
    component.onPageChange({ pageIndex: 2, pageSize: 25, length: 100 });
    expect(component.pageIndex).toBe(2);
    expect(component.pageSize).toBe(25);
  });

  it('should navigate to edit page', () => {
    component.editPattern(mockPage.content[0]);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/rotation-patterns', 1, 'edit']);
  });

  it('should delete pattern after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    rotationPatternServiceSpy.delete.and.returnValue(of(void 0));
    component.deletePattern(mockPage.content[0]);
    expect(rotationPatternServiceSpy.delete).toHaveBeenCalledWith(1);
    expect(notificationServiceSpy.success).toHaveBeenCalled();
  });

  it('should not delete pattern if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deletePattern(mockPage.content[0]);
    expect(rotationPatternServiceSpy.delete).not.toHaveBeenCalled();
  });

  it('should handle delete error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    rotationPatternServiceSpy.delete.and.returnValue(throwError(() => new Error('fail')));
    component.deletePattern(mockPage.content[0]);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to delete rotation pattern');
  });
});
