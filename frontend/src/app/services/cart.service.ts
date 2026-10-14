import { computed, Injectable, signal } from '@angular/core';

export type SaleUnit = 'STRIP' | 'CAPSULE';

export interface CartItem {
  itemId: number | string;
  name: string;
  mfgName?: string | null;
  batchNumber?: string | null;
  expireDate?: string | null;
  scheduleH1?: boolean;
  quantity: number;
  stock: number;
  unitPrice: number;
  stripPrice: number;
  saleUnit: SaleUnit;
  capsulesPerStrip: number | null;
  stripStock: number;
  capsuleStock: number;
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
            ? { ...line, ...item, quantity: Math.min(line.quantity + 1, item.stock) }
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

  setSaleUnit(itemId: number | string, saleUnit: SaleUnit): void {
    const items = this.itemsState().map((item) => {
      if (item.itemId !== itemId) return item;
      const stock = saleUnit === 'CAPSULE' ? item.capsuleStock : item.stripStock;
      if (stock < 1) return item;
      const unitPrice = saleUnit === 'CAPSULE' && item.capsulesPerStrip
        ? Math.round((item.stripPrice / item.capsulesPerStrip + Number.EPSILON) * 100) / 100
        : item.stripPrice;
      return { ...item, saleUnit, unitPrice, stock, quantity: 1 };
    });
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
      const stored = JSON.parse(localStorage.getItem(STORAGE_KEY) ?? '[]') as Partial<CartItem>[];
      return Array.isArray(stored)
        ? stored.flatMap((item) => {
            if (
              !item ||
              (typeof item.itemId !== 'number' && typeof item.itemId !== 'string') ||
              typeof item.name !== 'string' ||
              !Number.isFinite(item.unitPrice) ||
              !Number.isFinite(item.quantity) ||
              !Number.isFinite(item.stock)
            ) return [];
            const saleUnit: SaleUnit = item.saleUnit === 'CAPSULE' ? 'CAPSULE' : 'STRIP';
            const stripPrice = Number.isFinite(item.stripPrice) ? item.stripPrice! : item.unitPrice!;
            const packSize = Number.isInteger(item.capsulesPerStrip) ? item.capsulesPerStrip! : null;
            const stripStock = Number.isFinite(item.stripStock) ? item.stripStock! : item.stock!;
            const capsuleStock = Number.isFinite(item.capsuleStock) ? item.capsuleStock! : 0;
            const unitPrice = saleUnit === 'CAPSULE' && packSize
              ? Math.round((stripPrice / packSize + Number.EPSILON) * 100) / 100
              : stripPrice;
            const stock = saleUnit === 'CAPSULE' ? capsuleStock : stripStock;
            if (stock < 1) return [];
            return [{
              itemId: item.itemId!,
              name: item.name,
              mfgName: item.mfgName ?? null,
              batchNumber: item.batchNumber ?? null,
              expireDate: item.expireDate ?? null,
              scheduleH1: item.scheduleH1 ?? false,
              quantity: Math.max(1, Math.min(Math.floor(item.quantity!), stock)),
              stock,
              unitPrice,
              stripPrice,
              saleUnit,
              capsulesPerStrip: packSize,
              stripStock,
              capsuleStock,
            }];
          })
        : [];
    } catch {
      return [];
    }
  }
}
