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

  stockDisplay(item: InventoryItem): string {
    if (!item.capsulesPerStrip || item.capsuleQuantity == null) return `${item.quantity} units`;
    const strips = Math.floor(item.capsuleQuantity / item.capsulesPerStrip);
    const loose = item.capsuleQuantity % item.capsulesPerStrip;
    return `${strips} strips${loose ? ` + ${loose} capsules` : ''}`;
  }

  isOutOfStock(item: InventoryItem): boolean {
    return (item.capsuleQuantity ?? item.quantity) < 1;
  }

  isLowStock(item: InventoryItem): boolean {
    return item.capsuleQuantity != null && item.capsulesPerStrip
      ? item.capsuleQuantity < item.capsulesPerStrip * 5
      : item.quantity > 0 && item.quantity < 5;
  }

  isInStock(item: InventoryItem): boolean {
    return !this.isOutOfStock(item) && !this.isLowStock(item);
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
    const capsuleTracked = item.capsulesPerStrip != null && item.capsuleQuantity != null;
    const packSize = capsuleTracked ? item.capsulesPerStrip! : null;
    const capsuleStock = capsuleTracked ? item.capsuleQuantity! : 0;
    const stripStock = capsuleTracked ? Math.floor(capsuleStock / packSize!) : item.quantity;
    if (capsuleTracked && capsuleStock < 1 || !capsuleTracked && item.quantity < 1) {
      this.showNotice('This item is out of stock.');
      return;
    }
    const saleUnit = capsuleTracked && stripStock === 0 ? 'CAPSULE' : 'STRIP';
    this.cart.add({
      itemId: item.id,
      name: item.name,
      mfgName: item.mfgName ?? null,
      batchNumber: item.batchNumber ?? null,
      expireDate: item.expireDate ?? null,
      scheduleH1: item.scheduleH1 ?? false,
      quantity: 1,
      stock: saleUnit === 'CAPSULE' ? capsuleStock : stripStock,
      stripStock,
      capsuleStock,
      capsulesPerStrip: packSize,
      saleUnit,
      stripPrice: Number(item.price),
      unitPrice: saleUnit === 'CAPSULE'
        ? Math.round((Number(item.price) / packSize! + Number.EPSILON) * 100) / 100
        : Number(item.price),
    });
    this.showNotice(
        saleUnit
        ? `You have reached the maximum quantity of ${item.quantity} for ${item.name}.`
        : `${item.name} added to cart.`,
    );
  }

  async refreshAfterCheckout(): Promise<void> {
    await this.loadItems(this.query, this.page);
    await this.loadAnalysis();
    this.showNotice('Order saved and inventory updated.');
  }

  async submitForm(form: ItemFormData): Promise<void> {
    if (!this.modal) return;
    const pageToLoad = this.modal.type === 'update' ? this.page : 0;
    this.busy = true;
    const payload: ItemWriteRequest = {
      ...form,
      price: Number(form.price),
      quantity: Number(form.quantity),
      capsulesPerStrip: Number(form.capsulesPerStrip),
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
      await this.loadItems(this.query, pageToLoad);
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
