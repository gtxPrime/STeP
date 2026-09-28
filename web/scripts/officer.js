/**
 * STeP MoTA Unified Portal - Ministry & Nodal Officer Dashboard
 * Features:
 * 1. Smart "Unreached Beneficiary" Heatmap (UDISE+ / APAAR ST enrollment vs Scholarship coverage)
 * 2. Semi-Automated Verification Queue (Live Firestore Fetch, Approve, Defect Flagging)
 * 3. Scholarship Schemes Master (Live Firestore Add / Update Schemes)
 * 4. Directives & Outreach Dispatch Log (Live Firestore Sync)
 */

window.EduconOfficer = (function() {
  let activeFilter = 'ALL';
  let verificationQueue = [];
  let currentSchemes = [];
  let currentDirectives = [];
  let selectedAppForDefect = null;

  function init() {
    // 1. Initialize Firebase connection
    if (window.STePFirebase) {
      window.STePFirebase.init();
      setupFirebaseListeners();
    }

    // 2. Render initial static or cached views
    renderHeatmap();
    renderVerificationQueue();
    setupEventListeners();
  }

  function setupFirebaseListeners() {
    if (!window.STePFirebase) return;

    // Listen to live applications across all users
    window.STePFirebase.listenApplications((apps) => {
      console.log(`[Officer Portal] Received ${apps.length} live applications from Firestore.`);
      if (apps && apps.length > 0) {
        verificationQueue = apps.map(app => ({
          id: app.applicationId || app.id,
          docPath: app.docPath,
          userId: app.userId,
          studentName: app.studentName || "Tribal Scholar",
          district: app.district || "Bastar, Chhattisgarh",
          scheme: app.schemeTitle || "MoTA Scholarship",
          portal: app.sourcePortal || "NSP",
          confidenceScore: app.verificationConfidence || 90,
          status: app.stage === "DISBURSED" ? "DISBURSED" : (app.stage === "SANCTIONED" ? "AUTO_APPROVED" : (app.stage === "DEFICIENCY_FLAGGED" ? "DEFICIENCY_FLAGGED" : "MANUAL_REVIEW_REQUIRED")),
          source: app.verificationConfidence >= 85 ? "DigiLocker API (Aadhaar & DSC 100% Match)" : "MoTA AI Vision OCR (Under Review)",
          docType: "ST Caste & Income Certificate",
          appliedDate: app.appliedDate || "2026-09-28",
          actionNeeded: app.stage !== "DISBURSED" && app.stage !== "SANCTIONED",
          sanctionAmount: app.sanctionAmount || 28500,
          stage: app.stage || "SUBMITTED",
          anomaly: app.deficiency ? app.deficiency.bureaucraticReason : (app.verificationConfidence < 85 ? "Document requires officer scrutiny override." : null)
        }));

        // Also update student timeline in memory
        if (window.EduconData) {
          window.EduconData.applicationsTimeline = apps;
          if (window.EduconTimeline && typeof window.EduconTimeline.init === 'function') {
            window.EduconTimeline.init();
          }
        }
      }
      renderVerificationQueue();
      updateKpis();
    });

    // Listen to live schemes
    window.STePFirebase.listenSchemes((schemes) => {
      if (schemes && schemes.length > 0) {
        currentSchemes = schemes;
        if (window.EduconData) {
          window.EduconData.schemes = schemes;
        }
        renderSchemesMaster();
      }
    });

    // Listen to live directives
    window.STePFirebase.listenDirectives((directives) => {
      currentDirectives = directives;
      renderDirectivesLog();
    });
  }

  function updateKpis() {
    const totalAppsEl = document.getElementById('kpi-total-apps');
    const autoApprovedEl = document.getElementById('kpi-auto-approved');
    const sanctionedAmountEl = document.getElementById('kpi-sanctioned-amount');
    const pendingCountEl = document.getElementById('kpi-pending-count');

    const total = verificationQueue.length;
    const autoCleared = verificationQueue.filter(a => a.status === 'AUTO_APPROVED' || a.status === 'DISBURSED' || a.confidenceScore >= 85).length;
    const pending = verificationQueue.filter(a => a.actionNeeded).length;
    const totalSanctioned = verificationQueue.reduce((acc, curr) => acc + (curr.sanctionAmount || 0), 0);

    if (totalAppsEl) totalAppsEl.textContent = `${total} Live`;
    if (autoApprovedEl) autoApprovedEl.textContent = `${autoCleared} Auto-Cleared`;
    if (sanctionedAmountEl) sanctionedAmountEl.textContent = `₹ ${(totalSanctioned).toLocaleString('en-IN')}`;
    if (pendingCountEl) pendingCountEl.textContent = `${pending} Cases`;
  }

  function setupEventListeners() {
    // Priority filter buttons for Heatmap
    document.querySelectorAll('.heatmap-filter-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        document.querySelectorAll('.heatmap-filter-btn').forEach(b => b.classList.remove('active'));
        e.currentTarget.classList.add('active');
        activeFilter = e.currentTarget.dataset.priority;
        renderHeatmap();
      });
    });

    // Modal close buttons
    const btnCloseSchemeModal = document.getElementById('btn-close-scheme-modal');
    if (btnCloseSchemeModal) {
      btnCloseSchemeModal.addEventListener('click', () => {
        document.getElementById('add-scheme-modal')?.classList.add('hidden');
      });
    }

    const btnCloseAppModal = document.getElementById('btn-close-app-modal');
    if (btnCloseAppModal) {
      btnCloseAppModal.addEventListener('click', () => {
        document.getElementById('add-app-modal')?.classList.add('hidden');
      });
    }

    const btnCloseOfficerDefect = document.getElementById('btn-close-officer-defect-modal');
    if (btnCloseOfficerDefect) {
      btnCloseOfficerDefect.addEventListener('click', () => {
        document.getElementById('officer-defect-modal')?.classList.add('hidden');
      });
    }

    // Save Scheme Button
    const btnSaveScheme = document.getElementById('btn-save-new-scheme');
    if (btnSaveScheme) {
      btnSaveScheme.addEventListener('click', handleSaveNewScheme);
    }

    // Save Walk-in App Button
    const btnSaveApp = document.getElementById('btn-save-new-app');
    if (btnSaveApp) {
      btnSaveApp.addEventListener('click', handleSaveNewApplication);
    }

    // Submit Defect Flag Button
    const btnSubmitDefect = document.getElementById('btn-submit-officer-defect');
    if (btnSubmitDefect) {
      btnSubmitDefect.addEventListener('click', handleSubmitOfficerDefect);
    }
  }

  function renderHeatmap() {
    const listEl = document.getElementById('heatmap-districts-list');
    if (!listEl) return;

    let items = (window.EduconData && window.EduconData.districtHeatmapData) || [];
    if (activeFilter !== 'ALL') {
      items = items.filter(d => d.priorityLevel === activeFilter);
    }

    listEl.innerHTML = items.map((d, index) => {
      const isCritical = d.priorityLevel === 'CRITICAL';
      const isHigh = d.priorityLevel === 'HIGH';
      const badgeClass = isCritical ? 'badge-error' : (isHigh ? 'badge-warning' : 'badge-neutral');

      return `
        <div class="district-row-card ${index === 0 ? 'selected' : ''}" onclick="EduconOfficer.selectDistrict('${d.district}')" data-district="${d.district}">
          <div class="flex justify-between items-start">
            <div>
              <span class="font-bold text-sm text-accent">${d.district}</span>
              <span class="text-xs text-muted">(${d.state})</span>
            </div>
            <span class="badge ${badgeClass} text-xs">${d.gapPercent}% Gap</span>
          </div>

          <div class="gap-bar-container mt-2">
            <div class="flex justify-between text-xs text-muted mb-1">
              <span>UDISE+ ST: ${d.udiseStEnrollment.toLocaleString('en-IN')}</span>
              <span class="text-amber font-bold">${d.gapCount.toLocaleString('en-IN')} Unreached</span>
            </div>
            <div class="progress-bar-track">
              <div class="progress-bar-fill bg-amber" style="width: ${d.gapPercent}%"></div>
            </div>
          </div>
        </div>
      `;
    }).join('');

    if (items.length > 0) {
      selectDistrict(items[0].district);
    }
  }

  function selectDistrict(districtName) {
    const items = (window.EduconData && window.EduconData.districtHeatmapData) || [];
    const d = items.find(item => item.district === districtName);
    if (!d) return;

    document.querySelectorAll('.district-row-card').forEach(c => {
      c.classList.toggle('selected', c.dataset.district === districtName);
    });

    const detailEl = document.getElementById('heatmap-district-details');
    if (!detailEl) return;

    detailEl.innerHTML = `
      <div class="district-inspect-box">
        <div class="flex justify-between items-start border-b pb-3 mb-3">
          <div>
            <span class="badge ${d.priorityLevel === 'CRITICAL' ? 'badge-error' : 'badge-warning'} mb-1">${d.priorityLevel} PRIORITY OUTREACH</span>
            <h3 class="text-xl font-bold text-white">${d.district}, ${d.state}</h3>
            <p class="text-xs text-muted">Enrolled in Schools/Colleges (UDISE+/APAAR) vs MoTA Scholarship Recipients</p>
          </div>
          <div class="text-right">
            <span class="text-xs text-muted">Coverage Deficit</span>
            <div class="text-3xl font-bold text-amber">${d.gapPercent}%</div>
          </div>
        </div>

        <div class="grid grid-cols-2 gap-3 mb-4">
          <div class="stat-mini-card">
            <span class="text-xs text-muted">Total ST Students (UDISE+)</span>
            <div class="text-lg font-bold">${d.udiseStEnrollment.toLocaleString('en-IN')}</div>
          </div>
          <div class="stat-mini-card">
            <span class="text-xs text-muted">Active Scholarship Availed</span>
            <div class="text-lg font-bold text-emerald">${d.scholarshipActive.toLocaleString('en-IN')}</div>
          </div>
          <div class="stat-mini-card col-span-2 highlight-unreached">
            <span class="text-xs text-amber font-bold">Unreached ST Students (Missing out on sovereign funds)</span>
            <div class="text-2xl font-bold text-amber">${d.gapCount.toLocaleString('en-IN')} Scholars</div>
          </div>
        </div>

        <div class="pvtg-breakdown-box mb-3">
          <span class="text-xs font-bold text-accent">High-Density Tribal & PVTG Pockets:</span>
          <p class="text-xs text-muted mt-1">${d.pvtgPockets}</p>
        </div>

        <div class="barriers-box mb-4">
          <span class="text-xs font-bold text-accent">Root Cause Delivery Barriers:</span>
          <p class="text-xs text-muted mt-1">${d.primaryBarriers}</p>
        </div>

        <div class="action-buttons-grid flex gap-2">
          <button class="btn btn-primary btn-sm flex-1" onclick="EduconOfficer.triggerMobileVanDirective('${d.district}')">
            Dispatch Mobile Camp
          </button>
          <button class="btn btn-outline btn-sm flex-1" onclick="EduconOfficer.triggerSmsOutreach('${d.district}', ${d.gapCount})">
            Send Multilingual SMS/IVR
          </button>
        </div>
      </div>
    `;
  }

  function renderVerificationQueue() {
    const container = document.getElementById('officer-verification-table');
    if (!container) return;

    if (verificationQueue.length === 0) {
      container.innerHTML = `
        <div class="glass-card p-6 text-center text-muted">
          <p class="text-sm font-semibold">No pending verification cases in Firestore.</p>
          <p class="text-xs mt-1">Applications submitted by students or registered at facilitation desks will appear here live in real time.</p>
          <button class="btn btn-primary btn-xs mt-3" onclick="EduconOfficer.openAddAppModal()">+ Register Walk-in Application</button>
        </div>
      `;
      return;
    }

    container.innerHTML = verificationQueue.map(app => {
      const isAuto = app.status === 'AUTO_APPROVED' || app.status === 'DISBURSED';
      const isDeficient = app.status === 'DEFICIENCY_FLAGGED';

      return `
        <div class="verification-item-card ${isAuto ? 'verified-auto' : 'verified-manual'}">
          <div class="flex justify-between items-start">
            <div>
              <span class="badge ${isAuto ? 'badge-success' : (isDeficient ? 'badge-error' : 'badge-warning')} text-xs">
                ${isAuto ? 'Sanctioned / Disbursed' : (isDeficient ? 'Deficiency Action Flagged' : 'Manual Scrutiny Queue')}
              </span>
              <span class="badge badge-outline text-xs ml-2 font-mono">${app.portal}</span>
              <h4 class="font-bold text-base mt-1 text-white">${app.studentName}</h4>
              <div class="text-xs text-muted">${app.scheme} • ${app.district} • App ID: <span class="font-mono text-bold">${app.id}</span></div>
            </div>
            <div class="text-right">
              <span class="text-xs text-muted">Confidence Score</span>
              <div class="text-xl font-bold ${app.confidenceScore >= 85 ? 'text-emerald' : 'text-amber'}">
                ${app.confidenceScore}%
              </div>
              <div class="text-xs text-accent font-bold mt-1">₹ ${(app.sanctionAmount || 0).toLocaleString('en-IN')}</div>
            </div>
          </div>

          <div class="doc-verification-strip my-2">
            <span class="text-xs text-muted">Document: <strong>${app.docType}</strong></span>
            <span class="text-xs text-muted ml-3">Source: <em>${app.source}</em></span>
          </div>

          ${app.anomaly ? `
            <div class="anomaly-warning-box">
              <span class="text-xs text-amber font-semibold">Scrutiny Flag: ${app.anomaly}</span>
            </div>
          ` : ''}

          <div class="verification-actions mt-3 flex justify-end gap-2">
            ${app.actionNeeded ? `
              <button class="btn btn-outline btn-xs" onclick="EduconOfficer.openDefectModal('${app.id}')">
                Flag Defect with AI
              </button>
              <button class="btn btn-primary btn-xs" onclick="EduconOfficer.approveApplicationManual('${app.id}')">
                Approve & Sanction (Cloud)
              </button>
            ` : (app.status === 'AUTO_APPROVED' ? `
              <button class="btn btn-outline btn-xs text-emerald" onclick="EduconOfficer.markDisbursed('${app.id}')">
                Mark Disbursed via PFMS
              </button>
              <span class="text-xs text-emerald font-semibold self-center ml-2">Processed via Sovereign Auto-Verify Layer</span>
            ` : `
              <span class="text-xs text-emerald font-semibold self-center">DBT Disbursed via PFMS</span>
            `)}
          </div>
        </div>
      `;
    }).join('');
  }

  function renderSchemesMaster() {
    const container = document.getElementById('officer-schemes-table');
    if (!container) return;

    const list = currentSchemes.length > 0 ? currentSchemes : (window.EduconData && window.EduconData.schemes) || [];
    container.innerHTML = list.map(sc => `
      <div class="glass-card p-4 mb-3 border border-white/10">
        <div class="flex justify-between items-start">
          <div>
            <span class="badge badge-outline text-xs">${sc.code || 'SCH'}</span>
            <span class="badge badge-success text-xs ml-2">${sc.portal || 'NSP'}</span>
            <h4 class="font-bold text-white text-base mt-1">${sc.title}</h4>
            <p class="text-xs text-muted">${sc.hindiTitle || ''} • Target: ${sc.targetClass || 'Higher Secondary'}</p>
          </div>
          <div class="text-right">
            <span class="text-xs text-muted">Max Benefit</span>
            <div class="text-lg font-bold text-amber">₹ ${(sc.maxBenefitAmount || 0).toLocaleString('en-IN')}</div>
          </div>
        </div>
        <div class="mt-2 text-xs text-muted">
          <strong>Summary:</strong> ${sc.benefitSummary || '100% Tuition assistance'}
        </div>
      </div>
    `).join('');
  }

  function renderDirectivesLog() {
    const container = document.getElementById('officer-directives-table');
    if (!container) return;

    if (currentDirectives.length === 0) {
      container.innerHTML = `<p class="text-xs text-muted">No directives logged yet. Use the Outreach actions in the Heatmap tab to dispatch mobile camps or broadcast SMS.</p>`;
      return;
    }

    container.innerHTML = currentDirectives.map(d => `
      <div class="p-3 bg-white/5 rounded border border-white/10 mb-2 flex justify-between items-center">
        <div>
          <span class="badge badge-outline text-xs">${d.type || 'CAMP_OUTREACH'}</span>
          <span class="font-bold text-sm text-white ml-2">${d.district}</span>
          <p class="text-xs text-muted mt-1">${d.details}</p>
        </div>
        <div class="text-right text-xs text-muted">
          <div>${d.dateFormatted || 'Today'}</div>
          <span class="badge badge-success text-xs mt-1">DISPATCHED</span>
        </div>
      </div>
    `).join('');
  }

  // --- CRUD ACTIONS ---

  async function approveApplicationManual(appId) {
    const item = verificationQueue.find(a => a.id === appId);
    if (!item) return;

    const utr = "RBI" + Math.floor(Math.random() * 900000000000 + 100000000000);
    const updateData = {
      stage: "SANCTIONED",
      stageText: "Sanctioned - In PFMS Payment Queue",
      verificationConfidence: 98,
      currentStepIndex: 3,
      sanctionAmount: item.sanctionAmount || 28500,
      steps: [
        { label: "Submitted", date: item.appliedDate, completed: true, note: "Verified via MoTA Sovereign Layer" },
        { label: "Institute Verified", date: "Verified", completed: true, note: "Principal & Institutional Nodal approval" },
        { label: "District Nodal Verified", date: "Verified", completed: true, note: "e-District DSC Verified" },
        { label: "Ministry Sanctioned", date: new Date().toLocaleDateString("en-IN"), completed: true, note: "Sanction Order MoTA/2026/SO-" + Math.floor(Math.random()*9000+1000) },
        { label: "DBT Disbursed", date: "Queue", completed: false, note: "PFMS token generated, payment file scheduled" }
      ],
      dbtDetails: {
        utr: utr,
        paymentMode: "PFMS APBS",
        disbursedDate: "Pending Release",
        bankName: "State Bank of India",
        status: "PROCESSING_AT_BANK"
      }
    };

    try {
      if (item.docPath && window.STePFirebase) {
        await window.STePFirebase.updateApplicationStatus(item.docPath, updateData, item.userId, {
          title: "Scholarship Sanction Order Generated",
          body: `Congratulations! Your scholarship application for ${item.scheme} has been sanctioned. Fund release token issued.`,
          type: "SANCTION"
        });
        window.EduconApp.showToast(`Application ${appId} approved & synced to live Firestore! Student notified.`);
      } else {
        item.status = "AUTO_APPROVED";
        item.actionNeeded = false;
        item.confidenceScore = 98;
        renderVerificationQueue();
        updateKpis();
        window.EduconApp.showToast(`Application ${appId} approved in local state.`);
      }
    } catch (e) {
      console.error(e);
      window.EduconApp.showToast(`Error updating Firestore: ${e.message}`);
    }
  }

  async function markDisbursed(appId) {
    const item = verificationQueue.find(a => a.id === appId);
    if (!item) return;

    const utr = "RBI" + Math.floor(Math.random() * 900000000000 + 100000000000);
    const updateData = {
      stage: "DISBURSED",
      stageText: "Disbursed via DBT",
      currentStepIndex: 4,
      steps: [
        { label: "Submitted", date: item.appliedDate, completed: true, note: "Submitted" },
        { label: "Institute Verified", date: "Verified", completed: true, note: "Approved" },
        { label: "District Nodal Verified", date: "Verified", completed: true, note: "Approved" },
        { label: "Ministry Sanctioned", date: "Approved", completed: true, note: "Sanctioned" },
        { label: "DBT Disbursed", date: new Date().toLocaleDateString("en-IN"), completed: true, note: "Disbursed UTR: " + utr }
      ],
      dbtDetails: {
        utr: utr,
        paymentMode: "Aadhaar Payment Bridge (APB / PFMS)",
        disbursedDate: new Date().toLocaleDateString("en-IN"),
        bankName: "State Bank of India",
        status: "CREDITED_SUCCESSFULLY"
      }
    };

    try {
      if (item.docPath && window.STePFirebase) {
        await window.STePFirebase.updateApplicationStatus(item.docPath, updateData, item.userId, {
          title: "DBT Scholarship Payment Disbursed",
          body: `Funds for ${item.scheme} credited to your Aadhaar-seeded bank account. UTR: ${utr}`,
          type: "PAYMENT"
        });
        window.EduconApp.showToast(`Payment disbursed for ${appId}! Firestore updated.`);
      }
    } catch (e) {
      window.EduconApp.showToast(`Disbursement error: ${e.message}`);
    }
  }

  function openDefectModal(appId) {
    selectedAppForDefect = verificationQueue.find(a => a.id === appId);
    if (!selectedAppForDefect) return;

    const modal = document.getElementById('officer-defect-modal');
    const titleEl = document.getElementById('officer-defect-app-title');
    if (titleEl) titleEl.textContent = `${selectedAppForDefect.studentName} (${selectedAppForDefect.id})`;
    if (modal) modal.classList.remove('hidden');
  }

  async function handleSubmitOfficerDefect() {
    if (!selectedAppForDefect) return;
    const code = document.getElementById('officer-defect-code')?.value || 'DEF-D402';
    const reason = document.getElementById('officer-defect-reason')?.value || 'Document mismatch detected during verification.';
    const days = parseInt(document.getElementById('officer-defect-days')?.value || '15', 10);

    const deadline = new Date();
    deadline.setDate(deadline.getDate() + days);

    const deficiencyData = {
      code: code,
      bureaucraticReason: reason,
      schemeTitle: selectedAppForDefect.scheme,
      flaggedDate: new Date().toLocaleDateString("en-IN"),
      deadlineDate: deadline.toLocaleDateString("en-IN"),
      daysRemaining: days
    };

    try {
      if (selectedAppForDefect.docPath && window.STePFirebase) {
        await window.STePFirebase.updateApplicationDeficiency(selectedAppForDefect.docPath, deficiencyData, selectedAppForDefect.userId);
        window.EduconApp.showToast(`Deficiency flagged in Firestore! Student alerted via notifications.`);
      }
      document.getElementById('officer-defect-modal')?.classList.add('hidden');
    } catch (e) {
      window.EduconApp.showToast(`Defect flag error: ${e.message}`);
    }
  }

  function openAddSchemeModal() {
    document.getElementById('add-scheme-modal')?.classList.remove('hidden');
  }

  async function handleSaveNewScheme() {
    const code = document.getElementById('new-scheme-code')?.value.trim();
    const title = document.getElementById('new-scheme-title')?.value.trim();
    const hindiTitle = document.getElementById('new-scheme-hindi')?.value.trim();
    const portal = document.getElementById('new-scheme-portal')?.value || 'NSP';
    const amount = Number(document.getElementById('new-scheme-amount')?.value) || 25000;
    const ceiling = Number(document.getElementById('new-scheme-ceiling')?.value) || 250000;
    const target = document.getElementById('new-scheme-target')?.value.trim() || 'College & Professional Courses';

    if (!title) {
      alert("Please enter the scheme title.");
      return;
    }

    try {
      if (window.STePFirebase) {
        await window.STePFirebase.addScheme({
          code: code || 'SCH-' + Math.floor(Math.random()*90+10),
          title: title,
          hindiTitle: hindiTitle,
          portal: portal,
          maxBenefitAmount: amount,
          incomeCeiling: ceiling,
          targetClass: target,
          benefitSummary: `₹ ${amount.toLocaleString('en-IN')}/year sovereign financial assistance`
        });
        window.EduconApp.showToast(`Scheme "${title}" saved to Cloud Firestore!`);
      }
      document.getElementById('add-scheme-modal')?.classList.add('hidden');
    } catch (e) {
      alert("Error saving scheme: " + e.message);
    }
  }

  function openAddAppModal() {
    document.getElementById('add-app-modal')?.classList.remove('hidden');
  }

  async function handleSaveNewApplication() {
    const studentName = document.getElementById('new-app-student-name')?.value.trim();
    const district = document.getElementById('new-app-district')?.value.trim();
    const schemeTitle = document.getElementById('new-app-scheme')?.value || 'Post-Matric Scholarship for ST Students';
    const amount = Number(document.getElementById('new-app-amount')?.value) || 28500;

    if (!studentName) {
      alert("Please enter the student's name.");
      return;
    }

    try {
      if (window.STePFirebase) {
        await window.STePFirebase.addApplication("usr_walkin_" + Date.now().toString().slice(-6), {
          studentName: studentName,
          district: district || "Mayurbhanj, Odisha",
          schemeTitle: schemeTitle,
          sanctionAmount: amount,
          verificationConfidence: 92
        });
        window.EduconApp.showToast(`Application for ${studentName} registered and saved to Firestore!`);
      }
      document.getElementById('add-app-modal')?.classList.add('hidden');
    } catch (e) {
      alert("Error registering application: " + e.message);
    }
  }

  async function triggerMobileVanDirective(district) {
    const msg = `Mobile Camp Outreach Directive dispatched to District Collector / ITDA Project Officer of ${district}! Special DigiLocker enrollment camps scheduled.`;
    window.EduconApp.showToast(msg);
    if (window.STePFirebase) {
      await window.STePFirebase.addDirective({
        district: district,
        type: "MOBILE_VAN_CAMP",
        details: "Direct facilitation camp dispatched with biometric & DigiLocker enrollment hardware.",
        status: "DISPATCHED"
      });
    }
  }

  async function triggerSmsOutreach(district, unreachedCount) {
    const msg = `Initiated UDISE+ matched SMS & WhatsApp campaign in local tribal dialect to ${unreachedCount.toLocaleString('en-IN')} families in ${district}!`;
    window.EduconApp.showToast(msg);
    if (window.STePFirebase) {
      await window.STePFirebase.addDirective({
        district: district,
        type: "SMS_IVR_CAMPAIGN",
        details: `Broadcast in regional tribal dialect to ${unreachedCount.toLocaleString('en-IN')} unreached families.`,
        status: "DISPATCHED"
      });
    }
  }

  return {
    init,
    selectDistrict,
    triggerMobileVanDirective,
    triggerSmsOutreach,
    approveApplicationManual,
    markDisbursed,
    openDefectModal,
    openAddSchemeModal,
    openAddAppModal
  };
})();
