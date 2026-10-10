import { Component, OnDestroy } from '@angular/core';
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
  notice = '';
  private noticeTimer: number | undefined;

  showNotice(message: string): void {
    this.notice = message;
    if (this.noticeTimer !== undefined) window.clearTimeout(this.noticeTimer);
    this.noticeTimer = window.setTimeout(() => {
      this.notice = '';
      this.noticeTimer = undefined;
    }, 3600);
  }

  ngOnDestroy(): void {
    if (this.noticeTimer !== undefined) window.clearTimeout(this.noticeTimer);
  }
}
