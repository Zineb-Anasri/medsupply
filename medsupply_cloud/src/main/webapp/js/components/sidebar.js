const sidebarConfig = {
  admin: [
    {
      group: "Tableau de bord",
      items: [
        {
          icon: "layout-dashboard",
          label: "Dashboard",
          href: "/medsupply-cloud/pages/admin/dashboard.html",
        },
      ],
    },
    {
      group: "Commercial",
      items: [
        {
          icon: "users",
          label: "Clients",
          href: "/medsupply-cloud/pages/admin/clients.html",
        },
        {
          icon: "file-text",
          label: "Devis",
          href: "/medsupply-cloud/pages/admin/quotes.html",
          badge: "pending",
        },
        {
          icon: "shopping-cart",
          label: "Commandes",
          href: "/medsupply-cloud/pages/admin/orders.html",
        },
        {
          icon: "credit-card",
          label: "Paiements",
          href: "/medsupply-cloud/pages/admin/payments.html",
        },
      ],
    },
    {
      group: "Logistique",
      items: [
        {
          icon: "package",
          label: "Stocks",
          href: "/medsupply-cloud/pages/admin/stocks.html",
          badge: "low-stock",
        },
        {
          icon: "building-2",
          label: "Fournisseurs",
          href: "/medsupply-cloud/pages/admin/suppliers.html",
        },
        {
          icon: "truck",
          label: "Livraisons",
          href: "/medsupply-cloud/pages/admin/deliveries.html",
        },
      ],
    },
    {
      group: "Après-Vente",
      items: [
        {
          icon: "wrench",
          label: "Maintenance",
          href: "/medsupply-cloud/pages/admin/maintenance.html",
        },
      ],
    },
    {
      group: "Marketplace",
      items: [
        {
          icon: "gavel",
          label: "Marketplace",
          href: "/medsupply-cloud/pages/admin/marketplace.html",
        },
      ],
    },
    {
      group: "Système",
      items: [
        {
          icon: "bell",
          label: "Notifications",
          href: "/medsupply-cloud/notifications.html",
          badge: "unread",
        },
      ],
    },
  ],
  client: [
    {
      icon: "layout-dashboard",
      label: "Dashboard",
      href: "/medsupply-cloud/pages/client/dashboard.html",
    },
    {
      icon: "grid",
      label: "Catalogue",
      href: "/medsupply-cloud/pages/client/catalog.html",
    },
    {
      icon: "file-text",
      label: "Mes Devis",
      href: "/medsupply-cloud/pages/client/quotes.html",
    },
    {
      icon: "shopping-cart",
      label: "Mes Commandes",
      href: "/medsupply-cloud/pages/client/orders.html",
    },
    {
      icon: "credit-card",
      label: "Paiements",
      href: "/medsupply-cloud/pages/client/payments.html",
    },
    {
      icon: "wrench",
      label: "Maintenance",
      href: "/medsupply-cloud/pages/client/maintenance.html",
    },
    {
      icon: "gavel",
      label: "Marketplace",
      href: "/medsupply-cloud/pages/client/marketplace.html",
    },
    {
      icon: "bell",
      label: "Notifications",
      href: "/medsupply-cloud/notifications.html",
      badge: "unread",
    },
  ],
  supplier: [
    {
      icon: "layout-dashboard",
      label: "Dashboard",
      href: "/medsupply-cloud/pages/supplier/dashboard.html",
    },
    {
      icon: "gavel",
      label: "Appels d'offres",
      href: "/medsupply-cloud/pages/supplier/marketplace.html",
    },
    {
      icon: "tag",
      label: "Mes Offres",
      href: "/medsupply-cloud/pages/supplier/offers.html",
    },
    {
      icon: "bell",
      label: "Notifications",
      href: "/medsupply-cloud/notifications.html",
      badge: "unread",
    },
  ],
};

function renderSidebar(role) {
  role = String(role || "CLIENT").toLowerCase();
  const config = sidebarConfig[role] || sidebarConfig.client;
  const sidebar = document.getElementById("sidebar");
  if (!sidebar) return;

  const currentPath = window.location.pathname;

  let menuItems = "";

  // Check if config has groups (admin) or is flat (client/supplier)
  if (config[0] && config[0].group) {
    // Admin - grouped navigation
    menuItems = config
      .map(
        (group) => `
      <div class="mb-4">
        <p class="sidebar-text px-4 py-2 text-xs font-semibold text-gray-400 uppercase tracking-wider">${group.group}</p>
        ${group.items
          .map(
            (item) => `
          <a href="${item.href}" class="flex items-center justify-between gap-3 px-4 py-2.5 text-gray-300 hover:bg-[#2C74B3] hover:text-white rounded-lg transition-colors group/item ${isActive(item.href, currentPath) ? "bg-[#2C74B3] text-white" : ""}">
            <div class="flex items-center gap-3">
              <i data-lucide="${item.icon}" class="w-5 h-5"></i>
              <span class="sidebar-text">${item.label}</span>
            </div>
            ${item.badge ? `<span class="sidebar-badge flex items-center justify-center w-5 h-5 text-xs font-bold rounded-full ${getBadgeColor(item.badge)}" id="badge-${item.badge}">0</span>` : ""}
          </a>
        `,
          )
          .join("")}
      </div>
    `,
      )
      .join("");
  } else {
    // Client/Supplier - flat navigation
    menuItems = config
      .map(
        (item) => `
      <a href="${item.href}" class="flex items-center justify-between gap-3 px-4 py-2.5 text-gray-300 hover:bg-[#2C74B3] hover:text-white rounded-lg transition-colors group/item ${isActive(item.href, currentPath) ? "bg-[#2C74B3] text-white" : ""}">
        <div class="flex items-center gap-3">
          <i data-lucide="${item.icon}" class="w-5 h-5"></i>
          <span class="sidebar-text">${item.label}</span>
        </div>
        ${item.badge ? `<span class="sidebar-badge flex items-center justify-center w-5 h-5 text-xs font-bold rounded-full ${getBadgeColor(item.badge)}" id="badge-${item.badge}">0</span>` : ""}
      </a>
    `,
      )
      .join("");
  }

  sidebar.innerHTML = `
    <div class="h-full flex flex-col bg-[#0A2647] w-72 transition-all duration-300 shadow-xl" id="sidebar-inner">
      <div class="p-4 border-b border-[#144272]">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 bg-[#2C74B3] rounded-lg flex items-center justify-center flex-shrink-0">
            <i data-lucide="activity" class="w-6 h-6 text-white"></i>
          </div>
          <span class="sidebar-text font-bold text-xl text-white">MedSupply Cloud</span>
        </div>
      </div>
      <nav class="flex-1 p-4 overflow-y-auto">
        ${menuItems}
      </nav>
      <div class="p-4 border-t border-[#144272]">
        <div class="flex items-center gap-3 mb-3">
          <div class="w-10 h-10 bg-[#2C74B3] bg-opacity-50 rounded-full flex items-center justify-center">
            <i data-lucide="user" class="w-5 h-5 text-white"></i>
          </div>
          <div class="sidebar-text flex-1 min-w-0">
            <p class="text-sm font-medium text-white truncate" id="sidebar-user-name">Utilisateur</p>
            <p class="text-xs text-gray-400 truncate" id="sidebar-user-role">Rôle</p>
          </div>
        </div>
        <div class="flex items-center gap-2">
          <button id="sidebar-toggle" class="flex items-center gap-2 text-gray-400 hover:text-white transition-colors flex-1">
            <i data-lucide="chevron-left" class="w-5 h-5 sidebar-icon"></i>
            <span class="sidebar-text">Réduire</span>
          </button>
          <button onclick="handleLogout()" class="text-gray-400 hover:text-red-400 transition-colors" title="Se déconnecter">
            <i data-lucide="log-out" class="w-5 h-5"></i>
          </button>
        </div>
      </div>
    </div>
  `;

  // Initialize Lucide icons
  if (typeof lucide !== "undefined") {
    lucide.createIcons();
  }

  // Update user info
  updateUserInfo();

  // Sidebar toggle
  const toggleBtn = document.getElementById("sidebar-toggle");
  if (toggleBtn) {
    toggleBtn.addEventListener("click", toggleSidebar);
  }

  // Update badges
  updateBadges();
}

function updateUserInfo() {
  const userName = document.getElementById("sidebar-user-name");
  const userRole = document.getElementById("sidebar-user-role");

  if (userName) {
    const user = JSON.parse(sessionStorage.getItem("medsupply_user") || "{}");
    userName.textContent = user.email?.split("@")[0] || "Utilisateur";
  }

  if (userRole) {
    const role = sessionStorage.getItem("medsupply_role") || "CLIENT";
    const roleLabels = {
      ADMIN: "Administrateur",
      CLIENT: "Client",
      SUPPLIER: "Fournisseur",
    };
    userRole.textContent = roleLabels[role] || role;
  }
}

function handleLogout() {
  if (confirm("Voulez-vous vous deconnecter ?")) {
    if (typeof window.logout === "function") {
      window.logout();
    } else {
      sessionStorage.clear();
      window.location.href = "/medsupply-cloud/pages/auth/login.html";
    }
  }
}

function isActive(href, currentPath) {
  const hrefPath = href.replace(/\/$/, "");
  const currentPathNoSlash = currentPath.replace(/\/$/, "");
  return (
    currentPathNoSlash === hrefPath ||
    currentPathNoSlash.startsWith(hrefPath + "/")
  );
}

function getBadgeColor(badgeType) {
  const colors = {
    pending: "bg-yellow-500 text-white",
    "low-stock": "bg-amber-500 text-white",
    unread: "bg-red-500 text-white",
  };
  return colors[badgeType] || "bg-gray-500 text-white";
}

async function updateBadges() {
  try {
    // Update unread notifications badge
    const notifResponse = await apiFetch("/notifications/count");
    const unreadBadge = document.getElementById("badge-unread");
    if (unreadBadge && notifResponse?.data?.unreadCount > 0) {
      unreadBadge.textContent =
        notifResponse.data.unreadCount > 9
          ? "9+"
          : notifResponse.data.unreadCount;
      unreadBadge.classList.remove("hidden");
    }

    // Update pending quotes badge (admin only) — derived from the quotes list
    const pendingBadge = document.getElementById("badge-pending");
    if (pendingBadge) {
      const quotesResponse = await apiFetch("/quotes?status=PENDING");
      const pendingCount =
        quotesResponse?.data?.count ??
        (quotesResponse?.data?.quotes || []).length;
      if (pendingCount > 0) {
        pendingBadge.textContent = pendingCount > 9 ? "9+" : pendingCount;
        pendingBadge.classList.remove("hidden");
      }
    }
    // NOTE: low-stock badge intentionally omitted — the products table has no
    // stock-quantity column and there is no stocks-alerts endpoint in the API.
  } catch (error) {
    console.error("Failed to update badges:", error);
  }
}

function toggleSidebar() {
  const sidebarInner = document.getElementById("sidebar-inner");
  const mainContent = document.getElementById("main-content");
  const sidebarTexts = document.querySelectorAll(".sidebar-text");
  const sidebarBadges = document.querySelectorAll(".sidebar-badge");
  const sidebarIcon = document.querySelector(".sidebar-icon");

  sidebarInner.classList.toggle("w-72");
  sidebarInner.classList.toggle("w-20");

  sidebarTexts.forEach((text) => {
    text.classList.toggle("hidden");
  });

  sidebarBadges.forEach((badge) => {
    badge.classList.toggle("hidden");
  });

  if (sidebarIcon) {
    sidebarIcon.classList.toggle("chevron-left");
    sidebarIcon.classList.toggle("chevron-right");
  }
}
