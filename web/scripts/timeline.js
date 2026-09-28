/**
 * Educon MoTA Unified Portal - Timeline, DBT Tracker, & Deficiency Explainer
 * Integrates NSP, SFMP, and NOS portals into a single glassmorphic experience
 */

window.EduconTimeline = (function() {
  function init() {
    renderTimelineCards();
    renderDbtTracker();
    renderPeerNavigator();
    renderStudentImpact();
    setupDeficiencyModal();
    setupGrievanceModal();
  }

  function renderTimelineCards() {
    const container = document.getElementById('timeline-cards-container');
    if (!container) return;

    const apps = EduconData.applicationsTimeline;

    container.innerHTML = apps.map(app => {
      const isDeficient = app.stage === 'DEFICIENCY_FLAGGED';

      return `
        <div class="timeline-app-card ${isDeficient ? 'card-deficiency-glow' : ''}">
          <div class="app-card-topbar">
            <div>
              <span class="portal-badge">${app.sourcePortal}</span>
              <span class="badge ${app.stageBadgeClass} ml-2">${app.stageText}</span>
              <h3 class="app-title">${app.schemeTitle}</h3>
              <div class="app-meta">App ID: <span class="font-mono text-bold">${app.applicationId}</span> • Session: ${app.academicYear}</div>
            </div>
            <div class="text-right">
              <span class="text-xs text-muted">Sanction Amount</span>
              <div class="app-amount">₹ ${app.sanctionAmount.toLocaleString('en-IN')}</div>
            </div>
          </div>

          <!-- Visual Progress Dots Stream -->
          <div class="timeline-stepper">
            ${app.steps.map((st, idx) => `
              <div class="stepper-node ${st.completed ? 'completed' : (isDeficient && idx === 1 ? 'error' : 'pending')}">
                <div class="node-dot">
                  ${st.completed ? '✓' : (isDeficient && idx === 1 ? '!' : idx + 1)}
                </div>
                <div class="node-label">${st.label}</div>
                <div class="node-date">${st.date}</div>
              </div>
            `).join('')}
          </div>

          <!-- Deficiency Notice Alert (if present) -->
          ${isDeficient ? `
            <div class="deficiency-banner">
              <div class="flex items-center gap-2">
                <span class="warning-icon">⚠</span>
                <div>
                  <h4 class="font-bold text-amber">Defect Code: ${app.deficiencyData.code} (Action Required)</h4>
                  <p class="text-xs text-muted mb-2 font-mono">${app.deficiencyData.bureaucraticReason.substring(0, 140)}...</p>
                </div>
              </div>
              <div class="deficiency-actions">
                <button class="btn btn-warning btn-sm" onclick="EduconTimeline.openDeficiencyExplainer('${app.applicationId}')">
                  ✨ Explain in Simple Words (Gemini AI)
                </button>
                <span class="text-xs text-amber font-bold ml-2">⏳ ${app.deficiencyData.daysRemaining} days left to rectify</span>
              </div>
            </div>
          ` : ''}

          <!-- DBT Quick Info Strip -->
          ${app.dbtDetails ? `
            <div class="dbt-strip">
              <span class="text-xs">DBT UTR: <strong class="font-mono text-accent">${app.dbtDetails.utr}</strong></span>
              <span class="text-xs">Bank: <strong>${app.dbtDetails.bankName} (${app.dbtDetails.accountNo})</strong></span>
              ${app.dbtDetails.status === 'CREDITED_SUCCESSFULLY' ? `
                <button class="btn btn-ghost btn-xs text-emerald" onclick="EduconApp.switchTab('dbt')">View DBT Receipt →</button>
              ` : `
                <span class="badge badge-warning text-xs">Payment Processing</span>
              `}
            </div>
          ` : ''}
        </div>
      `;
    }).join('');
  }

  function renderDbtTracker() {
    const listContainer = document.getElementById('dbt-transactions-list');
    if (!listContainer) return;

    const apps = EduconData.applicationsTimeline.filter(a => a.dbtDetails);

    listContainer.innerHTML = apps.map(app => `
      <div class="dbt-ledger-card">
        <div class="dbt-card-header">
          <div>
            <span class="badge badge-success text-xs">PFMS / APB Direct Benefit Transfer</span>
            <h4 class="font-bold text-base mt-1">${app.schemeTitle}</h4>
            <div class="text-xs text-muted">Aadhaar Seeded Account: ${app.dbtDetails.bankName} (${app.dbtDetails.accountNo})</div>
          </div>
          <div class="text-right">
            <span class="text-xs text-muted">Disbursed Amount</span>
            <div class="text-xl font-bold text-emerald">₹ ${app.sanctionAmount.toLocaleString('en-IN')}</div>
          </div>
        </div>

        <div class="dbt-card-grid">
          <div class="dbt-info-item">
            <span class="label">PFMS UTR Number</span>
            <span class="value font-mono flex items-center gap-1">
              ${app.dbtDetails.utr}
              <button class="btn btn-ghost btn-xs" onclick="navigator.clipboard.writeText('${app.dbtDetails.utr}'); EduconApp.showToast('UTR Copied!');">📋</button>
            </span>
          </div>
          <div class="dbt-info-item">
            <span class="label">Payment Mode</span>
            <span class="value">${app.dbtDetails.paymentMode}</span>
          </div>
          <div class="dbt-info-item">
            <span class="label">Date of Credit</span>
            <span class="value">${app.dbtDetails.disbursedDate}</span>
          </div>
          <div class="dbt-info-item">
            <span class="label">Share Ratio</span>
            <span class="value text-xs">${app.dbtDetails.centralShare || 'Central Share 100%'}</span>
          </div>
        </div>

        <div class="dbt-card-footer">
          <div class="flex items-center gap-2">
            <span class="status-indicator-dot online"></span>
            <span class="text-xs text-muted">NPCI Aadhaar Mapper Confirmed (Active)</span>
          </div>
          <button class="btn btn-outline btn-xs" onclick="EduconTimeline.openGrievanceEscalation('${app.applicationId}', '${app.dbtDetails.utr}')">
            ⚠️ Payment Not Received? Escalate
          </button>
        </div>
      </div>
    `).join('');
  }

  function renderPeerNavigator() {
    const peerCard = document.getElementById('peer-navigator-card');
    if (!peerCard) return;

    const data = EduconData.peerStatistics;
    peerCard.innerHTML = `
      <div class="peer-header">
        <span class="peer-tag">PEER SCHOLARSHIP BENCHMARK</span>
        <h4 class="font-bold text-lg text-amber">Students Like You in ${EduconData.currentStudent.district}</h4>
        <p class="text-xs text-muted">${data.cohortDescription}</p>
      </div>
      <div class="peer-stats-row">
        <div class="peer-stat-box">
          <span class="stat-number text-emerald">${data.averageAward}</span>
          <span class="stat-title">Avg Award Received</span>
        </div>
        <div class="peer-stat-box">
          <span class="stat-number text-accent">${data.successRate}</span>
          <span class="stat-title">Application Approval Rate</span>
        </div>
        <div class="peer-stat-box">
          <span class="stat-number">${data.disbursementMedianDays}</span>
          <span class="stat-title">Median Sanction Time</span>
        </div>
      </div>
      <p class="peer-story text-xs italic mt-2">"${data.impactStory}"</p>
    `;
  }

  function renderStudentImpact() {
    const impactCard = document.getElementById('student-impact-card');
    if (!impactCard) return;

    const imp = EduconData.studentImpact;
    impactCard.innerHTML = `
      <div class="impact-gradient-box">
        <div class="flex justify-between items-start">
          <div>
            <span class="impact-tag">YOUR SCHOLARSHIP IMPACT STORY</span>
            <h3 class="text-2xl font-bold mt-1 text-white">₹ ${imp.totalReceivedTillDate.toLocaleString('en-IN')} Received</h3>
            <p class="text-xs opacity-90 text-white mt-1">${imp.milestoneQuote}</p>
          </div>
          <div class="impact-badge">
            <span class="impact-score">100%</span>
            <span class="text-xs">Tuition Free</span>
          </div>
        </div>
        <div class="impact-progress-grid mt-4">
          <div class="impact-bar-item">
            <div class="flex justify-between text-xs text-white mb-1">
              <span>Tuition Coverage</span>
              <span>100%</span>
            </div>
            <div class="progress-bar-track"><div class="progress-bar-fill bg-emerald" style="width: 100%"></div></div>
          </div>
          <div class="impact-bar-item">
            <div class="flex justify-between text-xs text-white mb-1">
              <span>Hostel Subsidy</span>
              <span>8 of 10 Months</span>
            </div>
            <div class="progress-bar-track"><div class="progress-bar-fill bg-amber" style="width: 80%"></div></div>
          </div>
        </div>
      </div>
    `;
  }

  // Multilingual Deficiency Explainer Modal
  let currentExplainingApp = null;
  let currentLanguage = 'en';

  function setupDeficiencyModal() {
    const modal = document.getElementById('deficiency-modal');
    const closeBtn = document.getElementById('btn-close-deficiency-modal');
    if (closeBtn && modal) {
      closeBtn.addEventListener('click', () => modal.classList.add('hidden'));
    }

    // Language pills
    document.querySelectorAll('.lang-pill').forEach(pill => {
      pill.addEventListener('click', (e) => {
        document.querySelectorAll('.lang-pill').forEach(p => p.classList.remove('active'));
        e.currentTarget.classList.add('active');
        currentLanguage = e.currentTarget.dataset.lang;
        if (currentExplainingApp) {
          triggerDeficiencyTranslation(currentExplainingApp, currentLanguage);
        }
      });
    });
  }

  async function openDeficiencyExplainer(appId) {
    const app = EduconData.applicationsTimeline.find(a => a.applicationId === appId);
    if (!app || !app.deficiencyData) return;

    currentExplainingApp = app;
    const modal = document.getElementById('deficiency-modal');
    if (modal) modal.classList.remove('hidden');

    const origEl = document.getElementById('deficiency-original-text');
    if (origEl) origEl.textContent = app.deficiencyData.bureaucraticReason;

    await triggerDeficiencyTranslation(app, currentLanguage);
  }

  async function triggerDeficiencyTranslation(app, lang) {
    const contentEl = document.getElementById('deficiency-explanation-body');
    if (contentEl) {
      contentEl.innerHTML = `<div class="p-6 text-center"><span class="spinner-ring"></span> Gemini AI translating bureaucratic defect to plain everyday language...</div>`;
    }

    const res = await EduconGemini.explainDeficiency(app.deficiencyData.bureaucraticReason, lang);

    if (contentEl) {
      contentEl.innerHTML = `
        <div class="deficiency-result-box">
          <div class="plain-summary">
            <h4 class="font-bold text-accent text-base mb-1">💡 What does this actually mean?</h4>
            <p class="text-sm leading-relaxed">${res.simpleExplanation}</p>
          </div>

          <div class="action-checklist mt-4">
            <h4 class="font-bold text-base mb-2">📋 How to Fix It (3-Step Checklist):</h4>
            ${res.actionSteps.map(step => `
              <div class="step-check-item">
                <input type="checkbox" checked readonly />
                <span>${step}</span>
              </div>
            `).join('')}
          </div>

          <div class="required-doc-box mt-3">
            <span class="text-xs text-muted">Required Document to Upload:</span>
            <div class="font-bold text-sm text-emerald">${res.requiredDocument}</div>
          </div>

          <div class="urgency-callout mt-3">
            <span class="text-xs text-amber font-bold">⏰ ${res.urgencyNote}</span>
          </div>

          <div class="mt-4 pt-3 border-t flex justify-end gap-2">
            <button class="btn btn-outline btn-sm" onclick="EduconApp.showToast('Direct helpline connecting to MoTA Overseas Desk: 1800-11-7700')">📞 Call Officer Helpline</button>
            <button class="btn btn-primary btn-sm" onclick="EduconApp.showToast('Opening Document Upload for Offer Letter...'); document.getElementById('deficiency-modal').classList.add('hidden'); EduconApp.switchTab('scanner');">📸 Upload Corrected Letter</button>
          </div>
        </div>
      `;
    }
  }

  // Grievance Escalation & SLA Countdown Modal
  function setupGrievanceModal() {
    const modal = document.getElementById('grievance-modal');
    const closeBtn = document.getElementById('btn-close-grievance-modal');
    if (closeBtn && modal) {
      closeBtn.addEventListener('click', () => modal.classList.add('hidden'));
    }
  }

  function openGrievanceEscalation(appId, utr) {
    const modal = document.getElementById('grievance-modal');
    if (!modal) return;
    modal.classList.remove('hidden');

    const app = EduconData.applicationsTimeline.find(a => a.applicationId === appId);
    const grvBody = document.getElementById('grievance-modal-body');

    if (grvBody) {
      grvBody.innerHTML = `
        <div class="grievance-form">
          <div class="alert-box-info mb-3">
            <p class="text-xs">Under the <strong>Citizen's Charter & RTI Rules</strong>, MoTA mandates that all DBT non-credit grievances must be resolved within <strong>30 calendar days</strong> with automated bank reconciliation.</p>
          </div>

          <div class="form-group mb-2">
            <label class="form-label text-xs">Application ID</label>
            <input type="text" class="input input-sm w-full font-mono" value="${appId}" readonly />
          </div>
          <div class="form-group mb-2">
            <label class="form-label text-xs">PFMS / Bank UTR Reference</label>
            <input type="text" class="input input-sm w-full font-mono" value="${utr}" readonly />
          </div>
          <div class="form-group mb-3">
            <label class="form-label text-xs">Grievance Description</label>
            <textarea class="input input-sm w-full" rows="3">Scholarship DBT status shows disbursed on ${app?.dbtDetails?.disbursedDate || 'portal'}, however bank statement for A/C ****4920 indicates funds not yet credited. Request bank clearing confirmation and re-push through NPCI APB bridge.</textarea>
          </div>

          <!-- Live SLA Countdown Clock Preview -->
          <div class="sla-clock-card">
            <div class="flex justify-between items-center">
              <div>
                <span class="text-xs text-muted">Ministry SLA Resolution Window</span>
                <div class="text-lg font-bold text-accent">30 Days Maximum SLA</div>
              </div>
              <div class="sla-badge">Active Charter</div>
            </div>
            <div class="progress-bar-track mt-2"><div class="progress-bar-fill bg-accent" style="width: 100%"></div></div>
          </div>

          <div class="mt-4 flex justify-end gap-2">
            <button class="btn btn-ghost btn-sm" onclick="document.getElementById('grievance-modal').classList.add('hidden')">Cancel</button>
            <button class="btn btn-primary btn-sm" onclick="EduconTimeline.submitGrievanceTicket('${appId}', '${utr}')">Submit Grievance Ticket</button>
          </div>
        </div>
      `;
    }
  }

  function submitGrievanceTicket(appId, utr) {
    const modal = document.getElementById('grievance-modal');
    if (modal) modal.classList.add('hidden');

    const newTicketId = `GRV-2026-MOTA-${Math.floor(1000 + Math.random() * 9000)}`;
    EduconData.grievances.unshift({
      ticketId: newTicketId,
      relatedAppId: appId,
      subject: `DBT Non-Credit Escalation for ${appId}`,
      createdDate: new Date().toISOString().split('T')[0],
      slaDaysTotal: 30,
      daysElapsed: 0,
      daysRemaining: 30,
      status: "IN_PROGRESS",
      assignedOfficer: "PFMS Cell, Ministry of Tribal Affairs",
      timeline: [
        { time: "Just now", note: "Auto-escalation ticket generated with SLA countdown clock." }
      ]
    });

    EduconApp.showToast(`✓ Grievance ${newTicketId} registered! Assigned 30-day SLA with MoTA PFMS Cell.`);
    EduconApp.switchTab('dbt');
  }

  return {
    init,
    openDeficiencyExplainer,
    openGrievanceEscalation,
    submitGrievanceTicket
  };
})();
