import { Injectable, Renderer2, RendererFactory2 } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly STORAGE_KEY = 'hrm-theme';
  private readonly DARK_CLASS = 'dark-theme';
  private renderer: Renderer2;

  private isDarkSubject = new BehaviorSubject<boolean>(false);
  isDark$ = this.isDarkSubject.asObservable();

  constructor(rendererFactory: RendererFactory2) {
    this.renderer = rendererFactory.createRenderer(null, null);
    const saved = localStorage.getItem(this.STORAGE_KEY);
    if (saved === 'dark') {
      this.setDark(true);
    }
  }

  get isDark(): boolean {
    return this.isDarkSubject.value;
  }

  toggle(): void {
    this.setDark(!this.isDark);
  }

  private setDark(dark: boolean): void {
    if (dark) {
      this.renderer.addClass(document.body, this.DARK_CLASS);
    } else {
      this.renderer.removeClass(document.body, this.DARK_CLASS);
    }
    localStorage.setItem(this.STORAGE_KEY, dark ? 'dark' : 'light');
    this.isDarkSubject.next(dark);
  }
}
