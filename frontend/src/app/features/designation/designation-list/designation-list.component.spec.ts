import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { DesignationListComponent } from './designation-list.component';
import { DesignationService } from '../../../services/designation.service';
import { DepartmentService } from '../../../services/department.service';
import { NotificationService } from '../../../services/notification.service';

describe('DesignationListComponent', () => {
  let component: DesignationListComponent;
  let fixture: ComponentFixture<DesignationListComponent>;
  let designationServiceSpy: jasmine.SpyObj<DesignationService>;
  let departmentServiceSpy: jasmine.SpyObj<DepartmentService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

  beforeEach(async () => {
    designationServiceSpy = jasmine.createSpyObj('DesignationService', ['getAll', 'getByDepartment', 'create', 'update', 'delete']);
    departmentServiceSpy = jasmine.createSpyObj('DepartmentService', ['getAll']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error', 'info']);

    designationServiceSpy.getAll.and.returnValue(of([]));
    departmentServiceSpy.getAll.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [DesignationListComponent, HttpClientTestingModule, NoopAnimationsModule],
      providers: [
        { provide: DesignationService, useValue: designationServiceSpy },
        { provide: DepartmentService, useValue: departmentServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DesignationListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load designations on init', () => {
    expect(designationServiceSpy.getAll).toHaveBeenCalled();
  });

  it('should load departments on init', () => {
    expect(departmentServiceSpy.getAll).toHaveBeenCalled();
  });
});
