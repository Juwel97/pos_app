import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

export interface ReceiptItem {
  itemId: number;
  itemName: string;
  quantity: number;
  unitPrice: number;
  offerPrice: number;
  lineTotal: number;
  lineOfferTotal: number;
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
}
