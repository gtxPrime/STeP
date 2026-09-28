/**
 * Educon MoTA Unified Portal - Ministry & Nodal Officer Dashboard
 * Features:
 * 1. Smart "Unreached Beneficiary" Heatmap (UDISE+ / APAAR ST enrollment vs Scholarship coverage)
 * 2. Semi-Automated Verification Queue (Confidence Score based auto vs manual routing)
 * 3. Cross-Portal Macro Metrics (NSP + SFMP + NOS)
 */

window.EduconOfficer = (function() {
  let activeFilter = 'ALL';
  let verificationQueue = [
    {
      id: "APP-VER-8901",
      studentName: "Somu Madkami",
      district: "Bastar, Chhattisgarh",
      scheme: "Pre-Matric Scholarship (Class 10)",
      portal: "NSP",
      confidenceScore: 96,
      status: "AUTO_APPROVED",
      source: "DigiLocker API (Aadhaar & e-District DSC 100% Match)",
      docType: "ST Caste Certificate (Murai Gond)",
      appliedDate: "2026-09-24",
      actionNeeded: false
    },
    {
      id: "APP-VER-8902",
      studentName: "Mangala Ho",
      district: "Mayurbhanj, Odisha",
      scheme: "Post-Matric Scholarship (B.Sc Botany)",
      portal: "NSP",
      confidenceScore: 72,
      status: "MANUAL_REVIEW_REQUIRED",
      source: "Gemini Vision OCR (Camera photo, slight glare on Tehsildar seal)",
      docType: "Income Certificate (₹92,000 p.a.)",
      appliedDate: "2026-09-25",
      actionNeeded: true,
      anomaly: "Income seal partially obscured. Applicant name matches APAAR record."
    },
    {
      id: "APP-VER-8903",
      studentName: "Dharmesh Pawra",
      district: "Nandurbar, Maharashtra",
      scheme: "Top Class Education (IIT Bombay M.Tech)",
      portal: "SFMP (Canara Bank)",
      confidenceScore: 94,
      status: "AUTO_APPROVED",
      source: "GATE Scorecard & DigiLocker e-Pramaan",
      docType: "IIT Admission Offer & ST Certificate",
      appliedDate: "2026-09-26",
      actionNeeded: false
    },
    {
      id: "APP-VER-8904",
      studentName: "Sushila Kujur",
      district: "Khunti, Jharkhand",
      scheme: "National Fellowship (NFST Ph.D. Anthropology)",
      portal: "SFMP",
      confidenceScore: 68,
      status: "MANUAL_REVIEW_REQUIRED",
      source: "Uploaded PDF Scan",
      docType: "UGC-NET JRF Award Letter",
      appliedDate: "2026-09-27",
      actionNeeded: true,
      anomaly: "UGC Roll Number format requires secondary manual NTA server cross-check."
    }
  ];

  function init() {
    renderHeatmap();
    renderVerificationQueue();
    setupEventListeners();
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
  }

  function renderHeatmap() {
    const listEl = document.getElementById('heatmap-districts-list');
    const selectedDetailEl = document.getElementById('heatmap-district-details');
    if (!listEl) return;

    let items = EduconData.districtHeatmapData;
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

          <!-- Gap Bar -->
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

    // Default select first item
    if (items.length > 0) {
      selectDistrict(items[0].district);
    }
  }

  function selectDistrict(districtName) {
    const d = EduconData.districtHeatmapData.find(item => item.district === districtName);
    if (!d) return;

    // Highlight row
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
          <span class="text-xs font-bold text-accent">📍 High-Density Tribal & PVTG Pockets:</span>
          <p class="text-xs text-muted mt-1">${d.pvtgPockets}</p>
        </div>

        <div class="barriers-box mb-4">
          <span class="text-xs font-bold text-accent">🚧 Root Cause Delivery Barriers:</span>
          <p class="text-xs text-muted mt-1">${d.primaryBarriers}</p>
        </div>

        <div class="action-buttons-grid flex gap-2">
          <button class="btn btn-primary btn-sm flex-1" onclick="EduconOfficer.triggerMobileVanDirective('${d.district}')">
            🚐 Dispatch Mobile Camp
          </button>
          <button class="btn btn-outline btn-sm flex-1" onclick="EduconOfficer.triggerSmsOutreach('${d.district}', ${d.gapCount})">
            📢 Send Multilingual SMS/IVR
          </button>
        </div>
      </div>
    `;
  }

  function renderVerificationQueue() {
    const container = document.getElementById('officer-verification-table');
    if (!container) return;

    container.innerHTML = verificationQueue.map(app => {
      const isAuto = app.status === 'AUTO_APPROVED';

      return `
        <div class="verification-item-card ${isAuto ? 'verified-auto' : 'verified-manual'}">
          <div class="flex justify-between items-start">
            <div>
              <span class="badge ${isAuto ? 'badge-success' : 'badge-warning'} text-xs">
                ${isAuto ? '✓ Auto-Approved (DigiLocker)' : '⚠ Manual Scrutiny Queue'}
              </span>
              <h4 class="font-bold text-base mt-1 text-white">${app.studentName}</h4>
              <div class="text-xs text-muted">${app.scheme} • ${app.district}</div>
            </div>
            <div class="text-right">
              <span class="text-xs text-muted">Confidence Score</span>
              <div class="text-xl font-bold ${app.confidenceScore >= 85 ? 'text-emerald' : 'text-amber'}">
                ${app.confidenceScore}%
              </div>
            </div>
          </div>

          <div class="doc-verification-strip my-2">
            <span class="text-xs text-muted">Document: <strong>${app.docType}</strong></span>
            <span class="text-xs text-muted ml-3">Source: <em>${app.source}</em></span>
          </div>

          ${app.anomaly ? `
            <div class="anomaly-warning-box">
              <span class="text-xs text-amber font-semibold">🔍 Scrutiny Flag: ${app.anomaly}</span>
            </div>
          ` : ''}

          <div class="verification-actions mt-3 flex justify-end gap-2">
            ${app.actionNeeded ? `
              <button class="btn btn-outline btn-xs" onclick="EduconOfficer.flagApplicationDefect('${app.id}')">
                Flag Defect with AI
              </button>
              <button class="btn btn-primary btn-xs" onclick="EduconOfficer.approveApplicationManual('${app.id}')">
                ✓ Approve Override
              </button>
            ` : `
              <span class="text-xs text-emerald font-semibold">Processed via Sovereign Auto-Verify Layer</span>
            `}
          </div>
        </div>
      `;
    }).join('');
  }

  function triggerMobileVanDirective(district) {
    EduconApp.showToast(`✓ Mobile Camp Outreach Directive dispatched to District Collector / ITDA Project Officer of ${district}! Special DigiLocker enrollment camps scheduled.`);
  }

  function triggerSmsOutreach(district, unreachedCount) {
    EduconApp.showToast(`✓ Initiated UDISE+ matched SMS & WhatsApp campaign in local tribal dialect to ${unreachedCount.toLocaleString('en-IN')} families in ${district}!`);
  }

  function approveApplicationManual(appId) {
    const item = verificationQueue.find(a => a.id === appId);
    if (!item) return;
    item.status = "AUTO_APPROVED";
    item.actionNeeded = false;
    item.confidenceScore = 95;
    renderVerificationQueue();
    EduconApp.showToast(`✓ Application ${appId} for ${item.studentName} approved and pushed to Sanction Order Batch.`);
  }

  function flagApplicationDefect(appId) {
    EduconApp.showToast(`✓ Defect notification sent with Gemini plain-language guidance to student's mobile app.`);
  }

  return {
    init,
    selectDistrict,
    triggerMobileVanDirective,
    triggerSmsOutreach,
    approveApplicationManual,
    flagApplicationDefect
  };
})();
