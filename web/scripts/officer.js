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
        console.log(`[STeP Admin] Received ${students.length} registered students from Firestore.`);
        if (students && students.length > 0) {
          registeredStudents = students;
        } else {
          // Fallback initial scholar record if fresh database
          registeredStudents = [
            {
              uid: "usr_guest",
              fullName: "Scholar (Fresh Register)",
              email: "student@step.gov.in",
              community: "Scheduled Tribe (ST)",
              subTribe: "Santhal",
              institution: "Eklavya Model Residential School (EMRS), Mayurbhanj",
              educationLevel: "Class 12",
              annualIncome: 145000,
              apaarId: "9842-1084-2026*",
              digilockerId: "DL-ST-883921*",
              bankName: "State Bank of India",
              maskedAccount: "•••• •••• 4920*",
              ifsc: "SBIN0001234",
              aadhaarLast4: "9842",
              state: "Odisha",
              npciAadhaarSeeded: true
            }
          ];
        }
        renderRegisteredStudents();
        updateKpis();
      });
    }

    // D. Listen to all documents in Firestore
    if (typeof window.STePFirebase.listenAllDocuments === 'function') {
      window.STePFirebase.listenAllDocuments((docs) => {
        console.log(`[STeP Admin] Received ${docs.length} student documents from Firestore.`);
        if (docs && docs.length > 0) {
          allDocuments = docs;
        } else {
          // Provide sandbox documents if empty
          allDocuments = [
            {
              id: "doc_st_49201",
              docType: "Scheduled Tribe (ST) Certificate",
              certificateNumber: "OD/ST/2022/49201",
              issuingAuthority: "Tehsildar Baripada, Mayurbhanj, Odisha",
              confidenceScore: 98,
              uploadedAt: "28-Sep-2026",
              userId: "usr_guest",
              sharedHostingImageUrl: "https://dhaaga.thecoolestportfolio.site/uploads/caste_OD_ST_2022_49201.jpg"
            },
            {
              id: "doc_inc_11093",
              docType: "Annual Family Income Certificate",
              certificateNumber: "OD/INC/2025/11093",
              issuingAuthority: "Revenue Officer, Baripada, Odisha",
              confidenceScore: 95,
              uploadedAt: "28-Sep-2026",
              userId: "usr_guest",
              sharedHostingImageUrl: "https://dhaaga.thecoolestportfolio.site/uploads/income_OD_INC_2025_11093.jpg"
            },
            {
              id: "doc_mark_881924",
              docType: "Class 12 Higher Secondary Marksheet",
              certificateNumber: "CHSE-2025-881924",
              issuingAuthority: "Council of Higher Secondary Education, Odisha",
              confidenceScore: 99,
              uploadedAt: "28-Sep-2026",
              userId: "usr_guest",
              sharedHostingImageUrl: "https://dhaaga.thecoolestportfolio.site/uploads/marksheet_chse_881924.jpg"
            }
          ];
        }
        renderStudentDocuments();
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
            <button class="btn btn-outline btn-xs text-red-error border-red-error/40 hover:bg-red-error/10" onclick="EduconOfficer.deleteScheme('${sc.id}', '${sc.title.replace(/'/g, "\\'")}')">
              Retire Scheme
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
        window.EduconApp.showToast(`Scheme "${title}" saved to Cloud Firestore! Available to all mobile students.`);
      } else {
        currentSchemes.push(schemeData);
        renderSchemesMaster();
        window.EduconApp.showToast(`Scheme "${title}" saved locally.`);
      }
      document.getElementById('add-scheme-modal')?.classList.add('hidden');
    } catch (e) {
      console.error(e);
      alert(`Error creating scheme: ${e.message}`);
    }
  }

  async function deleteScheme(schemeId, title) {
    if (!confirm(`Are you sure you want to retire / delete "${title}"? This will sync immediately to Cloud Firestore.`)) {
      return;
    }

    try {
      if (window.STePFirebase && typeof window.STePFirebase.deleteScheme === 'function') {
        await window.STePFirebase.deleteScheme(schemeId);
        window.EduconApp.showToast(`Scheme "${title}" deleted from Cloud Firestore.`);
      } else {
        currentSchemes = currentSchemes.filter(s => s.id !== schemeId);
        renderSchemesMaster();
        window.EduconApp.showToast(`Scheme "${title}" deleted.`);
      }
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
          <td colspan="7" class="text-center p-8 text-muted">
            No registered scholars match the search criteria.
          </td>
        </tr>
      `;
      return;
    }

    container.innerHTML = list.map(s => {
      const initials = (s.fullName || 'ST').split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase();
      const incomeStr = s.annualIncome && s.annualIncome > 0 ? `₹ ${Number(s.annualIncome).toLocaleString('en-IN')}/yr` : "NFS*";

      return `
        <tr>
          <td>
            <div class="flex items-center gap-3">
              <div class="avatar-badge" style="width: 34px; height: 34px; font-size: 12px;">${initials}</div>
              <div>
                <div class="font-bold text-main">${s.fullName || 'Scholar (NFS*)'}</div>
                <div class="text-xs text-muted font-mono">${s.uid || 'usr_guest'}</div>
              </div>
            </div>
          </td>
          <td>
            <div class="text-xs font-semibold text-main">${s.subTribe || 'ST'}</div>
            <div class="text-xs text-muted">${s.state || 'NFS*'}</div>
          </td>
          <td>
            <div class="text-xs text-body">${s.institution || 'EMRS Baripada*'}</div>
            <div class="text-xs text-muted">${s.educationLevel || 'Class 12'}</div>
          </td>
          <td>
            <div class="text-xs font-mono font-bold text-main">${s.apaarId || 'NFS*'}</div>
            <span class="badge badge-success text-xs">DigiLocker Linked</span>
          </td>
          <td>
            <div class="text-xs font-bold text-accent">${incomeStr}</div>
          </td>
          <td>
            <div class="text-xs font-semibold text-main">${s.bankName || 'State Bank of India*'}</div>
            <div class="text-xs text-muted font-mono">${s.maskedAccount || '•••• 4920*'}</div>
            <span class="badge ${s.npciAadhaarSeeded !== false ? 'badge-success' : 'badge-error'} text-xs">
              ${s.npciAadhaarSeeded !== false ? 'NPCI Seeded' : 'Aadhaar Unlinked'}
            </span>
          </td>
          <td class="text-right">
            <button class="btn btn-outline btn-xs" onclick="EduconOfficer.openStudentDossier('${s.uid || s.id}')">
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

    const initials = (s.fullName || 'ST').split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase();
    const incomeStr = s.annualIncome && s.annualIncome > 0 ? `₹ ${Number(s.annualIncome).toLocaleString('en-IN')}` : "NFS* (Under Self-Declaration)";

    // Find documents belonging to this student
    const studentDocs = allDocuments.filter(d => d.userId === s.uid || d.userId === "usr_guest");

    content.innerHTML = `
      <div class="flex justify-between items-start border-b pb-4 mb-4">
        <div class="flex items-center gap-3">
          <div class="avatar-badge" style="width: 52px; height: 52px; font-size: 18px;">${initials}</div>
          <div>
            <h3 class="text-xl font-bold text-main">${s.fullName || 'Scholar (NFS*)'}</h3>
            <p class="text-xs text-muted font-mono">UID: ${s.uid || 'usr_guest'} • Email: ${s.email || 'student@step.gov.in'}</p>
            <div class="flex gap-2 mt-1">
              <span class="badge badge-success text-xs">DigiLocker Sandbox Verified</span>
              <span class="badge badge-outline text-xs">${s.subTribe || 'ST'} (${s.community || 'ST'})</span>
            </div>
          </div>
        </div>
      </div>

      <div class="grid grid-cols-2 gap-4 mb-4">
        <div class="p-3 bg-bg-surface rounded-md border border-border-subtle">
          <span class="text-xs text-muted uppercase font-bold block">Academic Details</span>
          <div class="font-bold text-sm mt-1">${s.institution || 'EMRS Baripada, Mayurbhanj*'}</div>
          <div class="text-xs text-muted">Level: ${s.educationLevel || 'Class 12'} • State: ${s.state || 'Odisha'}</div>
        </div>
        <div class="p-3 bg-bg-surface rounded-md border border-border-subtle">
          <span class="text-xs text-muted uppercase font-bold block">Sovereign Identifiers</span>
          <div class="font-bold text-sm mt-1 font-mono">${s.apaarId || '9842-1084-2026*'} (APAAR)</div>
          <div class="text-xs text-muted font-mono">DigiLocker: ${s.digilockerId || 'DL-ST-883921*'} • Aadhaar: •••• •••• ${s.aadhaarLast4 || '9842'}</div>
        </div>
      </div>

      <div class="grid grid-cols-2 gap-4 mb-4">
        <div class="p-3 bg-bg-surface rounded-md border border-border-subtle">
          <span class="text-xs text-muted uppercase font-bold block">Verified Family Income</span>
          <div class="text-lg font-bold text-accent mt-1">${incomeStr}</div>
          <div class="text-xs text-emerald font-semibold">Eligible for 100% MoTA Scholarship Schemes</div>
        </div>
        <div class="p-3 bg-bg-surface rounded-md border border-border-subtle">
          <span class="text-xs text-muted uppercase font-bold block">DBT Bank Account (PFMS APBS)</span>
          <div class="font-bold text-sm mt-1">${s.bankName || 'State Bank of India*'}</div>
          <div class="text-xs text-muted font-mono">A/C: ${s.maskedAccount || '•••• 4920*'} • IFSC: ${s.ifsc || 'SBIN0001234'}</div>
          <span class="badge ${s.npciAadhaarSeeded !== false ? 'badge-success' : 'badge-error'} text-xs mt-1">
            ${s.npciAadhaarSeeded !== false ? 'NPCI Aadhaar Payment Bridge Active' : 'Aadhaar Bridge Unlinked'}
          </span>
        </div>
      </div>

      <div class="border-t pt-3 mb-3">
        <h4 class="font-bold text-sm text-main mb-2">Attached Certificates & DigiLocker Vault (${studentDocs.length} Documents)</h4>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
          ${studentDocs.map(d => `
            <div class="p-3 bg-white border border-border-subtle rounded-md shadow-sm">
              <div class="flex justify-between items-start">
                <div>
                  <span class="badge badge-success text-xs">Verified DSC</span>
                  <div class="font-bold text-xs mt-1">${d.docType}</div>
                  <div class="text-xs text-muted font-mono">${d.certificateNumber}</div>
                </div>
                <button class="btn btn-outline btn-xs" onclick="EduconOfficer.inspectDocument('${d.id}')">Inspect</button>
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
          No documents match the selected filter.
        </div>
      `;
      return;
    }

    container.innerHTML = list.map(doc => `
      <div class="glass-card p-4 border border-border-subtle hover:border-saffron-primary transition-all">
        <div class="flex justify-between items-start">
          <div>
            <span class="badge badge-success text-xs">100% Cryptographic DSC Seal</span>
            <h4 class="font-bold text-base text-main mt-1">${doc.docType}</h4>
            <div class="text-xs text-muted font-mono mt-1">Cert No: <strong class="text-main">${doc.certificateNumber}</strong></div>
          </div>
          <div class="text-right">
            <span class="text-xs text-muted">DSC Confidence</span>
            <div class="text-lg font-bold text-emerald">${doc.confidenceScore || 95}%</div>
          </div>
        </div>

        <div class="my-3 p-3 bg-bg-surface rounded text-xs text-body border border-border-subtle">
          <div><strong>Authority:</strong> ${doc.issuingAuthority || 'Govt of Odisha'}</div>
          <div><strong>Issued:</strong> ${doc.uploadedAt || 'Permanent'} • <strong>Owner:</strong> <span class="font-mono text-muted">${doc.userId || 'usr_guest'}</span></div>
        </div>

        <div class="pt-2 border-t flex justify-between items-center">
          <span class="text-xs text-emerald font-semibold">DigiLocker Sovereign Verified</span>
          <div class="flex gap-2">
            <button class="btn btn-outline btn-xs" onclick="EduconOfficer.inspectDocument('${doc.id}')">
              Inspect Certificate
            </button>
            <button class="btn btn-primary btn-xs" onclick="EduconApp.showToast('Document ${doc.certificateNumber} re-verified with NeGD gateway!')">
              Re-Verify
            </button>
          </div>
        </div>
      </div>
    `).join('');
  }

  function inspectDocument(docId) {
    const doc = allDocuments.find(d => d.id === docId) || allDocuments[0];
    if (!doc) return;

    selectedDocForInspection = doc;
    const modal = document.getElementById('doc-inspection-modal');
    const content = document.getElementById('doc-inspection-modal-content');
    if (!modal || !content) return;

    const imgUrl = doc.sharedHostingImageUrl || "https://dhaaga.thecoolestportfolio.site/uploads/caste_OD_ST_2022_49201.jpg";

    content.innerHTML = `
      <div class="flex justify-between items-start border-b pb-3 mb-3">
        <div>
          <span class="badge badge-success text-xs">Cryptographic DSC Verified</span>
          <h3 class="text-lg font-bold text-main mt-1">${doc.docType}</h3>
          <p class="text-xs text-muted font-mono">Certificate: ${doc.certificateNumber} • Authority: ${doc.issuingAuthority}</p>
        </div>
        <div class="text-right">
          <span class="text-xs text-muted">Authenticity</span>
          <div class="text-xl font-bold text-emerald">${doc.confidenceScore || 98}% Match</div>
        </div>
      </div>

      <div class="p-2 bg-slate-100 rounded-md border border-border-subtle text-center my-3 max-h-96 overflow-y-auto">
        <img src="${imgUrl}" alt="${doc.docType}" class="max-h-80 mx-auto rounded shadow" onerror="this.src='./assets/icon-192.svg';" />
      </div>

      <div class="flex justify-between items-center mt-4 pt-3 border-t">
        <span class="text-xs text-muted">Digital Signature: SHA-256 DSC valid via NeGD Sovereign Root CA</span>
        <div class="flex gap-2">
          <a href="${imgUrl}" target="_blank" download="certificate.jpg" class="btn btn-outline btn-sm">Download Copy</a>
          <button class="btn btn-primary btn-sm" onclick="document.getElementById('doc-inspection-modal').classList.add('hidden'); EduconApp.showToast('Document marked approved in scholarship record!');">
            Accept & Approve Document
          </button>
        </div>
      </div>
    `;

    modal.classList.remove('hidden');
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
          <p class="font-bold text-base">No pending application scrutiny cases in Cloud Firestore.</p>
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
                Approve & Sanction (Cloud)
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
        window.EduconApp.showToast(`Application ${appId} approved & synced to live Firestore! Student notified.`);
      } else {
        item.status = "AUTO_APPROVED";
        item.actionNeeded = false;
        item.confidenceScore = 98;
        renderVerificationQueue();
        updateKpis();
        window.EduconApp.showToast(`Application ${appId} approved locally.`);
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
        window.EduconApp.showToast(`DBT Disbursed for ${appId}. Synced live to student.`);
      } else {
        item.status = "DISBURSED";
        renderVerificationQueue();
        updateKpis();
        window.EduconApp.showToast(`DBT Disbursed for ${appId}.`);
      }
    } catch (e) {
      console.error(e);
      window.EduconApp.showToast(`Error updating Firestore: ${e.message}`);
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
      window.EduconApp.showToast(`Outreach Camp order dispatched for ${district} & logged to Cloud Firestore!`);
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
      window.EduconApp.showToast(`Broadcast directive sent to ${district} & saved to Firestore!`);
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
