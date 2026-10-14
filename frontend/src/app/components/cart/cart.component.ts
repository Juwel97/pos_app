import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  LucideArrowLeft,
  LucidePrinter,
  LucideShoppingCart,
  LucideTag,
  LucideTrash2,
  LucideX,
} from '@lucide/angular';
import { ReceiptComponent, ReceiptData } from '../receipt/receipt.component';
import { CartItem, CartService } from '../../services/cart.service';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    LucideArrowLeft,
    LucidePrinter,
    LucideShoppingCart,
    LucideTag,
    LucideTrash2,
    LucideX,
    ReceiptComponent,
  ],
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.css',
})
export class CartComponent {
  @Output() readonly back = new EventEmitter<void>();
  @Output() readonly notice = new EventEmitter<string>();
  @Output() readonly checkoutComplete = new EventEmitter<void>();

  readonly cart = inject(CartService);
  readonly money = new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' });
  discountDialogOpen = false;
  discountDraft = '';
  doctorName = '';
  patientName = '';
  patientPhone = '';
  busy = false;
  error = '';
  receipt: ReceiptData | null = null;

  get totalPrice(): number {
    return this.cart.items().reduce((total, item) => total + item.unitPrice * item.quantity, 0);
  }

  get totalOfferPrice(): number {
    return this.cart.items().reduce((total, item) => total + this.lineOfferTotal(item), 0);
  }

  get totalCgst(): number {
    return this.cart.items().reduce((total, item) => total + this.lineCgst(item), 0);
  }

  get totalSgst(): number {
    return this.cart.items().reduce((total, item) => total + this.lineSgst(item), 0);
  }

  get grandTotal(): number {
    return this.totalOfferPrice + this.totalCgst + this.totalSgst;
  }

  get quantitySummary(): string {
    const totals = this.cart.items().reduce<Record<string, number>>((summary, item) => {
      summary[item.saleUnit] = (summary[item.saleUnit] ?? 0) + item.quantity;
      return summary;
    }, {});
    return Object.entries(totals)
      .map(([unit, quantity]) => `${quantity} ${unit === 'CAPSULE' ? 'capsule(s)' : 'strip(s)/unit(s)'}`)
      .join(' + ');
  }

  lineTotal(item: CartItem): number {
    return item.unitPrice * item.quantity;
  }

  lineOfferPrice(item: CartItem): number {
    const offerPrice = item.unitPrice * (1 - this.cart.discountPercent() / 100);
    return Math.round((offerPrice + Number.EPSILON) * 100) / 100;
  }

  lineOfferTotal(item: CartItem): number {
    return this.lineOfferPrice(item) * item.quantity;
  }

  setQuantity(item: CartItem, quantity: number): void {
    this.cart.setQuantity(item.itemId, quantity);
    if (quantity > item.stock) {
      this.showMaximumQuantityNotice(item);
    }
  }

  increaseQuantity(item: CartItem): void {
    if (item.quantity >= item.stock) {
      this.showMaximumQuantityNotice(item);
      return;
    }
    this.setQuantity(item, item.quantity + 1);
  }

  private showMaximumQuantityNotice(item: CartItem): void {
    this.notice.emit(`You have reached the maximum quantity of ${item.stock} for ${item.name}.`);
  }

  lineCgst(item: CartItem): number {
    return this.taxAmount(this.lineOfferTotal(item));
  }

  lineSgst(item: CartItem): number {
    return this.taxAmount(this.lineOfferTotal(item));
  }

  private taxAmount(amount: number): number {
    return Math.round((amount * 0.025 + Number.EPSILON) * 100) / 100;
  }

  openDiscountDialog(): void {
    this.discountDraft = '';
    this.error = '';
    this.discountDialogOpen = true;
  }

  applyDiscount(): void {
    const discount = Number(this.discountDraft);
    if (!/^\d{1,3}$/.test(this.discountDraft) || discount < 0 || discount > 100) {
      this.error = 'Enter a whole number from 0 to 100.';
      return;
    }
    this.cart.setDiscount(discount);
    this.discountDialogOpen = false;
    this.error = '';
  }

  async checkout(): Promise<void> {
    if (this.busy || !this.cart.items().length) return;
    this.busy = true;
    this.error = '';
    const customerDetails = {
      doctorName: this.doctorName.trim() || null,
      patientName: this.patientName.trim() || null,
      patientPhone: this.patientPhone.trim() || null,
    };
    try {
      const response = await fetch('/orders/checkout', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          discountPercent: this.cart.discountPercent(),
          ...customerDetails,
          items: this.cart.items().map((item) => ({
            itemId: Number(item.itemId),
            quantity: item.quantity,
            saleUnit: item.saleUnit,
          })),
        }),
      });
      if (!response.ok) {
        const body = (await response.json().catch(() => ({}))) as { error?: string };
        throw new Error(body.error || `Checkout failed (${response.status})`);
      }
      const receipt = (await response.json()) as ReceiptData;
      this.receipt = {
        ...receipt,
        doctorName: receipt.doctorName ?? customerDetails.doctorName,
        patientName: receipt.patientName ?? customerDetails.patientName,
        patientPhone: receipt.patientPhone ?? customerDetails.patientPhone,
      };
      this.cart.clear();
      this.doctorName = '';
      this.patientName = '';
      this.patientPhone = '';
      this.checkoutComplete.emit();
    } catch (error) {
      this.error = error instanceof Error ? error.message : 'Checkout could not be completed.';
    } finally {
      this.busy = false;
    }
  }

  printReceipt(): void {
    window.print();
  }
}
