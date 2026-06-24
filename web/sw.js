const CACHE_NAME = 'sisempresa-v4';
const ASSETS = [
  'index.html',
  'style.css',
  'app.js',
  'manifest.json',
  'logo.png',
  'icon-512.png',
  'config.json'
];

// Instalação do Service Worker e Caching dos Arquivos Estáticos
self.addEventListener('install', (e) => {
  self.skipWaiting(); // Força a ativação do novo Service Worker imediatamente
  e.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      return cache.addAll(ASSETS);
    })
  );
});

// Ativação e Limpeza de Caches Antigos
self.addEventListener('activate', (e) => {
  e.waitUntil(
    caches.keys().then((keys) => {
      return Promise.all(
        keys.map((key) => {
          if (key !== CACHE_NAME) {
            return caches.delete(key);
          }
        })
      );
    }).then(() => {
      return self.clients.claim(); // Toma controle dos clientes abertos imediatamente
    })
  );
});

// Interceptação de Requisições (Estratégia Network-First)
self.addEventListener('fetch', (e) => {
  // Ignora requisições de API externas (Supabase, CDNs) para que os dados do banco venham sempre em tempo real
  if (e.request.url.includes('supabase.co') || 
      e.request.url.includes('jsdelivr') || 
      e.request.url.includes('cdnjs') ||
      e.request.url.includes('googleapis') ||
      e.request.url.includes('gstatic')) {
    return;
  }
  
  e.respondWith(
    fetch(e.request)
      .then((networkResponse) => {
        // Se a requisição foi bem sucedida, clona e atualiza o cache
        if (networkResponse && networkResponse.status === 200) {
          const responseClone = networkResponse.clone();
          caches.open(CACHE_NAME).then((cache) => {
            cache.put(e.request, responseClone);
          });
        }
        return networkResponse;
      })
      .catch(() => {
        // Se falhar (offline), busca no cache
        return caches.match(e.request);
      })
  );
});
