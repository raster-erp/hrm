import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatDialog } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { DesignationListComponent } from './designation-list.component';
import { DesignationService } from '../../../services/designation.service';
import { DepartmentService } from '../../../services/department.service';
import { NotificationService } from '../../../services/notification.service';
import { DesignationResponse } from '../../../models/designation.model';

describe('DesignationListComponent', () => {
  let component: DesignationListComponent;
  let fixture: ComponentFixture<DesignationListComponent>;
  let designationServiceSpy: jasmine.SpyObj<DesignationService>;
  let departmentServiceSpy: jasmine.SpyObj<DepartmentService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let dialogSpy: jasmine.SpyObj<MatDialog>;

  const mockDesignation: DesignationResponse = {
    id: 1, title: 'Developer', code: 'DEV', level: 3, grade: 'A',
    departmentId: 1, departmentName: 'Engineering',
    createdAt: '', updatedAt: ''
  };

  beforeEach(async () => {
    designationServiceSpy = jasmine.createSpyObj('DesignationService', ['getAll', 'getByDepartment', 'create', 'update', 'delete']);
    departmentServiceSpy = jasmine.createSpyObj('DepartmentService', ['getAll']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error', 'info']);
    dialogSpy = jasmine.createSpyObj('MatDialog', ['open']);

    designationServiceSpy.getAll.and.returnValue(of([mockDesignation]));
    departmentServiceSpy.getAll.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [DesignationListComponent, HttpClientTestingModule, NoopAnimationsModule],
      providers: [
        { provide: DesignationService, useValue: designationServiceSpy },
        { provide: DepartmentService, useValue: departmentServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: MatDialog, useValue: dialogSpy }
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

  it('should populate table data source', () => {
    expect(component.dataSource.data.length).toBe(1);
    expect(component.dataSource.data[0].title).toBe('Developer');
  });

  it('should show error on load failure', () => {
    designationServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.loadDesignations();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load designations');
  });

  it('should filter by department when selected', () => {
    designationServiceSpy.getByDepartment.and.returnValue(of([]));
    component.selectedDepartmentId = 1;
    component.onDepartmentFilterChange();
    expect(designationServiceSpy.getByDepartment).toHaveBeenCalledWith(1);
  });

  it('should load all designations when no department filter', () => {
    designationServiceSpy.getAll.calls.reset();
    component.selectedDepartmentId = null;
    component.loadDesignations();
    expect(designationServiceSpy.getAll).toHaveBeenCalled();
  });

  it('should open dialog for adding designation', () => {
    dialogSpy.open.and.returnValue({ afterClosed: () => of(null) } as any);
    component.addDesignation();
    expect(dialogSpy.open).toHaveBeenCalled();
  });

  it('should open dialog for editing designation', () => {
    dialogSpy.open.and.returnValue({ afterClosed: () => of(null) } as any);
    component.editDesignation(mockDesignation);
    expect(dialogSpy.open).toHaveBeenCalled();
  });

  it('should delete designation on confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    designationServiceSpy.delete.and.returnValue(of(void 0));
    designationServiceSpy.getAll.and.returnValue(of([]));

    component.deleteDesignation(mockDesignation);
    expect(designationServiceSpy.delete).toHaveBeenCalledWith(1);
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Designation deleted successfully');
  });

  it('should not delete designation when cancelled', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteDesignation(mockDesignation);
    expect(designationServiceSpy.delete).not.toHaveBeenCalled();
  });
});
