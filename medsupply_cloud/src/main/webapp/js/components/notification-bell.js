let notificationDropdownOpen = false;
let notificationPollingInterval = null;

function initNotificationBell() {
  const notificationBtn = document.getElementById("notification-btn");
  if (!notificationBtn) return;

  // Create dropdown panel
  const dropdown = document.createElement("div");
  dropdown.id = "notification-dropdown";
  dropdown.className =
    "absolute right-0 top-full mt-2 w-80 bg-white rounded-lg shadow-lg border border-gray-200 hidden z-50";
  dropdown.innerHTML = `
    <div class="p-4 border-b border-gray-200">
      <div class="flex items-center justify-between">
        <h3 class="font-semibold text-gray-900">Notifications</h3>
        <button onclick="markAllAsReadFromDropdown()" class="text-sm text-primary hover:text-secondary">
          Tout marquer comme lu
        </button>
      </div>
    </div>
    <div id="notification-dropdown-list" class="max-h-96 overflow-y-auto">
      <p class="p-4 text-sm text-gray-500 text-center">Chargement...</p>
    </div>
    <div class="p-3 border-t border-gray-200">
      <a href="/medsupply-cloud/notifications.html" class="block text-center text-sm text-primary hover:text-secondary font-medium">
        Voir toutes les notifications
      </a>
    </div>
  `;
  notificationBtn.parentElement.appendChild(dropdown);

  // Toggle dropdown on click
  notificationBtn.addEventListener("click", (e) => {
    e.stopPropagation();
    notificationDropdownOpen = !notificationDropdownOpen;
    dropdown.classList.toggle("hidden", !notificationDropdownOpen);

    if (notificationDropdownOpen) {
      loadDropdownNotifications();
    }
  });

  // Close dropdown when clicking outside
  document.addEventListener("click", () => {
    if (notificationDropdownOpen) {
      notificationDropdownOpen = false;
      dropdown.classList.add("hidden");
    }
  });

  // Start polling for unread count
  startNotificationPolling();
}

async function loadDropdownNotifications() {
  try {
    const response = await apiFetch("/notifications?limit=10");
    const notifications = response?.data?.notifications || [];

    const container = document.getElementById("notification-dropdown-list");

    if (notifications.length === 0) {
      container.innerHTML = `
        <div class="p-4 text-center">
          <i data-lucide="bell" class="w-8 h-8 mx-auto text-gray-400 mb-2"></i>
          <p class="text-sm text-gray-500">Aucune notification</p>
        </div>
      `;
    } else {
      container.innerHTML = notifications
        .map(
          (notif) => `
        <div class="p-3 hover:bg-gray-50 cursor-pointer border-b border-gray-100 last:border-b-0 ${notif.isRead ? "" : "bg-blue-50"}" onclick="handleNotificationClick('${notif.notificationId}', ${notif.isRead})">
          <div class="flex items-start gap-3">
            <div class="w-8 h-8 ${getNotificationIconBg(notif.type)} rounded-full flex items-center justify-center flex-shrink-0">
              <i data-lucide="${getNotificationIcon(notif.type)}" class="w-4 h-4 ${getNotificationIconColor(notif.type)}"></i>
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium text-gray-900 truncate">${notif.title}</p>
              <p class="text-xs text-gray-500 truncate">${notif.message}</p>
              <p class="text-xs text-gray-400 mt-1">${formatRelativeTime(notif.createdAt)}</p>
            </div>
            ${!notif.isRead ? '<span class="w-2 h-2 bg-blue-500 rounded-full flex-shrink-0 mt-1"></span>' : ""}
          </div>
        </div>
      `,
        )
        .join("");
    }

    lucide.createIcons();
  } catch (error) {
    console.error("Failed to load dropdown notifications:", error);
    const container = document.getElementById("notification-dropdown-list");
    container.innerHTML = `<p class="p-4 text-sm text-red-500 text-center">Erreur de chargement</p>`;
  }
}

async function handleNotificationClick(notificationId, isRead) {
  if (!isRead) {
    try {
      await apiFetch(`/notifications/${notificationId}/read`, {
        method: "PUT",
      });
      updateNotificationBadge();
    } catch (error) {
      console.error("Failed to mark notification as read:", error);
    }
  }
  window.location.href = "/medsupply-cloud/notifications.html";
}

async function markAllAsReadFromDropdown() {
  try {
    await apiFetch("/notifications/read-all", { method: "PUT" });
    loadDropdownNotifications();
    updateNotificationBadge();
  } catch (error) {
    console.error("Failed to mark all notifications as read:", error);
  }
}

function getNotificationIcon(type) {
  const icons = {
    NEW_QUOTE: "file-text",
    QUOTE_VALIDATED: "check-circle",
    QUOTE_REJECTED: "x-circle",
    ORDER_CONFIRMED: "package",
    ORDER_SHIPPED: "truck",
    ORDER_DELIVERED: "check-circle-2",
    PAYMENT_RECEIVED: "credit-card",
    PAYMENT_OVERDUE: "alert-triangle",
    MAINTENANCE_EXPIRING: "wrench",
    NEW_TENDER_OFFER: "gavel",
  };
  return icons[type] || "bell";
}

function getNotificationIconBg(type) {
  const bgs = {
    NEW_QUOTE: "bg-blue-100",
    QUOTE_VALIDATED: "bg-green-100",
    QUOTE_REJECTED: "bg-red-100",
    ORDER_CONFIRMED: "bg-blue-100",
    ORDER_SHIPPED: "bg-amber-100",
    ORDER_DELIVERED: "bg-green-100",
    PAYMENT_RECEIVED: "bg-green-100",
    PAYMENT_OVERDUE: "bg-red-100",
    MAINTENANCE_EXPIRING: "bg-amber-100",
    NEW_TENDER_OFFER: "bg-purple-100",
  };
  return bgs[type] || "bg-gray-100";
}

function getNotificationIconColor(type) {
  const colors = {
    NEW_QUOTE: "text-blue-600",
    QUOTE_VALIDATED: "text-green-600",
    QUOTE_REJECTED: "text-red-600",
    ORDER_CONFIRMED: "text-blue-600",
    ORDER_SHIPPED: "text-amber-600",
    ORDER_DELIVERED: "text-green-600",
    PAYMENT_RECEIVED: "text-green-600",
    PAYMENT_OVERDUE: "text-red-600",
    MAINTENANCE_EXPIRING: "text-amber-600",
    NEW_TENDER_OFFER: "text-purple-600",
  };
  return colors[type] || "text-gray-600";
}

function formatRelativeTime(dateString) {
  const date = new Date(dateString);
  const now = new Date();
  const diffMs = now - date;
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMs / 3600000);
  const diffDays = Math.floor(diffMs / 86400000);

  if (diffMins < 1) return "À l'instant";
  if (diffMins < 60) return `Il y a ${diffMins} min`;
  if (diffHours < 24) return `Il y a ${diffHours} h`;
  if (diffDays === 1) return "Hier";
  if (diffDays < 7) return `Il y a ${diffDays} jours`;

  return date.toLocaleDateString("fr-FR", { day: "numeric", month: "short" });
}

function startNotificationPolling() {
  // Update badge immediately
  updateNotificationBadge();

  // Poll every 30 seconds
  notificationPollingInterval = setInterval(() => {
    updateNotificationBadge();
  }, 30000);
}

async function updateNotificationBadge() {
  try {
    const response = await apiFetch("/notifications/unread-count");
    const count = response?.data?.count || 0;
    const badge = document.getElementById("notification-badge");
    if (badge) {
      badge.textContent = count > 0 ? count : "";
      badge.classList.toggle("hidden", count === 0);
    }
  } catch (error) {
    console.error("Failed to update notification badge:", error);
  }
}

function stopNotificationPolling() {
  if (notificationPollingInterval) {
    clearInterval(notificationPollingInterval);
    notificationPollingInterval = null;
  }
}

// Initialize when DOM is ready
if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initNotificationBell);
} else {
  initNotificationBell();
}
