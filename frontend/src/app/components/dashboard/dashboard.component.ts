import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, EventEmitter, OnInit, Output } from '@angular/core';
import {
  LucideArchive,
  LucideCirclePlus,
  LucidePencil,
  LucideShoppingCart,
  LucideTrash,
} from '@lucide/angular';
import { CartService } from '../../services/cart.service';
import {
  InventoryApiService,
  InventoryItem,
  ItemPageResponse,
  ItemWriteRequest,
} from '../../services/inventory-api.service';
import { CartComponent } from '../cart/cart.component';
import { HeaderComponent } from '../header/header.component';
import { ItemFormComponent, ItemFormData, ItemFormMode } from '../item-form/item-form.component';

interface ItemModal {
  type: ItemFormMode;
  item: InventoryItem | null;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    HeaderComponent,
    CartComponent,
    ItemFormComponent,
    LucideArchive,
    LucideCirclePlus,
    LucidePencil,
    LucideShoppingCart,
    LucideTrash,
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent implements OnInit {
  @Output() readonly notice = new EventEmitter<string>();

  constructor(
    private readonly changeDetector: ChangeDetectorRef,
    readonly cart: CartService,
    private readonly inventoryApi: InventoryApiService,
  ) {}

  readonly money = new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
  });

  items: InventoryItem[] = [];
  totalMedicineCount = 0;
  totalInventoryValue = 0;
  limitedStockCount = 0;
  lowStockOnly = false;
  query = '';
  page = 0;
  readonly pageSize = 10;
  totalItems = 0;
  totalPages = 0;
  hasPrev = false;
  hasNext = false;
  modal: ItemModal | null = null;
  busy = false;
  showCart = false;

  get displayedPage(): number {
    return Math.max(this.page + 1, 1);
  }

  get displayedTotalPages(): number {
    return Math.max(this.totalPages, 1);
  }

  formatPrice(price: number | string): string {
    return this.money.format(Number(price));
  }

  ngOnInit(): void {
    void this.loadItems();
    void this.loadAnalysis();
  }

  async loadAnalysis(): Promise<void> {
    try {
      const analysis = await this.inventoryApi.getAnalysis();
      this.totalMedicineCount = analysis.totalMedicineCount;
      this.totalInventoryValue = analysis.totalInventoryValue;
      this.limitedStockCount = analysis.limitedStockCount;
      this.changeDetector.markForCheck();
    } catch (error) {
      this.showNotice(this.errorMessage(error));
    }
  }

  async loadItems(term = '', requestedPage = 0): Promise<void> {
    try {
      const data = await this.inventoryApi.searchItems(
        term,
        requestedPage,
        this.pageSize,
        this.lowStockOnly,
      );
      this.items = data.content ?? [];
      this.page = data.page ?? 0;
      this.totalItems = data.totalElements ?? 0;
      this.totalPages = data.totalPages ?? 0;
      this.hasPrev = Boolean(data.hasPrev);
      this.hasNext = Boolean(data.hasNext);
      this.changeDetector.markForCheck();
    } catch (error) {
      this.showNotice(this.errorMessage(error));
    }
  }

  openModal(type: ItemFormMode, item: InventoryItem | null = null): void {
    this.modal = { type, item };
  }

  async submitSearch(term: string): Promise<void> {
    this.query = term;
    await this.loadItems(term, 0);
  }

  clearSearch(): void {
    this.query = '';
    void this.loadItems('', 0);
  }

  toggleLowStockFilter(): void {
    this.lowStockOnly = !this.lowStockOnly;
    void this.loadItems(this.query, 0);
  }

  addToCart(item: InventoryItem): void {
    if (item.quantity < 1) {
      this.showNotice('This item is out of stock.');
      return;
    }
    this.cart.add({
      itemId: item.id,
      name: item.name,
      quantity: 1,
      stock: item.quantity,
      unitPrice: Number(item.price),
    });
    this.showNotice(`${item.name} added to cart.`);
  }

  async refreshAfterCheckout(): Promise<void> {
    await this.loadItems(this.query, this.page);
    await this.loadAnalysis();
    this.showNotice('Order saved and inventory updated.');
  }

  async submitForm(form: ItemFormData): Promise<void> {
    if (!this.modal) return;
    this.busy = true;
    const payload: ItemWriteRequest = {
      ...form,
      price: Number(form.price),
      quantity: Number(form.quantity),
    };
    try {
      if (this.modal.type === 'add') {
        await this.inventoryApi.addItem(payload);
        this.showNotice('Item added to your inventory.');
      } else if (this.modal.type === 'update' && this.modal.item) {
        await this.inventoryApi.updateItem(this.modal.item.id, payload);
        this.showNotice('Item details updated.');
      }
      this.modal = null;
      await this.loadItems(this.query, 0);
      await this.loadAnalysis();
    } catch (error) {
      this.showNotice(this.errorMessage(error));
    } finally {
      this.busy = false;
      this.changeDetector.markForCheck();
    }
  }

  async deleteItem(item: InventoryItem): Promise<void> {
    if (!window.confirm(`Delete “${item.name}” from your inventory?`)) return;
    try {
      await this.inventoryApi.deleteItem(item.id);
      this.showNotice('Item deleted.');
      await this.loadItems(this.query, Math.max(this.page - 1, 0));
      await this.loadAnalysis();
    } catch (error) {
      this.showNotice(this.errorMessage(error));
    }
  }

  showNotice(message: string): void {
    this.notice.emit(message);
  }

  private errorMessage(error: unknown): string {
    return error instanceof Error ? error.message : 'The request failed.';
  }
}
