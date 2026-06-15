(function () {
  const APP_CONTEXT = "/medsupply-cloud";

  const roleRoutes = {
    ADMIN: "/pages/admin/dashboard.html",
    CLIENT: "/pages/client/dashboard.html",
    SUPPLIER: "/pages/supplier/dashboard.html",
  };

  const protectedRoutes = [
    { prefix: "/pages/admin/", role: "ADMIN" },
    { prefix: "/pages/client/", role: "CLIENT" },
    { prefix: "/pages/supplier/", role: "SUPPLIER" },
    { prefix: "/notifications.html", role: null },
  ];

  const publicRoutes = [
    "/",
    "/index.html",
    "/404.html",
    "/pages/auth/login.html",
    "/pages/auth/register.html",
    "/pages/auth/pending.html",
  ];

  function normalizeRole(role) {
    return String(role || "").trim().toUpperCase();
  }

  function appPath(path) {
    if (!path) return APP_CONTEXT + "/";
    if (/^https?:\/\//i.test(path)) return path;
    if (path.startsWith(APP_CONTEXT + "/") || path === APP_CONTEXT) return path;
    return APP_CONTEXT + (path.startsWith("/") ? path : "/" + path);
  }

  function routePath() {
    const pathname = window.location.pathname;
    if (pathname === APP_CONTEXT) return "/";
    return pathname.startsWith(APP_CONTEXT)
      ? pathname.slice(APP_CONTEXT.length) || "/"
      : pathname;
  }

  function redirect(path) {
    window.location.href = appPath(path);
  }

  function getCurrentRole() {
    return normalizeRole(sessionStorage.getItem("medsupply_role"));
  }

  function getCurrentUser() {
    try {
      return JSON.parse(sessionStorage.getItem("medsupply_user") || "null");
    } catch {
      return null;
    }
  }

  function dashboardFor(role) {
    return appPath(roleRoutes[normalizeRole(role)] || "/pages/auth/login.html");
  }

  function isPublicPath(path) {
    return publicRoutes.some((publicPath) => path === publicPath);
  }

  function routeRule(path) {
    return protectedRoutes.find((route) => path.startsWith(route.prefix));
  }

  function isAuthenticated() {
    return !!getCurrentRole() && !!getCurrentUser();
  }

  function checkAuth() {
    if (!isAuthenticated()) {
      redirect("/pages/auth/login.html");
      return false;
    }
    return true;
  }

  function checkRole(expectedRole) {
    const role = getCurrentRole();
    const expected = normalizeRole(expectedRole);
    if (expected && role !== expected) {
      redirect(dashboardFor(role));
      return false;
    }
    return true;
  }

  function checkRouteAccess() {
    const path = routePath();

    if (isPublicPath(path)) return true;

    const rule = routeRule(path);
    if (!rule) return true;

    if (!checkAuth()) return false;
    if (rule.role && !checkRole(rule.role)) return false;
    return true;
  }

  function redirectToDashboard(role) {
    redirect(dashboardFor(role || getCurrentRole()));
  }

  function requireAuth(allowedRoles = []) {
    if (!checkAuth()) return false;
    const roles = allowedRoles.map(normalizeRole).filter(Boolean);
    if (roles.length > 0 && !roles.includes(getCurrentRole())) {
      redirectToDashboard();
      return false;
    }
    return true;
  }

  function initAuthPage() {
    if (isAuthenticated()) redirectToDashboard();
  }

  function initPage(expectedRole) {
    if (!checkRouteAccess()) return false;
    if (expectedRole && !checkRole(expectedRole)) return false;

    const role = getCurrentRole();
    if (typeof window.renderSidebar === "function") window.renderSidebar(role);
    if (typeof window.renderHeader === "function") window.renderHeader();
    if (typeof window.updateNotificationBadge === "function") {
      window.updateNotificationBadge();
    }
    if (typeof window.initIcons === "function") window.initIcons();
    return true;
  }

  function initIcons() {
    if (window.lucide && typeof window.lucide.createIcons === "function") {
      window.lucide.createIcons();
    }
  }

  window.MedSupply = {
    APP_CONTEXT,
    appPath,
    routePath,
    redirect,
    dashboardFor,
    getCurrentRole,
    getCurrentUser,
    redirectToDashboard,
    initIcons,
  };

  window.roleRoutes = Object.fromEntries(
    Object.entries(roleRoutes).map(([role, path]) => [role, appPath(path)]),
  );
  window.appPath = appPath;
  window.getCurrentRole = getCurrentRole;
  window.getCurrentUser = getCurrentUser;
  window.isAuthenticated = isAuthenticated;
  window.checkAuth = checkAuth;
  window.checkRole = checkRole;
  window.checkRouteAccess = checkRouteAccess;
  window.redirectToDashboard = redirectToDashboard;
  window.requireAuth = requireAuth;
  window.initAuthPage = initAuthPage;
  window.initPage = initPage;
  window.initIcons = initIcons;

  document.addEventListener("DOMContentLoaded", () => {
    checkRouteAccess();
    initIcons();
  });
})();
