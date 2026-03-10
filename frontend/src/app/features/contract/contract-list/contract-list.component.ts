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
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ContractService } from '../../../services/contract.service';
import { NotificationService } from '../../../services/notification.service';
import { ContractResponse } from '../../../models/contract.model';

@Component({
  selector: 'app-contract-list',
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
    MatCardModule,
    MatTooltipModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './contract-list.component.html',
  styleUrl: './contract-list.component.scss',
})
export class ContractListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'employeeName', 'contractType', 'startDate', 'endDate', 'status', 'actions'
  ];
  dataSource = new MatTableDataSource<ContractResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

  selectedStatus = '';
  statuses = ['ACTIVE', 'EXPIRED', 'RENEWED', 'TERMINATED'];
  contractTypes = ['PERMANENT', 'PROBATION', 'FIXED_TERM', 'CONSULTANT'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private contractService: ContractService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadContracts();
  }

  loadContracts(): void {
    this.loading = true;
    this.contractService.getAll(this.pageIndex, this.pageSize)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          if (this.selectedStatus) {
            this.dataSource.data = page.content.filter(c => c.status === this.selectedStatus);
            this.totalElements = this.dataSource.data.length;
          } else {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
          }
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load contracts');
          this.loading = false;
        }
      });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadContracts();
  }

  onStatusFilter(): void {
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadContracts();
  }

  clearFilters(): void {
    this.selectedStatus = '';
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadContracts();
  }

  viewContract(contract: ContractResponse): void {
    this.router.navigate(['/contracts', contract.id]);
  }

  editContract(contract: ContractResponse): void {
    this.router.navigate(['/contracts', contract.id, 'edit']);
  }

  renewContract(contract: ContractResponse): void {
    if (confirm(`Are you sure you want to renew the contract for ${contract.employeeName}?`)) {
      this.contractService.renew(contract.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Contract renewed successfully');
            this.loadContracts();
          },
          error: () => this.notificationService.error('Failed to renew contract')
        });
    }
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'primary';
      case 'EXPIRED': return 'warn';
      case 'RENEWED': return 'accent';
      case 'TERMINATED': return 'warn';
      default: return '';
    }
  }

  formatType(type: string): string {
    return type.replace(/_/g, ' ');
  }
}
