// MediBridge Cross-Platform Web Application Engine

const INITIAL_ORGS = [
  {
    id: 1,
    name: "Hope Health Foundation NGO",
    type: "Verified NGO",
    license: "NGO-MED-2024-8841",
    city: "Central Metro",
    distance: "2.4 km",
    rating: 4.9,
    donationsReceived: 142,
    urgentNeeds: "Amoxicillin, Azithromycin, Metformin",
    phone: "+1 (555) 234-5678"
  },
  {
    id: 2,
    name: "CarePoint Free Community Clinic",
    type: "Community Clinic",
    license: "CLINIC-REG-39120",
    city: "Eastside",
    distance: "3.8 km",
    rating: 4.8,
    donationsReceived: 98,
    urgentNeeds: "Insulin Glargine, Salbutamol, Amlodipine",
    phone: "+1 (555) 345-6789"
  },
  {
    id: 3,
    name: "Seva Charitable Mission Hospital",
    type: "Charitable Hospital",
    license: "HOSP-CHAR-77102",
    city: "South Hills",
    distance: "5.1 km",
    rating: 5.0,
    donationsReceived: 310,
    urgentNeeds: "Ceftriaxone, Paracetamol IV, Multivitamins",
    phone: "+1 (555) 456-7890"
  },
  {
    id: 4,
    name: "Apex Licensed Pharmacy Redistribution",
    type: "Licensed Pharmacy",
    license: "PHARM-LIC-99042",
    city: "Downtown",
    distance: "1.2 km",
    rating: 4.7,
    donationsReceived: 76,
    urgentNeeds: "Ibuprofen 400mg, Antacids, Cetirizine",
    phone: "+1 (555) 567-8901"
  }
];

const INITIAL_REQUESTS = [
  {
    id: 1,
    org: "Hope Health Foundation NGO",
    medicine: "Amoxicillin 500mg",
    category: "Antibiotics",
    urgency: "CRITICAL",
    needed: 100,
    fulfilled: 30,
    unit: "Strips"
  },
  {
    id: 2,
    org: "CarePoint Free Community Clinic",
    medicine: "Metformin 500mg",
    category: "Chronic Care",
    urgency: "HIGH",
    needed: 150,
    fulfilled: 60,
    unit: "Strips"
  },
  {
    id: 3,
    org: "CarePoint Free Community Clinic",
    medicine: "Salbutamol Inhaler (100mcg)",
    category: "Respiratory",
    urgency: "CRITICAL",
    needed: 40,
    fulfilled: 10,
    unit: "Canisters"
  }
];

const INITIAL_DONATIONS = [
  {
    id: 101,
    medicine: "Amoxicillin & Clavulanate 625mg",
    dosage: "625mg",
    qty: 30,
    unit: "Strips",
    expiry: "2027-02-28",
    batch: "AMC-2024-X8",
    status: "MATCHED",
    statusIndex: 2,
    orgName: "Hope Health Foundation NGO",
    score: 98
  },
  {
    id: 102,
    medicine: "Metformin Hydrochloride ER 500mg",
    dosage: "500mg",
    qty: 60,
    unit: "Tablets",
    expiry: "2026-11-15",
    batch: "MTF-9921-A",
    status: "PICKUP_SCHEDULED",
    statusIndex: 3,
    orgName: "CarePoint Free Community Clinic",
    score: 96
  },
  {
    id: 103,
    medicine: "Paracetamol Tablets IP 650mg",
    dosage: "650mg",
    qty: 50,
    unit: "Strips",
    expiry: "2026-08-30",
    batch: "PCM-8830-K",
    status: "REDISTRIBUTED",
    statusIndex: 4,
    orgName: "Seva Charitable Mission Hospital",
    score: 99
  }
];

// Presets data
const PRESETS = {
  amox: {
    name: "Amoxicillin Trihydrate 500mg",
    category: "Antibiotics",
    dosage: "500mg",
    qty: 10,
    unit: "Strips",
    expiry: "2027-04-30",
    batch: "AMX-89301",
    condition: "Intact Blister Strip",
    storage: "Room Temperature (15-25°C)",
    score: 98,
    matchedOrg: "Hope Health Foundation NGO",
    matchedSub: "Verified NGO • 2.4 km away (Urgent Demand)"
  },
  metformin: {
    name: "Metformin Hydrochloride 850mg",
    category: "Chronic Care",
    dosage: "850mg",
    qty: 60,
    unit: "Tablets",
    expiry: "2026-12-15",
    batch: "MET-55410",
    condition: "Original Bottle Sealed",
    storage: "Cool & Dry Place",
    score: 96,
    matchedOrg: "CarePoint Free Community Clinic",
    matchedSub: "Community Clinic • 3.8 km away"
  },
  salbutamol: {
    name: "Salbutamol Sulfate Inhaler 100mcg",
    category: "Respiratory",
    dosage: "100mcg",
    qty: 2,
    unit: "Canisters",
    expiry: "2027-01-10",
    batch: "SAL-10294",
    condition: "Unopened Sealed Box",
    storage: "Room Temperature (15-25°C)",
    score: 97,
    matchedOrg: "CarePoint Free Community Clinic",
    matchedSub: "Asthma Pediatric Camp • Critical Need"
  },
  paracetamol: {
    name: "Paracetamol Tablets IP 650mg",
    category: "Pain Relief",
    dosage: "650mg",
    qty: 4,
    unit: "Strips",
    expiry: "2026-10-31",
    batch: "PCM-44390",
    condition: "Intact Blister Strip",
    storage: "Room Temperature (15-25°C)",
    score: 95,
    matchedOrg: "Seva Charitable Mission Hospital",
    matchedSub: "Charitable Hospital • Free Ward"
  },
  insulin: {
    name: "Insulin Glargine Lantus SoloStar",
    category: "Chronic Care",
    dosage: "100 U/mL",
    qty: 3,
    unit: "Pens",
    expiry: "2026-11-20",
    batch: "INS-88120",
    condition: "Unopened Sealed Box",
    storage: "Refrigerated (2-8°C)",
    score: 99,
    matchedOrg: "CarePoint Free Community Clinic",
    matchedSub: "Cold-chain courier certified"
  }
};

// State
let donations = JSON.parse(localStorage.getItem("medibridge_donations")) || INITIAL_DONATIONS;
let requests = INITIAL_REQUESTS;
let partners = INITIAL_ORGS;

// Navigation
function switchTab(tabId) {
  document.querySelectorAll(".page-section").forEach(sec => sec.classList.remove("active"));
  document.querySelectorAll(".nav-item").forEach(item => item.classList.remove("active"));

  const targetSection = document.getElementById("section-" + tabId);
  if (targetSection) targetSection.classList.add("active");

  const activeLink = document.querySelector(`a[href="#${tabId}"]`);
  if (activeLink) activeLink.classList.add("active");

  window.scrollTo({ top: 0, behavior: 'smooth' });
}

// Preset Handler
function applyPreset(key) {
  const p = PRESETS[key];
  if (!p) return;

  document.getElementById("med-name").value = p.name;
  document.getElementById("med-category").value = p.category;
  document.getElementById("med-dosage").value = p.dosage;
  document.getElementById("med-qty").value = p.qty;
  document.getElementById("med-unit").value = p.unit;
  document.getElementById("med-expiry").value = p.expiry;
  document.getElementById("med-batch").value = p.batch;
  document.getElementById("med-condition").value = p.condition;
  document.getElementById("med-storage").value = p.storage;

  document.getElementById("ai-score-display").innerText = `${p.score}% Safety Score`;
  document.getElementById("matched-org-name").innerText = p.matchedOrg;
  document.getElementById("matched-org-sub").innerText = p.matchedSub;
}

// Form Submission
function handleDonationSubmit(e) {
  e.preventDefault();

  const newDonation = {
    id: Date.now(),
    medicine: document.getElementById("med-name").value,
    dosage: document.getElementById("med-dosage").value || "Standard",
    qty: parseInt(document.getElementById("med-qty").value) || 1,
    unit: document.getElementById("med-unit").value,
    expiry: document.getElementById("med-expiry").value,
    batch: document.getElementById("med-batch").value,
    status: "AI_VERIFIED",
    statusIndex: 1,
    orgName: document.getElementById("matched-org-name").innerText,
    score: 98
  };

  donations.unshift(newDonation);
  localStorage.setItem("medibridge_donations", JSON.stringify(donations));

  // If AndroidBridge is available in WebView
  if (window.AndroidBridge && window.AndroidBridge.onWebDonationSubmitted) {
    window.AndroidBridge.onWebDonationSubmitted(JSON.stringify(newDonation));
  }

  alert(`Donation of ${newDonation.medicine} submitted & AI verified! Assigned to ${newDonation.orgName}`);
  renderTracker();
  switchTab("tracker");
}

// Render Functions
function renderUrgentRequests() {
  const container = document.getElementById("urgent-requests-container");
  if (!container) return;

  container.innerHTML = requests.map(r => `
    <div class="request-card">
      <div>
        <div class="card-top">
          <span class="urgency-badge urgency-${r.urgency}">${r.urgency}</span>
          <span style="font-size: 0.8rem; color: var(--text-muted);">${r.category}</span>
        </div>
        <h4 style="font-size: 1.05rem; font-weight: 700;">${r.medicine}</h4>
        <p style="font-size: 0.85rem; color: var(--text-muted);">${r.org}</p>
        <div class="progress-bar-bg">
          <div class="progress-fill" style="width: ${(r.fulfilled / r.needed) * 100}%;"></div>
        </div>
        <p style="font-size: 0.78rem; color: var(--text-muted);">${r.fulfilled} / ${r.needed} ${r.unit} fulfilled</p>
      </div>
      <button class="btn btn-primary btn-block btn-sm" style="margin-top: 14px;" onclick="pledgeFor('${r.medicine}')">Donate for This</button>
    </div>
  `).join("");
}

function pledgeFor(med) {
  document.getElementById("med-name").value = med;
  switchTab("donate");
}

function renderPartners(filter = "ALL") {
  const container = document.getElementById("partners-container");
  if (!container) return;

  const filtered = partners.filter(p => {
    if (filter === "ALL") return true;
    return p.type.includes(filter);
  });

  container.innerHTML = filtered.map(p => `
    <div class="org-card">
      <div>
        <div class="card-top">
          <span style="font-size: 0.75rem; font-weight: 700; color: var(--primary);">${p.type}</span>
          <span style="font-size: 0.8rem; font-weight: 700;">★ ${p.rating}</span>
        </div>
        <h4 style="font-size: 1.1rem; font-weight: 800;">${p.name}</h4>
        <p style="font-size: 0.82rem; color: var(--text-muted);">Lic: ${p.license} • ${p.city} (${p.distance})</p>
        <div style="background: var(--surface-alt); padding: 8px 12px; border-radius: 8px; margin: 10px 0;">
          <p style="font-size: 0.75rem; font-weight: 700; color: var(--primary-dark);">Urgent Demand:</p>
          <p style="font-size: 0.82rem;">${p.urgentNeeds}</p>
        </div>
      </div>
      <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 12px;">
        <span style="font-size: 0.75rem; color: var(--accent); font-weight: 700;">✓ ${p.donationsReceived} redistributed</span>
        <button class="btn btn-outline btn-sm" onclick="alert('Contacting ${p.name} at ${p.phone}')">Contact</button>
      </div>
    </div>
  `).join("");
}

function filterPartners(type) {
  document.querySelectorAll(".filter-btn").forEach(b => b.classList.remove("active"));
  event.target.classList.add("active");
  renderPartners(type);
}

function renderTracker() {
  const container = document.getElementById("tracker-container");
  if (!container) return;

  const milestones = [
    { key: "VERIFIED", title: "Verified", desc: "AI OCR safety validation completed. Packaging seal & batch verified." },
    { key: "PENDING_PICKUP", title: "Pending Pickup", desc: "Matched with recipient organization. Courier dispatch scheduled." },
    { key: "IN_TRANSIT", title: "In Transit", desc: "Courier collected medication package from donor location. En route under compliant storage." },
    { key: "DELIVERED", title: "Delivered", desc: "Delivered to recipient clinic pharmacy and dispensed to patients." }
  ];

  container.innerHTML = donations.map(d => {
    // Determine milestone level (1: Verified, 2: Pending Pickup, 3: In Transit, 4: Delivered)
    let currentLevel = 1;
    if (d.status === "PENDING_PICKUP" || d.status === "MATCHED") currentLevel = 2;
    else if (d.status === "IN_TRANSIT" || d.status === "PICKUP_SCHEDULED") currentLevel = 3;
    else if (d.status === "DELIVERED" || d.status === "REDISTRIBUTED") currentLevel = 4;

    const baseDate = new Date(d.id || Date.now() - 86400000);
    const formatDate = (offsetMin) => {
      const date = new Date(baseDate.getTime() + offsetMin * 60 * 1000);
      return date.toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" }) +
        " • " + date.toLocaleTimeString("en-US", { hour: "2-digit", minute: "2-digit" });
    };

    const timeMarkers = [
      formatDate(2),
      formatDate(45),
      formatDate(180),
      formatDate(320)
    ];

    return `
    <div class="tracker-item">
      <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;">
        <div>
          <h4 style="font-size: 1.15rem; font-weight: 800;">${d.medicine}</h4>
          <p style="font-size: 0.85rem; color: var(--text-muted);">${d.qty} ${d.unit} • Batch: ${d.batch} • Exp: ${d.expiry}</p>
          <p style="font-size: 0.85rem; font-weight: 600; color: var(--primary); margin-top: 2px;">Recipient: ${d.orgName}</p>
        </div>
        <span style="background: var(--primary-light); color: var(--primary-dark); font-weight: 800; font-size: 0.78rem; padding: 4px 10px; border-radius: 6px;">
          ${d.status.replace("_", " ")}
        </span>
      </div>

      <!-- REAL-TIME GEOGRAPHIC ROUTE MAP -->
      <div class="geo-map-container" style="background: #0f172a; border-radius: 12px; padding: 12px; margin: 12px 0; color: white;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
          <span style="font-size: 0.75rem; font-weight: 800; color: #38bdf8; display: flex; align-items: center; gap: 6px;">
            <span style="width: 8px; height: 8px; border-radius: 50%; background: ${currentLevel >= 4 ? '#10b981' : '#38bdf8'}; display: inline-block;"></span>
            ${currentLevel >= 4 ? 'DELIVERY COMPLETED' : 'LIVE TRANSIT TELEMETRY'}
          </span>
          <span style="font-size: 0.72rem; color: #94a3b8;">Courier #VB-402 • 32 km/h</span>
        </div>

        <svg viewBox="0 0 540 120" style="width: 100%; height: 110px; overflow: visible;">
          <!-- Grid Lines -->
          <line x1="0" y1="30" x2="540" y2="30" stroke="#1e293b" stroke-width="1" />
          <line x1="0" y1="60" x2="540" y2="60" stroke="#1e293b" stroke-width="1" />
          <line x1="0" y1="90" x2="540" y2="90" stroke="#1e293b" stroke-width="1" />
          <line x1="140" y1="0" x2="140" y2="120" stroke="#1e293b" stroke-width="1" />
          <line x1="280" y1="0" x2="280" y2="120" stroke="#1e293b" stroke-width="1" />
          <line x1="420" y1="0" x2="420" y2="120" stroke="#1e293b" stroke-width="1" />

          <!-- Background Planned Route -->
          <path d="M 50 85 C 180 20, 340 110, 480 35" fill="none" stroke="#334155" stroke-width="5" stroke-dasharray="6,6" />

          <!-- Active Traveled Route -->
          <path d="M 50 85 C 180 20, 340 110, 480 35" fill="none" stroke="url(#routeGrad)" stroke-width="6" stroke-linecap="round"
                stroke-dasharray="500" stroke-dashoffset="${500 - (500 * (currentLevel / 4))}" style="transition: stroke-dashoffset 1s ease;" />

          <defs>
            <linearGradient id="routeGrad" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stop-color="#10b981" />
              <stop offset="60%" stop-color="#06b6d4" />
              <stop offset="100%" stop-color="#3b82f6" />
            </linearGradient>
          </defs>

          <!-- Origin Node (Donor) -->
          <circle cx="50" cy="85" r="10" fill="#10b981" />
          <circle cx="50" cy="85" r="4" fill="white" />
          <text x="50" y="110" font-size="10" fill="#a7f3d0" font-weight="700" text-anchor="middle">Donor Origin</text>

          <!-- Destination Node (NGO Partner) -->
          <circle cx="480" cy="35" r="16" fill="rgba(124, 58, 237, 0.3)" />
          <circle cx="480" cy="35" r="10" fill="#7c3aed" />
          <circle cx="480" cy="35" r="4" fill="white" />
          <text x="480" y="62" font-size="10" fill="#ddd6fe" font-weight="700" text-anchor="middle">${d.orgName.split(" ")[0]} Clinic</text>
        </svg>

        <!-- Live Telemetry Bar -->
        <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 6px; border-top: 1px solid #1e293b; padding-top: 8px; margin-top: 4px; font-size: 0.75rem; text-align: center;">
          <div><span style="color: #94a3b8; display: block; font-size: 0.68rem;">Route</span><strong style="color: #f8fafc;">2.4 km total</strong></div>
          <div><span style="color: #94a3b8; display: block; font-size: 0.68rem;">Remaining</span><strong style="color: #38bdf8;">${currentLevel >= 4 ? '0 km' : '0.8 km'}</strong></div>
          <div><span style="color: #94a3b8; display: block; font-size: 0.68rem;">Storage</span><strong style="color: #10b981;">4.8°C Safe</strong></div>
          <div><span style="color: #94a3b8; display: block; font-size: 0.68rem;">ETA</span><strong style="color: #fbbf24;">${currentLevel >= 4 ? 'Arrived' : '14 mins'}</strong></div>
        </div>
      </div>

      <!-- DETAILED VISUAL TIMELINE -->
      <div class="timeline-wrapper">
        ${milestones.map((m, idx) => {
          const stepNum = idx + 1;
          const isDone = currentLevel >= stepNum;
          const isCurrent = currentLevel === stepNum;
          const isLast = idx === milestones.length - 1;
          const itemClass = isDone ? "completed" : (isCurrent ? "current" : "upcoming");

          return `
            <div class="timeline-item ${itemClass}">
              <div class="timeline-track">
                <div class="timeline-icon">${isDone ? "✓" : stepNum}</div>
                ${!isLast ? `<div class="timeline-line"></div>` : ""}
              </div>
              <div class="timeline-content">
                <div class="timeline-top">
                  <span class="timeline-title">${m.title}</span>
                  <span class="timeline-time">${timeMarkers[idx]}</span>
                </div>
                <p class="timeline-desc">${m.desc}</p>
              </div>
            </div>
          `;
        }).join("")}
      </div>

      <div style="display: flex; justify-content: flex-end; gap: 8px; margin-top: 10px;">
        ${currentLevel < 4 ? `<button class="btn btn-sm btn-primary" onclick="advanceDonationStatus(${d.id})">Advance Next Stage &rarr;</button>` : `<span style="font-size: 0.85rem; color: var(--accent); font-weight: 800;">✓ Fully Delivered & Dispensed</span>`}
      </div>
    </div>
  `;
  }).join("");
}

function advanceDonationStatus(id) {
  const d = donations.find(x => x.id === id);
  if (!d) return;

  const sequence = ["VERIFIED", "PENDING_PICKUP", "IN_TRANSIT", "DELIVERED"];
  let currIdx = 0;
  if (d.status === "PENDING_PICKUP" || d.status === "MATCHED") currIdx = 1;
  else if (d.status === "IN_TRANSIT" || d.status === "PICKUP_SCHEDULED") currIdx = 2;
  else if (d.status === "DELIVERED" || d.status === "REDISTRIBUTED") currIdx = 3;

  if (currIdx < 3) {
    d.status = sequence[currIdx + 1];
    localStorage.setItem("medibridge_donations", JSON.stringify(donations));
    renderTracker();
  }
}

function exportDataJson() {
  const data = {
    donations,
    partners,
    requests,
    exportedAt: new Date().toISOString()
  };
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: "application/json" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `medibridge_data_${Date.now()}.json`;
  a.click();
}

// Live Search Filter (Like Amazon / Flipkart universal search)
function filterMedicinesLive() {
  const query = (document.getElementById("universal-search-input")?.value || "").toLowerCase().trim();
  const category = document.getElementById("search-category")?.value || "ALL";

  const container = document.getElementById("urgent-requests-container");
  if (!container) return;

  const filtered = requests.filter(r => {
    const matchesCat = category === "ALL" || r.category.toLowerCase().includes(category.toLowerCase());
    const matchesQuery = !query ||
      r.medicine.toLowerCase().includes(query) ||
      r.org.toLowerCase().includes(query) ||
      r.category.toLowerCase().includes(query);
    return matchesCat && matchesQuery;
  });

  if (filtered.length === 0) {
    container.innerHTML = `
      <div style="grid-column: 1 / -1; padding: 30px; text-align: center; background: white; border-radius: 12px; border: 1px dashed var(--border);">
        <p style="color: var(--text-muted); font-size: 0.95rem;">No clinic requests found matching "${query}". Try searching "Amoxicillin", "Insulin", or "Inhaler".</p>
      </div>
    `;
    return;
  }

  container.innerHTML = filtered.map(req => `
    <div class="urgent-card">
      <div class="urgent-top">
        <span class="badge ${req.urgency === 'Critical' ? 'badge-danger' : 'badge-warning'}">${req.urgency.toUpperCase()} NEED</span>
        <span class="req-cat">${req.category}</span>
      </div>
      <h4 class="req-title">${req.medicine}</h4>
      <p class="req-org">🏥 Requested by: <strong>${req.org}</strong></p>
      <div class="req-progress-bar">
        <div class="req-fill" style="width: ${(req.collected / req.needed) * 100}%"></div>
      </div>
      <div class="req-stats">
        <span>Collected: <strong>${req.collected}</strong></span>
        <span>Goal: <strong>${req.needed}</strong></span>
      </div>
      <button class="btn btn-sm btn-outline-primary" style="width: 100%; margin-top: 10px;" onclick="prefillDonation('${req.medicine}', '${req.category}')">
        Pledge & Donate Medicine &rarr;
      </button>
    </div>
  `).join("");
}

// Ecosystem Modal Controls
function openEcosystemModal() {
  const m = document.getElementById("ecosystem-modal");
  if (m) m.style.display = "flex";
}

function closeEcosystemModal() {
  const m = document.getElementById("ecosystem-modal");
  if (m) m.style.display = "none";
}

function openInstallPwaGuide() {
  alert("Install MediBridge PWA:\n\n1. In Chrome/Edge: Click the 'Install App' icon (⊕) on the right side of the address bar.\n2. On iPhone Safari: Tap Share ➔ 'Add to Home Screen'.\n3. On Android Chrome: Tap Menu (⋮) ➔ 'Install App'.\n\nRuns offline and launches full-screen just like a native app!");
}

// Service Worker Registration for PWA Support
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('./sw.js').then(
      (reg) => { console.log('MediBridge PWA ServiceWorker registered:', reg.scope); },
      (err) => { console.log('ServiceWorker registration failed:', err); }
    );
  });
}

// Init
window.addEventListener("DOMContentLoaded", () => {
  renderUrgentRequests();
  renderPartners();
  renderTracker();
  applyPreset('amox');
  const countBadge = document.getElementById("active-donations-badge");
  if (countBadge) countBadge.innerText = donations.length;
});
