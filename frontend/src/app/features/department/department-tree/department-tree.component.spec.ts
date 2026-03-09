import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { DepartmentTreeComponent } from './department-tree.component';
import { DepartmentService } from '../../../services/department.service';
import { NotificationService } from '../../../services/notification.service';

describe('DepartmentTreeComponent', () => {
  let component: DepartmentTreeComponent;
  let fixture: ComponentFixture<DepartmentTreeComponent>;
  let departmentServiceSpy: jasmine.SpyObj<DepartmentService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

  beforeEach(async () => {
    departmentServiceSpy = jasmine.createSpyObj('DepartmentService', ['getRootDepartments', 'getById', 'create', 'update', 'delete']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error', 'info']);

    departmentServiceSpy.getRootDepartments.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [DepartmentTreeComponent, HttpClientTestingModule, NoopAnimationsModule],
      providers: [
        { provide: DepartmentService, useValue: departmentServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DepartmentTreeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load departments on init', () => {
    expect(departmentServiceSpy.getRootDepartments).toHaveBeenCalled();
  });
});
