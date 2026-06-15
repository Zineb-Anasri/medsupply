(function () {
  function appPath(path) {
    if (window.MedSupply?.appPath) return window.MedSupply.appPath(path);
    return "/medsupply-cloud" + (path.startsWith("/") ? path : "/" + path);
  }

  function normalizeRole(role) {
    return String(role || "").trim().toUpperCase();
  }

  function dashboardFor(role) {
    const routes = {
      ADMIN: "/pages/admin/dashboard.html",
      CLIENT: "/pages/client/dashboard.html",
      SUPPLIER: "/pages/supplier/dashboard.html",
    };
    return appPath(routes[normalizeRole(role)] || "/pages/auth/login.html");
  }

  function notify(message, type = "info") {
    if (typeof window.showToast === "function") {
      window.showToast(message, type);
    }
  }

  function showLoginError(message) {
    const errorDiv = document.getElementById("loginError");
    if (errorDiv) {
      errorDiv.textContent = message;
      errorDiv.classList.remove("hidden");
    } else {
      notify(message, "error");
    }
  }

  function hideLoginError() {
    const errorDiv = document.getElementById("loginError");
    if (errorDiv) errorDiv.classList.add("hidden");
  }

  function saveSession(data) {
    const role = normalizeRole(data.role);
    const user = {
      userId: data.userId,
      email: data.email,
      role,
    };

    sessionStorage.setItem("medsupply_role", role);
    sessionStorage.setItem("medsupply_user", JSON.stringify(user));
    if (data.token) sessionStorage.setItem("medsupply_token", data.token);
  }

  async function login(email, password) {
    const result = await apiFetch("/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });

    if (result?.data?.success) {
      saveSession(result.data);
      notify("Connexion reussie", "success");
      window.location.href = dashboardFor(result.data.role);
      return result.data;
    }

    const message = result?.data?.message || "Email ou mot de passe incorrect";
    showLoginError(message);
    return null;
  }

  async function register(userData) {
    const result = await apiFetch("/auth/register", {
      method: "POST",
      body: JSON.stringify(userData),
    });

    if (result?.data?.success) return result.data;
    throw new Error(result?.data?.message || "Erreur lors de l'inscription");
  }

  async function logout() {
    try {
      await apiFetch("/auth/logout", { method: "POST" });
    } finally {
      sessionStorage.clear();
      window.location.href = appPath("/pages/auth/login.html");
    }
  }

  function getCurrentUser() {
    if (window.MedSupply?.getCurrentUser) return window.MedSupply.getCurrentUser();
    try {
      return JSON.parse(sessionStorage.getItem("medsupply_user") || "null");
    } catch {
      return null;
    }
  }

  function getCurrentRole() {
    if (window.MedSupply?.getCurrentRole) return window.MedSupply.getCurrentRole();
    return normalizeRole(sessionStorage.getItem("medsupply_role"));
  }

  function requireAuth(allowedRoles = []) {
    const role = getCurrentRole();
    if (!role || !getCurrentUser()) {
      window.location.href = appPath("/pages/auth/login.html");
      return false;
    }

    const roles = allowedRoles.map(normalizeRole).filter(Boolean);
    if (roles.length > 0 && !roles.includes(role)) {
      window.location.href = dashboardFor(role);
      return false;
    }
    return true;
  }

  function initAuthPage() {
    const role = getCurrentRole();
    if (role && getCurrentUser()) window.location.href = dashboardFor(role);
  }

  window.login = login;
  window.register = register;
  window.logout = logout;
  window.handleLogout = logout;
  window.getCurrentUser = getCurrentUser;
  window.getCurrentRole = getCurrentRole;
  window.requireAuth = window.requireAuth || requireAuth;
  window.initAuthPage = window.initAuthPage || initAuthPage;
  window.showLoginError = showLoginError;
  window.hideLoginError = hideLoginError;
  window.saveAuthSession = saveSession;
})();
