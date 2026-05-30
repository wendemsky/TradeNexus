import { computed, Injectable, signal } from '@angular/core';
import { Price } from '../shared/models/price.models';
import { authStore } from './auth.store';

@Injectable({ providedIn: 'root' })
export class PriceStore {
  private readonly _prices = signal<Map<string, Price>>(new Map());
  private ws: WebSocket | null = null;
  private reconnectDelay = 1000;
  private reconnectTimer?: ReturnType<typeof setTimeout>;
  private currentWsUrl = '';

  readonly prices    = this._prices.asReadonly();
  readonly priceList = computed(() => Array.from(this._prices().values()));

  connect(wsUrl: string, token: string): void {
    this.currentWsUrl = wsUrl;
    this.openConnection(token);
  }

  reconnect(newToken: string): void {
    this.closeConnection();
    this.reconnectDelay = 1000;
    this.openConnection(newToken);
  }

  disconnect(): void {
    this.closeConnection();
    this.reconnectDelay = 1000;
  }

  private openConnection(token: string): void {
    this.ws = new WebSocket(this.currentWsUrl);

    this.ws.onopen = () => {
      this.ws!.send(JSON.stringify({ type: 'AUTH', token }));
    };

    this.ws.onmessage = (event) => {
      const frame = JSON.parse(event.data as string) as {
        type: string;
        prices?: Price[];
      };

      if (frame.type === 'PRICE_SNAPSHOT' || frame.type === 'PRICE_UPDATE') {
        this._prices.update(map => {
          const next = new Map(map);
          (frame.prices ?? []).forEach(p => next.set(p.instrumentId, p));
          return next;
        });
        if (frame.type === 'PRICE_SNAPSHOT') {
          this.reconnectDelay = 1000;
        }
      }

      if (frame.type === 'PING') {
        this.ws?.send(JSON.stringify({ type: 'PONG' }));
      }

      if (frame.type === 'AUTH_ERROR') {
        this.closeConnection();
      }
    };

    this.ws.onclose = () => {
      const freshToken = authStore.profile()?.token;
      if (freshToken) {
        this.reconnectTimer = setTimeout(() => {
          this.openConnection(freshToken);
        }, this.reconnectDelay);
        this.reconnectDelay = Math.min(this.reconnectDelay * 2, 30_000);
      }
    };

    this.ws.onerror = () => {
      this.ws?.close();
    };
  }

  private closeConnection(): void {
    clearTimeout(this.reconnectTimer);
    if (this.ws) {
      this.ws.onclose = null;
      this.ws.close();
      this.ws = null;
    }
  }
}
