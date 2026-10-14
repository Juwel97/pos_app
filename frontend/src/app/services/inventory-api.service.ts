import { Injectable } from '@angular/core';

export interface InventoryItem {
  id: number | string;
  name: string;
  mfgName?: string | null;
  serialNumber: string;
  batchNumber?: string | null;
  description?: string | null;
  price: number | string;
  quantity: number;
  capsulesPerStrip?: number | null;
  capsuleQuantity?: number | null;
  looseCapsules?: number;
  scheduleH1?: boolean;
  category?: string | null;
  expireDate?: string | null;
}

export interface ItemPageResponse {
  content?: InventoryItem[];
  page?: number;
  totalElements?: number;
  totalPages?: number;
  hasPrev?: boolean;
  hasNext?: boolean;
}

export interface ItemWriteRequest {
  name: string;
  mfgName: string;
  serialNumber: string;
  batchNumber: string;
  description: string;
  price: number;
  quantity: number;
  capsulesPerStrip: number;
  scheduleH1: boolean;
  category: string;
  expireDate: string;
}

export interface InventoryAnalysis {
  totalMedicineCount: number;
  totalInventoryValue: number;
  limitedStockCount: number;
}

@Injectable({ providedIn: 'root' })
export class InventoryApiService {
  async searchItems(
    term: string,
    page: number,
    size: number,
    lowStockOnly = false,
  ): Promise<ItemPageResponse> {
    const params = new URLSearchParams();
    if (term) params.set('q', term);
    params.set('page', String(Math.max(page, 0)));
    params.set('size', String(size));
    if (lowStockOnly) params.set('lowStockOnly', 'true');
    return (await this.request<ItemPageResponse>(`/item/search?${params}`)) ?? {};
  }

  async getAnalysis(): Promise<InventoryAnalysis> {
    const [totalMedicineCount, totalInventoryValue, limitedStockCount] = await Promise.all([
      this.request<number>('/analysis/total-count'),
      this.request<number>('/analysis/total-value'),
      this.request<number>('/analysis/limited-stock'),
    ]);
    return {
      totalMedicineCount: totalMedicineCount ?? 0,
      totalInventoryValue: totalInventoryValue ?? 0,
      limitedStockCount: limitedStockCount ?? 0,
    };
  }

  async addItem(item: ItemWriteRequest): Promise<void> {
    await this.request<InventoryItem>('/item/add', {
      method: 'POST',
      body: JSON.stringify(item),
    });
  }

  async updateItem(id: number | string, item: ItemWriteRequest): Promise<void> {
    await this.request<InventoryItem>(`/item/update/${id}`, {
      method: 'PUT',
      body: JSON.stringify(item),
    });
  }

  async deleteItem(id: number | string): Promise<void> {
    await this.request<void>(`/item/delete/${id}`, { method: 'DELETE' });
  }

  private async request<T>(url: string, options: RequestInit = {}): Promise<T | null> {
    const response = await fetch(url, {
      ...options,
      headers: { 'Content-Type': 'application/json', ...options.headers },
    });
    if (!response.ok) {
      const body = (await response.json().catch(() => ({}))) as { error?: string };
      throw new Error(body.error || `Request failed (${response.status})`);
    }
    return response.status === 204 ? null : ((await response.json()) as T);
  }
}
