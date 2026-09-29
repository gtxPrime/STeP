/**
 * Educon MoTA Unified Portal - Core Data Models & Schemas
 * Ministry of Tribal Affairs (MoTA), Government of India
 */

window.EduconData = {
  // Current logged in student profile (mocked from DigiLocker + APAAR)
  currentStudent: {
    apaarId: "NFS*",
    digilockerId: "NFS*",
    aadhaarSuffix: "NFS*",
    fullName: "Scholar (NFS*)",
    dob: "NFS*",
    gender: "NFS*",
    community: "Scheduled Tribe (ST)",
    subTribe: "NFS*",
    pvtgStatus: false, // Particularly Vulnerable Tribal Group
    state: "NFS*",
    district: "NFS*",
    tehsil: "NFS*",
    pincode: "NFS*",
    phone: "NFS*",
    email: "student@step.gov.in",
    fatherName: "NFS*",
    motherName: "NFS*",
    annualIncome: 0,
    disabilityStatus: "None",
    
    // Academic record from APAAR / UDISE+
    currentEducation: {
      level: "Class 12",
      institution: "NFS*",
      udiseCode: "NFS*",
      board: "CBSE",
      marksPercentage: 0,
      passingYear: "2026",
      stream: "Science",
      hostelStatus: "Day Scholar"
    },

    // DBT Bank Account (NPCI Aadhaar Payment Bridge Seeding Status)
    dbtBank: {
      bankName: "NFS*",
      branch: "NFS*",
      accountMasked: "•••• •••• NFS*",
      ifsc: "NFS*",
      npciAadhaarSeeded: false,
      lastStatusVerified: "NFS*"
    },

    // DigiLocker Verified Documents (populated live from Firestore / DigiLocker Sandbox)
    documents: []
  },

  // The 5 Schemes of Ministry of Tribal Affairs
  schemes: [
    {
      id: "PRE_MATRIC",
      code: "SCH-01",
      title: "Pre-Matric Scholarship for ST Students",
      hindiTitle: "अनुसूचित जनजाति के छात्रों हेतु प्री-मैट्रिक छात्रवृत्ति",
      portal: "National Scholarship Portal (NSP)",
      targetClass: "Class IX & X",
      incomeCeiling: 250000,
      benefitSummary: "₹3,500/year (Day Scholar) | ₹7,000/year (Hosteller) + ₹1,000 book grant",
      maxBenefitAmount: 7000,
      eligibilityRules: [
        "Must belong to Scheduled Tribe (ST) or PVTG community.",
        "Must be studying in Class 9 or 10 in a recognized Government / Aided school or EMRS.",
        "Total family annual income must not exceed ₹2.50 Lakh from all sources.",
        "Student must not be holding any other Central/State scholarship simultaneously."
      ],
      guidelineSnippet: "Pre-Matric scholarship aims to reduce dropout rate of ST students at transition from elementary to secondary stage. 100% DBT credited via PFMS."
    },
    {
      id: "POST_MATRIC",
      code: "SCH-02",
      title: "Post-Matric Scholarship for ST Students (PMS-ST)",
      hindiTitle: "अनुसूचित जनजाति के छात्रों हेतु पोस्ट-मैट्रिक छात्रवृत्ति",
      portal: "National Scholarship Portal (NSP)",
      targetClass: "Class XI, XII, ITI, Diploma, Graduation, PG, Professional Courses",
      incomeCeiling: 250000,
      benefitSummary: "100% Compulsory Tuition Fee waiver + Maintenance Allowance up to ₹13,500/year",
      maxBenefitAmount: 28500,
      eligibilityRules: [
        "Must be an ST candidate who has passed Matriculation / Higher Secondary from recognized board.",
        "Annual family income must not exceed ₹2,50,000.",
        "Covers Group 1 (Medical/Engineering/MBA), Group 2 (Professional PG), Group 3 (General Degree), Group 4 (Class 11-12/ITI).",
        "Disbursement split into 75% Central Share + 25% State Share credited directly to student bank account."
      ],
      guidelineSnippet: "PMS-ST is an umbrella scheme ensuring ST students pursue higher education without financial distress. Mandatory Aadhaar authentication."
    },
    {
      id: "TOP_CLASS",
      code: "SCH-03",
      title: "National Scholarship for Higher Education (Top Class Education)",
      hindiTitle: "उच्च शिक्षा हेतु राष्ट्रीय छात्रवृत्ति (शीर्ष संस्थान योजना)",
      portal: "SFMP (Canara Bank) & NSP",
      targetClass: "Undergraduate / Postgraduate in 265 Premier Institutes (IITs, IIMs, NITs, AIIMS, NLUs)",
      incomeCeiling: 600000,
      benefitSummary: "Full Tuition Fee (up to ₹2.00 Lakhs in private / full in govt) + Living Expenses ₹3,000/mo + Books ₹5,000/yr + Computer ₹45,000 (one-time)",
      maxBenefitAmount: 286000,
      eligibilityRules: [
        "ST students who have secured admission in any of the 265 notified premier institutions (IIT, IIM, NIT, AIIMS, IIIT, National Law Universities).",
        "Total family annual income up to ₹6.00 Lakhs.",
        "Total fresh slots available per year: 1,000 ST scholars.",
        "Scholarship continues till completion of the course, subject to satisfactory academic performance."
      ],
      guidelineSnippet: "Top Class scheme covers elite professional institutions. Computer grant of ₹45,000 is provided in the 1st year of admission."
    },
    {
      id: "NFST",
      code: "SCH-04",
      title: "National Fellowship for Higher Education of ST Students (NFST)",
      hindiTitle: "अनुसूचित जनजाति के छात्रों हेतु राष्ट्रीय अध्येतावृत्ति (NFST)",
      portal: "Scholarship Fellowship Management Portal (SFMP / Canara Bank)",
      targetClass: "Regular & Full-time M.Phil and Ph.D. scholars",
      incomeCeiling: null, // No income ceiling for fellowship
      benefitSummary: "JRF: ₹31,000/month | SRF: ₹35,000/month + HRA (8%-24%) + Contingency up to ₹25,000/year",
      maxBenefitAmount: 480000,
      eligibilityRules: [
        "ST candidates qualified UGC-NET / CSIR-NET / GATE and registered for regular M.Phil / Ph.D. courses.",
        "750 fresh fellowships allocated annually by Ministry of Tribal Affairs.",
        "No family income limit applicable.",
        "Tenure: 2 years JRF + 3 years SRF after evaluation committee assessment."
      ],
      guidelineSnippet: "NFST provides financial support for tribal scholars to achieve doctorate degrees in Humanities, Social Sciences, Engineering, and Pure Sciences."
    },
    {
      id: "NOS",
      code: "SCH-05",
      title: "National Overseas Scholarship for ST Candidates (NOS)",
      hindiTitle: "अनुसूचित जनजाति के अभ्यर्थियों हेतु राष्ट्रीय प्रवासी छात्रवृत्ति (NOS)",
      portal: "Standalone NOS Online Portal (overseas.tribal.gov.in)",
      targetClass: "Master's & Ph.D. abroad in Top 500 QS World Ranking Universities",
      incomeCeiling: 800000,
      benefitSummary: "100% Tuition Fees + Annual Living Allowance £9,900 (UK) / $15,400 (USA) + Economy Airfare + Visa fees",
      maxBenefitAmount: 2200000,
      eligibilityRules: [
        "ST candidates having secured unconditional offer of admission in Top 500 QS World Ranked universities.",
        "Minimum 55% marks or equivalent grade in qualifying Master's or Bachelor's degree.",
        "Age must not be more than 35 years as on 1st July of the application year.",
        "Total family income from all sources must not exceed ₹8.00 Lakh per annum.",
        "20 fresh slots per year (17 ST + 3 PVTG reserved)."
      ],
      guidelineSnippet: "NOS empowers ST scholars to access Ivy League, Oxford, Cambridge, and world-class universities with full sovereign sponsorship."
    }
  ],

  // Single Consolidated Scholarship Timeline (Across NSP, SFMP, and NOS!)
  applicationsTimeline: [
    {
      applicationId: "NSP-2025-PMS-74921",
      schemeId: "POST_MATRIC",
      schemeTitle: "Post-Matric Scholarship for ST Students",
      academicYear: "2025-26",
      sourcePortal: "NSP (National Scholarship Portal)",
      appliedDate: "2025-08-12",
      stage: "DISBURSED",
      stageBadgeClass: "status-disbursed",
      stageText: "Disbursed via DBT",
      sanctionAmount: 24500,
      disbursedAmount: 24500,
      steps: [
        { label: "Submitted", date: "12-Aug-2025", completed: true, note: "Applied via Unified MoTA Portal" },
        { label: "Institute Verified", date: "24-Aug-2025", completed: true, note: "Principal, EMRS Mayurbhanj" },
        { label: "District Nodal Verified", date: "09-Sep-2025", completed: true, note: "DWO Mayurbhanj verified via e-District API (96% Confidence)" },
        { label: "Ministry Sanctioned", date: "15-Dec-2025", completed: true, note: "Sanction Order: MoTA/PMS/25-26/1842" },
        { label: "DBT Disbursed", date: "12-Feb-2026", completed: true, note: "UTR: RBI492810488219 (SBI A/C ****4920)" }
      ],
      dbtDetails: {
        utr: "RBI492810488219",
        paymentMode: "Aadhaar Payment Bridge (APB / PFMS)",
        disbursedDate: "2026-02-12",
        bankName: "State Bank of India",
        accountNo: "XXXX-XXXX-4920",
        centralShare: "₹18,375 (75%)",
        stateShare: "₹6,125 (25%)",
        status: "CREDITED_SUCCESSFULLY"
      }
    },
    {
      applicationId: "SFMP-2026-TC-09312",
      schemeId: "TOP_CLASS",
      schemeTitle: "National Scholarship for Top Class Education (IIT Bhubaneswar B.Tech)",
      academicYear: "2026-27",
      sourcePortal: "SFMP (Canara Bank / MoTA)",
      appliedDate: "2026-07-10",
      stage: "SANCTIONED",
      stageBadgeClass: "status-sanctioned",
      stageText: "Sanctioned - In PFMS Payment Queue",
      sanctionAmount: 185000,
      disbursedAmount: 0,
      steps: [
        { label: "Submitted", date: "10-Jul-2026", completed: true, note: "Auto-filled via DigiLocker + JEE Rank" },
        { label: "Institute Verified", date: "18-Jul-2026", completed: true, note: "Dean Academics, IIT Bhubaneswar" },
        { label: "State Nodal Verified", date: "04-Aug-2026", completed: true, note: "Nodal Officer ST & SC Dept, Govt of Odisha" },
        { label: "Ministry Sanctioned", date: "02-Sep-2026", completed: true, note: "Sanction Order MoTA/TC/2026-27/0894 approved by Joint Secretary" },
        { label: "DBT Disbursed", date: "Estimated 10-Oct-2026", completed: false, note: "PFMS token generated, awaiting fund release by Canara Bank SFMP" }
      ],
      dbtDetails: {
        utr: "PENDING_PFMS_BATCH_621",
        paymentMode: "PFMS DBT",
        disbursedDate: "Pending Release",
        bankName: "State Bank of India",
        accountNo: "XXXX-XXXX-4920",
        centralShare: "₹1,85,000 (100%)",
        status: "PROCESSING_AT_BANK"
      }
    },
    {
      applicationId: "NOS-2027-INT-0042",
      schemeId: "NOS",
      schemeTitle: "National Overseas Scholarship (Imperial College London M.Sc)",
      academicYear: "2027-28",
      sourcePortal: "Standalone NOS Portal",
      appliedDate: "2026-08-20",
      stage: "DEFICIENCY_FLAGGED",
      stageBadgeClass: "status-deficiency",
      stageText: "Deficiency Flagged - Action Required",
      sanctionAmount: 2200000,
      disbursedAmount: 0,
      steps: [
        { label: "Submitted", date: "20-Aug-2026", completed: true, note: "Uploaded Passport, IELTS 7.5, Offer Letter" },
        { label: "Scrutiny Team", date: "28-Aug-2026", completed: false, note: "Flagged with Defect Code D-402 on Offer Letter" },
        { label: "Screening Committee", date: "Pending Scrutiny", completed: false, note: "Awaiting applicant cure" },
        { label: "Ministry Sanctioned", date: "Pending", completed: false, note: "Scheduled for Selection Committee Round" },
        { label: "DBT Disbursed", date: "Pending", completed: false, note: "Direct foreign currency transfer" }
      ],
      // Official bureaucratic deficiency message
      deficiencyData: {
        code: "DEF-D402",
        bureaucraticReason: "APPLICATION DEFECT: Uploaded conditional admission letter from Imperial College London does not explicitly certify candidate's unencumbered academic admission status as per MoTA NOS Operational Guidelines Clause 7(ii)(b). Offer letter states 'Subject to UKVI CAS generation and final transcripts submission'. Furthermore, institutional QS World Ranking 2026 certificate snippet is absent.",
        flaggedDate: "2026-08-28",
        deadlineDate: "2026-10-15",
        daysRemaining: 17
      }
    }
  ],

  // Peer Scholarship Navigator Dataset (Realistic Aggregated & Anonymized)
  peerStatistics: {
    cohortDescription: "Class 12 / Higher Secondary ST Students in Odisha & Jharkhand (Family Income < ₹2 Lakhs)",
    totalPeersAnalyzed: 14280,
    averageAward: "₹28,500/year",
    successRate: "94.2%",
    topBeneficialScheme: "Post-Matric Scholarship + EMRS Hostel Subsidy",
    disbursementMedianDays: "24 days from District Nodal verification",
    impactStory: "Over 8,400 students from your district leveraged this exact scholarship to enroll in premier engineering & state colleges last year."
  },

  // Student Impact Summary (The "Extra Mile" Idea)
  studentImpact: {
    totalReceivedTillDate: 74500, // Total ₹
    schemesCount: 2,
    tuitionCoveragePercent: 100,
    monthsHostelCovered: 8,
    booksFundedCount: 14,
    milestoneQuote: "Your MoTA scholarships have covered 100% of your Class 11-12 tuition fees and 8 months of boarding at EMRS Baripada."
  },

  // Active Grievances & SLA Tracker
  grievances: [
    {
      ticketId: "GRV-2026-MOTA-9104",
      relatedAppId: "NSP-2025-PMS-74921",
      subject: "PFMS UTR Status Confirmation for Post-Matric Q4 maintenance allowance",
      createdDate: "2026-09-18",
      slaDaysTotal: 30,
      daysElapsed: 10,
      daysRemaining: 20,
      status: "IN_PROGRESS",
      assignedOfficer: "Shri R. K. Soren, Deputy Secretary (Scholarships), MoTA New Delhi",
      timeline: [
        { time: "18-Sep-2026 10:14 AM", note: "Grievance auto-lodged via Educon Single-Tap Escalation" },
        { time: "20-Sep-2026 02:30 PM", note: "Assigned to Canara Bank SFMP Nodal Desk & PFMS Liaison Cell" },
        { time: "24-Sep-2026 11:00 AM", note: "PFMS confirmed payment credit file sent to SBI Clearing Cell" }
      ]
    }
  ],

  // District-Level UDISE+ / APAAR ST Enrollment vs MoTA Scholarship Gap (Heatmap Data)
  districtHeatmapData: [
    {
      state: "Chhattisgarh",
      district: "Bastar",
      udiseStEnrollment: 54200,
      scholarshipActive: 21300,
      gapCount: 32900,
      gapPercent: 60.7,
      pvtgPockets: "Abujhmarh, Tokapal, Bastanar",
      primaryBarriers: "Lack of internet at village panchayats, expired income certificates",
      priorityLevel: "CRITICAL",
      lat: 19.07,
      lng: 82.02
    },
    {
      state: "Odisha",
      district: "Mayurbhanj",
      udiseStEnrollment: 78500,
      scholarshipActive: 48200,
      gapCount: 30300,
      gapPercent: 38.6,
      pvtgPockets: "Similipal, Jashipur, Khunta",
      primaryBarriers: "Bank Aadhaar NPCI seeding mismatch",
      priorityLevel: "MODERATE",
      lat: 21.93,
      lng: 86.74
    },
    {
      state: "Maharashtra",
      district: "Gadchiroli",
      udiseStEnrollment: 36400,
      scholarshipActive: 15100,
      gapCount: 21300,
      gapPercent: 58.5,
      pvtgPockets: "Bhamragad, Etapalli, Dhanora (Madia Gond)",
      primaryBarriers: "No mobile connectivity, biometric device failures",
      priorityLevel: "HIGH",
      lat: 20.18,
      lng: 80.00
    },
    {
      state: "Jharkhand",
      district: "Khunti",
      udiseStEnrollment: 41000,
      scholarshipActive: 29800,
      gapCount: 11200,
      gapPercent: 27.3,
      pvtgPockets: "Torpa, Rania (Birhor community)",
      primaryBarriers: "Document awareness in remote hamlets",
      priorityLevel: "LOW",
      lat: 23.07,
      lng: 85.28
    },
    {
      state: "Maharashtra",
      district: "Nandurbar",
      udiseStEnrollment: 62000,
      scholarshipActive: 31000,
      gapCount: 31000,
      gapPercent: 50.0,
      pvtgPockets: "Dhadgaon, Molgi, Akrani (Bhil & Pawra)",
      primaryBarriers: "Seasonal migration for sugarcane cutting",
      priorityLevel: "HIGH",
      lat: 21.37,
      lng: 74.24
    },
    {
      state: "Odisha",
      district: "Rayagada",
      udiseStEnrollment: 44500,
      scholarshipActive: 18900,
      gapCount: 25600,
      gapPercent: 57.5,
      pvtgPockets: "Niyamgiri Hills (Dongria Kondh), Bissam Cuttack",
      primaryBarriers: "Language gap (Kui/Kuvi mother tongue), manual caste certs",
      priorityLevel: "HIGH",
      lat: 19.17,
      lng: 83.41
    },
    {
      state: "Madhya Pradesh",
      district: "Jhabua",
      udiseStEnrollment: 68000,
      scholarshipActive: 34200,
      gapCount: 33800,
      gapPercent: 49.7,
      pvtgPockets: "Thandla, Meghnagar, Petlawad",
      primaryBarriers: "Income certificate delays at Tehsil counters",
      priorityLevel: "HIGH",
      lat: 22.77,
      lng: 74.59
    },
    {
      state: "Telangana",
      district: "Adilabad",
      udiseStEnrollment: 29000,
      scholarshipActive: 12200,
      gapCount: 16800,
      gapPercent: 57.9,
      pvtgPockets: "Utnoor, Jainoor, Asifabad (Kolam & Thoti)",
      primaryBarriers: "Caste validity certificate pendency at ITDA",
      priorityLevel: "HIGH",
      lat: 19.66,
      lng: 78.53
    },
    {
      state: "Meghalaya",
      district: "West Khasi Hills",
      udiseStEnrollment: 31500,
      scholarshipActive: 24100,
      gapCount: 7400,
      gapPercent: 23.5,
      pvtgPockets: "Nongstoin, Mairang",
      primaryBarriers: "Institutional verification backlog at college level",
      priorityLevel: "LOW",
      lat: 25.52,
      lng: 91.27
    },
    {
      state: "Kerala",
      district: "Wayanad",
      udiseStEnrollment: 14200,
      scholarshipActive: 9800,
      gapCount: 4400,
      gapPercent: 31.0,
      pvtgPockets: "Mananthavady, Sulthan Bathery (Paniya & Kattunayakan)",
      primaryBarriers: "Bank branch distance from forest settlements",
      priorityLevel: "MODERATE",
      lat: 11.68,
      lng: 76.13
    }
  ],

  // No hardcoded sample documents: all documents retrieved from DigiLocker Sandbox or Firestore
  sampleDocuments: []
};

