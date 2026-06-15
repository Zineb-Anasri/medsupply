const API_BASE = "/medsupply-cloud/api";
const USE_MOCK_API = false;

function safeToast(message, type = "info") {
  if (typeof window.showToast === "function") {
    window.showToast(message, type);
  } else {
    console[type === "error" ? "error" : "log"](message);
  }
}

function loginPath() {
  return window.MedSupply?.appPath
    ? window.MedSupply.appPath("/pages/auth/login.html")
    : "/medsupply-cloud/pages/auth/login.html";
}

function getJsonBody(options) {
  if (!options?.body) return null;
  try {
    return typeof options.body === "string" ? JSON.parse(options.body) : options.body;
  } catch {
    return null;
  }
}

function createMockResponse(data, status = 200) {
  return { ok: status >= 200 && status < 300, status, data };
}

async function ensureMockDataLoaded() {
  if (typeof MOCK_PRODUCTS !== "undefined") return;
  await new Promise((resolve) => {
    const script = document.createElement("script");
    script.src = "/medsupply-cloud/js/mock-data.js";
    script.onload = resolve;
    script.onerror = resolve;
    document.head.appendChild(script);
  });
}

async function mockFetch(endpoint, options = {}) {
  await ensureMockDataLoaded();
  const method = (options.method || "GET").toUpperCase();
  const body = getJsonBody(options);

  if (endpoint === "/auth/login" && method === "POST") {
    const user = getUserByEmail(body?.email || "");
    if (user && body?.password === user.password) {
      return createMockResponse({
        success: true,
        role: user.role,
        userId: user.userId,
        email: user.email,
      });
    }
    return createMockResponse({ success: false, message: "Email ou mot de passe incorrect" }, 401);
  }

  if (endpoint === "/auth/register" && method === "POST") {
    return createMockResponse({ success: true, message: "Inscription reussie" }, 201);
  }

  if (endpoint === "/auth/logout" && method === "POST") {
    return createMockResponse({ success: true });
  }

  if (endpoint.startsWith("/notifications/unread-count")) {
    return createMockResponse({ unreadCount: getUnreadNotificationCount?.() || 0 });
  }

  if (endpoint.startsWith("/notifications")) {
    return createMockResponse({ notifications: typeof MOCK_NOTIFICATIONS !== "undefined" ? MOCK_NOTIFICATIONS : [] });
  }

  if (endpoint === "/admin/quotes/stats") {
    return createMockResponse({ pending: 0, approved: 0, rejected: 0 });
  }

  if (endpoint === "/admin/stocks/alerts") {
    return createMockResponse({ lowStock: 0 });
  }

  return createMockResponse({ success: true, data: {} });
}

async function parseResponse(response) {
  const text = await response.text();
  if (!text) return {};
  try {
    return JSON.parse(text);
  } catch {
    return { message: text };
  }
}

async function apiFetch(endpoint, options = {}) {
  const normalizedEndpoint = endpoint.startsWith("/") ? endpoint : "/" + endpoint;

  if (USE_MOCK_API) {
    try {
      return await mockFetch(normalizedEndpoint, options);
    } catch (error) {
      console.error("Mock API Error:", error);
      safeToast("Erreur interne du mock API", "error");
      return null;
    }
  }

  const token = sessionStorage.getItem("medsupply_token");
  const headers = {
    Accept: "application/json",
    ...(options.body ? { "Content-Type": "application/json" } : {}),
    ...(token ? { Authorization: "Bearer " + token } : {}),
    ...(options.headers || {}),
  };

  try {
    const response = await fetch(API_BASE + normalizedEndpoint, {
      ...options,
      headers,
      credentials: "include",
    });

    const data = await parseResponse(response);

    if (response.status === 401) {
      sessionStorage.clear();
      window.location.href = loginPath();
      return null;
    }

    return { ok: response.ok, status: response.status, data };
  } catch (error) {
    console.error("API Error:", error);
    safeToast("Erreur de connexion au serveur", "error");
    return null;
  }
}
