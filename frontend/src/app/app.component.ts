import { Component, OnDestroy, signal } from '@angular/core';
import { LucideX } from '@lucide/angular';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { FooterComponent } from './components/footer/footer.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [DashboardComponent, FooterComponent, LucideX],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent implements OnDestroy {
  readonly notice = signal('');
  private noticeTimer: number | undefined;

  showNotice(message: string): void {
    this.notice.set(message);
    if (this.noticeTimer !== undefined) window.clearTimeout(this.noticeTimer);
    this.noticeTimer = window.setTimeout(() => {
      this.notice.set('');
      this.noticeTimer = undefined;
    }, 2000);
  }

  dismissNotice(): void {
    this.notice.set('');
    if (this.noticeTimer !== undefined) {
      window.clearTimeout(this.noticeTimer);
      this.noticeTimer = undefined;
    }
  }

  ngOnDestroy(): void {
    if (this.noticeTimer !== undefined) window.clearTimeout(this.noticeTimer);
  }
}
