import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

export interface ReceiptItem {
  itemId: number;
  itemName: string;
  mfgName: string | null;
  batchNumber: string | null;
  expireDate: string | null;
  scheduleH1: boolean;
  quantity: number;
  unitPrice: number;
  offerPrice: number;
  lineTotal: number;
  lineOfferTotal: number;
  lineCgst?: number | null;
  lineSgst?: number | null;
  saleUnit: 'STRIP' | 'CAPSULE';
  capsulesPerStrip: number;
}

export interface ReceiptData {
  orderId: number;
  createdAt: string;
  discountPercent: number;
  doctorName: string | null;
  patientName: string | null;
  patientPhone: string | null;
  totalQuantity: number;
  totalPrice: number;
  totalOfferPrice: number;
  totalCgst?: number | null;
  totalSgst?: number | null;
  grandTotal?: number | null;
  items: ReceiptItem[];
}

@Component({
  selector: 'app-receipt',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './receipt.component.html',
  styleUrl: './receipt.component.css',
})
export class ReceiptComponent {
  @Input({ required: true }) receipt!: ReceiptData;

  readonly money = new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
  });

  formatPatientPhone(phone: string | null): string {
    if (!phone) return '';

    const digits = phone.replace(/\D/g, '');
    if (digits.length === 10) return `+91-${digits}`;
    if (digits.length === 12 && digits.startsWith('91')) return `+91-${digits.slice(2)}`;
    return phone;
  }

  quantitySummary(): string {
    const totals = this.receipt.items.reduce<Record<string, number>>((summary, item) => {
      const unit = item.saleUnit ?? 'STRIP';
      summary[unit] = (summary[unit] ?? 0) + item.quantity;
      return summary;
    }, {});
    const summary = Object.entries(totals)
      .map(([unit, quantity]) => `${quantity} ${unit === 'CAPSULE' ? 'capsule(s)' : 'strip(s)'}`)
      .join(' + ');
    return `Total: ${summary}`;
  }

  lineCgst(item: ReceiptItem): number {
    return this.validAmount(item.lineCgst) ? item.lineCgst : this.taxAmount(item.lineOfferTotal);
  }

  lineSgst(item: ReceiptItem): number {
    return this.validAmount(item.lineSgst) ? item.lineSgst : this.taxAmount(item.lineOfferTotal);
  }

  totalCgst(): number {
    return this.validAmount(this.receipt.totalCgst)
      ? this.receipt.totalCgst
      : this.receipt.items.reduce((total, item) => total + this.lineCgst(item), 0);
  }

  totalSgst(): number {
    return this.validAmount(this.receipt.totalSgst)
      ? this.receipt.totalSgst
      : this.receipt.items.reduce((total, item) => total + this.lineSgst(item), 0);
  }

  grandTotal(): number {
    return this.validAmount(this.receipt.grandTotal)
      ? this.receipt.grandTotal
      : this.offerTotal() + this.totalCgst() + this.totalSgst();
  }

  private offerTotal(): number {
    return this.validAmount(this.receipt.totalOfferPrice)
      ? this.receipt.totalOfferPrice
      : this.receipt.items.reduce(
          (total, item) => total + (this.validAmount(item.lineOfferTotal) ? item.lineOfferTotal : 0),
          0,
        );
  }

  private taxAmount(amount: number): number {
    const taxableAmount = this.validAmount(amount) ? amount : 0;
    return Math.round((taxableAmount * 0.025 + Number.EPSILON) * 100) / 100;
  }

  private validAmount(amount: number | null | undefined): amount is number {
    return typeof amount === 'number' && Number.isFinite(amount);
  }
}
