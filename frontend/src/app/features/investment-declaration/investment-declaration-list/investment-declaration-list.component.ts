import { Component, OnInit, ViewChild, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { InvestmentDeclarationService } from '../../../services/investment-declaration.service';
import { NotificationService } from '../../../services/notification.service';
import { InvestmentDeclarationResponse } from '../../../models/investment-declaration.model';

@Component({
  selector: 'app-investment-declaration-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatCardModule,
    MatChipsModule,
    MatTooltipModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './investment-declaration-list.component.html',
  styleUrl: './investment-declaration-list.component.scss',
})
export class InvestmentDeclarationListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'employeeName', 'financialYear', 'regime', 'totalDeclaredAmount', 'status', 'actions'
  ];
  dataSource = new MatTableDataSource<InvestmentDeclarationResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private investmentDeclarationService: InvestmentDeclarationService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadDeclarations();
  }

  loadDeclarations(): void {
    this.loading = true;
    this.investmentDeclarationService.getAll(this.pageIndex, this.pageSize)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          this.dataSource.data = page.content;
          this.totalElements = page.totalElements;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load investment declarations');
          this.loading = false;
        }
      });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadDeclarations();
  }

  editDeclaration(decl: InvestmentDeclarationResponse): void {
    this.router.navigate(['/investment-declarations', decl.id, 'edit']);
  }

  submitDeclaration(decl: InvestmentDeclarationResponse): void {
    this.investmentDeclarationService.submit(decl.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Declaration submitted successfully');
          this.loadDeclarations();
        },
        error: () => this.notificationService.error('Failed to submit declaration')
      });
  }

  verifyDeclaration(decl: InvestmentDeclarationResponse): void {
    this.investmentDeclarationService.verify(decl.id, 1)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Declaration verified successfully');
          this.loadDeclarations();
        },
        error: () => this.notificationService.error('Failed to verify declaration')
      });
  }

  rejectDeclaration(decl: InvestmentDeclarationResponse): void {
    const remarks = prompt('Enter rejection remarks:');
    if (remarks) {
      this.investmentDeclarationService.reject(decl.id, remarks)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Declaration rejected');
            this.loadDeclarations();
          },
          error: () => this.notificationService.error('Failed to reject declaration')
        });
    }
  }

  deleteDeclaration(decl: InvestmentDeclarationResponse): void {
    if (confirm('Are you sure you want to delete this declaration?')) {
      this.investmentDeclarationService.delete(decl.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Declaration deleted successfully');
            this.loadDeclarations();
          },
          error: () => this.notificationService.error('Failed to delete declaration')
        });
    }
  }
}
