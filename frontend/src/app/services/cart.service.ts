import { computed, Injectable, signal } from '@angular/core';

export interface CartItem {
  itemId: number | string;
  name: string;
  quantity: number;
  stock: number;
  unitPrice: number;
}

const STORAGE_KEY = 'jk-wellness-cart';

@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly itemsState = signal<CartItem[]>(this.readStoredItems());
  private readonly discountState = signal(0);

  readonly items = this.itemsState.asReadonly();
  readonly discountPercent = this.discountState.asReadonly();
  readonly totalQuantity = computed(() =>
    this.itemsState().reduce((total, item) => total + item.quantity, 0),
  );

  add(item: CartItem): boolean {
    const items = this.itemsState();
    const existing = items.find((line) => line.itemId === item.itemId);
    if (existing) {
      const maximumReached = existing.quantity >= item.stock;
      this.updateItems(
        items.map((line) =>
          line.itemId === item.itemId
            ? { ...line, stock: item.stock, quantity: Math.min(line.quantity + 1, item.stock) }
            : line,
        ),
      );
      return maximumReached;
    }
    if (item.stock > 0) this.updateItems([...items, { ...item, quantity: 1 }]);
    return false;
  }

  setQuantity(itemId: number | string, quantity: number): void {
    const items = this.itemsState().map((item) =>
      item.itemId === itemId
        ? { ...item, quantity: Math.max(1, Math.min(Math.floor(quantity), item.stock)) }
        : item,
    );
    this.updateItems(items);
  }

  remove(itemId: number | string): void {
    this.updateItems(this.itemsState().filter((item) => item.itemId !== itemId));
  }

  setDiscount(percent: number): void {
    this.discountState.set(Math.max(0, Math.min(100, Math.floor(percent))));
  }

  clearDiscount(): void {
    this.discountState.set(0);
  }

  clear(): void {
    this.updateItems([]);
    this.clearDiscount();
  }

  private updateItems(items: CartItem[]): void {
    this.itemsState.set(items);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
  }

  private readStoredItems(): CartItem[] {
    try {
      const stored = JSON.parse(localStorage.getItem(STORAGE_KEY) ?? '[]') as CartItem[];
      return Array.isArray(stored)
        ? stored.filter(
            (item) =>
              item &&
              (typeof item.itemId === 'number' || typeof item.itemId === 'string') &&
              typeof item.name === 'string' &&
              Number.isFinite(item.unitPrice) &&
              Number.isFinite(item.quantity) &&
              Number.isFinite(item.stock),
          )
        : [];
    } catch {
      return [];
    }
  }
}
