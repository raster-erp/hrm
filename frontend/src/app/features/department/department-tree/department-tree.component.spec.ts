import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatDialog } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { DepartmentTreeComponent } from './department-tree.component';
import { DepartmentService } from '../../../services/department.service';
import { NotificationService } from '../../../services/notification.service';
import { DepartmentResponse } from '../../../models/department.model';

describe('DepartmentTreeComponent', () => {
  let component: DepartmentTreeComponent;
  let fixture: ComponentFixture<DepartmentTreeComponent>;
  let departmentServiceSpy: jasmine.SpyObj<DepartmentService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let dialogSpy: jasmine.SpyObj<MatDialog>;

  const mockDepartments: DepartmentResponse[] = [
    {
      id: 1, name: 'Engineering', code: 'ENG', active: true,
      createdAt: '', updatedAt: '',
      children: [
        { id: 2, name: 'Frontend', code: 'FE', parentId: 1, active: true, createdAt: '', updatedAt: '' }
      ]
    }
  ];

  beforeEach(async () => {
    departmentServiceSpy = jasmine.createSpyObj('DepartmentService', ['getRootDepartments', 'getById', 'create', 'update', 'delete']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error', 'info']);
    dialogSpy = jasmine.createSpyObj('MatDialog', ['open']);

    departmentServiceSpy.getRootDepartments.and.returnValue(of(mockDepartments));

    await TestBed.configureTestingModule({
      imports: [DepartmentTreeComponent, HttpClientTestingModule, NoopAnimationsModule],
      providers: [
        { provide: DepartmentService, useValue: departmentServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: MatDialog, useValue: dialogSpy }
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

  it('should populate tree data source', () => {
    expect(component.dataSource.data.length).toBe(1);
    expect(component.dataSource.data[0].name).toBe('Engineering');
  });

  it('should show error on load failure', () => {
    departmentServiceSpy.getRootDepartments.and.returnValue(throwError(() => new Error('fail')));
    component.loadDepartments();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load departments');
  });

  it('should identify expandable nodes', () => {
    expect(component.hasChild(0, { id: 1, name: 'A', code: 'A', active: true, level: 0, expandable: true })).toBeTrue();
    expect(component.hasChild(0, { id: 2, name: 'B', code: 'B', active: true, level: 0, expandable: false })).toBeFalse();
  });

  it('should open dialog for adding root department', () => {
    dialogSpy.open.and.returnValue({ afterClosed: () => of(null) } as any);
    component.addRootDepartment();
    expect(dialogSpy.open).toHaveBeenCalled();
  });

  it('should open dialog for adding child department', () => {
    dialogSpy.open.and.returnValue({ afterClosed: () => of(null) } as any);
    component.addChildDepartment({ id: 1, name: 'A', code: 'A', active: true, level: 0, expandable: false });
    expect(dialogSpy.open).toHaveBeenCalled();
  });

  it('should delete department on confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    departmentServiceSpy.delete.and.returnValue(of(void 0));
    departmentServiceSpy.getRootDepartments.and.returnValue(of([]));

    component.deleteDepartment({ id: 1, name: 'A', code: 'A', active: true, level: 0, expandable: false });
    expect(departmentServiceSpy.delete).toHaveBeenCalledWith(1);
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Department deleted successfully');
  });

  it('should not delete department when cancelled', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteDepartment({ id: 1, name: 'A', code: 'A', active: true, level: 0, expandable: false });
    expect(departmentServiceSpy.delete).not.toHaveBeenCalled();
  });
});
