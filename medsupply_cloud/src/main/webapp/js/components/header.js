function renderHeader() {
  const header = document.getElementById("header");
  if (!header) return;

  const role = String(sessionStorage.getItem("medsupply_role") || "CLIENT").toUpperCase();
  const user = JSON.parse(sessionStorage.getItem("medsupply_user") || "{}");
  const userName = user.email?.split("@")[0] || "Utilisateur";
  const currentPage = window.location.pathname.split("/").pop() || "dashboard.html";
  const roleLabel = role === "ADMIN" ? "Administrateur" : role === "SUPPLIER" ? "Fournisseur" : "Client";

  header.innerHTML = `
    <div class="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-4 md:px-6 shadow-sm">
      <div class="flex items-center gap-4 min-w-0">
        <button id="sidebar-toggle-btn" class="p-2 rounded-lg hover:bg-slate-100 transition-colors" title="Menu">
          <i data-lucide="menu" class="w-5 h-5 text-slate-600"></i>
        </button>
        <nav class="hidden sm:flex items-center gap-2 text-sm min-w-0">
          <span class="text-slate-500">MedSupply Cloud</span>
          <i data-lucide="chevron-right" class="w-4 h-4 text-slate-400"></i>
          <span class="text-slate-900 font-medium truncate">${getPageTitle(currentPage)}</span>
        </nav>
      </div>

      <div class="flex items-center gap-2 md:gap-4">
        <a href="/medsupply-cloud/notifications.html" id="notification-btn" class="relative p-2 rounded-lg hover:bg-slate-100 transition-colors" title="Notifications">
          <i data-lucide="bell" class="w-5 h-5 text-slate-600"></i>
          <span id="notification-badge" class="absolute top-1 right-1 min-w-5 h-5 px-1 bg-red-500 text-white text-xs font-bold rounded-full hidden items-center justify-center">0</span>
        </a>

        <div class="relative">
          <button id="user-dropdown-btn" class="flex items-center gap-3 pl-3 border-l border-slate-200 hover:bg-slate-50 rounded-lg p-2 transition-colors">
            <div class="w-9 h-9 bg-[#2C74B3]/10 rounded-full flex items-center justify-center">
              <i data-lucide="user" class="w-5 h-5 text-[#2C74B3]"></i>
            </div>
            <div class="hidden md:block text-left">
              <p class="text-sm font-medium text-slate-900">${userName}</p>
              <p class="text-xs text-slate-500">${roleLabel}</p>
            </div>
            <i data-lucide="chevron-down" class="w-4 h-4 text-slate-400 hidden md:block"></i>
          </button>

          <div id="user-dropdown" class="absolute right-0 top-full mt-2 w-52 bg-white rounded-lg shadow-lg border border-slate-200 hidden z-50">
            <div class="py-1">
              <a href="/medsupply-cloud/pages/profile.html" class="flex items-center gap-2 px-4 py-2 text-sm text-slate-700 hover:bg-slate-50">
                <i data-lucide="user" class="w-4 h-4"></i>
                <span>Mon profil</span>
              </a>
              <a href="/medsupply-cloud/pages/profile.html#settings" class="flex items-center gap-2 px-4 py-2 text-sm text-slate-700 hover:bg-slate-50">
                <i data-lucide="settings" class="w-4 h-4"></i>
                <span>Parametres</span>
              </a>
              <div class="border-t border-slate-200 my-1"></div>
              <button id="logout-dropdown-btn" class="flex items-center gap-2 w-full px-4 py-2 text-sm text-red-600 hover:bg-red-50">
                <i data-lucide="log-out" class="w-4 h-4"></i>
                <span>Se deconnecter</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `;

  const sidebarToggleBtn = document.getElementById("sidebar-toggle-btn");
  if (sidebarToggleBtn && typeof toggleSidebar === "function") {
    sidebarToggleBtn.addEventListener("click", toggleSidebar);
  }

  const userDropdownBtn = document.getElementById("user-dropdown-btn");
  const userDropdown = document.getElementById("user-dropdown");
  if (userDropdownBtn && userDropdown) {
    userDropdownBtn.addEventListener("click", (event) => {
      event.stopPropagation();
      userDropdown.classList.toggle("hidden");
    });
    document.addEventListener("click", () => userDropdown.classList.add("hidden"));
  }

  const logoutDropdownBtn = document.getElementById("logout-dropdown-btn");
  if (logoutDropdownBtn) logoutDropdownBtn.addEventListener("click", handleLogout);

  updateNotificationBadge();
  if (typeof initIcons === "function") initIcons();
}

function getPageTitle(page) {
  const titles = {
    "dashboard.html": "Tableau de bord",
    "clients.html": "Clients",
    "client-detail.html": "Detail client",
    "products.html": "Produits",
    "product-form.html": "Produit",
    "quotes.html": "Devis",
    "quote-new.html": "Nouveau devis",
    "orders.html": "Commandes",
    "payments.html": "Paiements",
    "stocks.html": "Stocks",
    "suppliers.html": "Fournisseurs",
    "deliveries.html": "Livraisons",
    "maintenance.html": "Maintenance",
    "marketplace.html": "Marketplace",
    "catalog.html": "Catalogue",
    "offers.html": "Offres",
    "notifications.html": "Notifications",
  };
  return titles[page] || "MedSupply Cloud";
}

async function handleLogout() {
  if (!confirm("Voulez-vous vous deconnecter ?")) return;
  if (typeof window.logout === "function") {
    await window.logout();
  } else {
    sessionStorage.clear();
    window.location.href = "/medsupply-cloud/pages/auth/login.html";
  }
}

async function updateNotificationBadge() {
  try {
    const response = await apiFetch("/notifications/unread-count");
    const badge = document.getElementById("notification-badge");
    const count = response?.data?.unreadCount || 0;
    if (badge && count > 0) {
      badge.classList.remove("hidden");
      badge.classList.add("flex");
      badge.textContent = count > 9 ? "9+" : count;
    }
  } catch (error) {
    console.error("Failed to fetch notification count:", error);
  }
}
