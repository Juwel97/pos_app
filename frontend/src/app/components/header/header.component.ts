import { Component, EventEmitter, Output, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LucideArchive, LucideSearch, LucideShoppingCart, LucideX } from '@lucide/angular';
import { CartService } from '../../services/cart.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [FormsModule, LucideArchive, LucideSearch, LucideShoppingCart, LucideX],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent {
  @Output() readonly search = new EventEmitter<string>();
  @Output() readonly clearSearch = new EventEmitter<void>();
  @Output() readonly cartClick = new EventEmitter<void>();

  readonly cartCount = inject(CartService).totalQuantity;

  searchText = '';

  submitSearch(event: SubmitEvent): void {
    event.preventDefault();
    this.search.emit(this.searchText.trim());
  }

  clearSearchText(): void {
    this.searchText = '';
    this.clearSearch.emit();
  }
}
