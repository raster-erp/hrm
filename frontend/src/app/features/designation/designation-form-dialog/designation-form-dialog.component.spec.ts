import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { DesignationFormDialogComponent } from './designation-form-dialog.component';
import { DepartmentService } from '../../../services/department.service';
import { of } from 'rxjs';

describe('DesignationFormDialogComponent', () => {
  let component: DesignationFormDialogComponent;
  let fixture: ComponentFixture<DesignationFormDialogComponent>;
  let departmentServiceSpy: jasmine.SpyObj<DepartmentService>;

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

    fixture = TestBed.createComponent(DesignationFormDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should be in create mode when no designation is provided', () => {
    expect(component.isEditMode).toBeFalse();
  });
});
