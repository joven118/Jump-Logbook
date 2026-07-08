import { createClient } from "@supabase/supabase-js";
import "./styles.css";

const supabaseUrl = import.meta.env.VITE_SUPABASE_URL;
const supabaseAnonKey = import.meta.env.VITE_SUPABASE_ANON_KEY;

const app = document.querySelector("#app");

let supabase = null;
let session = null;
let profile = null;
let facilities = [];
let inventory = [];
let waivers = [];
let schedules = [];
let statusMessage = "";
let activeTab = "profile";

const fieldMap = {
  name: "Operator / jumper name",
  screen_name: "Screen name",
  license_number: "License number",
  mobile_number: "Mobile number",
  email: "Email",
  dz_name: "Dropzone name",
  dz_street: "Street",
  dz_city: "City",
  dz_province: "Province / state",
  dz_country: "Country",
  dz_mobile_number: "DZ phone",
  dz_email: "DZ email",
  dz_website: "Website",
  operating_days: "Operating days",
  operating_hours: "Operating hours",
};

function nowId() {
  return Date.now();
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;");
}

function initSupabase() {
  if (!supabaseUrl || !supabaseAnonKey) {
    renderConfigMissing();
    return false;
  }
  supabase = createClient(supabaseUrl, supabaseAnonKey);
  return true;
}

async function boot() {
  if (!initSupabase()) return;
  const { data } = await supabase.auth.getSession();
  session = data.session;
  if (session) {
    await loadDashboard();
  }
  render();
}

async function loadDashboard() {
  statusMessage = "Syncing dashboard...";
  render();

  const userId = session.user.id;
  const { data: userProfile, error } = await supabase
    .from("users")
    .select("*")
    .eq("user_id", userId)
    .maybeSingle();

  if (error) {
    statusMessage = error.message;
    render();
    return;
  }

  profile = userProfile;
  if (!profile) {
    profile = {
      user_id: userId,
      name: session.user.email?.split("@")[0] ?? "Jump Logbook User",
      email: session.user.email,
      role: "JUMPER",
      license_number: "",
      wallet_balance: 0,
      is_verified: false,
    };
    const { error: insertError } = await supabase.from("users").upsert(profile);
    if (insertError) {
      statusMessage = insertError.message;
      render();
      return;
    }
  }

  if (profile.role === "DZ_OPERATOR") {
    await Promise.all([loadFacilities(), loadInventory(), loadWaivers(), loadSchedules()]);
  }

  statusMessage = "Dashboard synced.";
}

async function loadFacilities() {
  const { data, error } = await supabase
    .from("dz_facilities")
    .select("*")
    .eq("dz_id", profile.user_id)
    .order("id", { ascending: false });
  if (!error) facilities = data ?? [];
}

async function loadInventory() {
  const { data, error } = await supabase
    .from("dz_inventory")
    .select("*")
    .eq("dz_id", profile.user_id)
    .order("id", { ascending: false });
  if (!error) inventory = data ?? [];
}

async function loadWaivers() {
  const { data, error } = await supabase
    .from("dz_waivers")
    .select("*")
    .eq("dz_id", profile.user_id)
    .order("last_updated", { ascending: false });
  if (!error) waivers = data ?? [];
}

async function loadSchedules() {
  const { data, error } = await supabase
    .from("flight_schedules")
    .select("*")
    .eq("dz_id", profile.user_id)
    .order("date_of_flight", { ascending: true });
  if (!error) schedules = data ?? [];
}

async function signIn(event) {
  event.preventDefault();
  const form = new FormData(event.currentTarget);
  statusMessage = "Signing in...";
  render();

  const { data, error } = await supabase.auth.signInWithPassword({
    email: form.get("email"),
    password: form.get("password"),
  });

  if (error) {
    statusMessage = error.message;
    render();
    return;
  }

  session = data.session;
  await loadDashboard();
  render();
}

async function signOut() {
  await supabase.auth.signOut();
  session = null;
  profile = null;
  facilities = [];
  inventory = [];
  waivers = [];
  schedules = [];
  statusMessage = "Signed out.";
  render();
}

async function saveProfile(event) {
  event.preventDefault();
  const form = new FormData(event.currentTarget);
  const nextProfile = { ...profile };
  for (const key of Object.keys(fieldMap)) {
    nextProfile[key] = form.get(key) || null;
  }
  nextProfile.user_id = profile.user_id;
  nextProfile.role = profile.role || "JUMPER";
  nextProfile.license_number = nextProfile.license_number ?? "";
  nextProfile.wallet_balance = profile.wallet_balance ?? 0;
  nextProfile.is_verified = profile.is_verified ?? false;

  const { error } = await supabase.from("users").upsert(nextProfile);
  if (error) {
    statusMessage = error.message;
    render();
    return;
  }

  profile = nextProfile;
  statusMessage = "Profile saved.";
  render();
}

async function saveFacility(event) {
  event.preventDefault();
  const form = new FormData(event.currentTarget);
  const facility = {
    id: nowId(),
    dz_id: profile.user_id,
    name: form.get("name"),
    description: form.get("description") || "",
  };
  const { error } = await supabase.from("dz_facilities").upsert(facility);
  statusMessage = error ? error.message : "Facility saved.";
  await loadFacilities();
  render();
}

async function saveInventory(event) {
  event.preventDefault();
  const form = new FormData(event.currentTarget);
  const item = {
    id: nowId(),
    dz_id: profile.user_id,
    name: form.get("name"),
    category: form.get("category"),
    make_model: form.get("make_model") || "",
    serial_number: form.get("serial_number") || "",
    aircraft_type: form.get("aircraft_type") || "",
    max_jumpers: Number(form.get("max_jumpers") || 0),
  };
  const { error } = await supabase.from("dz_inventory").upsert(item);
  statusMessage = error ? error.message : "Inventory item saved.";
  await loadInventory();
  render();
}

async function saveWaiver(event) {
  event.preventDefault();
  const form = new FormData(event.currentTarget);
  const waiver = {
    id: nowId(),
    dz_id: profile.user_id,
    title: form.get("title"),
    content: form.get("content"),
    is_active: true,
    last_updated: Date.now(),
  };
  const { error } = await supabase.from("dz_waivers").upsert(waiver);
  statusMessage = error ? error.message : "Waiver saved.";
  await loadWaivers();
  render();
}

async function saveSchedule(event) {
  event.preventDefault();
  const form = new FormData(event.currentTarget);
  const schedule = {
    schedule_id: nowId(),
    dz_id: profile.user_id,
    date_of_flight: new Date(form.get("date_of_flight")).getTime(),
    frequency: form.get("frequency") || "Specific Date",
    load_number: Number(form.get("load_number") || 1),
    aircraft_id: null,
    aircraft_name: form.get("aircraft_name") || "",
    aircraft_type: form.get("aircraft_type") || "",
    aircraft_tail_number: form.get("aircraft_tail_number") || "",
    load_capacity: Number(form.get("load_capacity") || 0),
    creation_source: "DASHBOARD",
    booked_jumper_ids: "",
  };
  const { error } = await supabase.from("flight_schedules").upsert(schedule);
  statusMessage = error ? error.message : "Flight schedule saved.";
  await loadSchedules();
  render();
}

function renderConfigMissing() {
  app.innerHTML = `
    <main class="shell auth-shell">
      <section class="auth-panel">
        <p class="eyebrow">Configuration required</p>
        <h1>Dashboard environment variables are missing.</h1>
        <p class="muted">Set <code>VITE_SUPABASE_URL</code> and <code>VITE_SUPABASE_ANON_KEY</code> in Cloudflare Pages for app.jumplogbook.com.</p>
      </section>
    </main>
  `;
}

function render() {
  if (!session) {
    renderAuth();
    return;
  }

  app.innerHTML = `
    <div class="dashboard-shell">
      <aside class="sidebar">
        <a class="brand" href="/">
          <span class="brand-mark">JL</span>
          <span>Jump Logbook</span>
        </a>
        <nav class="side-nav">
          ${navButton("profile", "Profile")}
          ${profile?.role === "DZ_OPERATOR" ? navButton("facilities", "Facilities") : ""}
          ${profile?.role === "DZ_OPERATOR" ? navButton("inventory", "Inventory") : ""}
          ${profile?.role === "DZ_OPERATOR" ? navButton("waivers", "Waivers") : ""}
          ${profile?.role === "DZ_OPERATOR" ? navButton("schedules", "Schedules") : ""}
        </nav>
        <button class="ghost full" data-action="sign-out">Sign out</button>
      </aside>
      <main class="content">
        <header class="dashboard-header">
          <div>
            <p class="eyebrow">app.jumplogbook.com</p>
            <h1>${escapeHtml(profile?.dz_name || profile?.name || "Dashboard")}</h1>
          </div>
          <div class="status-pill">${escapeHtml(statusMessage || "Ready")}</div>
        </header>
        ${renderActiveTab()}
      </main>
    </div>
  `;
  bindDashboardEvents();
}

function renderAuth() {
  app.innerHTML = `
    <main class="shell auth-shell">
      <section class="auth-copy">
        <p class="eyebrow">Jump Logbook Dashboard</p>
        <h1>Operate from the web, sync with the field.</h1>
        <p class="muted">Use your Jump Logbook account to manage profile records and DZO data from app.jumplogbook.com.</p>
      </section>
      <form class="auth-panel" data-form="sign-in">
        <h2>Sign in</h2>
        <label>Email<input required name="email" type="email" autocomplete="email" /></label>
        <label>Password<input required name="password" type="password" autocomplete="current-password" /></label>
        <button class="primary" type="submit">Open dashboard</button>
        <p class="form-note">${escapeHtml(statusMessage)}</p>
      </form>
    </main>
  `;
  document.querySelector('[data-form="sign-in"]').addEventListener("submit", signIn);
}

function navButton(tab, label) {
  return `<button class="${activeTab === tab ? "active" : ""}" data-tab="${tab}">${label}</button>`;
}

function renderActiveTab() {
  if (activeTab === "facilities") return renderFacilities();
  if (activeTab === "inventory") return renderInventory();
  if (activeTab === "waivers") return renderWaivers();
  if (activeTab === "schedules") return renderSchedules();
  return renderProfile();
}

function renderProfile() {
  return `
    <section class="panel">
      <div class="panel-heading">
        <h2>Profile</h2>
        <p>These fields are saved to the shared Supabase profile used by mobile and web.</p>
      </div>
      <form class="form-grid" data-form="profile">
        ${Object.entries(fieldMap)
          .map(([key, label]) => field(label, key, profile?.[key]))
          .join("")}
        <button class="primary span-2" type="submit">Save profile</button>
      </form>
    </section>
  `;
}

function renderFacilities() {
  return collectionPanel(
    "Facilities",
    "Add hangars, classrooms, packing areas, lounges, or other DZ facilities.",
    "facility",
    `
      ${field("Facility name", "name")}
      ${field("Description", "description")}
    `,
    facilities.map((item) => card(item.name, item.description))
  );
}

function renderInventory() {
  return collectionPanel(
    "Inventory",
    "Track aircraft, rental rigs, student gear, and other operational assets.",
    "inventory",
    `
      ${field("Item name", "name")}
      ${field("Category", "category", "Aircraft")}
      ${field("Make / model", "make_model")}
      ${field("Serial number", "serial_number")}
      ${field("Aircraft type", "aircraft_type")}
      ${field("Max jumpers", "max_jumpers", "", "number")}
    `,
    inventory.map((item) => card(item.name, `${item.category || ""} ${item.serial_number || ""}`))
  );
}

function renderWaivers() {
  return collectionPanel(
    "Waivers",
    "Create active waiver templates for jumper signatures.",
    "waiver",
    `
      ${field("Title", "title")}
      <label class="span-2">Content<textarea required name="content" rows="7"></textarea></label>
    `,
    waivers.map((item) => card(item.title, item.is_active ? "Active" : "Inactive"))
  );
}

function renderSchedules() {
  return collectionPanel(
    "Flight schedules",
    "Publish upcoming loads for dashboard visibility.",
    "schedule",
    `
      ${field("Date and time", "date_of_flight", "", "datetime-local")}
      ${field("Frequency", "frequency", "Specific Date")}
      ${field("Load number", "load_number", "1", "number")}
      ${field("Aircraft name", "aircraft_name")}
      ${field("Aircraft type", "aircraft_type")}
      ${field("Tail number", "aircraft_tail_number")}
      ${field("Load capacity", "load_capacity", "", "number")}
    `,
    schedules.map((item) =>
      card(`Load #${item.load_number}`, `${item.aircraft_name || "Aircraft"} · ${formatDate(item.date_of_flight)}`)
    )
  );
}

function collectionPanel(title, description, formName, fieldsHtml, cards) {
  return `
    <section class="panel">
      <div class="panel-heading">
        <h2>${title}</h2>
        <p>${description}</p>
      </div>
      <form class="form-grid" data-form="${formName}">
        ${fieldsHtml}
        <button class="primary span-2" type="submit">Save ${title.toLowerCase()}</button>
      </form>
    </section>
    <section class="cards">${cards.join("") || emptyCard(title)}</section>
  `;
}

function field(label, name, value = "", type = "text") {
  return `
    <label>${label}
      <input ${name === "name" ? "required" : ""} name="${name}" type="${type}" value="${escapeHtml(value)}" />
    </label>
  `;
}

function card(title, detail) {
  return `
    <article class="data-card">
      <strong>${escapeHtml(title || "Untitled")}</strong>
      <span>${escapeHtml(detail || "No details")}</span>
    </article>
  `;
}

function emptyCard(title) {
  return `<article class="data-card empty">No ${title.toLowerCase()} saved yet.</article>`;
}

function formatDate(value) {
  if (!value) return "No date";
  return new Date(Number(value)).toLocaleString();
}

function bindDashboardEvents() {
  document.querySelectorAll("[data-tab]").forEach((button) => {
    button.addEventListener("click", () => {
      activeTab = button.dataset.tab;
      render();
    });
  });
  document.querySelector('[data-action="sign-out"]').addEventListener("click", signOut);
  document.querySelector('[data-form="profile"]')?.addEventListener("submit", saveProfile);
  document.querySelector('[data-form="facility"]')?.addEventListener("submit", saveFacility);
  document.querySelector('[data-form="inventory"]')?.addEventListener("submit", saveInventory);
  document.querySelector('[data-form="waiver"]')?.addEventListener("submit", saveWaiver);
  document.querySelector('[data-form="schedule"]')?.addEventListener("submit", saveSchedule);
}

boot();
