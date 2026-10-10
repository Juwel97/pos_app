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
}
