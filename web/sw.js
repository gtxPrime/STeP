// STeP MoTA Sovereign Portal - Cache Bust & Live Service Worker
const CACHE_NAME = 'step-mota-v3-admin';

self.addEventListener('install', (event) => {
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) => {
      return Promise.all(
        keys.map((key) => {
          console.log('[ServiceWorker] Purging cache:', key);
          return caches.delete(key);
        })
      );
    })
  );
  self.clients.claim();
});

self.addEventListener('fetch', (event) => {
  // Always fetch fresh from network for development / localhost
  event.respondWith(
    fetch(event.request).catch(() => caches.match(event.request))
  );
});

