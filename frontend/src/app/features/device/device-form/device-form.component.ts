import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DeviceService } from '../../../services/device.service';
import { NotificationService } from '../../../services/notification.service';
import { DeviceRequest } from '../../../models/device.model';

@Component({
  selector: 'app-device-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatCardModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './device-form.component.html',
  styleUrl: './device-form.component.scss',
})
export class DeviceFormComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  deviceForm!: FormGroup;
  isEditMode = false;
  deviceId: number | null = null;
  loading = false;
  saving = false;

  types = ['BIOMETRIC', 'RFID'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private deviceService: DeviceService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.deviceId = +id;
      this.loadDevice(this.deviceId);
    }
  }

  private initForm(): void {
    this.deviceForm = this.fb.group({
      serialNumber: ['', [Validators.required, Validators.maxLength(100)]],
      name: ['', [Validators.required, Validators.maxLength(100)]],
      type: ['', Validators.required],
      location: ['', Validators.maxLength(255)],
      ipAddress: ['', Validators.maxLength(45)]
    });
  }

  loadDevice(id: number): void {
    this.loading = true;
    this.deviceService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: device => {
          this.deviceForm.patchValue({
            serialNumber: device.serialNumber,
            name: device.name,
            type: device.type,
            location: device.location,
            ipAddress: device.ipAddress
          });
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load device');
          this.loading = false;
          this.router.navigate(['/devices']);
        }
      });
  }

  onSubmit(): void {
    if (this.deviceForm.invalid) {
      this.deviceForm.markAllAsTouched();
      this.notificationService.error('Please fill in all required fields correctly');
      return;
    }

    this.saving = true;
    const request: DeviceRequest = this.deviceForm.value;

    const operation$ = this.isEditMode && this.deviceId
      ? this.deviceService.update(this.deviceId, request)
      : this.deviceService.create(request);

    operation$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          const message = this.isEditMode ? 'Device updated successfully' : 'Device created successfully';
          this.notificationService.success(message);
          this.saving = false;
          this.router.navigate(['/devices']);
        },
        error: () => {
          const message = this.isEditMode ? 'Failed to update device' : 'Failed to create device';
          this.notificationService.error(message);
          this.saving = false;
        }
      });
  }

  onCancel(): void {
    this.router.navigate(['/devices']);
  }
}
