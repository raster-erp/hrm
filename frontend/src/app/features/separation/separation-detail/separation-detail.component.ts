import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { MatTabsModule } from '@angular/material/tabs';
import { SeparationService } from '../../../services/separation.service';
import { NotificationService } from '../../../services/notification.service';
import { SeparationResponse, ExitChecklistResponse, NoDuesResponse } from '../../../models/separation.model';

@Component({
  selector: 'app-separation-detail',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatButtonModule, MatIconModule, MatCardModule,
    MatTableModule, MatInputModule, MatFormFieldModule,
    MatProgressSpinnerModule, MatDividerModule, MatTabsModule
  ],
  templateUrl: './separation-detail.component.html',
  styleUrl: './separation-detail.component.scss',
})
export class SeparationDetailComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  separation: SeparationResponse | null = null;
  checklistItems: ExitChecklistResponse[] = [];
  noDuesItems: NoDuesResponse[] = [];
  loading = false;

  checklistForm!: FormGroup;
  noDuesForm!: FormGroup;

  checklistColumns = ['itemName', 'department', 'notes', 'cleared', 'actions'];
  noDuesColumns = ['department', 'amountDue', 'notes', 'cleared', 'actions'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private separationService: SeparationService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForms();
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadSeparation(+id);
    } else {
      this.router.navigate(['/separations']);
    }
  }

  private initForms(): void {
    this.checklistForm = this.fb.group({
      itemName: ['', Validators.required],
      department: ['', Validators.required],
      notes: ['']
    });

    this.noDuesForm = this.fb.group({
      department: ['', Validators.required],
      amountDue: [0, [Validators.required, Validators.min(0)]],
      notes: ['']
    });
  }

  loadSeparation(id: number): void {
    this.loading = true;
    this.separationService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: separation => {
          this.separation = separation;
          this.loading = false;
          this.loadChecklist(id);
          this.loadNoDues(id);
        },
        error: () => {
          this.notificationService.error('Failed to load separation details');
          this.loading = false;
          this.router.navigate(['/separations']);
        }
      });
  }

  loadChecklist(separationId: number): void {
    this.separationService.getChecklist(separationId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(items => this.checklistItems = items);
  }

  loadNoDues(separationId: number): void {
    this.separationService.getNoDues(separationId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(items => this.noDuesItems = items);
  }

  approve(): void {
    if (!this.separation) return;
    this.separationService.approve(this.separation.id, 1)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: updated => {
          this.separation = updated;
          this.notificationService.success('Separation approved');
        },
        error: () => this.notificationService.error('Failed to approve separation')
      });
  }

  reject(): void {
    if (!this.separation) return;
    this.separationService.reject(this.separation.id, 1)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: updated => {
          this.separation = updated;
          this.notificationService.success('Separation rejected');
        },
        error: () => this.notificationService.error('Failed to reject separation')
      });
  }

  finalize(): void {
    if (!this.separation) return;
    this.separationService.finalize(this.separation.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: updated => {
          this.separation = updated;
          this.notificationService.success('Separation finalized');
        },
        error: () => this.notificationService.error('Failed to finalize separation')
      });
  }

  addChecklistItem(): void {
    if (!this.separation || this.checklistForm.invalid) return;
    const request = {
      separationId: this.separation.id,
      ...this.checklistForm.value
    };
    this.separationService.addChecklistItem(request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.loadChecklist(this.separation!.id);
          this.checklistForm.reset();
          this.notificationService.success('Checklist item added');
        },
        error: () => this.notificationService.error('Failed to add checklist item')
      });
  }

  clearChecklistItem(item: ExitChecklistResponse): void {
    this.separationService.clearChecklistItem(item.id, 'Admin')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.loadChecklist(this.separation!.id);
          this.notificationService.success('Item cleared');
        },
        error: () => this.notificationService.error('Failed to clear item')
      });
  }

  deleteChecklistItem(item: ExitChecklistResponse): void {
    this.separationService.deleteChecklistItem(item.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.loadChecklist(this.separation!.id);
          this.notificationService.success('Item removed');
        },
        error: () => this.notificationService.error('Failed to remove item')
      });
  }

  addNoDues(): void {
    if (!this.separation || this.noDuesForm.invalid) return;
    const request = {
      separationId: this.separation.id,
      ...this.noDuesForm.value
    };
    this.separationService.addNoDues(request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.loadNoDues(this.separation!.id);
          this.noDuesForm.reset({ amountDue: 0 });
          this.notificationService.success('No-dues entry added');
        },
        error: () => this.notificationService.error('Failed to add no-dues entry')
      });
  }

  clearNoDues(item: NoDuesResponse): void {
    this.separationService.clearNoDues(item.id, 'Admin')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.loadNoDues(this.separation!.id);
          this.notificationService.success('No-dues cleared');
        },
        error: () => this.notificationService.error('Failed to clear no-dues')
      });
  }

  deleteNoDues(item: NoDuesResponse): void {
    this.separationService.deleteNoDues(item.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.loadNoDues(this.separation!.id);
          this.notificationService.success('No-dues entry removed');
        },
        error: () => this.notificationService.error('Failed to remove no-dues entry')
      });
  }

  goBack(): void {
    this.router.navigate(['/separations']);
  }
}
