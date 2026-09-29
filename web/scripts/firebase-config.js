/**
 * STeP MoTA Sovereign Portal - Firebase Firestore Cloud Sync
 * Web App Project: step-sih
 * Connects Web Admin Dashboard & Student Portal to live Firebase Firestore
 * Enables real-time fetching, updating, and adding of schemes, applications, and directives.
 */

window.STePFirebase = (function() {
  const firebaseConfig = {
    apiKey: "AIzaSyDOZGYoAEpkFkJgg3mXE4Id2Axp0XsDuKk",
    authDomain: "step-sih.firebaseapp.com",
    projectId: "step-sih",
    storageBucket: "step-sih.firebasestorage.app",
    messagingSenderId: "948854630900",
    appId: "1:948854630900:web:76043325fbbec98f0f71a1",
    measurementId: "G-23BBRRLRQE"
  };

  let db = null;
  let isConnected = false;
  let connectionListeners = [];

  function init() {
    try {
      if (!window.firebase) {
        console.warn("[STeP Firebase] Firebase SDK not loaded in window.");
        return;
      }
      if (!firebase.apps.length) {
        firebase.initializeApp(firebaseConfig);
      }
      db = firebase.firestore();
      isConnected = true;
      console.log("[STeP Firebase] Initialized Firestore connection to project: " + firebaseConfig.projectId);
      notifyConnectionStatus(true);
      updateSyncBadge(true, "Connected to step-sih");

      // Verify connection by checking /schemes & auto-seeding demo scholarships if empty
      db.collection("schemes").limit(1).get()
        .then(snap => {
          updateSyncBadge(true, "Live Synced (step-sih)");
          if (snap.empty) {
            seedSchemesIfEmpty();
          }
        })
        .catch(err => {
          console.warn("[STeP Firebase] Firestore check warning:", err.message);
          updateSyncBadge(true, "Cloud Ready (step-sih)");
        });

      // Zero demo applications seeded by default

    } catch (e) {
      console.error("[STeP Firebase] Initialization error:", e);
      isConnected = false;
      notifyConnectionStatus(false);
      updateSyncBadge(false, "Offline / Local Mode");
    }
  }

  function updateSyncBadge(online, text) {
    const badge = document.getElementById("firebase-sync-badge");
    if (badge) {
      badge.className = online ? "badge badge-success text-xs font-mono" : "badge badge-neutral text-xs font-mono";
      badge.textContent = "Firestore: " + text;
    }
  }

  function onConnectionChange(fn) {
    connectionListeners.push(fn);
  }

  function notifyConnectionStatus(status) {
    connectionListeners.forEach(fn => {
      try { fn(status); } catch (e) { console.error(e); }
    });
  }

  // -------------------------------------------------------------
  // 1. FETCH / LISTEN OPERATIONS
  // -------------------------------------------------------------

  /**
   * Listen to all applications across all users via collectionGroup
   */
  function listenApplications(callback) {
    if (!db) return;
    try {
      return db.collectionGroup("applications").onSnapshot(snapshot => {
        const apps = [];
        snapshot.forEach(doc => {
          const data = doc.data();
          let userId = "usr_birsa_munda";
          try {
            if (doc.ref.parent && doc.ref.parent.parent) {
              userId = doc.ref.parent.parent.id;
            }
          } catch (_) {}

          apps.push({
            id: doc.id,
            docPath: doc.ref.path,
            userId: userId,
            ...data
          });
        });
        callback(apps);
      }, err => {
        console.warn("[STeP Firebase] Error listening to applications collectionGroup:", err);
      });
    } catch (e) {
      console.warn("[STeP Firebase] collectionGroup not available or failed:", e);
    }
  }

  /**
   * Listen to dynamic schemes in Firestore
   */
  function listenSchemes(callback) {
    if (!db) return;
    return db.collection("schemes").onSnapshot(snapshot => {
      if (snapshot.empty) {
        callback([]);
      } else {
        const schemes = [];
        snapshot.forEach(doc => {
          schemes.push({ id: doc.id, ...doc.data() });
        });
        callback(schemes);
      }
    }, err => {
      console.warn("[STeP Firebase] Error listening to schemes:", err);
    });
  }

  /**
   * Listen to directives / outreaches in Firestore
   */
  function listenDirectives(callback) {
    if (!db) return;
    return db.collection("directives").orderBy("timestamp", "desc").limit(25).onSnapshot(snapshot => {
      const items = [];
      snapshot.forEach(doc => {
        items.push({ id: doc.id, ...doc.data() });
      });
      callback(items);
    }, err => {
      db.collection("directives").limit(25).get().then(snap => {
        const items = [];
        snap.forEach(d => items.push({ id: d.id, ...d.data() }));
        callback(items);
      }).catch(() => {});
    });
  }

  /**
   * Listen to all registered student profiles across all users
   */
  function listenRegisteredStudents(callback) {
    if (!db) return;
    try {
      return db.collectionGroup("profile").onSnapshot(snapshot => {
        const students = [];
        const seenUids = new Set();
        snapshot.forEach(doc => {
          const data = doc.data();
          let uid = data.uid || doc.id;
          try {
            if (doc.ref.parent && doc.ref.parent.parent) {
              uid = doc.ref.parent.parent.id;
            }
          } catch (_) {}

          // Filter out duplicates by uid or email
          const key = (data.email || uid).toLowerCase().trim();
          if (!seenUids.has(key)) {
            seenUids.add(key);
            students.push({
              id: doc.id,
              uid: uid,
              ...data
            });
          }
        });
        callback(students);
      }, err => {
        console.warn("[STeP Firebase] Error listening to profile collectionGroup:", err);
      });
    } catch (e) {
      console.warn("[STeP Firebase] collectionGroup profile failed:", e);
    }
  }

  /**
   * Listen to all uploaded / DigiLocker documents across all users
   */
  function listenAllDocuments(callback) {
    if (!db) return;
    try {
      return db.collectionGroup("documents").onSnapshot(snapshot => {
        const docs = [];
        snapshot.forEach(doc => {
          const data = doc.data();
          let uid = "usr_scholar";
          try {
            if (doc.ref.parent && doc.ref.parent.parent) {
              uid = doc.ref.parent.parent.id;
            }
          } catch (_) {}

          docs.push({
            id: doc.id,
            docPath: doc.ref.path,
            userId: uid,
            ...data
          });
        });
        callback(docs);
      }, err => {
        console.warn("[STeP Firebase] Error listening to documents collectionGroup:", err);
      });
    } catch (e) {
      console.warn("[STeP Firebase] collectionGroup documents failed:", e);
    }
  }

  /**
   * Delete a scholarship scheme from Firestore
   */
  async function deleteScheme(schemeId) {
    if (!db) throw new Error("Firestore not initialized");
    await db.collection("schemes").doc(schemeId).delete();
  }

  // -------------------------------------------------------------
  // 2. UPDATE OPERATIONS
  // -------------------------------------------------------------

  /**
   * Update application status (Approve, Sanction, Disburse)
   */
  async function updateApplicationStatus(docPath, updateData, userId, studentNotification) {
    if (!db) throw new Error("Firestore not initialized");
    const docRef = db.doc(docPath);
    await docRef.update({
      ...updateData,
      updatedAt: new Date().toISOString()
    });

    if (userId && studentNotification) {
      try {
        const notifRef = db.collection("users").doc(userId).collection("notifications").doc();
        await notifRef.set({
          id: notifRef.id,
          title: studentNotification.title || "Application Status Update",
          body: studentNotification.body || "Your application status has been updated.",
          time: new Date().toLocaleTimeString("en-IN", { hour: "2-digit", minute: "2-digit" }),
          isUnread: true,
          type: studentNotification.type || "SANCTION",
          createdAt: new Date().toISOString()
        });
      } catch (err) {
        console.warn("[STeP Firebase] Failed to write notification to user:", err);
      }
    }
  }

  /**
   * Flag an application deficiency in Firestore
   */
  async function updateApplicationDeficiency(docPath, deficiencyData, userId) {
    if (!db) throw new Error("Firestore not initialized");
    const docRef = db.doc(docPath);
    await docRef.update({
      stage: "DEFICIENCY_FLAGGED",
      stageText: "Deficiency Flagged - Action Required",
      deficiency: deficiencyData,
      updatedAt: new Date().toISOString()
    });

    if (userId) {
      try {
        const actionRef = db.collection("users").doc(userId).collection("pending_actions").doc();
        await actionRef.set({
          id: actionRef.id,
          title: "Rectify Application Defect (" + deficiencyData.code + ")",
          scheme: deficiencyData.schemeTitle || "MoTA Scholarship",
          reason: deficiencyData.bureaucraticReason,
          isUrgent: true,
          actionText: "Upload Clarification",
          deadlineDate: deficiencyData.deadlineDate
        });

        const notifRef = db.collection("users").doc(userId).collection("notifications").doc();
        await notifRef.set({
          id: notifRef.id,
          title: "Defect Code " + deficiencyData.code + " Flagged",
          body: deficiencyData.bureaucraticReason,
          time: "Just now",
          isUnread: true,
          type: "DEFICIENCY",
          createdAt: new Date().toISOString()
        });
      } catch (err) {
        console.warn("[STeP Firebase] Failed to write deficiency pending action:", err);
      }
    }
  }

  // -------------------------------------------------------------
  // 3. ADD OPERATIONS
  // -------------------------------------------------------------

  /**
   * Add a new scholarship scheme to Firestore
   */
  async function addScheme(scheme) {
    if (!db) throw new Error("Firestore not initialized");
    const schemeId = scheme.id || "SCH_" + Date.now();
    await db.collection("schemes").doc(schemeId).set({
      id: schemeId,
      code: scheme.code || "SCH-" + Math.floor(Math.random() * 90 + 10),
      title: scheme.title,
      hindiTitle: scheme.hindiTitle || "",
      portal: scheme.portal || "NSP",
      targetClass: scheme.targetClass || "Higher Secondary / College",
      incomeCeiling: Number(scheme.incomeCeiling) || 250000,
      benefitSummary: scheme.benefitSummary || "100% Tuition Assistance",
      maxBenefitAmount: Number(scheme.maxBenefitAmount) || 25000,
      benefitAmountFormatted: "₹ " + (Number(scheme.maxBenefitAmount) || 25000).toLocaleString("en-IN"),
      deadlineFormatted: scheme.deadlineFormatted || "31-Dec-2026",
      eligibilityTag: scheme.eligibilityTag || "Official MoTA Scheme",
      description: scheme.description || "",
      documentsNeeded: scheme.documentsNeeded || ["ST Caste Certificate", "Income Certificate"],
      rules: scheme.rules || ["Applicant must belong to recognized Scheduled Tribe"],
      createdAt: new Date().toISOString()
    }, { merge: true });
    return schemeId;
  }

  /**
   * Add an application on behalf of a student or walk-in registration
   */
  async function addApplication(userId, applicationData) {
    if (!db) throw new Error("Firestore not initialized");
    const targetUserId = userId || "usr_walkin_" + Date.now();
    const appId = applicationData.applicationId || "MOTA-APP-" + Date.now().toString().slice(-6);

    const fullAppRecord = {
      applicationId: appId,
      schemeId: applicationData.schemeId || "POST_MATRIC",
      schemeTitle: applicationData.schemeTitle || "Post-Matric Scholarship for ST Students",
      academicYear: "2026-27",
      sourcePortal: applicationData.sourcePortal || "NSP",
      stage: applicationData.stage || "SUBMITTED",
      stageText: applicationData.stageText || "Submitted",
      currentStepIndex: applicationData.currentStepIndex || 1,
      sanctionAmount: Number(applicationData.sanctionAmount) || 28500,
      nextActionText: applicationData.nextActionText || "Under Institutional Verification",
      verificationConfidence: Number(applicationData.verificationConfidence) || 94,
      studentName: applicationData.studentName || "Tribal Scholar",
      district: applicationData.district || "Bastar, Chhattisgarh",
      appliedDate: new Date().toISOString().split("T")[0],
      steps: applicationData.steps || [
        { label: "Submitted", date: new Date().toLocaleDateString("en-IN"), completed: true, note: "Walk-in application recorded via MoTA Officer Desk" },
        { label: "Institute Verified", date: "Pending", completed: false, note: "Under review" },
        { label: "District Nodal Verified", date: "Pending", completed: false, note: "Awaiting e-District verification" },
        { label: "Ministry Sanctioned", date: "Pending", completed: false, note: "In queue" },
        { label: "DBT Disbursed", date: "Pending", completed: false, note: "PFMS Queue" }
      ],
      createdAt: new Date().toISOString()
    };

    await db.collection("users").doc(targetUserId).collection("applications").doc(appId).set(fullAppRecord);

    await db.collection("users").doc(targetUserId).collection("profile").doc("info").set({
      fullName: applicationData.studentName || "Tribal Scholar",
      district: applicationData.district || "Bastar",
      tribe: applicationData.tribe || "Scheduled Tribe",
      authenticatedVia: "Officer Facilitation Desk",
      updatedAt: new Date().toISOString()
    }, { merge: true });

    return { userId: targetUserId, appId };
  }

  /**
   * Save an outreach / mobile camp directive
   */
  async function addDirective(directive) {
    if (!db) throw new Error("Firestore not initialized");
    const ref = await db.collection("directives").add({
      ...directive,
      timestamp: firebase.firestore.FieldValue.serverTimestamp(),
      dateFormatted: new Date().toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric" })
    });
    return ref.id;
  }

  /**
   * Seed initial 5 MoTA schemes if collection is empty
   */
  async function seedSchemesIfEmpty() {
    if (!db) return;
    try {
      const snap = await db.collection("schemes").limit(1).get();
      if (snap.empty && window.EduconData && window.EduconData.schemes) {
        console.log("[STeP Firebase] Seeding 5 standard MoTA schemes into Firestore...");
        const batch = db.batch();
        EduconData.schemes.forEach(sc => {
          const ref = db.collection("schemes").doc(sc.id);
          batch.set(ref, {
            id: sc.id,
            code: sc.code,
            title: sc.title,
            hindiTitle: sc.hindiTitle || "",
            portal: sc.portal,
            targetClass: sc.targetClass,
            incomeCeiling: sc.incomeCeiling,
            benefitSummary: sc.benefitSummary,
            maxBenefitAmount: sc.maxBenefitAmount,
            benefitAmountFormatted: "₹ " + sc.maxBenefitAmount.toLocaleString("en-IN"),
            deadlineFormatted: "31-Dec-2026",
            eligibilityTag: "Official MoTA Scheme",
            description: sc.guidelineSnippet || "",
            rules: sc.eligibilityRules || [],
            createdAt: new Date().toISOString()
          });
        });
        await batch.commit();
        console.log("[STeP Firebase] Initial schemes seeded successfully.");
      }
    } catch (e) {
      console.warn("[STeP Firebase] Scheme seeding error:", e.message);
    }
  }

  /**
   * Seed demo applications for scholarships if empty so first-time users can see rich data
   */
  async function seedDemoDataIfEmpty() {
    if (!db) return;
    try {
      const snap = await db.collectionGroup("applications").limit(1).get();
      if (snap.empty && window.EduconData && window.EduconData.applicationsTimeline) {
        console.log("[STeP Firebase] Seeding demo applications to Firestore for first time user...");
        const demoUserId = "usr_birsa_munda_demo";
        for (const app of EduconData.applicationsTimeline) {
          await db.collection("users").doc(demoUserId).collection("applications").doc(app.applicationId).set({
            ...app,
            studentName: "Birsa Munda",
            district: "Mayurbhanj, Odisha",
            verificationConfidence: app.stage === "DISBURSED" ? 98 : (app.stage === "SANCTIONED" ? 94 : 76),
            createdAt: new Date().toISOString()
          }, { merge: true });
        }
        console.log("[STeP Firebase] Demo applications successfully seeded to Firestore.");
      }
    } catch (e) {
      console.warn("[STeP Firebase] Demo applications check/seed:", e.message);
    }
  }

  return {
    init,
    get db() { return db; },
    get isConnected() { return isConnected; },
    onConnectionChange,
    listenApplications,
    listenSchemes,
    listenDirectives,
    listenRegisteredStudents,
    listenAllDocuments,
    deleteScheme,
    updateApplicationStatus,
    updateApplicationDeficiency,
    addScheme,
    addApplication,
    addDirective,
    seedSchemesIfEmpty
  };
})();
