import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LucideX } from '@lucide/angular';
import { InventoryItem } from '../../services/inventory-api.service';

export type ItemFormMode = 'add' | 'update';

export interface ItemFormData {
  name: string;
  serialNumber: string;
  batchNumber: string;
  description: string;
  price: string;
  quantity: string;
  category: string;
  expireDate: string;
}

const EMPTY_FORM: ItemFormData = {
  name: '',
  serialNumber: '',
  batchNumber: '',
  description: '',
  price: '',
  quantity: '',
  category: '',
  expireDate: '',
};

@Component({
  selector: 'app-item-form',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideX],
  templateUrl: './item-form.component.html',
  styleUrl: './item-form.component.css',
})
export class ItemFormComponent implements OnInit {
  @Input({ required: true }) mode!: ItemFormMode;
  @Input() item: InventoryItem | null = null;
  @Input() busy = false;

  @Output() readonly save = new EventEmitter<ItemFormData>();
  @Output() readonly cancel = new EventEmitter<void>();

  form: ItemFormData = { ...EMPTY_FORM };

  ngOnInit(): void {
    const item = this.item;
    this.form = item
      ? {
          name: item.name,
          serialNumber: item.serialNumber,
          batchNumber: item.batchNumber || '',
          description: item.description || '',
          price: String(item.price),
          quantity: String(item.quantity),
          category: item.category || '',
          expireDate: item.expireDate || '',
        }
      : { ...EMPTY_FORM };
  }

  submit(): void {
    this.save.emit({ ...this.form });
  }

  closeOnBackdrop(event: MouseEvent): void {
    if (event.target === event.currentTarget) this.cancel.emit();
  }
}
