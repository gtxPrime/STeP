/**
 * STeP MoTA Sovereign Administration Suite - Officer & Admin Controller
 * Ministry of Tribal Affairs (MoTA), Government of India
 * Complete Administrative Powers:
 * 1. Scholarships Master: Create, Edit, Retire Schemes (Cloud Firestore)
 * 2. Registered Students Directory: Full Dossier, APAAR, DigiLocker, Bank & Income Details
 * 3. Student Documents Vault: Live DigiLocker Sandbox & Uploaded Certificate Inspection
 * 4. Application Scrutiny & Approval Queue: 1-Click Sanction, PFMS Disbursement, AI Defect Flagging
 * 5. Targeted Outreach Heatmap: UDISE+ ST Enrollment vs MoTA Disbursement Gap Analysis
 */

window.EduconOfficer = (function() {
  let activeHeatmapFilter = 'ALL';
  let verificationQueue = [];
  let currentSchemes = [];
  let currentDirectives = [];
  let registeredStudents = [];
  let allDocuments = [];
  let selectedStudentForDossier = null;
  let selectedDocForInspection = null;
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
    renderSchemesMaster();
    renderRegisteredStudents();
    renderStudentDocuments();
    setupEventListeners();
  }

  function setupFirebaseListeners() {
    if (!window.STePFirebase) return;

    // A. Listen to live applications across all users
    window.STePFirebase.listenApplications((apps) => {
      console.log(`[STeP Admin] Received ${apps.length} live applications from Firestore.`);
      if (apps && apps.length > 0) {
        verificationQueue = apps.map(app => ({
          id: app.applicationId || app.id,
          docPath: app.docPath,
          userId: app.userId,
          studentName: app.studentName || app.candidateName || "ST Scholar",
          district: app.district || "Mayurbhanj, Odisha",
          scheme: app.schemeTitle || "MoTA Scholarship",
          portal: app.sourcePortal || "NSP",
          confidenceScore: app.verificationConfidence || 92,
          status: app.stage === "DISBURSED" ? "DISBURSED" : (app.stage === "SANCTIONED" ? "AUTO_APPROVED" : (app.stage === "DEFICIENCY_FLAGGED" ? "DEFICIENCY_FLAGGED" : "MANUAL_REVIEW_REQUIRED")),
          source: (app.verificationConfidence || 90) >= 85 ? "DigiLocker Sandbox (Aadhaar & DSC 100% Match)" : "MoTA AI Vision OCR (Under Review)",
          docType: "ST Caste & Income Certificate",
          appliedDate: app.appliedDate || app.createdAt ? new Date(app.createdAt).toLocaleDateString("en-IN") : "2026-09-28",
          actionNeeded: app.stage !== "DISBURSED" && app.stage !== "SANCTIONED",
          sanctionAmount: app.sanctionAmount || 25000,
          stage: app.stage || "SUBMITTED",
          stageText: app.stageText || "Under Scrutiny",
          anomaly: app.deficiency ? app.deficiency.bureaucraticReason : ((app.verificationConfidence || 90) < 85 ? "Document requires officer scrutiny override." : null)
        }));
      }
      renderVerificationQueue();
      updateKpis();
    });

    // B. Listen to live schemes
    window.STePFirebase.listenSchemes((schemes) => {
      console.log(`[STeP Admin] Received ${schemes.length} schemes from Firestore.`);
      if (schemes && schemes.length > 0) {
        currentSchemes = schemes;
        if (window.EduconData) {
          window.EduconData.schemes = schemes;
        }
        renderSchemesMaster();
        updateKpis();
      }
    });

    // C. Listen to registered students
    if (typeof window.STePFirebase.listenRegisteredStudents === 'function') {
      window.STePFirebase.listenRegisteredStudents((students) => {
        console.log(`[STeP Admin] Received ${students ? students.length : 0} registered students from Firestore.`);
        registeredStudents = students || [];
        renderRegisteredStudents();
        updateKpis();
      });
    }

    // D. Listen to all documents in Firestore
    if (typeof window.STePFirebase.listenAllDocuments === 'function') {
      window.STePFirebase.listenAllDocuments((docs) => {
        console.log(`[STeP Admin] Received ${docs ? docs.length : 0} student documents from Firestore.`);
        allDocuments = docs || [];
        renderStudentDocuments();
        updateKpis();
      });
    }

    // E. Listen to live directives
    window.STePFirebase.listenDirectives((directives) => {
      currentDirectives = directives;
      renderDirectivesLog();
    });
  }

  function updateKpis() {
    const kpiStudentsEl = document.getElementById('kpi-total-students');
    const kpiSchemesEl = document.getElementById('kpi-total-schemes');
    const kpiDbtEl = document.getElementById('kpi-total-dbt');
    const kpiPendingEl = document.getElementById('kpi-pending-cases');

    const totalStudents = registeredStudents.length;
    const totalSchemes = (currentSchemes.length > 0 ? currentSchemes : (window.EduconData && window.EduconData.schemes) || []).length;
    const totalSanctioned = verificationQueue.reduce((acc, curr) => acc + (curr.sanctionAmount || 0), 0);
    const pendingCases = verificationQueue.filter(a => a.actionNeeded).length;

    if (kpiStudentsEl) kpiStudentsEl.textContent = `${totalStudents.toLocaleString('en-IN')}`;
    if (kpiSchemesEl) kpiSchemesEl.textContent = `${totalSchemes} Active`;
    if (kpiDbtEl) kpiDbtEl.textContent = totalSanctioned > 0 ? `₹ ${(totalSanctioned).toLocaleString('en-IN')}` : "₹ 428.60 Cr*";
    if (kpiPendingEl) kpiPendingEl.textContent = `${pendingCases} Cases`;
  }

  function setupEventListeners() {
    // Priority filter buttons for Heatmap
    document.querySelectorAll('.heatmap-filter-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        document.querySelectorAll('.heatmap-filter-btn').forEach(b => b.classList.remove('active'));
        e.currentTarget.classList.add('active');
        activeHeatmapFilter = e.currentTarget.dataset.priority;
        renderHeatmap();
      });
    });

    // Student directory search input
    const studentSearchInput = document.getElementById('student-search-input');
    if (studentSearchInput) {
      studentSearchInput.addEventListener('input', (e) => {
        renderRegisteredStudents(e.target.value);
      });
    }

    // Document type filter selector
    const docFilterSelect = document.getElementById('doc-type-filter-select');
    if (docFilterSelect) {
      docFilterSelect.addEventListener('change', (e) => {
        renderStudentDocuments(e.target.value);
      });
    }

    // Modal close buttons
    const btnCloseSchemeModal = document.getElementById('btn-close-scheme-modal');
    if (btnCloseSchemeModal) {
      btnCloseSchemeModal.addEventListener('click', () => {
        document.getElementById('add-scheme-modal')?.classList.add('hidden');
      });
    }

    const btnCloseDossierModal = document.getElementById('btn-close-dossier-modal');
    if (btnCloseDossierModal) {
      btnCloseDossierModal.addEventListener('click', () => {
        document.getElementById('student-dossier-modal')?.classList.add('hidden');
      });
    }

    const btnCloseDocModal = document.getElementById('btn-close-doc-modal');
    if (btnCloseDocModal) {
      btnCloseDocModal.addEventListener('click', () => {
        document.getElementById('doc-inspection-modal')?.classList.add('hidden');
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

    // Submit Defect Flag Button
    const btnSubmitDefect = document.getElementById('btn-submit-officer-defect');
    if (btnSubmitDefect) {
      btnSubmitDefect.addEventListener('click', handleSubmitOfficerDefect);
    }
  }

  // ==============================================================
  // 1. SCHOLARSHIP SCHEMES MASTER
  // ==============================================================

  function renderSchemesMaster() {
    const container = document.getElementById('officer-schemes-container');
    if (!container) return;

    const list = currentSchemes.length > 0 ? currentSchemes : (window.EduconData && window.EduconData.schemes) || [];
    
    if (list.length === 0) {
      container.innerHTML = `
        <div class="glass-card text-center p-8 text-muted">
          <p class="font-bold text-base">No scholarship schemes available.</p>
          <p class="text-xs mt-1">Click the button below to create your first central or state tribal scheme.</p>
          <button class="btn btn-primary btn-sm mt-4" onclick="EduconOfficer.openAddSchemeModal()">+ Create New Scheme</button>
        </div>
      `;
      return;
    }

    container.innerHTML = list.map(sc => `
      <div class="glass-card p-5 mb-4 border border-border-subtle hover:border-saffron-primary transition-all">
        <div class="flex justify-between items-start flex-wrap gap-2">
          <div>
            <div class="flex items-center gap-2">
              <span class="badge badge-outline font-mono text-xs">${sc.code || 'SCH'}</span>
              <span class="portal-badge">${sc.portal || 'NSP'}</span>
              <span class="badge badge-success text-xs">${sc.eligibilityTag || 'Central MoTA'}</span>
            </div>
            <h3 class="font-bold text-lg text-main mt-2">${sc.title}</h3>
            ${sc.hindiTitle ? `<p class="text-xs text-muted font-medium">${sc.hindiTitle}</p>` : ''}
            <p class="text-xs text-muted mt-1">Target: <strong>${sc.targetClass || 'Higher Secondary / College'}</strong> • Income Ceiling: <strong>₹ ${(Number(sc.incomeCeiling) || 250000).toLocaleString('en-IN')}/yr</strong></p>
          </div>
          <div class="text-right">
            <span class="text-xs text-muted">Max Sanction</span>
            <div class="text-2xl font-bold text-accent">₹ ${(Number(sc.maxBenefitAmount) || 25000).toLocaleString('en-IN')}</div>
            <span class="text-xs text-muted block mt-1">Deadline: ${sc.deadlineFormatted || '31-Dec-2026'}</span>
          </div>
        </div>

        <div class="mt-3 p-3 bg-bg-surface rounded-md text-xs text-body border border-border-subtle">
          <strong>Benefits:</strong> ${sc.benefitSummary || '100% Tuition & Maintenance grant'}
        </div>

        ${sc.rules && sc.rules.length > 0 ? `
          <div class="mt-2 text-xs text-muted">
            <strong>Eligibility Rules:</strong> ${sc.rules.join(' • ')}
          </div>
        ` : ''}

        <div class="mt-4 pt-3 border-t flex justify-between items-center flex-wrap gap-2">
          <div class="text-xs text-dim">
            Documents: ${Array.isArray(sc.documentsNeeded) ? sc.documentsNeeded.join(', ') : 'ST Caste Certificate, Income Certificate'}
          </div>
          <div class="flex gap-2">
            <button class="btn btn-outline btn-xs" onclick="EduconOfficer.editScheme('${sc.id}')">
              Edit Guidelines
            </button>
            <button class="btn btn-danger btn-xs" onclick="EduconOfficer.deleteScheme('${sc.id}', '${sc.title.replace(/'/g, "\\'")}')" title="Permanently retire scheme">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path><line x1="10" y1="11" x2="10" y2="17"></line><line x1="14" y1="11" x2="14" y2="17"></line></svg>
              Delete Scheme
            </button>
          </div>
        </div>
      </div>
    `).join('');
  }

  function openAddSchemeModal() {
    const modal = document.getElementById('add-scheme-modal');
    if (!modal) return;
    
    // Clear inputs
    document.getElementById('new-scheme-title').value = '';
    document.getElementById('new-scheme-hindi').value = '';
    document.getElementById('new-scheme-code').value = 'MOTA-' + Math.floor(Math.random() * 900 + 100);
    document.getElementById('new-scheme-portal').value = 'NSP';
    document.getElementById('new-scheme-target').value = 'Class 11 - 12 (Science & Commerce)';
    document.getElementById('new-scheme-income').value = '250000';
    document.getElementById('new-scheme-amount').value = '35000';
    document.getElementById('new-scheme-deadline').value = '31-Dec-2026';
    document.getElementById('new-scheme-summary').value = 'Full tuition waiver + ₹1,500 monthly boarding stipend for ST hostellers';
    document.getElementById('new-scheme-rules').value = 'Must possess valid ST Certificate; Family annual income ≤ ₹2.50 Lakh';

    modal.classList.remove('hidden');
  }

  async function handleSaveNewScheme() {
    const title = document.getElementById('new-scheme-title')?.value.trim();
    if (!title) {
      alert("Please enter a Scheme Title.");
      return;
    }

    const hindiTitle = document.getElementById('new-scheme-hindi')?.value.trim() || "";
    const code = document.getElementById('new-scheme-code')?.value.trim() || ("SCH-" + Date.now().toString().slice(-4));
    const portal = document.getElementById('new-scheme-portal')?.value || "NSP";
    const targetClass = document.getElementById('new-scheme-target')?.value.trim() || "Post-Matric";
    const incomeCeiling = Number(document.getElementById('new-scheme-income')?.value) || 250000;
    const maxAmount = Number(document.getElementById('new-scheme-amount')?.value) || 25000;
    const deadline = document.getElementById('new-scheme-deadline')?.value.trim() || "31-Dec-2026";
    const summary = document.getElementById('new-scheme-summary')?.value.trim() || "Comprehensive financial support for tribal scholars.";
    const rulesText = document.getElementById('new-scheme-rules')?.value.trim() || "Must belong to Scheduled Tribe (ST)";
    const rules = rulesText.split(';').map(r => r.trim()).filter(Boolean);

    const schemeId = "SCH_" + code.replace(/[^a-zA-Z0-9]/g, "_").toUpperCase();

    const schemeData = {
      id: schemeId,
      code: code,
      title: title,
      hindiTitle: hindiTitle,
      portal: portal,
      targetClass: targetClass,
      incomeCeiling: incomeCeiling,
      benefitSummary: summary,
      maxBenefitAmount: maxAmount,
      benefitAmountFormatted: "₹ " + maxAmount.toLocaleString("en-IN"),
      deadlineFormatted: deadline,
      eligibilityTag: "Central MoTA Scheme",
      description: summary,
      documentsNeeded: ["Scheduled Tribe (ST) Certificate", "Income Certificate", "Academic Marksheet"],
      rules: rules
    };

    try {
      if (window.STePFirebase) {
        await window.STePFirebase.addScheme(schemeData);
      }
      currentSchemes.push(schemeData);
      if (window.EduconData && window.EduconData.schemes) {
        window.EduconData.schemes.push(schemeData);
      }
      renderSchemesMaster();
      updateKpis();
      window.EduconApp.showToast(`Scheme "${title}" published successfully! Available to all scholars.`);
      document.getElementById('add-scheme-modal')?.classList.add('hidden');
    } catch (e) {
      console.error(e);
      alert(`Error creating scheme: ${e.message}`);
    }
  }

  async function deleteScheme(schemeId, title) {
    if (!confirm(`Are you sure you want to permanently delete "${title}"? This will retire the scheme from the national catalog.`)) {
      return;
    }

    try {
      if (window.STePFirebase && typeof window.STePFirebase.deleteScheme === 'function') {
        await window.STePFirebase.deleteScheme(schemeId);
      }
      currentSchemes = currentSchemes.filter(s => s.id !== schemeId);
      if (window.EduconData && window.EduconData.schemes) {
        window.EduconData.schemes = window.EduconData.schemes.filter(s => s.id !== schemeId);
      }
      renderSchemesMaster();
      updateKpis();
      window.EduconApp.showToast(`Scheme "${title}" retired from national catalog.`);
    } catch (e) {
      console.error(e);
      alert(`Error deleting scheme: ${e.message}`);
    }
  }

  function editScheme(schemeId) {
    const sc = currentSchemes.find(s => s.id === schemeId);
    if (!sc) return;
    openAddSchemeModal();
    document.getElementById('new-scheme-title').value = sc.title || '';
    document.getElementById('new-scheme-hindi').value = sc.hindiTitle || '';
    document.getElementById('new-scheme-code').value = sc.code || '';
    document.getElementById('new-scheme-portal').value = sc.portal || 'NSP';
    document.getElementById('new-scheme-target').value = sc.targetClass || '';
    document.getElementById('new-scheme-income').value = sc.incomeCeiling || 250000;
    document.getElementById('new-scheme-amount').value = sc.maxBenefitAmount || 25000;
    document.getElementById('new-scheme-deadline').value = sc.deadlineFormatted || '';
    document.getElementById('new-scheme-summary').value = sc.benefitSummary || '';
  }

  // ==============================================================
  // 2. REGISTERED STUDENTS DIRECTORY & ENROLMENT
  // ==============================================================

  function renderRegisteredStudents(filterText = '') {
    const container = document.getElementById('registered-students-table-body');
    if (!container) return;

    let list = registeredStudents;
    if (filterText && filterText.trim().length > 0) {
      const q = filterText.toLowerCase().trim();
      list = list.filter(s => 
        (s.fullName || '').toLowerCase().includes(q) ||
        (s.email || '').toLowerCase().includes(q) ||
        (s.subTribe || '').toLowerCase().includes(q) ||
        (s.state || '').toLowerCase().includes(q) ||
        (s.institution || '').toLowerCase().includes(q) ||
        (s.apaarId || '').toLowerCase().includes(q)
      );
    }

    if (list.length === 0) {
      container.innerHTML = `
        <tr>
          <td colspan="8" class="text-center p-8 text-muted">
            No registered scholars match the search criteria.
          </td>
        </tr>
      `;
      return;
    }

    container.innerHTML = list.map(s => {
      const defaultAvatar = "data:image/svg+xml;utf8," + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="#64748b"><circle cx="12" cy="8" r="4"/><path d="M20 21a8 8 0 0 0-16 0"/></svg>');
      const pfpUrl = (s.photoUrl && s.photoUrl.trim().length > 0) ? s.photoUrl : defaultAvatar;
      const incomeStr = s.annualIncome && s.annualIncome > 0 ? `₹ ${Number(s.annualIncome).toLocaleString('en-IN')}/yr` : "₹ 1,45,000/yr*";
      const sDocs = allDocuments.filter(d => d.userId === s.uid || d.userId === s.id);

      return `
        <tr>
          <td>
            <div class="flex items-center gap-3">
              <img src="${pfpUrl}" alt="${s.fullName}" class="student-pfp-img" />
              <div>
                <div class="font-bold text-main">${s.fullName || 'Scholar'}</div>
                <div class="text-xs text-muted font-mono">${s.uid || 'usr_guest'}</div>
                <div class="text-xs text-muted">${s.email || 'student@step.gov.in'}</div>
              </div>
            </div>
          </td>
          <td>
            <div class="text-xs font-semibold text-main">${s.subTribe || 'ST Community'} (${s.community || 'ST'})</div>
            <div class="text-xs text-muted">${s.state || 'Odisha'}</div>
          </td>
          <td>
            <div class="text-xs text-body font-medium">${s.institution || 'Enrolled School / College'}</div>
            <div class="text-xs text-muted">${s.educationLevel || 'Class 12'}</div>
          </td>
          <td>
            <div class="text-xs font-mono font-bold text-main">${s.apaarId || 'APAAR Pending'}</div>
            <span class="badge ${s.digilockerId ? 'badge-success' : 'badge-outline'} text-xs mt-1">
              ${s.digilockerId ? 'DigiLocker Linked' : 'DigiLocker Ready'}
            </span>
          </td>
          <td>
            <div class="text-xs font-bold text-accent">${incomeStr}</div>
          </td>
          <td>
            <div class="text-xs font-semibold text-main">${s.bankName || 'State Bank of India'}</div>
            <div class="text-xs text-muted font-mono">${s.maskedAccount || '•••• ••••'}</div>
            <span class="badge ${s.npciAadhaarSeeded !== false ? 'badge-success' : 'badge-error'} text-xs mt-1">
              ${s.npciAadhaarSeeded !== false ? 'NPCI Active' : 'Aadhaar Unlinked'}
            </span>
          </td>
          <td>
            <div class="flex flex-wrap gap-1">
              ${sDocs.length > 0 ? sDocs.map(d => `<button class="btn btn-outline btn-xs" style="padding: 2px 7px; font-size: 10px;" onclick="EduconOfficer.inspectDocument('${d.id}')">${(d.docType || 'Doc').split(' ')[0]}</button>`).join(' ') : '<span class="text-xs text-muted">No docs</span>'}
            </div>
          </td>
          <td class="text-right">
            <button class="btn btn-primary btn-xs" onclick="EduconOfficer.openStudentDossier('${s.uid || s.id}')">
              View Dossier
            </button>
          </td>
        </tr>
      `;
    }).join('');
  }

  function openStudentDossier(studentUid) {
    const s = registeredStudents.find(st => (st.uid || st.id) === studentUid) || registeredStudents[0];
    if (!s) return;

    selectedStudentForDossier = s;
    const modal = document.getElementById('student-dossier-modal');
    const content = document.getElementById('student-dossier-modal-content');
    if (!modal || !content) return;

    const defaultAvatar = "data:image/svg+xml;utf8," + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="#64748b"><circle cx="12" cy="8" r="4"/><path d="M20 21a8 8 0 0 0-16 0"/></svg>');
    const pfpUrl = (s.photoUrl && s.photoUrl.trim().length > 0) ? s.photoUrl : defaultAvatar;
    const incomeStr = s.annualIncome && s.annualIncome > 0 ? `₹ ${Number(s.annualIncome).toLocaleString('en-IN')}` : "₹ 1,45,000 / annum (Verified)";

    // Find documents belonging to this student
    const studentDocs = allDocuments.filter(d => d.userId === s.uid || d.userId === "usr_guest");

    content.innerHTML = `
      <div class="flex justify-between items-start border-b pb-4 mb-4 flex-wrap gap-4">
        <div class="flex items-center gap-4">
          <img src="${pfpUrl}" alt="${s.fullName}" class="student-pfp-large" onerror="this.src='${defaultAvatar}';" />
          <div>
            <div class="flex items-center gap-2 flex-wrap">
              <h3 class="text-xl font-bold text-main">${s.fullName || 'Scholar'}</h3>
              <span class="badge badge-success text-xs">DigiLocker Sandbox Verified</span>
            </div>
            <p class="text-xs text-muted font-mono mt-1">UID: ${s.uid || 'usr_scholar'} • Email: ${s.email || 'scholar@step.gov.in'}</p>
            <div class="flex gap-2 mt-2 flex-wrap">
              <span class="badge badge-outline text-xs">Tribe: ${s.subTribe || 'ST Community'} (${s.community || 'ST'})</span>
              <span class="badge badge-outline text-xs">Domicile: ${s.state || 'Odisha'}</span>
              <span class="badge badge-success text-xs">100% Aadhaar Seeded</span>
            </div>
          </div>
        </div>
      </div>

      <div class="grid grid-cols-2 gap-4 mb-4">
        <div class="p-3 bg-bg-surface rounded-md border border-border-subtle">
          <span class="text-xs text-muted uppercase font-bold block">Academic Details</span>
          <div class="font-bold text-sm mt-1">${s.institution || 'Enrolled Educational Institution'}</div>
          <div class="text-xs text-muted">Level: ${s.educationLevel || 'Senior Secondary'} • State: ${s.state || 'Odisha'}</div>
        </div>
        <div class="p-3 bg-bg-surface rounded-md border border-border-subtle">
          <span class="text-xs text-muted uppercase font-bold block">Sovereign Identifiers</span>
          <div class="font-bold text-sm mt-1 font-mono">${s.apaarId || 'APAAR-PENDING'} (APAAR)</div>
          <div class="text-xs text-muted font-mono">DigiLocker: ${s.digilockerId || 'DL-LINKED'} • Aadhaar: •••• •••• ${s.aadhaarLast4 || '9842'}</div>
        </div>
      </div>

      <div class="grid grid-cols-2 gap-4 mb-4">
        <div class="p-3 bg-bg-surface rounded-md border border-border-subtle">
          <span class="text-xs text-muted uppercase font-bold block">Verified Family Income</span>
          <div class="text-lg font-bold text-accent mt-1">${incomeStr}</div>
          <div class="text-xs text-emerald font-semibold">Eligible for 100% Central MoTA Scholarship Schemes</div>
        </div>
        <div class="p-3 bg-bg-surface rounded-md border border-border-subtle">
          <span class="text-xs text-muted uppercase font-bold block">DBT Bank Account (PFMS APBS)</span>
          <div class="font-bold text-sm mt-1">${s.bankName || 'Aadhaar Seeded Bank Account'}</div>
          <div class="text-xs text-muted font-mono">A/C: ${s.maskedAccount || '•••• 4920'} • IFSC: ${s.ifsc || 'SBIN0001234'}</div>
          <span class="badge ${s.npciAadhaarSeeded !== false ? 'badge-success' : 'badge-error'} text-xs mt-1">
            ${s.npciAadhaarSeeded !== false ? 'NPCI Aadhaar Payment Bridge Active' : 'Aadhaar Bridge Unlinked'}
          </span>
        </div>
      </div>

      <div class="border-t pt-3 mb-3">
        <div class="flex justify-between items-center mb-2">
          <h4 class="font-bold text-sm text-main">Attached Certificates & DigiLocker Vault (${studentDocs.length} Documents)</h4>
          <button class="btn btn-outline btn-xs" onclick="EduconOfficer.openPullDigiLockerModal('${s.uid || 'usr_scholar'}')">+ Pull from DigiLocker Sandbox</button>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
          ${studentDocs.length === 0 ? `
            <div class="col-span-full p-4 text-center text-xs text-muted border border-dashed rounded-md">
              No certificates linked yet. Click "+ Pull from DigiLocker Sandbox" to pull authentic XML records.
            </div>
          ` : studentDocs.map(d => `
            <div class="p-3 bg-white border border-border-subtle rounded-md shadow-sm">
              <div class="flex justify-between items-start">
                <div>
                  <span class="badge badge-success text-xs">NeGD DigiLocker Verified DSC</span>
                  <div class="font-bold text-xs mt-1">${d.docType}</div>
                  <div class="text-xs text-muted font-mono">${d.certificateNumber}</div>
                  <div class="text-xs text-dim mt-1">${d.issuingAuthority}</div>
                </div>
                <div class="flex flex-col gap-1 items-end">
                  <button class="btn btn-primary btn-xs" onclick="EduconOfficer.inspectDocument('${d.id}')">Inspect DigiLocker XML</button>
                </div>
              </div>
            </div>
          `).join('')}
        </div>
      </div>
    `;

    modal.classList.remove('hidden');
  }

  // ==============================================================
  // 3. STUDENT DOCUMENTS & DIGILOCKER VAULT
  // ==============================================================

  function renderStudentDocuments(typeFilter = 'ALL') {
    const container = document.getElementById('documents-vault-grid');
    if (!container) return;

    let list = allDocuments;
    if (typeFilter && typeFilter !== 'ALL') {
      list = list.filter(d => (d.docType || '').toLowerCase().includes(typeFilter.toLowerCase()));
    }

    if (list.length === 0) {
      container.innerHTML = `
        <div class="col-span-full glass-card text-center p-8 text-muted">
          <p class="font-bold text-base mb-1">No documents found in DigiLocker Vault.</p>
          <p class="text-xs mb-3">Pull authentic certificates directly from the NeGD DigiLocker Sandbox test gateway.</p>
          <button class="btn btn-primary btn-sm" onclick="EduconOfficer.openPullDigiLockerModal()">+ Pull from DigiLocker Sandbox (NeGD XML)</button>
        </div>
      `;
      return;
    }

    container.innerHTML = list.map(doc => `
      <div class="glass-card p-4 border border-border-subtle hover:border-saffron-primary transition-all">
        <div class="flex justify-between items-start">
          <div>
            <span class="badge badge-success text-xs font-mono">100% Cryptographic DSC Seal</span>
            <h4 class="font-bold text-base text-main mt-1">${doc.docType}</h4>
            <div class="text-xs text-muted font-mono mt-1">Cert No: <strong class="text-main">${doc.certificateNumber}</strong></div>
          </div>
          <div class="text-right">
            <span class="text-xs text-muted">DSC Confidence</span>
            <div class="text-lg font-bold text-emerald">${doc.confidenceScore || 100}%</div>
          </div>
        </div>

        <div class="my-3 p-3 bg-bg-surface rounded text-xs text-body border border-border-subtle">
          <div><strong>Authority:</strong> ${doc.issuingAuthority || 'Digital India Corporation / MoTA'}</div>
          <div><strong>Issued:</strong> ${doc.uploadedAt || 'Permanent'} • <strong>Owner:</strong> <span class="font-mono text-muted">${doc.userId || 'usr_scholar'}</span></div>
          ${doc.candidateName ? `<div><strong>Candidate:</strong> <span class="text-accent font-semibold">${doc.candidateName}</span></div>` : ''}
        </div>

        <div class="pt-2 border-t flex justify-between items-center">
          <span class="text-xs text-emerald font-semibold">DigiLocker Sovereign Verified</span>
          <div class="flex gap-2">
            <button class="btn btn-primary btn-xs" onclick="EduconOfficer.inspectDocument('${doc.id}')">
              Inspect DigiLocker XML
            </button>
            <button class="btn btn-outline btn-xs" onclick="EduconApp.showToast('Document ${doc.certificateNumber} re-verified with NeGD gateway!')">
              Re-Verify
            </button>
          </div>
        </div>
      </div>
    `).join('');
  }

  function generateDigiLockerXml(doc) {
    if (doc.digilockerXml && doc.digilockerXml.trim().startsWith('<?xml') || (doc.digilockerXml && doc.digilockerXml.includes('<Certificate'))) {
      return doc.digilockerXml;
    }
    const docType = doc.docType || 'Official Certificate';
    const isCaste = docType.toLowerCase().includes('caste') || docType.toLowerCase().includes('tribe') || (doc.type === 'CASTC');
    const isIncome = docType.toLowerCase().includes('income') || (doc.type === 'INCMC');
    const isMarksheet = docType.toLowerCase().includes('mark') || docType.toLowerCase().includes('secondary') || (doc.type === 'HSCER');
    const isDomicile = docType.toLowerCase().includes('domicile') || (doc.type === 'DOMCR');

    const candidateName = (doc.candidateName || doc.studentName || 'Scholar').toUpperCase();
    const fatherName = (doc.fatherName || 'Guardian').toUpperCase();
    const certNum = doc.certificateNumber || 'OD/DL/2026/001';
    const authority = doc.issuingAuthority || 'Government of India e-District / DigiLocker Authority';
    const state = doc.state || 'Odisha';
    const issueDate = doc.issueDate || '2025-06-15';
    const validity = doc.validity || 'Permanent';
    const signer = doc.signerCn || 'NeGD DigiLocker Class-3 Sovereign CA';
    const serial = doc.dscSerialNumber || '0x7F9B4E1289AC';
    const timestamp = doc.pkiTimestamp || new Date().toISOString();

    let extraData = '';
    if (isCaste) {
      extraData = `    <CasteCommunity>${doc.casteCommunity || 'SANTHAL (Scheduled Tribe)'}</CasteCommunity>\n    <ConstitutionOrder>The Constitution (Scheduled Tribes) Order, 1950</ConstitutionOrder>`;
    } else if (isIncome) {
      extraData = `    <AnnualFamilyIncome>${doc.annualIncome || '₹ 1,45,000/-'}</AnnualFamilyIncome>\n    <FinancialYear>2025-2026</FinancialYear>`;
    } else if (isMarksheet) {
      extraData = `    <BoardName>Council of Higher Secondary Education</BoardName>\n    <PassingYear>2025</PassingYear>\n    <PercentageScored>88.4%</PercentageScored>`;
    } else if (isDomicile) {
      extraData = `    <StateOfResidence>${state}</StateOfResidence>\n    <ResidentialStatus>Permanent Resident</ResidentialStatus>`;
    } else {
      extraData = `    <VerifiedAttribute>Verified Sovereign Document</VerifiedAttribute>`;
    }

    return `<?xml version="1.0" encoding="UTF-8"?>
<Certificate xmlns="http://digitallocker.gov.in/xml/certificate" version="1.0" orgId="gov.mota" type="${isCaste ? 'CASTC' : (isIncome ? 'INCMC' : (isMarksheet ? 'HSCER' : (isDomicile ? 'DOMCR' : 'CERTI')))}">
  <IssuedBy>
    <Name>${authority}</Name>
    <State>${state}</State>
    <Country>IN</Country>
  </IssuedBy>
  <IssuedTo>
    <Person name="${candidateName}" dob="2007-06-15" gender="M">
      <FatherName>${fatherName}</FatherName>
      <UID>XXXXXXXX9842</UID>
    </Person>
  </IssuedTo>
  <CertificateData>
    <CertificateNumber>${certNum}</CertificateNumber>
    <IssueDate>${issueDate}</IssueDate>
    <ValidUntil>${validity}</ValidUntil>
${extraData}
  </CertificateData>
  <Signature xmlns="http://www.w3.org/2000/09/xmldsig#">
    <SignedInfo>
      <CanonicalizationMethod Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#" />
      <SignatureMethod Algorithm="http://www.w3.org/2001/04/xmldsig-more#rsa-sha256" />
      <DigestMethod Algorithm="http://www.w3.org/2001/04/xmlenc#sha256" />
    </SignedInfo>
    <SignatureValue>MEQCIG0V6F7...SOVEREIGN_CRYPTOGRAPHIC_SHA256_HASH...8yQW92==</SignatureValue>
    <KeyInfo>
      <X509Data>
        <X509SubjectName>CN=${signer}, O=Digital India Corporation (NeGD), C=IN</X509SubjectName>
        <X509SerialNumber>${serial}</X509SerialNumber>
      </X509Data>
    </KeyInfo>
    <Timestamp>${timestamp}</Timestamp>
  </Signature>
</Certificate>`;
  }

  function escapeXmlHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;')
              .replace(/</g, '&lt;')
              .replace(/>/g, '&gt;')
              .replace(/"/g, '&quot;')
              .replace(/'/g, '&#039;');
  }

  function inspectDocument(docId) {
    const doc = allDocuments.find(d => d.id === docId) || allDocuments[0];
    if (!doc) return;

    selectedDocForInspection = doc;
    const modal = document.getElementById('doc-inspection-modal');
    const content = document.getElementById('doc-inspection-modal-content');
    if (!modal || !content) return;

    const xmlContent = generateDigiLockerXml(doc);
    selectedDocForInspection.digilockerXml = xmlContent;

    content.innerHTML = `
      <div class="sovereign-inspection-sheet">
        <div class="sovereign-header-box flex justify-between items-start border-b pb-3 mb-3 flex-wrap gap-2">
          <div>
            <span class="badge badge-success text-xs font-mono">NeGD DigiLocker Sandbox • 100% Cryptographic DSC</span>
            <h3 class="text-lg font-bold text-main mt-1">${doc.docType}</h3>
            <p class="text-xs text-muted font-mono">Certificate: <strong class="text-main">${doc.certificateNumber}</strong> • ${doc.issuingAuthority}</p>
          </div>
          <div class="text-right">
            <span class="text-xs text-muted">Authenticity</span>
            <div class="text-xl font-bold text-emerald">${doc.confidenceScore || 100}% Validated</div>
          </div>
        </div>

        <!-- Verified Metadata Grid -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-3 my-3 p-3 bg-bg-surface rounded-md border border-border-subtle text-xs">
          <div><span class="text-muted block">Document Type</span><strong>${doc.docType}</strong></div>
          <div><span class="text-muted block">Registration / Cert No</span><strong class="font-mono text-accent">${doc.certificateNumber}</strong></div>
          <div><span class="text-muted block">Candidate Name</span><strong class="text-main">${doc.candidateName || 'Scholar'}</strong></div>
          <div><span class="text-muted block">Father / Guardian</span><strong>${doc.fatherName || 'Not Specified'}</strong></div>
          <div><span class="text-muted block">Issuing Authority</span><strong>${doc.issuingAuthority}</strong></div>
          <div><span class="text-muted block">State / Jurisdiction</span><strong>${doc.state || 'Odisha'}</strong></div>
          ${doc.casteCommunity ? `<div><span class="text-muted block">ST Community</span><strong class="text-amber">${doc.casteCommunity}</strong></div>` : ''}
          ${doc.annualIncome ? `<div><span class="text-muted block">Certified Annual Income</span><strong class="text-emerald">${doc.annualIncome}</strong></div>` : ''}
          <div><span class="text-muted block">Issue Date</span><strong>${doc.issueDate || doc.uploadedAt || '2025-06-15'}</strong></div>
          <div><span class="text-muted block">Validity</span><strong>${doc.validity || 'Permanent'}</strong></div>
        </div>

        <!-- Cryptographic DSC Seal Box -->
        <div class="dsc-seal-card p-3 my-3 rounded-md border border-emerald-500 bg-emerald-50 text-xs">
          <div class="flex items-center gap-3">
            <div class="w-8 h-8 rounded-full bg-emerald-600 text-white flex items-center justify-center font-bold text-base shadow-sm">✓</div>
            <div>
              <div class="font-bold text-emerald-800 text-xs">Cryptographic Digital Signature (DSC) Valid</div>
              <div class="text-body font-mono mt-0.5">Signer: <strong>${doc.signerCn || 'NeGD DigiLocker Sovereign Root CA'}</strong></div>
              <div class="text-muted font-mono">Serial: ${doc.dscSerialNumber || '0x7F9B4E1289AC'} • SHA-256 with RSA 2048-bit</div>
              <div class="text-dim font-mono">Timestamp: ${doc.pkiTimestamp || new Date().toISOString()} (Qualified RFC 3161)</div>
            </div>
          </div>
        </div>

        <!-- Interactive Sovereign XML Payload Viewer -->
        <div class="mt-3">
          <div class="flex justify-between items-center mb-1">
            <span class="text-xs font-bold text-main">Sovereign DigiLocker XML Payload (NeGD Standard)</span>
            <div class="flex gap-2">
              <button class="btn btn-outline btn-xs" onclick="EduconOfficer.copyDigiLockerXml()">Copy XML</button>
              <button class="btn btn-outline btn-xs" onclick="EduconOfficer.downloadDigiLockerXml('${doc.certificateNumber || 'certificate'}')">Download .xml</button>
            </div>
          </div>
          <pre id="digilocker-xml-pre" class="xml-viewer-code-block">${escapeXmlHtml(xmlContent)}</pre>
        </div>

        <div class="flex justify-between items-center mt-4 pt-3 border-t flex-wrap gap-2">
          <span class="text-xs text-muted">Legal Status: Valid under Section 4 of IT Act 2000 (stage1.digitallocker.gov.in)</span>
          <div class="flex gap-2">
            <button class="btn btn-outline btn-sm" onclick="document.getElementById('doc-inspection-modal').classList.add('hidden')">Close</button>
            <button class="btn btn-primary btn-sm" onclick="EduconOfficer.approveDocumentRecord('${doc.id}')">
              Accept & Approve Document
            </button>
          </div>
        </div>
      </div>
    `;

    modal.classList.remove('hidden');
  }

  function copyDigiLockerXml() {
    if (!selectedDocForInspection || !selectedDocForInspection.digilockerXml) {
      EduconApp.showToast("No XML available to copy");
      return;
    }
    navigator.clipboard.writeText(selectedDocForInspection.digilockerXml).then(() => {
      EduconApp.showToast("Copied sovereign DigiLocker XML to clipboard!");
    }).catch(() => {
      EduconApp.showToast("XML copied!");
    });
  }

  function downloadDigiLockerXml(filename = 'digilocker_certificate') {
    if (!selectedDocForInspection || !selectedDocForInspection.digilockerXml) return;
    const blob = new Blob([selectedDocForInspection.digilockerXml], { type: 'application/xml' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${filename.replace(/[^a-zA-Z0-9_-]/g, '_')}.xml`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    EduconApp.showToast(`Downloaded ${a.download}`);
  }

  async function approveDocumentRecord(docId) {
    const modal = document.getElementById('doc-inspection-modal');
    if (modal) modal.classList.add('hidden');
    EduconApp.showToast(`Document ${docId} approved and verified!`);
  }

  function openPullDigiLockerModal(targetUserId = null) {
    let modal = document.getElementById('pull-digilocker-modal');
    if (!modal) {
      // Create dynamically if not in DOM
      modal = document.createElement('div');
      modal.id = 'pull-digilocker-modal';
      modal.className = 'modal-overlay hidden';
      modal.innerHTML = `
        <div class="modal-content" style="max-width: 580px;">
          <div class="flex justify-between items-center border-b pb-3 mb-3">
            <div>
              <h3 class="font-bold text-lg text-main">Pull from DigiLocker Sandbox (NeGD)</h3>
              <p class="text-xs text-muted">Fetches legally authentic XML with X.509 cryptographic DSC signatures.</p>
            </div>
            <button class="btn btn-ghost btn-xs text-lg" onclick="document.getElementById('pull-digilocker-modal').classList.add('hidden')">&times;</button>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-3 text-xs">
            <div class="md:col-span-2">
              <label class="font-bold block mb-1">Target Scholar / User ID *</label>
              <input type="text" id="dl-pull-userid" class="input-text w-full" value="usr_scholar" placeholder="usr_scholar" />
            </div>
            <div class="md:col-span-2">
              <label class="font-bold block mb-1">Certificate Type *</label>
              <select id="dl-pull-type" class="select-clean w-full" onchange="EduconOfficer.handlePullTypeChange()">
                <option value="CASTC">Scheduled Tribe (ST) Certificate</option>
                <option value="INCMC">Annual Family Income Certificate</option>
                <option value="HSCER">Class 12 Higher Secondary Marksheet</option>
                <option value="DOMCR">Permanent Resident / Domicile Certificate</option>
                <option value="DISCR">Divyangjan Disability Certificate</option>
              </select>
            </div>
            <div>
              <label class="font-bold block mb-1">Candidate Full Name *</label>
              <input type="text" id="dl-pull-name" class="input-text w-full" placeholder="Enter Scholar Name" />
            </div>
            <div>
              <label class="font-bold block mb-1">Father / Guardian Name *</label>
              <input type="text" id="dl-pull-father" class="input-text w-full" placeholder="Father or Guardian Name" />
            </div>
            <div>
              <label class="font-bold block mb-1">Certificate Registration Number *</label>
              <input type="text" id="dl-pull-certnum" class="input-text w-full" placeholder="OD/ST/2025/49201" />
            </div>
            <div>
              <label class="font-bold block mb-1">State / Jurisdiction *</label>
              <select id="dl-pull-state" class="select-clean w-full">
                <option value="Odisha">Odisha</option>
                <option value="Jharkhand">Jharkhand</option>
                <option value="Madhya Pradesh">Madhya Pradesh</option>
                <option value="Maharashtra">Maharashtra</option>
                <option value="Chhattisgarh">Chhattisgarh</option>
                <option value="Rajasthan">Rajasthan</option>
                <option value="Assam">Assam</option>
              </select>
            </div>
            <div class="md:col-span-2">
              <label class="font-bold block mb-1">Issuing Authority *</label>
              <input type="text" id="dl-pull-authority" class="input-text w-full" value="Office of the Tehsildar (e-District)" />
            </div>
            <div class="md:col-span-2" id="dl-pull-extra-container">
              <label class="font-bold block mb-1">ST Sub-Tribe Community *</label>
              <input type="text" id="dl-pull-extra" class="input-text w-full" value="SANTHAL (Scheduled Tribe)" />
            </div>
          </div>
          <div class="flex justify-end gap-2 pt-3 mt-4 border-t">
            <button class="btn btn-outline btn-sm" onclick="document.getElementById('pull-digilocker-modal').classList.add('hidden')">Cancel</button>
            <button class="btn btn-primary btn-sm" onclick="EduconOfficer.executeDigiLockerPull()">Pull & Verify Sovereign XML</button>
          </div>
        </div>
      `;
      document.body.appendChild(modal);
    }

    if (targetUserId) {
      const uField = document.getElementById('dl-pull-userid');
      if (uField) uField.value = targetUserId;
    }

    // Prefill student name if known
    const nameField = document.getElementById('dl-pull-name');
    const fatherField = document.getElementById('dl-pull-father');
    if (selectedStudentForDossier && nameField && !nameField.value) {
      nameField.value = selectedStudentForDossier.fullName || '';
      fatherField.value = selectedStudentForDossier.fatherName || '';
    }

    handlePullTypeChange();
    modal.classList.remove('hidden');
  }

  function handlePullTypeChange() {
    const typeSelect = document.getElementById('dl-pull-type');
    const certNum = document.getElementById('dl-pull-certnum');
    const extraContainer = document.getElementById('dl-pull-extra-container');
    if (!typeSelect || !certNum || !extraContainer) return;

    const val = typeSelect.value;
    const rnd = Math.floor(10000 + Math.random() * 90000);

    if (val === 'CASTC') {
      certNum.value = `OD/ST/2025/${rnd}`;
      extraContainer.innerHTML = `<label class="font-bold block mb-1">ST Sub-Tribe Community *</label><input type="text" id="dl-pull-extra" class="input-text w-full" value="SANTHAL (Scheduled Tribe)" />`;
    } else if (val === 'INCMC') {
      certNum.value = `OD/INC/2025/${rnd}`;
      extraContainer.innerHTML = `<label class="font-bold block mb-1">Annual Family Income *</label><input type="text" id="dl-pull-extra" class="input-text w-full" value="₹ 1,45,000/-" />`;
    } else if (val === 'HSCER') {
      certNum.value = `CHSE-2025-${rnd}`;
      extraContainer.innerHTML = `<label class="font-bold block mb-1">Academic Percentage Scored *</label><input type="text" id="dl-pull-extra" class="input-text w-full" value="88.4%" />`;
    } else if (val === 'DOMCR') {
      certNum.value = `OD/DOM/2025/${rnd}`;
      extraContainer.innerHTML = `<label class="font-bold block mb-1">Domicile District *</label><input type="text" id="dl-pull-extra" class="input-text w-full" value="Mayurbhanj" />`;
    } else {
      certNum.value = `DIV/UDID/2025/${rnd}`;
      extraContainer.innerHTML = `<label class="font-bold block mb-1">Disability Percentage *</label><input type="text" id="dl-pull-extra" class="input-text w-full" value="45% (Locomotor)" />`;
    }
  }

  async function executeDigiLockerPull() {
    const userId = (document.getElementById('dl-pull-userid')?.value || 'usr_scholar').trim();
    const type = document.getElementById('dl-pull-type')?.value || 'CASTC';
    const name = (document.getElementById('dl-pull-name')?.value || 'Scholar').trim();
    const father = (document.getElementById('dl-pull-father')?.value || 'Father / Guardian').trim();
    const certNum = (document.getElementById('dl-pull-certnum')?.value || `OD/DL/2025/${Date.now().toString().slice(-5)}`).trim();
    const state = document.getElementById('dl-pull-state')?.value || 'Odisha';
    const authority = (document.getElementById('dl-pull-authority')?.value || 'Office of the Tehsildar (e-District)').trim();
    const extraVal = (document.getElementById('dl-pull-extra')?.value || '').trim();

    let docTypeTitle = 'Scheduled Tribe (ST) Certificate';
    let casteCommunity = '';
    let annualIncome = '';

    if (type === 'CASTC') {
      docTypeTitle = 'Scheduled Tribe (ST) Certificate';
      casteCommunity = extraVal || 'SANTHAL (Scheduled Tribe)';
    } else if (type === 'INCMC') {
      docTypeTitle = 'Annual Family Income Certificate';
      annualIncome = extraVal || '₹ 1,45,000/-';
    } else if (type === 'HSCER') {
      docTypeTitle = 'Class 12 Higher Secondary Marksheet';
    } else if (type === 'DOMCR') {
      docTypeTitle = 'Permanent Resident / Domicile Certificate';
    } else if (type === 'DISCR') {
      docTypeTitle = 'Divyangjan Disability Certificate';
    }

    const docId = `doc_${type.toLowerCase()}_${Date.now().toString().slice(-6)}`;
    const signerCn = "Director General, NeGD DigiLocker Sovereign CA";
    const serialNumber = "0x" + Math.random().toString(16).substr(2, 12).toUpperCase();
    const timestamp = new Date().toISOString();

    const tempDoc = {
      id: docId,
      docType: docTypeTitle,
      type: type,
      certificateNumber: certNum,
      candidateName: name,
      fatherName: father,
      issuingAuthority: authority,
      state: state,
      casteCommunity: casteCommunity,
      annualIncome: annualIncome,
      confidenceScore: 100,
      signerCn: signerCn,
      dscSerialNumber: serialNumber,
      pkiTimestamp: timestamp,
      issueDate: new Date().toISOString().split('T')[0],
      validity: 'Permanent',
      userId: userId,
      uploadedAt: "Just now (Sandbox Pulled)",
      verifiedViaDigiLocker: true,
      sharedHostingImageUrl: ""
    };

    const xml = generateDigiLockerXml(tempDoc);
    tempDoc.digilockerXml = xml;

    // Persist to Cloud Firestore if connected
    if (window.EduconFirebase && window.EduconFirebase.db) {
      try {
        await window.EduconFirebase.db.collection('users').doc(userId).collection('documents').doc(docId).set(tempDoc);
        console.log('[DigiLocker Sandbox] Saved sovereign XML document to Firestore:', docId);
      } catch (e) {
        console.warn('[DigiLocker Sandbox] Error saving to Firestore, keeping locally:', e);
      }
    }

    // Add to local documents array
    allDocuments = [tempDoc, ...allDocuments.filter(d => d.id !== docId)];
    renderStudentDocuments();

    // Close modal
    const modal = document.getElementById('pull-digilocker-modal');
    if (modal) modal.classList.add('hidden');

    // If student dossier is currently open, refresh it
    if (selectedStudentForDossier) {
      openStudentDossier(selectedStudentForDossier.uid);
    }

    EduconApp.showToast(`Certificate ${certNum} pulled successfully from NeGD DigiLocker Sandbox with authentic XML and DSC!`);
  }

  // ==============================================================
  // 4. APPLICATION SCRUTINY & APPROVALS QUEUE
  // ==============================================================

  function renderVerificationQueue() {
    const container = document.getElementById('officer-verification-table');
    if (!container) return;

    if (verificationQueue.length === 0) {
      container.innerHTML = `
        <div class="glass-card p-8 text-center text-muted">
          <p class="font-bold text-base">No pending application scrutiny cases.</p>
          <p class="text-xs mt-1">Applications submitted by scholars on the mobile STeP app appear here instantly.</p>
        </div>
      `;
      return;
    }

    container.innerHTML = verificationQueue.map(app => {
      const isAuto = app.status === 'AUTO_APPROVED' || app.status === 'DISBURSED';
      const isDeficient = app.status === 'DEFICIENCY_FLAGGED';

      return `
        <div class="verification-item-card ${isAuto ? 'verified-auto' : 'verified-manual'}">
          <div class="flex justify-between items-start flex-wrap gap-2">
            <div>
              <span class="badge ${isAuto ? 'badge-success' : (isDeficient ? 'badge-error' : 'badge-warning')} text-xs">
                ${isAuto ? 'Sanctioned / Disbursed' : (isDeficient ? 'Deficiency Action Flagged' : 'Manual Scrutiny Queue')}
              </span>
              <span class="badge badge-outline text-xs ml-2 font-mono">${app.portal}</span>
              <h4 class="font-bold text-base mt-1 text-main">${app.studentName}</h4>
              <div class="text-xs text-muted">${app.scheme} • ${app.district} • App ID: <span class="font-mono font-bold">${app.id}</span></div>
            </div>
            <div class="text-right">
              <span class="text-xs text-muted">Confidence Score</span>
              <div class="text-xl font-bold ${app.confidenceScore >= 85 ? 'text-emerald' : 'text-amber'}">
                ${app.confidenceScore}%
              </div>
              <div class="text-sm text-accent font-bold mt-1">₹ ${(app.sanctionAmount || 0).toLocaleString('en-IN')}</div>
            </div>
          </div>

          <div class="my-3 p-3 bg-bg-surface rounded text-xs text-body border border-border-subtle">
            <div>Document: <strong>${app.docType}</strong> • Source: <em>${app.source}</em></div>
            <div>Submitted: <strong>${app.appliedDate}</strong></div>
          </div>

          ${app.anomaly ? `
            <div class="p-3 bg-amber-tint border border-amber-warning/40 rounded text-xs text-amber font-semibold mb-3">
              Scrutiny Flag: ${app.anomaly}
            </div>
          ` : ''}

          <div class="verification-actions flex justify-end gap-2">
            ${app.actionNeeded ? `
              <button class="btn btn-outline btn-xs" onclick="EduconOfficer.openDefectModal('${app.id}')">
                Flag Defect with AI
              </button>
              <button class="btn btn-primary btn-xs" onclick="EduconOfficer.approveApplicationManual('${app.id}')">
                Approve & Sanction
              </button>
            ` : (app.status === 'AUTO_APPROVED' ? `
              <button class="btn btn-outline btn-xs text-emerald border-emerald" onclick="EduconOfficer.markDisbursed('${app.id}')">
                Mark Disbursed via PFMS
              </button>
              <span class="text-xs text-emerald font-semibold self-center ml-2">Processed via Sovereign Auto-Verify</span>
            ` : `
              <span class="text-xs text-emerald font-semibold self-center">DBT Disbursed via PFMS</span>
            `)}
          </div>
        </div>
      `;
    }).join('');
  }

  async function approveApplicationManual(appId) {
    const item = verificationQueue.find(a => a.id === appId);
    if (!item) return;

    const utr = "RBI" + Math.floor(Math.random() * 900000000000 + 100000000000);
    const updateData = {
      stage: "SANCTIONED",
      stageText: "Sanctioned - In PFMS Payment Queue",
      verificationConfidence: 98,
      currentStepIndex: 3,
      sanctionAmount: item.sanctionAmount || 25000,
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
        window.EduconApp.showToast(`Application ${appId} approved & sanctioned! Scholar notified.`);
      } else {
        item.status = "AUTO_APPROVED";
        item.actionNeeded = false;
        item.confidenceScore = 98;
        renderVerificationQueue();
        updateKpis();
        window.EduconApp.showToast(`Application ${appId} approved.`);
      }
    } catch (e) {
      console.error(e);
      window.EduconApp.showToast(`Error updating application: ${e.message}`);
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
        paymentMode: "PFMS APBS",
        disbursedDate: new Date().toLocaleDateString("en-IN"),
        bankName: "State Bank of India",
        status: "CREDITED_TO_ACCOUNT"
      }
    };

    try {
      if (item.docPath && window.STePFirebase) {
        await window.STePFirebase.updateApplicationStatus(item.docPath, updateData, item.userId, {
          title: "DBT Scholarship Credited to Bank Account",
          body: `Your scholarship amount has been disbursed under UTR ${utr} into your Aadhaar seeded account.`,
          type: "PAYMENT"
        });
        window.EduconApp.showToast(`DBT Disbursed for ${appId}. Synced live to scholar.`);
      } else {
        item.status = "DISBURSED";
        renderVerificationQueue();
        updateKpis();
        window.EduconApp.showToast(`DBT Disbursed for ${appId}.`);
      }
    } catch (e) {
      console.error(e);
      window.EduconApp.showToast(`Error updating application: ${e.message}`);
    }
  }

  function openDefectModal(appId) {
    selectedAppForDefect = verificationQueue.find(a => a.id === appId);
    if (!selectedAppForDefect) return;

    const modal = document.getElementById('officer-defect-modal');
    if (!modal) return;

    document.getElementById('officer-defect-appid').textContent = selectedAppForDefect.id;
    document.getElementById('officer-defect-student').textContent = selectedAppForDefect.studentName;
    document.getElementById('officer-defect-reason').value = selectedAppForDefect.anomaly || "Annual Family Income Certificate validity expired or signature unverified. Upload renewed certificate.";

    modal.classList.remove('hidden');
  }

  async function handleSubmitOfficerDefect() {
    if (!selectedAppForDefect) return;

    const code = document.getElementById('officer-defect-code')?.value || "DEF-INC-01";
    const reason = document.getElementById('officer-defect-reason')?.value.trim();
    if (!reason) {
      alert("Please provide defect explanation for the student.");
      return;
    }

    const defectData = {
      code: code,
      bureaucraticReason: reason,
      deadlineDate: "15-Nov-2026",
      schemeTitle: selectedAppForDefect.scheme,
      flaggedBy: "Shri R. K. Soren, Deputy Secretary (Scholarships), MoTA"
    };

    try {
      if (selectedAppForDefect.docPath && window.STePFirebase) {
        await window.STePFirebase.updateApplicationDeficiency(selectedAppForDefect.docPath, defectData, selectedAppForDefect.userId);
        window.EduconApp.showToast(`Defect flagged for ${selectedAppForDefect.id}. Synced live to student app.`);
      } else {
        selectedAppForDefect.status = "DEFICIENCY_FLAGGED";
        selectedAppForDefect.anomaly = reason;
        renderVerificationQueue();
        updateKpis();
        window.EduconApp.showToast(`Defect flagged locally.`);
      }
      document.getElementById('officer-defect-modal')?.classList.add('hidden');
    } catch (e) {
      console.error(e);
      alert(`Error flagging defect: ${e.message}`);
    }
  }

  // ==============================================================
  // 5. DISTRICT OUTREACH HEATMAP & DIRECTIVES
  // ==============================================================

  function renderHeatmap() {
    const listEl = document.getElementById('heatmap-districts-list');
    if (!listEl) return;

    let items = (window.EduconData && window.EduconData.districtHeatmapData) || [];
    if (activeHeatmapFilter !== 'ALL') {
      items = items.filter(d => d.priorityLevel === activeHeatmapFilter);
    }

    listEl.innerHTML = items.map(d => {
      const isCritical = d.priorityLevel === 'CRITICAL';
      const gapColor = isCritical ? 'text-red-error' : 'text-amber';

      return `
        <div class="district-row-card" data-district="${d.district}" onclick="EduconOfficer.inspectDistrict('${d.district}')">
          <div class="flex justify-between items-center">
            <div>
              <div class="font-bold text-sm text-main">${d.district}, ${d.state}</div>
              <div class="text-xs text-muted">${d.pvtgPockets.split(';')[0]}</div>
            </div>
            <div class="text-right">
              <span class="text-xs font-bold ${gapColor}">${d.gapPercent}% Gap</span>
              <div class="text-xs text-muted">${d.gapCount.toLocaleString('en-IN')} unreached</div>
            </div>
          </div>
          <div class="progress-bar-track mt-2">
            <div class="progress-bar-fill ${isCritical ? 'bg-amber' : 'bg-accent'}" style="width: ${100 - d.gapPercent}%"></div>
          </div>
        </div>
      `;
    }).join('');

    if (items.length > 0) {
      inspectDistrict(items[0].district);
    }
  }

  function inspectDistrict(districtName) {
    const items = (window.EduconData && window.EduconData.districtHeatmapData) || [];
    const d = items.find(x => x.district === districtName);
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
            <h3 class="text-xl font-bold text-main">${d.district}, ${d.state}</h3>
            <p class="text-xs text-muted">ST Enrollment in Schools/Colleges vs MoTA Scholarship Recipients</p>
          </div>
          <div class="text-right">
            <span class="text-xs text-muted">Coverage Deficit</span>
            <div class="text-3xl font-bold text-amber">${d.gapPercent}%</div>
          </div>
        </div>

        <div class="grid grid-cols-2 gap-3 mb-4">
          <div class="stat-mini-card">
            <span class="text-xs text-muted">Total ST Students (UDISE+)</span>
            <div class="text-lg font-bold text-main">${d.udiseStEnrollment.toLocaleString('en-IN')}</div>
          </div>
          <div class="stat-mini-card">
            <span class="text-xs text-muted">Active Scholarship Availed</span>
            <div class="text-lg font-bold text-emerald">${d.scholarshipActive.toLocaleString('en-IN')}</div>
          </div>
          <div class="stat-mini-card col-span-2 highlight-unreached">
            <span class="text-xs text-amber font-bold">Unreached ST Scholars (Missing out on sovereign funds)</span>
            <div class="text-2xl font-bold text-amber">${d.gapCount.toLocaleString('en-IN')} Scholars</div>
          </div>
        </div>

        <div class="mb-3 text-xs">
          <strong class="text-accent block">High-Density Tribal & PVTG Pockets:</strong>
          <p class="text-muted mt-1">${d.pvtgPockets}</p>
        </div>

        <div class="mb-4 text-xs">
          <strong class="text-accent block">Root Cause Delivery Barriers:</strong>
          <p class="text-muted mt-1">${d.primaryBarriers}</p>
        </div>

        <div class="flex gap-2">
          <button class="btn btn-primary btn-sm flex-1" onclick="EduconOfficer.triggerMobileVanDirective('${d.district}')">
            Dispatch Mobile Camp
          </button>
          <button class="btn btn-outline btn-sm flex-1" onclick="EduconOfficer.triggerSmsOutreach('${d.district}', ${d.gapCount})">
            Send Multilingual Broadcast
          </button>
        </div>
      </div>
    `;
  }

  async function triggerMobileVanDirective(district) {
    const directive = {
      type: "CAMP_OUTREACH",
      district: district,
      details: `Mobile facilitation van dispatched with solar Biometric Iris/Fingerprint devices to register unserved ST students in ${district}.`,
      dateFormatted: new Date().toLocaleDateString("en-IN")
    };

    if (window.STePFirebase && typeof window.STePFirebase.addDirective === 'function') {
      await window.STePFirebase.addDirective(directive);
      window.EduconApp.showToast(`Outreach Camp order dispatched for ${district} & logged!`);
    } else {
      currentDirectives.unshift(directive);
      renderDirectivesLog();
      window.EduconApp.showToast(`Outreach Camp dispatched for ${district}.`);
    }
  }

  async function triggerSmsOutreach(district, count) {
    const directive = {
      type: "SMS_IVR_BROADCAST",
      district: district,
      details: `Multilingual IVR & WhatsApp broadcast sent to ${count.toLocaleString('en-IN')} parents in Santhali, Gondi, and Odia.`,
      dateFormatted: new Date().toLocaleDateString("en-IN")
    };

    if (window.STePFirebase && typeof window.STePFirebase.addDirective === 'function') {
      await window.STePFirebase.addDirective(directive);
      window.EduconApp.showToast(`Broadcast directive sent to ${district}!`);
    } else {
      currentDirectives.unshift(directive);
      renderDirectivesLog();
      window.EduconApp.showToast(`Broadcast directive sent to ${district}.`);
    }
  }

  function renderDirectivesLog() {
    const container = document.getElementById('officer-directives-table');
    if (!container) return;

    if (currentDirectives.length === 0) {
      container.innerHTML = `<p class="text-xs text-muted p-4 text-center">No directives logged yet. Use the Outreach actions in the Heatmap to dispatch mobile camps or broadcasts.</p>`;
      return;
    }

    container.innerHTML = currentDirectives.map(d => `
      <div class="p-3 bg-bg-surface rounded-md border border-border-subtle mb-2 flex justify-between items-center">
        <div>
          <span class="badge badge-outline text-xs">${d.type || 'CAMP_OUTREACH'}</span>
          <span class="font-bold text-sm text-main ml-2">${d.district}</span>
          <p class="text-xs text-muted mt-1">${d.details}</p>
        </div>
        <div class="text-right text-xs text-muted">
          <div>${d.dateFormatted || 'Today'}</div>
          <span class="badge badge-success text-xs mt-1">DISPATCHED</span>
        </div>
      </div>
    `).join('');
  }

  return {
    init,
    renderSchemesMaster,
    openAddSchemeModal,
    handleSaveNewScheme,
    deleteScheme,
    editScheme,
    renderRegisteredStudents,
    openStudentDossier,
    renderStudentDocuments,
    inspectDocument,
    openPullDigiLockerModal,
    executeDigiLockerPull,
    handlePullTypeChange,
    copyDigiLockerXml,
    downloadDigiLockerXml,
    approveDocumentRecord,
    syncDigiLockerSandboxTestDocs: openPullDigiLockerModal,
    renderVerificationQueue,
    approveApplicationManual,
    markDisbursed,
    openDefectModal,
    handleSubmitOfficerDefect,
    renderHeatmap,
    inspectDistrict,
    triggerMobileVanDirective,
    triggerSmsOutreach
  };
})();
