/**
 * Educon MoTA Unified Portal - 5-Scheme Eligibility Wizard Module
 * Conversational / Stepped flow with MoTa AI reasoning & benefit ranking
 */

window.EduconEligibility = (function() {
  let currentStep = 1;
  const totalSteps = 3;

  const state = {
    educationLevel: 'premier_institute', // default pre-selected for rich demo
    familyIncome: 145000,
    institutionName: 'IIT Bhubaneswar',
    isSt: true,
    isPvtg: false,
    hasDisability: false
  };

  function init() {
    setupEventListeners();
    runEligibilityEvaluation(); // Run initial evaluation
  }

  function setupEventListeners() {
    // Step navigation
    const nextBtn = document.getElementById('wizard-btn-next');
    const prevBtn = document.getElementById('wizard-btn-prev');
    const runBtn = document.getElementById('wizard-btn-evaluate');

    if (nextBtn) {
      nextBtn.addEventListener('click', () => {
        if (currentStep < totalSteps) {
          goToStep(currentStep + 1);
        }
      });
    }

    if (prevBtn) {
      prevBtn.addEventListener('click', () => {
        if (currentStep > 1) {
          goToStep(currentStep - 1);
        }
      });
    }

    if (runBtn) {
      runBtn.addEventListener('click', () => {
        runEligibilityEvaluation();
      });
    }

    // Education level option cards
    document.querySelectorAll('.edu-choice-card').forEach(card => {
      card.addEventListener('click', (e) => {
        document.querySelectorAll('.edu-choice-card').forEach(c => c.classList.remove('active'));
        const target = e.currentTarget;
        target.classList.add('active');
        state.educationLevel = target.dataset.eduLevel;
        
        // Auto update suggested institute
        const instInput = document.getElementById('wizard-inst-input');
        if (instInput) {
          if (state.educationLevel === 'premier_institute') {
            instInput.value = 'IIT Bhubaneswar';
            state.institutionName = 'IIT Bhubaneswar';
          } else if (state.educationLevel === 'study_abroad') {
            instInput.value = 'Imperial College London';
            state.institutionName = 'Imperial College London';
          } else if (state.educationLevel === 'class9_10') {
            instInput.value = 'EMRS Baripada';
            state.institutionName = 'EMRS Baripada';
          }
        }
      });
    });

    // Income range slider & quick chips
    const incomeSlider = document.getElementById('wizard-income-slider');
    const incomeDisplay = document.getElementById('wizard-income-display');

    if (incomeSlider && incomeDisplay) {
      incomeSlider.addEventListener('input', (e) => {
        const val = parseInt(e.target.value, 10);
        state.familyIncome = val;
        incomeDisplay.textContent = `₹ ${val.toLocaleString('en-IN')}`;
      });
    }

    document.querySelectorAll('.income-chip').forEach(chip => {
      chip.addEventListener('click', (e) => {
        document.querySelectorAll('.income-chip').forEach(c => c.classList.remove('active'));
        e.currentTarget.classList.add('active');
        const val = parseInt(e.currentTarget.dataset.income, 10);
        state.familyIncome = val;
        if (incomeSlider) incomeSlider.value = val;
        if (incomeDisplay) incomeDisplay.textContent = `₹ ${val.toLocaleString('en-IN')}`;
      });
    });

    // Special category checkboxes
    const pvtgCheck = document.getElementById('wizard-check-pvtg');
    if (pvtgCheck) {
      pvtgCheck.addEventListener('change', (e) => {
        state.isPvtg = e.target.checked;
      });
    }

    const stCheck = document.getElementById('wizard-check-st');
    if (stCheck) {
      stCheck.addEventListener('change', (e) => {
        state.isSt = e.target.checked;
      });
    }
  }

  function goToStep(stepNumber) {
    currentStep = stepNumber;
    for (let i = 1; i <= totalSteps; i++) {
      const stepEl = document.getElementById(`wizard-step-${i}`);
      const indicator = document.getElementById(`step-indicator-${i}`);
      if (stepEl) {
        if (i === currentStep) {
          stepEl.classList.remove('hidden');
        } else {
          stepEl.classList.add('hidden');
        }
      }
      if (indicator) {
        if (i <= currentStep) {
          indicator.classList.add('active');
        } else {
          indicator.classList.remove('active');
        }
      }
    }

    const prevBtn = document.getElementById('wizard-btn-prev');
    const nextBtn = document.getElementById('wizard-btn-next');
    const runBtn = document.getElementById('wizard-btn-evaluate');

    if (prevBtn) prevBtn.disabled = currentStep === 1;
    if (nextBtn) {
      if (currentStep === totalSteps) {
        nextBtn.classList.add('hidden');
        if (runBtn) runBtn.classList.remove('hidden');
      } else {
        nextBtn.classList.remove('hidden');
        if (runBtn) runBtn.classList.add('hidden');
      }
    }
  }

  async function runEligibilityEvaluation() {
    const resultsContainer = document.getElementById('wizard-results-list');
    const loadingBanner = document.getElementById('wizard-loading-banner');
    const topPickBanner = document.getElementById('wizard-top-pick-banner');

    if (loadingBanner) loadingBanner.classList.remove('hidden');
    if (resultsContainer) resultsContainer.innerHTML = '';

    const evaluation = await (window.EduconGemini || window.EduconMoTaAI).reasonEligibility(state);

    if (loadingBanner) loadingBanner.classList.add('hidden');

    if (topPickBanner && evaluation.topPick) {
      topPickBanner.classList.remove('hidden');
      topPickBanner.innerHTML = `
        <div class="top-pick-glow">
          <div class="top-pick-tag"> HIGHEST BENEFIT MATCH FOR YOU</div>
          <h3 class="text-xl font-bold">${evaluation.topPick.schemeTitle}</h3>
          <p class="text-emerald font-bold text-lg my-1">Entitlement: ${evaluation.topPick.benefit}</p>
          <p class="text-sm opacity-90">${evaluation.topPick.reason}</p>
          <div class="mt-3 flex gap-2">
            <button class="btn btn-primary btn-sm" onclick="EduconApp.showToast('Pre-filling DigiLocker documents for ${evaluation.topPick.schemeTitle}...'); EduconApp.switchTab('timeline');">Apply with 1-Tap DigiLocker</button>
            <button class="btn btn-ghost btn-sm" onclick="EduconJago.askQuestion('Can you give full details on how to get the ${evaluation.topPick.schemeTitle}?')">Ask JAGO Assistant</button>
          </div>
        </div>
      `;
    }

    if (resultsContainer) {
      resultsContainer.innerHTML = evaluation.recommendations.map(r => `
        <div class="scheme-eval-card ${r.eligible ? 'eligible' : 'ineligible'}">
          <div class="scheme-eval-header">
            <div>
              <span class="badge ${r.eligible ? 'badge-success' : 'badge-neutral'}">
                ${r.eligible ? ' Fully Eligible' : ' Not Applicable'}
              </span>
              <span class="badge badge-outline ml-2">${r.portal}</span>
              <h4 class="font-bold text-md mt-1">${r.schemeTitle}</h4>
            </div>
            <div class="text-right">
              <span class="text-xs text-muted">Max Est. Value</span>
              <div class="font-bold text-lg ${r.eligible ? 'text-emerald' : 'text-muted'}">
                ₹ ${r.maxAmount.toLocaleString('en-IN')}${r.schemeId === 'NOS' ? '+' : '/yr'}
              </div>
            </div>
          </div>
          <div class="scheme-eval-body">
            <p class="text-sm my-1"><strong>Benefit:</strong> ${r.benefit}</p>
            <p class="text-xs text-muted"><strong>Eligibility Verdict:</strong> ${r.reason}</p>
            ${r.pvtgBonus ? `<div class="pvtg-badge mt-1"><span class="badge badge-warning"> ${r.pvtgBonus}</span></div>` : ''}
          </div>
        </div>
      `).join('');
    }
  }

  return {
    init,
    goToStep,
    runEligibilityEvaluation
  };
})();
