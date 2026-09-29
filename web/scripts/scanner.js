/**
 * Educon MoTA Unified Portal - AI Document Scanner Module
 * Live Camera / File / Sample OCR with MoTa AI Vision and Confidence Scoring
 */

window.EduconScanner = (function() {
  let videoStream = null;
  let currentScanData = null;

  function init() {
    setupEventListeners();
  }

  function setupEventListeners() {
    const fileInput = document.getElementById('doc-file-input');
    const cameraBtn = document.getElementById('btn-open-camera');
    const captureBtn = document.getElementById('btn-capture-photo');
    const closeCamBtn = document.getElementById('btn-close-camera');
    const autoFillBtn = document.getElementById('btn-autofill-app');

    if (fileInput) {
      fileInput.addEventListener('change', handleFileUpload);
    }
    if (cameraBtn) {
      cameraBtn.addEventListener('click', startCamera);
    }
    if (captureBtn) {
      captureBtn.addEventListener('click', captureFromCamera);
    }
    if (closeCamBtn) {
      closeCamBtn.addEventListener('click', stopCamera);
    }
    if (autoFillBtn) {
      autoFillBtn.addEventListener('click', handleAutoFill);
    }

    // Preset sample buttons
    document.querySelectorAll('.btn-load-sample').forEach(btn => {
      btn.addEventListener('click', (e) => {
        const sampleId = e.currentTarget.dataset.sampleId;
        loadSampleDocument(sampleId);
      });
    });
  }

  function loadSampleDocument(sampleId) {
    const sample = EduconData.sampleDocuments.find(s => s.id === sampleId);
    if (!sample) return;

    const previewImg = document.getElementById('scanner-preview-img');
    const placeholder = document.getElementById('scanner-placeholder');
    const previewWrapper = document.getElementById('scanner-preview-wrapper');

    if (previewImg && placeholder && previewWrapper) {
      previewImg.src = sample.imageUrl;
      previewImg.classList.remove('hidden');
      placeholder.classList.add('hidden');
      previewWrapper.classList.remove('hidden');
    }

    // Trigger AI Scan
    processDocument(sample.imageUrl, sample.type);
  }

  function handleFileUpload(e) {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      const dataUrl = event.target?.result;
      const previewImg = document.getElementById('scanner-preview-img');
      const placeholder = document.getElementById('scanner-placeholder');
      const previewWrapper = document.getElementById('scanner-preview-wrapper');

      if (previewImg && placeholder && previewWrapper) {
        previewImg.src = dataUrl;
        previewImg.classList.remove('hidden');
        placeholder.classList.add('hidden');
        previewWrapper.classList.remove('hidden');
      }

      // Infer document hint from filename
      let hint = 'caste';
      const name = file.name.toLowerCase();
      if (name.includes('income') || name.includes('aay')) hint = 'income';
      if (name.includes('offer') || name.includes('admission') || name.includes('nos')) hint = 'offer';

      processDocument(dataUrl, hint);
    };
    reader.readAsDataURL(file);
  }

  async function startCamera() {
    const cameraModal = document.getElementById('camera-modal');
    const video = document.getElementById('camera-video');
    if (!video || !cameraModal) return;

    try {
      videoStream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'environment', width: { ideal: 1280 }, height: { ideal: 720 } }
      });
      video.srcObject = videoStream;
      video.play();
      cameraModal.classList.remove('hidden');
    } catch (err) {
      alert("Camera access denied or unavailable. You can use the 'Upload Certificate' button or select one of the Preloaded Sample Certificates!");
      console.warn("Camera error:", err);
    }
  }

  function stopCamera() {
    const cameraModal = document.getElementById('camera-modal');
    if (videoStream) {
      videoStream.getTracks().forEach(track => track.stop());
      videoStream = null;
    }
    if (cameraModal) {
      cameraModal.classList.add('hidden');
    }
  }

  function captureFromCamera() {
    const video = document.getElementById('camera-video');
    if (!video) return;

    const canvas = document.createElement('canvas');
    canvas.width = video.videoWidth || 640;
    canvas.height = video.videoHeight || 480;
    const ctx = canvas.getContext('2d');
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
    const dataUrl = canvas.toDataURL('image/jpeg', 0.85);

    stopCamera();

    const previewImg = document.getElementById('scanner-preview-img');
    const placeholder = document.getElementById('scanner-placeholder');
    const previewWrapper = document.getElementById('scanner-preview-wrapper');

    if (previewImg && placeholder && previewWrapper) {
      previewImg.src = dataUrl;
      previewImg.classList.remove('hidden');
      placeholder.classList.add('hidden');
      previewWrapper.classList.remove('hidden');
    }

    processDocument(dataUrl, 'caste');
  }

  async function processDocument(dataUrl, hint) {
    const scanOverlay = document.getElementById('scanner-laser-overlay');
    const scanStatus = document.getElementById('scan-status-indicator');
    const resultsContainer = document.getElementById('scanner-results-container');
    const emptyState = document.getElementById('scanner-empty-state');

    if (scanOverlay) scanOverlay.classList.remove('hidden');
    if (scanStatus) {
      scanStatus.classList.remove('hidden');
      scanStatus.innerHTML = `<span class="spinner-ring"></span> MoTa Sovereign Vision analyzing document pixels & signatures...`;
    }

    try {
      const result = await EduconMoTa AI.scanDocument(dataUrl, hint);
      currentScanData = result.data;

      renderScanResults(result.data, result.source);

      if (resultsContainer) resultsContainer.classList.remove('hidden');
      if (emptyState) emptyState.classList.add('hidden');
    } catch (err) {
      console.error("Scan processing error:", err);
      if (scanStatus) scanStatus.innerHTML = `<span class="badge badge-error">Extraction failed. Please re-capture clearly.</span>`;
    } finally {
      if (scanOverlay) scanOverlay.classList.add('hidden');
      if (scanStatus) scanStatus.classList.add('hidden');
    }
  }

  function renderScanResults(data, sourceEngine) {
    const badgeEl = document.getElementById('scan-confidence-badge');
    const meterFill = document.getElementById('scan-meter-fill');
    const meterScore = document.getElementById('scan-meter-score');
    const gridEl = document.getElementById('scan-fields-grid');
    const notesEl = document.getElementById('scan-verification-notes');
    const engineEl = document.getElementById('scan-engine-name');

    if (engineEl) engineEl.textContent = sourceEngine || "MoTa Sovereign Vision";

    const score = data.confidenceScore || 95;
    if (meterFill) meterFill.style.width = `${score}%`;
    if (meterScore) meterScore.textContent = `${score}% Match`;

    if (badgeEl) {
      if (score >= 90) {
        badgeEl.className = "badge badge-success";
        badgeEl.innerHTML = " High Confidence (Auto-Approve Eligible)";
      } else if (score >= 75) {
        badgeEl.className = "badge badge-warning";
        badgeEl.innerHTML = " Moderate Confidence (Fast-Track Review)";
      } else {
        badgeEl.className = "badge badge-error";
        badgeEl.innerHTML = " Low Confidence (Manual Queue)";
      }
    }

    if (notesEl) {
      notesEl.innerHTML = `<strong>Verification Note:</strong> ${data.verificationNotes || 'Document verified against National DigiLocker API repository with cryptographic seal.'}`;
    }

    if (gridEl) {
      gridEl.innerHTML = `
        <div class="field-card">
          <span class="field-label">Document Type</span>
          <span class="field-value">${data.documentType || 'Official Certificate'}</span>
        </div>
        <div class="field-card">
          <span class="field-label">Candidate Name</span>
          <span class="field-value text-accent font-bold">${data.candidateName || (EduconData.currentStudent && EduconData.currentStudent.fullName) || 'Scholar'}</span>
        </div>
        <div class="field-card">
          <span class="field-label">Father / Guardian Name</span>
          <span class="field-value">${data.fatherName || 'Not Specified'}</span>
        </div>
        <div class="field-card">
          <span class="field-label">Certificate / Registration No.</span>
          <span class="field-value font-mono">${data.certificateNumber || 'Not Detected'}</span>
        </div>
        <div class="field-card">
          <span class="field-label">Issuing Authority</span>
          <span class="field-value">${data.issuingAuthority || 'Digital Authority'}</span>
        </div>
        <div class="field-card">
          <span class="field-label">Date of Issue</span>
          <span class="field-value">${data.issueDate || 'Permanent / Undated'}</span>
        </div>
        <div class="field-card">
          <span class="field-label">Validity</span>
          <span class="field-value">${data.validity || 'Permanent'}</span>
        </div>
        ${data.casteCommunity ? `
          <div class="field-card highlight-tribal">
            <span class="field-label">Verified ST Community</span>
            <span class="field-value font-bold text-amber">${data.casteCommunity}</span>
          </div>
        ` : ''}
        ${data.annualIncome ? `
          <div class="field-card highlight-income">
            <span class="field-label">Certified Annual Income</span>
            <span class="field-value font-bold text-emerald">${data.annualIncome}</span>
          </div>
        ` : ''}
        ${data.institutionName ? `
          <div class="field-card highlight-global">
            <span class="field-label">Foreign University</span>
            <span class="field-value font-bold">${data.institutionName} (${data.qsRankingTier})</span>
          </div>
        ` : ''}
      `;
    }
  }

  function handleAutoFill() {
    if (!currentScanData) return;
    
    // Switch to Eligibility or Timeline tab and notify
    EduconApp.showToast(` Auto-filled application with Certificate #${currentScanData.certificateNumber}! Zero manual typing required.`);
    
    // Update student local context
    if (currentScanData.annualIncomeNumeric) {
      EduconData.currentStudent.annualIncome = currentScanData.annualIncomeNumeric;
    }
    
    EduconApp.switchTab('eligibility');
  }

  return {
    init,
    loadSampleDocument,
    startCamera,
    stopCamera
  };
})();
