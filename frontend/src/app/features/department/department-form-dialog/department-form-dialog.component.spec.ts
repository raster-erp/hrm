import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { DepartmentFormDialogComponent, DepartmentFormDialogData } from './department-form-dialog.component';
import { DepartmentResponse } from '../../../models/department.model';

describe('DepartmentFormDialogComponent', () => {
  let component: DepartmentFormDialogComponent;
  let fixture: ComponentFixture<DepartmentFormDialogComponent>;
  let dialogRefSpy: jasmine.SpyObj<MatDialogRef<DepartmentFormDialogComponent>>;

  function createComponent(data: DepartmentFormDialogData): void {
    dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

    TestBed.overrideProvider(MAT_DIALOG_DATA, { useValue: data });
    TestBed.overrideProvider(MatDialogRef, { useValue: dialogRefSpy });

    fixture = TestBed.createComponent(DepartmentFormDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DepartmentFormDialogComponent, NoopAnimationsModule],
      providers: [
        { provide: MatDialogRef, useValue: { close: jasmine.createSpy('close') } },
        { provide: MAT_DIALOG_DATA, useValue: {} }
      ]
    }).compileComponents();
  });

  describe('create mode', () => {
    beforeEach(() => createComponent({ parentId: 5 }));

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should be in create mode when no department is provided', () => {
      expect(component.isEditMode).toBeFalse();
    });

    it('should initialize form with empty values', () => {
      expect(component.form.value).toEqual({
        name: '',
        code: '',
        description: '',
        active: true
      });
    });

    it('should require name and code', () => {
      expect(component.form.get('name')?.hasError('required')).toBeTrue();
      expect(component.form.get('code')?.hasError('required')).toBeTrue();
    });

    it('should not submit when form is invalid', () => {
      component.onSubmit();
      expect(dialogRefSpy.close).not.toHaveBeenCalled();
    });

    it('should submit valid form with parentId', () => {
      component.form.patchValue({ name: 'Engineering', code: 'ENG' });
      component.onSubmit();
      expect(dialogRefSpy.close).toHaveBeenCalledWith({
        name: 'Engineering',
        code: 'ENG',
        description: '',
        active: true,
        parentId: 5
      });
    });

    it('should close dialog without result on cancel', () => {
      component.onCancel();
      expect(dialogRefSpy.close).toHaveBeenCalledWith();
    });
  });

  describe('edit mode', () => {
    const mockDepartment: DepartmentResponse = {
      id: 1, name: 'HR', code: 'HR', parentId: 2,
      description: 'Human Resources', active: false,
      createdAt: '', updatedAt: ''
    };

    beforeEach(() => createComponent({ department: mockDepartment, parentId: 2 }));

    it('should be in edit mode', () => {
      expect(component.isEditMode).toBeTrue();
    });

    it('should populate form with department data', () => {
      expect(component.form.value).toEqual({
        name: 'HR',
        code: 'HR',
        description: 'Human Resources',
        active: false
      });
    });
  });
});
