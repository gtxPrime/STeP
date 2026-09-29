/**
 * STeP MoTA Sovereign Portal - Core Application Controller (Admin Suite)
 * Dedicated to Ministry of Tribal Affairs (MoTA) Officers & Sovereign Nodal Authorities
 */

window.STePApp = window.EduconApp = (function() {
  let currentTab = 'schemes';

  function init() {
    registerServiceWorker();
    setupAdminNavigation();
    setupAccessibilityToggles();
    setupNetworkListeners();

    // Initialize Admin Module
    if (window.EduconOfficer) {
      EduconOfficer.init();
    }

    // Default to Schemes tab
    switchTab('schemes');
  }

  function registerServiceWorker() {
    if ('serviceWorker' in navigator) {
      if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
        // On localhost, ensure we unregister stale workers and wipe old caches
        navigator.serviceWorker.getRegistrations().then(regs => {
          for (let reg of regs) {
            reg.unregister();
          }
        });
        if ('caches' in window) {
          caches.keys().then(keys => {
            keys.forEach(k => caches.delete(k));
          });
        }
        return;
      }
      window.addEventListener('load', () => {
        navigator.serviceWorker.register('./sw.js')
          .then(reg => console.log('[STeP PWA] Service Worker registered', reg.scope))
          .catch(err => console.warn('[STeP PWA] Service Worker failed', err));
      });
    }
  }

  function setupAdminNavigation() {
    document.querySelectorAll('.admin-nav-tab-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        const tab = e.currentTarget.dataset.tab;
        if (tab) switchTab(tab);
      });
    });
  }

  function switchTab(tabId) {
    currentTab = tabId;

    document.querySelectorAll('.admin-nav-tab-btn').forEach(btn => {
      btn.classList.toggle('active', btn.dataset.tab === tabId);
    });

    document.querySelectorAll('.admin-panel').forEach(panel => {
      panel.classList.toggle('hidden', panel.id !== `admin-panel-${tabId}`);
    });

    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function setupAccessibilityToggles() {
    const contrastBtn = document.getElementById('btn-toggle-contrast');
    if (contrastBtn) {
      contrastBtn.addEventListener('click', () => {
        document.body.classList.toggle('high-contrast-mode');
        const isActive = document.body.classList.contains('high-contrast-mode');
        contrastBtn.classList.toggle('active', isActive);
        showToast(isActive ? "WCAG AAA High-Contrast Mode Activated" : "Standard Sovereign Color Palette Restored");
      });
    }
  }

  function setupNetworkListeners() {
    const indicator = document.getElementById('network-status-indicator');
    if (!indicator) return;

    window.addEventListener('online', () => {
      indicator.className = 'network-banner online';
      indicator.innerHTML = '<span class="status-indicator-dot online"></span><span><strong>Live Synced</strong> • NeGD DigiLocker Sandbox & PFMS Live</span>';
      setTimeout(() => indicator.classList.add('faded'), 3500);
    });

    window.addEventListener('offline', () => {
      indicator.className = 'network-banner offline';
      indicator.innerHTML = '<span class="status-indicator-dot offline"></span><span><strong>Offline Mode</strong> • Changes queued locally for sync</span>';
    });

    setTimeout(() => {
      if (navigator.onLine && indicator) indicator.classList.add('faded');
    }, 3000);
  }

  function showToast(message, duration = 3500) {
    let container = document.getElementById('toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toast-container';
      container.className = 'toast-container';
      document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = 'toast-message';
    toast.textContent = message;
    container.appendChild(toast);

    setTimeout(() => {
      toast.classList.add('toast-fade-out');
      setTimeout(() => toast.remove(), 400);
    }, duration);
  }

  return {
    init,
    switchTab,
    showToast
  };
})();

document.addEventListener('DOMContentLoaded', () => {
  window.EduconApp.init();
});
