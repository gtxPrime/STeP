/**
 * STeP MoTA Unified Portal - Core Application Controller
 * Handles Navigation, Mode Switching, Phone Frame Toggle, Toast, and Offline PWA Sync
 */

window.STePApp = window.EduconApp = (function() {
  let currentMode = 'officer'; // default to Ministry Admin Panel
  let currentTab = 'timeline';
  let isPhoneFrameActive = false;

  function init() {
    registerServiceWorker();
    setupGlobalNavigation();
    setupModeToggle();
    setupPhoneFrameToggle();
    setupAccessibilityToggles();
    setupApiKeyModal();
    setupNetworkListeners();
    setupRenewalAction();

    // Initialize submodules
    if (window.EduconScanner) EduconScanner.init();
    if (window.EduconEligibility) EduconEligibility.init();
    if (window.EduconTimeline) EduconTimeline.init();
    if (window.EduconJago) EduconJago.init();
    if (window.EduconOfficer) EduconOfficer.init();

    // Default to Officer / Admin View
    const studentShell = document.getElementById('student-portal-shell');
    const officerShell = document.getElementById('officer-portal-shell');
    const studentModeBtn = document.getElementById('btn-mode-student');
    const officerModeBtn = document.getElementById('btn-mode-officer');

    if (studentShell && officerShell && studentModeBtn && officerModeBtn) {
      studentShell.classList.add('hidden');
      officerShell.classList.remove('hidden');
      studentModeBtn.classList.remove('active');
      officerModeBtn.classList.add('active');
      switchOfficerTab('heatmap');
    }
  }

  function registerServiceWorker() {
    if ('serviceWorker' in navigator) {
      window.addEventListener('load', () => {
        navigator.serviceWorker.register('./sw.js')
          .then(reg => console.log('[STeP PWA] Service Worker registered successfully', reg.scope))
          .catch(err => console.warn('[STeP PWA] Service Worker registration failed', err));
      });
    }
  }

  function setupGlobalNavigation() {
    // Student bottom / top nav tabs
    document.querySelectorAll('.nav-tab-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        const tab = e.currentTarget.dataset.tab;
        if (tab) switchTab(tab);
      });
    });

    // Officer nav tabs
    document.querySelectorAll('.officer-tab-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        const tab = e.currentTarget.dataset.tab;
        if (tab) switchOfficerTab(tab);
      });
    });
  }

  function switchTab(tabId) {
    currentTab = tabId;

    // Update active nav button
    document.querySelectorAll('.nav-tab-btn').forEach(btn => {
      btn.classList.toggle('active', btn.dataset.tab === tabId);
    });

    // Update tab view panels
    document.querySelectorAll('.tab-content-panel').forEach(panel => {
      panel.classList.toggle('hidden', panel.id !== `tab-panel-${tabId}`);
    });

    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function switchOfficerTab(tabId) {
    document.querySelectorAll('.officer-tab-btn').forEach(btn => {
      btn.classList.toggle('active', btn.dataset.tab === tabId);
    });

    document.querySelectorAll('.officer-panel').forEach(panel => {
      panel.classList.toggle('hidden', panel.id !== `officer-panel-${tabId}`);
    });
  }

  function setupModeToggle() {
    const studentModeBtn = document.getElementById('btn-mode-student');
    const officerModeBtn = document.getElementById('btn-mode-officer');
    const studentShell = document.getElementById('student-portal-shell');
    const officerShell = document.getElementById('officer-portal-shell');

    if (studentModeBtn && officerModeBtn && studentShell && officerShell) {
      studentModeBtn.addEventListener('click', () => {
        currentMode = 'student';
        studentModeBtn.classList.add('active');
        officerModeBtn.classList.remove('active');
        studentShell.classList.remove('hidden');
        officerShell.classList.add('hidden');
        switchTab(currentTab);
        showToast("Switched to Student Self-Service Experience 📱");
      });

      officerModeBtn.addEventListener('click', () => {
        currentMode = 'officer';
        officerModeBtn.classList.add('active');
        studentModeBtn.classList.remove('active');
        officerShell.classList.remove('hidden');
        studentShell.classList.add('hidden');
        if (window.EduconOfficer) EduconOfficer.init();
        showToast("Switched to Ministry / Nodal Officer Portal 🏛️");
      });
    }
  }

  function setupPhoneFrameToggle() {
    const toggleBtn = document.getElementById('btn-toggle-phone-frame');
    const appContainer = document.getElementById('app-main-viewport');

    if (toggleBtn && appContainer) {
      toggleBtn.addEventListener('click', () => {
        isPhoneFrameActive = !isPhoneFrameActive;
        appContainer.classList.toggle('phone-frame-mode', isPhoneFrameActive);
        toggleBtn.classList.toggle('active', isPhoneFrameActive);
        toggleBtn.innerHTML = isPhoneFrameActive 
          ? `📱 Phone Frame: <strong>ON</strong>` 
          : `🖥️ Fullscreen View`;
        showToast(isPhoneFrameActive ? "Viewing in Mobile Frame simulator" : "Viewing in Full Responsive Mode");
      });
    }
  }

  function setupAccessibilityToggles() {
    const contrastBtn = document.getElementById('btn-toggle-contrast');
    if (contrastBtn) {
      contrastBtn.addEventListener('click', () => {
        document.body.classList.toggle('high-contrast-mode');
        const active = document.body.classList.contains('high-contrast-mode');
        contrastBtn.classList.toggle('active', active);
        showToast(active ? "High-Contrast Accessibility Mode Enabled (WCAG AAA)" : "Standard Tribal Theme Restored");
      });
    }

    // Global Language Selector
    const langSelect = document.getElementById('global-language-select');
    if (langSelect) {
      langSelect.addEventListener('change', (e) => {
        const lang = e.target.value;
        const labels = {
          en: "English",
          hi: "हिन्दी (Hindi)",
          or: "ଓଡ଼ିଆ (Odia)",
          mr: "मराठी (Marathi)",
          te: "తెలుగు (Telugu)",
          ta: "தமிழ் (Tamil)"
        };
        showToast(`Interface switched to ${labels[lang] || lang}`);
      });
    }
  }

  function setupApiKeyModal() {
    const apiKeyBtn = document.getElementById('btn-config-api-key');
    const modal = document.getElementById('api-key-modal');
    const closeBtn = document.getElementById('btn-close-api-key-modal');
    const saveBtn = document.getElementById('btn-save-api-key');
    const input = document.getElementById('input-gemini-key');

    if (apiKeyBtn && modal) {
      apiKeyBtn.addEventListener('click', () => {
        if (input) input.value = EduconGemini.getApiKey();
        modal.classList.remove('hidden');
      });
    }

    if (closeBtn && modal) {
      closeBtn.addEventListener('click', () => modal.classList.add('hidden'));
    }

    if (saveBtn && input && modal) {
      saveBtn.addEventListener('click', () => {
        const val = input.value.trim();
        EduconGemini.setApiKey(val);
        modal.classList.add('hidden');
        showToast(val ? "✓ Gemini API Key saved! Live Gemini 1.5 Flash activated." : "Gemini API Key removed. Using smart offline simulation.");
      });
    }
  }

  function setupNetworkListeners() {
    const banner = document.getElementById('network-status-indicator');
    
    function updateNetworkStatus() {
      if (!banner) return;
      if (navigator.onLine) {
        banner.className = "network-banner online";
        banner.innerHTML = `<span class="status-indicator-dot online"></span> <strong>Online</strong> • Real-time DigiLocker & PFMS sync active`;
        setTimeout(() => banner.classList.add('faded'), 4000);
      } else {
        banner.className = "network-banner offline";
        banner.innerHTML = `<span class="status-indicator-dot offline"></span> <strong>Offline PWA Mode</strong> • Tribal village cache active. Changes will auto-sync on reconnect.`;
        banner.classList.remove('faded');
      }
    }

    window.addEventListener('online', updateNetworkStatus);
    window.addEventListener('offline', updateNetworkStatus);
    updateNetworkStatus();
  }

  function setupRenewalAction() {
    const renewBtn = document.getElementById('btn-one-tap-renewal');
    if (renewBtn) {
      renewBtn.addEventListener('click', () => {
        renewBtn.disabled = true;
        renewBtn.innerHTML = `<span class="spinner-ring"></span> Syncing APAAR Academic Progression...`;

        setTimeout(() => {
          renewBtn.disabled = false;
          renewBtn.innerHTML = `✓ Renewal Application Submitted!`;
          showToast("✓ 1-Tap Renewal Complete! Class 12 marksheet auto-fetched from APAAR ID 9842-1084-2026. Forwarded to Institute Verification.");
          switchTab('timeline');
        }, 1200);
      });
    }
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
    toast.innerHTML = message;
    container.appendChild(toast);

    setTimeout(() => {
      toast.classList.add('toast-fade-out');
      setTimeout(() => toast.remove(), 400);
    }, duration);
  }

  return {
    init,
    switchTab,
    switchOfficerTab,
    showToast
  };
})();

// Auto-boot on DOM ready
document.addEventListener('DOMContentLoaded', () => {
  STePApp.init();
});
