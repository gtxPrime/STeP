/**
 * Educon MoTA Unified Portal - Core Data Models & Schemas
 * Ministry of Tribal Affairs (MoTA), Government of India
 */

window.EduconData = {
  // Current logged in student profile (mocked from DigiLocker + APAAR)
  currentStudent: {
    apaarId: "9842-1084-2026",
    digilockerId: "DL-ST-883921",
    aadhaarSuffix: "8492",
    fullName: "Birsa Munda",
    dob: "2006-11-15",
    gender: "Male",
    community: "Scheduled Tribe (ST)",
    subTribe: "Santhal",
    pvtgStatus: false, // Particularly Vulnerable Tribal Group
    state: "Odisha",
    district: "Mayurbhanj",
    tehsil: "Baripada",
    pincode: "757001",
    phone: "+91 98451 23456",
    email: "birsa.munda.st@student.gov.in",
    fatherName: "Sukram Munda",
    motherName: "Sumitra Munda",
    annualIncome: 145000, // ₹1,45,000 p.a.
    disabilityStatus: "None",
    
    // Academic record from APAAR / UDISE+
    currentEducation: {
      level: "Class 12",
      institution: "Eklavya Model Residential School (EMRS), Mayurbhanj",
      udiseCode: "21070104502",
      board: "CBSE",
      marksPercentage: 86.4,
      passingYear: "2026",
      stream: "Science (PCMB)",
      hostelStatus: "Hosteller"
    },

    // DBT Bank Account (NPCI Aadhaar Payment Bridge Seeding Status)
    dbtBank: {
      bankName: "State Bank of India",
      branch: "Baripada Main Branch",
      accountMasked: "XXXX-XXXX-4920",
      ifsc: "SBIN0001234",
      npciAadhaarSeeded: true,
      lastStatusVerified: "2026-09-15"
    },

    // DigiLocker Verified Documents
    documents: [
      {
        id: "DOC-ST-01",
        title: "ST Community Certificate",
        type: "caste",
        issuingAuthority: "Tehsildar Baripada, Revenue Dept, Govt of Odisha",
        certificateNo: "OD/ST/2022/49201",
        issueDate: "2022-06-14",
        expiryDate: "Permanent",
        verifiedVia: "DigiLocker API",
        confidenceScore: 98,
        status: "VERIFIED"
      },
      {
        id: "DOC-INC-02",
        title: "Annual Family Income Certificate",
        type: "income",
        issuingAuthority: "Tehsildar Baripada, Govt of Odisha",
        certificateNo: "OD/INC/2025/11093",
        issueDate: "2025-10-25",
        expiryDate: "2026-10-24", // Expiring soon alert demo!
        verifiedVia: "e-District QR OCR",
        confidenceScore: 94,
        status: "EXPIRING_SOON"
      },
      {
        id: "DOC-ACAD-03",
        title: "Class 10 CBSE Marksheet",
        type: "marksheet",
        issuingAuthority: "Central Board of Secondary Education",
        certificateNo: "CBSE/2024/92819",
        issueDate: "2024-05-20",
        verifiedVia: "DigiLocker API",
        confidenceScore: 99,
        status: "VERIFIED"
      }
    ]
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

  // Sample Documents for 1-Click Instant Demo Testing in Document Scanner
  sampleDocuments: [
    {
      id: "sample-caste-odisha",
      name: "Odisha ST Caste Certificate (Santhal)",
      type: "caste",
      imageUrl: "data:image/svg+xml;utf8," + encodeURIComponent(`
        <svg xmlns="http://www.w3.org/2000/svg" width="600" height="420" viewBox="0 0 600 420" style="background:#fff9f0; font-family:sans-serif;">
          <rect x="15" y="15" width="570" height="390" fill="#fffef7" stroke="#b45309" stroke-width="4"/>
          <rect x="25" y="25" width="550" height="370" fill="none" stroke="#d97706" stroke-width="1.5" stroke-dasharray="6 3"/>
          <text x="300" y="65" text-anchor="middle" font-size="18" font-weight="bold" fill="#78350f">GOVERNMENT OF ODISHA</text>
          <text x="300" y="85" text-anchor="middle" font-size="13" font-weight="600" fill="#92400e">OFFICE OF THE TEHSILDAR, BARIPADA, MAYURBHANJ</text>
          <text x="300" y="115" text-anchor="middle" font-size="16" font-weight="bold" fill="#b45309" text-decoration="underline">SCHEDULED TRIBE (ST) CERTIFICATE</text>
          
          <text x="50" y="155" font-size="12" fill="#333">Certificate No: <tspan font-weight="bold" fill="#0a2540">OD/ST/2022/49201</tspan></text>
          <text x="400" y="155" font-size="12" fill="#333">Date of Issue: <tspan font-weight="bold">14/06/2022</tspan></text>
          
          <text x="50" y="195" font-size="13" fill="#1f2937">This is to certify that Shri/Kumari: <tspan font-weight="bold" fill="#0a2540">BIRSA MUNDA</tspan></text>
          <text x="50" y="225" font-size="13" fill="#1f2937">Son/Daughter of: <tspan font-weight="bold" fill="#0a2540">SUKRAM MUNDA</tspan> and <tspan font-weight="bold">SUMITRA MUNDA</tspan></text>
          <text x="50" y="255" font-size="13" fill="#1f2937">Resident of Village: <tspan font-weight="bold">Baripada Town</tspan>, District: <tspan font-weight="bold">Mayurbhanj</tspan>, State: <tspan font-weight="bold">Odisha</tspan></text>
          
          <text x="50" y="290" font-size="13" fill="#1f2937">belongs to the <tspan font-weight="bold" fill="#b45309">SANTHAL</tspan> Community which is recognized as a <tspan font-weight="bold" fill="#0a2540">SCHEDULED TRIBE</tspan></text>
          <text x="50" y="310" font-size="11" fill="#4b5563">under the Constitution (Scheduled Tribes) Order, 1950 as amended from time to time.</text>
          
          <rect x="420" y="315" width="130" height="70" fill="#f8fafc" stroke="#94a3b8" stroke-dasharray="3 3"/>
          <text x="485" y="340" text-anchor="middle" font-size="10" fill="#059669" font-weight="bold"> e-DIGITALLY SIGNED</text>
          <text x="485" y="355" text-anchor="middle" font-size="9" fill="#64748b">Tehsildar Baripada</text>
          <text x="485" y="370" text-anchor="middle" font-size="8" fill="#64748b">Odisha e-District Portal</text>
          
          <rect x="50" y="325" width="60" height="60" fill="#1e293b"/>
          <rect x="55" y="330" width="20" height="20" fill="#fff"/>
          <rect x="85" y="360" width="20" height="20" fill="#fff"/>
          <text x="120" y="360" font-size="10" fill="#475569">QR Code Verified: OD-ST-VALID-2022</text>
        </svg>
      `),
      expectedFields: {
        documentType: "Scheduled Tribe (ST) Certificate",
        candidateName: "BIRSA MUNDA",
        fatherName: "SUKRAM MUNDA",
        motherName: "SUMITRA MUNDA",
        community: "SANTHAL (Scheduled Tribe)",
        certificateNumber: "OD/ST/2022/49201",
        state: "Odisha",
        district: "Mayurbhanj",
        issuingAuthority: "Tehsildar, Baripada",
        issueDate: "2022-06-14",
        confidenceScore: 98,
        digiLockerMatch: true
      }
    },
    {
      id: "sample-income-mp",
      name: "MP Revenue Income Certificate (₹1,45,000)",
      type: "income",
      imageUrl: "data:image/svg+xml;utf8," + encodeURIComponent(`
        <svg xmlns="http://www.w3.org/2000/svg" width="600" height="420" viewBox="0 0 600 420" style="background:#f0fdf4; font-family:sans-serif;">
          <rect x="15" y="15" width="570" height="390" fill="#ffffff" stroke="#16a34a" stroke-width="4"/>
          <rect x="25" y="25" width="550" height="370" fill="none" stroke="#22c55e" stroke-width="1.5" stroke-dasharray="6 3"/>
          <text x="300" y="65" text-anchor="middle" font-size="18" font-weight="bold" fill="#14532d">REVENUE DEPARTMENT, GOVT OF ODISHA</text>
          <text x="300" y="88" text-anchor="middle" font-size="13" font-weight="600" fill="#166534">OFFICE OF THE REVENUE OFFICER / TEHSILDAR</text>
          <text x="300" y="115" text-anchor="middle" font-size="16" font-weight="bold" fill="#15803d" text-decoration="underline">ANNUAL INCOME CERTIFICATE (पारिवारिक आय प्रमाण पत्र)</text>
          
          <text x="50" y="155" font-size="12" fill="#333">Certificate ID: <tspan font-weight="bold" fill="#0a2540">OD/INC/2025/11093</tspan></text>
          <text x="400" y="155" font-size="12" fill="#333">Date of Issue: <tspan font-weight="bold">25/10/2025</tspan></text>
          
          <text x="50" y="195" font-size="13" fill="#1f2937">Certified that Shri/Smt: <tspan font-weight="bold" fill="#0a2540">SUKRAM MUNDA</tspan></text>
          <text x="50" y="225" font-size="13" fill="#1f2937">Father/Guardian of student: <tspan font-weight="bold" fill="#0a2540">BIRSA MUNDA</tspan></text>
          <text x="50" y="255" font-size="13" fill="#1f2937">Resident of: Ward 4, Baripada, Dist: Mayurbhanj, Odisha</text>
          
          <rect x="50" y="275" width="500" height="42" fill="#dcfce7" rx="6"/>
          <text x="300" y="302" text-anchor="middle" font-size="15" font-weight="bold" fill="#14532d">Total Annual Family Income: ₹ 1,45,000/- (Rupees One Lakh Forty-Five Thousand Only)</text>
          
          <text x="50" y="340" font-size="11" fill="#4b5563">Valid for Financial Year: 2025-2026 (Expires on 24-Oct-2026)</text>
          
          <rect x="420" y="340" width="130" height="45" fill="#f8fafc" stroke="#16a34a"/>
          <text x="485" y="358" text-anchor="middle" font-size="9" fill="#15803d" font-weight="bold">DIGITALLY CERTIFIED</text>
          <text x="485" y="372" text-anchor="middle" font-size="8" fill="#64748b">Govt e-District Authority</text>
        </svg>
      `),
      expectedFields: {
        documentType: "Annual Family Income Certificate",
        parentName: "SUKRAM MUNDA",
        candidateName: "BIRSA MUNDA",
        annualIncome: "₹ 1,45,000",
        annualIncomeNumeric: 145000,
        certificateNumber: "OD/INC/2025/11093",
        issueDate: "2025-10-25",
        validTill: "2026-10-24",
        issuingAuthority: "Tehsildar Baripada",
        confidenceScore: 95,
        digiLockerMatch: true
      }
    },
    {
      id: "sample-nos-offer",
      name: "Foreign University Offer Letter (Imperial College London)",
      type: "offer_letter",
      imageUrl: "data:image/svg+xml;utf8," + encodeURIComponent(`
        <svg xmlns="http://www.w3.org/2000/svg" width="600" height="420" viewBox="0 0 600 420" style="background:#ffffff; font-family:serif;">
          <rect x="15" y="15" width="570" height="390" fill="#ffffff" stroke="#002147" stroke-width="3"/>
          <text x="300" y="55" text-anchor="middle" font-size="18" font-weight="bold" fill="#002147">Imperial College London</text>
          <text x="300" y="75" text-anchor="middle" font-size="11" fill="#475569">Faculty of Natural Sciences • South Kensington, London SW7 2AZ</text>
          <line x1="50" y1="85" x2="550" y2="85" stroke="#002147" stroke-width="1"/>
          
          <text x="50" y="120" font-size="12" fill="#333">Date: 15 August 2026</text>
          <text x="50" y="140" font-size="12" fill="#333">Dear Mr. Birsa Munda (CID: 02194812),</text>
          
          <text x="50" y="170" font-size="13" font-weight="bold" fill="#002147">CONDITIONAL OFFER OF ADMISSION: M.Sc in Environmental Data Science (2027 Intake)</text>
          <text x="50" y="195" font-size="11" fill="#333">We are delighted to offer you a place on the Master of Science program commencing October 2027.</text>
          <text x="50" y="215" font-size="11" fill="#333">Tuition Fee: £39,500 per annum.</text>
          
          <rect x="50" y="235" width="500" height="60" fill="#fef2f2" stroke="#ef4444" stroke-width="1.5" rx="4"/>
          <text x="65" y="255" font-size="11" font-weight="bold" fill="#b91c1c">CONDITIONS OUTSTANDING:</text>
          <text x="65" y="272" font-size="10" fill="#7f1d1d">1. Provision of final official Bachelor degree transcripts with 1st Class Honours.</text>
          <text x="65" y="287" font-size="10" fill="#7f1d1d">2. Issuance of UK Visas and Immigration CAS statement.</text>
          
          <text x="50" y="325" font-size="10" fill="#4b5563">Note: Imperial College London is ranked #2 globally in the QS World University Rankings 2026.</text>
          
          <text x="50" y="365" font-size="11" font-weight="bold" fill="#002147">Admissions Registry, Imperial College London</text>
        </svg>
      `),
      expectedFields: {
        documentType: "Foreign University Offer Letter",
        candidateName: "Birsa Munda",
        institutionName: "Imperial College London",
        courseName: "M.Sc Environmental Data Science",
        qsRankingTier: "Top 10 Global (QS #2)",
        offerType: "Conditional Offer",
        tuitionFeeForeign: "£39,500/year",
        deficiencyTrigger: "Conditional offer requires CAS and final transcript waiver for MoTA NOS guideline compliance.",
        confidenceScore: 78,
        digiLockerMatch: false
      }
    }
  ]
};
