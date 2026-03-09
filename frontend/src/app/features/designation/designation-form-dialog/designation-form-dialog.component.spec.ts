import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { DesignationFormDialogComponent, DesignationFormDialogData } from './designation-form-dialog.component';
import { DepartmentService } from '../../../services/department.service';
import { DesignationResponse } from '../../../models/designation.model';
import { of } from 'rxjs';

describe('DesignationFormDialogComponent', () => {
  let component: DesignationFormDialogComponent;
  let fixture: ComponentFixture<DesignationFormDialogComponent>;
  let dialogRefSpy: jasmine.SpyObj<MatDialogRef<DesignationFormDialogComponent>>;
  let departmentServiceSpy: jasmine.SpyObj<DepartmentService>;

  function createComponent(data: DesignationFormDialogData): void {
    dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

    TestBed.overrideProvider(MAT_DIALOG_DATA, { useValue: data });
    TestBed.overrideProvider(MatDialogRef, { useValue: dialogRefSpy });

    fixture = TestBed.createComponent(DesignationFormDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  beforeEach(async () => {
    departmentServiceSpy = jasmine.createSpyObj('DepartmentService', ['getAll']);
    departmentServiceSpy.getAll.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [DesignationFormDialogComponent, HttpClientTestingModule, NoopAnimationsModule],
      providers: [
        { provide: MatDialogRef, useValue: { close: jasmine.createSpy('close') } },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: DepartmentService, useValue: departmentServiceSpy }
      ]
    }).compileComponents();
  });

  describe('create mode', () => {
    beforeEach(() => createComponent({}));

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should be in create mode when no designation is provided', () => {
      expect(component.isEditMode).toBeFalse();
    });

    it('should load departments on init', () => {
      expect(departmentServiceSpy.getAll).toHaveBeenCalled();
    });

    it('should require title, code, level, grade, and departmentId', () => {
      expect(component.form.get('title')?.hasError('required')).toBeTrue();
      expect(component.form.get('code')?.hasError('required')).toBeTrue();
      expect(component.form.get('level')?.hasError('required')).toBeTrue();
      expect(component.form.get('grade')?.hasError('required')).toBeTrue();
      expect(component.form.get('departmentId')?.hasError('required')).toBeTrue();
    });

    it('should not submit when form is invalid', () => {
      component.onSubmit();
      expect(dialogRefSpy.close).not.toHaveBeenCalled();
    });

    it('should submit valid form', () => {
      component.form.patchValue({
        title: 'Developer', code: 'DEV', level: 3,
        grade: 'A', departmentId: 1
      });
      component.onSubmit();
      expect(dialogRefSpy.close).toHaveBeenCalledWith(jasmine.objectContaining({
        title: 'Developer', code: 'DEV', level: 3, grade: 'A', departmentId: 1
      }));
    });

    it('should close dialog without result on cancel', () => {
      component.onCancel();
      expect(dialogRefSpy.close).toHaveBeenCalledWith();
    });
  });

  describe('edit mode', () => {
    const mockDesignation: DesignationResponse = {
      id: 1, title: 'Manager', code: 'MGR', level: 5, grade: 'B',
      departmentId: 2, departmentName: 'HR', description: 'Manages team',
      createdAt: '', updatedAt: ''
    };

    beforeEach(() => createComponent({ designation: mockDesignation }));

    it('should be in edit mode', () => {
      expect(component.isEditMode).toBeTrue();
    });

    it('should populate form with designation data', () => {
      expect(component.form.value).toEqual({
        title: 'Manager', code: 'MGR', level: 5, grade: 'B',
        departmentId: 2, description: 'Manages team'
      });
    });
  });
});
