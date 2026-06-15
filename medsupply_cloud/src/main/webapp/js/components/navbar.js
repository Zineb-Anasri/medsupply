// Global Navbar and Cart Management

function renderNavbar() {
  const user = getCurrentUser();
  const cart = getCart();
  const cartCount = cart.reduce((sum, item) => sum + item.quantity, 0);

  const navbar =
    document.getElementById("navbar-container") || createNavbarContainer();

  navbar.innerHTML = `
    <nav class="bg-white shadow-lg sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
          <!-- Logo -->
          <div class="flex items-center gap-2">
            <a href="/medsupply-cloud/" class="flex items-center gap-2 text-2xl font-bold text-primary hover:text-secondary transition">
              <svg class="w-8 h-8" fill="currentColor" viewBox="0 0 100 100">
                <path d="M30 35 L50 25 L70 35 L70 65 L50 75 L30 65 Z" stroke="currentColor" stroke-width="2" fill="none"/>
                <path d="M30 35 L50 45 L70 35" stroke="currentColor" stroke-width="2" fill="none"/>
                <path d="M50 45 L50 75" stroke="currentColor" stroke-width="2" fill="none"/>
                <circle cx="50" cy="50" r="6" fill="currentColor"/>
              </svg>
              <span>MedSupply</span>
            </a>
          </div>

          <!-- Search Bar -->
          <div class="hidden md:flex flex-1 max-w-md mx-8">
            <div class="relative w-full">
              <input type="text" id="search-input" placeholder="Rechercher des produits..." 
                class="w-full px-4 py-2 border border-border rounded-lg focus:ring-2 focus:ring-accent outline-none">
              <button onclick="performSearch()" class="absolute right-3 top-1/2 -translate-y-1/2 text-textSecondary hover:text-textPrimary">
                <i data-lucide="search" class="w-5 h-5"></i>
              </button>
            </div>
          </div>

          <!-- Right Icons -->
          <div class="flex items-center gap-6">
            <!-- User Dropdown -->
            <div class="relative group">
              <button class="flex items-center gap-2 text-textPrimary hover:text-accent transition">
                <img src="${user.avatar}" alt="${user.firstName}" class="w-8 h-8 rounded-full">
                <span class="hidden sm:inline text-sm font-medium">${user.firstName}</span>
              </button>
              <div class="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200">
                <a href="/medsupply-cloud/pages/profile.html" class="block px-4 py-2 text-sm text-textPrimary hover:bg-background">Mon Profil</a>
                <a href="/medsupply-cloud/pages/profile.html#orders" class="block px-4 py-2 text-sm text-textPrimary hover:bg-background">Mes Commandes</a>
                <a href="#" class="block px-4 py-2 text-sm text-textPrimary hover:bg-background">Paramètres</a>
                <hr>
                <a href="#" onclick="logout()" class="block px-4 py-2 text-sm text-danger hover:bg-red-50">Déconnexion</a>
              </div>
            </div>

            <!-- Cart Icon -->
            <a href="/medsupply-cloud/pages/cart.html" class="relative text-textPrimary hover:text-accent transition">
              <i data-lucide="shopping-cart" class="w-6 h-6"></i>
              ${cartCount > 0 ? `<span class="absolute -top-2 -right-2 bg-danger text-white text-xs rounded-full w-5 h-5 flex items-center justify-center font-bold">${cartCount}</span>` : ""}
            </a>
          </div>
        </div>

        <!-- Categories Bar -->
        <div class="border-t border-border py-3 -mx-4 sm:-mx-6 lg:-mx-8 px-4 sm:px-6 lg:px-8 overflow-x-auto">
          <div class="flex gap-8">
            <a href="/medsupply-cloud/" class="text-sm font-medium text-textPrimary hover:text-accent transition whitespace-nowrap">Accueil</a>
            <a href="/medsupply-cloud/pages/products.html" class="text-sm font-medium text-textPrimary hover:text-accent transition whitespace-nowrap">Tous les Produits</a>
            ${MOCK_CATEGORIES.map(
              (cat) => `
              <a href="/medsupply-cloud/pages/products.html?category=${cat.slug}" class="text-sm font-medium text-textPrimary hover:text-accent transition whitespace-nowrap">
                ${cat.icon} ${cat.name}
              </a>
            `,
            ).join("")}
          </div>
        </div>
      </div>
    </nav>
  `;

  if (window.lucide) lucide.createIcons();
}

function createNavbarContainer() {
  const container = document.createElement("div");
  container.id = "navbar-container";
  document.body.insertBefore(container, document.body.firstChild);
  return container;
}

function performSearch() {
  const searchTerm = document.getElementById("search-input")?.value || "";
  window.location.href = `/medsupply-cloud/pages/products.html?search=${encodeURIComponent(searchTerm)}`;
}

function logout() {
  sessionStorage.clear();
  localStorage.removeItem("cart");
  window.location.href = "/medsupply-cloud/pages/auth/login.html";
}

// Render Footer
function renderFooter() {
  const footer =
    document.getElementById("footer-container") || createFooterContainer();

  footer.innerHTML = `
    <footer class="bg-primary text-white mt-20">
      <!-- Newsletter Section -->
      <div class="bg-secondary py-12">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div class="grid md:grid-cols-2 gap-8 items-center">
            <div>
              <h3 class="text-2xl font-bold mb-2">Restez Informé</h3>
              <p class="text-white text-opacity-80">Recevez les dernières offres et nouveaux produits</p>
            </div>
            <form onsubmit="handleNewsletterSubscribe(event)" class="flex">
              <input type="email" placeholder="votre@email.com" required
                class="flex-1 px-4 py-3 text-textPrimary rounded-l-lg outline-none">
              <button type="submit" class="bg-accent hover:bg-accent text-white px-6 py-3 font-medium rounded-r-lg transition">
                S'inscrire
              </button>
            </form>
          </div>
        </div>
      </div>

      <!-- Main Footer -->
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div class="grid md:grid-cols-4 gap-8 mb-8">
          <div>
            <h4 class="font-bold mb-4 text-lg">MedSupply Cloud</h4>
            <p class="text-white text-opacity-70 text-sm">Votre plateforme de référence pour les fournitures médicales de qualité.</p>
          </div>
          <div>
            <h4 class="font-bold mb-4">Produits</h4>
            <ul class="space-y-2 text-sm text-white text-opacity-70">
              <li><a href="/medsupply-cloud/pages/products.html" class="hover:text-white transition">Tous les Produits</a></li>
              <li><a href="/medsupply-cloud/pages/products.html?category=equipements-medicaux" class="hover:text-white transition">Équipements</a></li>
              <li><a href="/medsupply-cloud/pages/products.html?category=appareils-diagnostic" class="hover:text-white transition">Diagnostic</a></li>
              <li><a href="/medsupply-cloud/pages/products.html?category=fournitures-medicales" class="hover:text-white transition">Fournitures</a></li>
            </ul>
          </div>
          <div>
            <h4 class="font-bold mb-4">Support</h4>
            <ul class="space-y-2 text-sm text-white text-opacity-70">
              <li><a href="#" class="hover:text-white transition">Nous Contacter</a></li>
              <li><a href="#" class="hover:text-white transition">FAQ</a></li>
              <li><a href="#" class="hover:text-white transition">Conditions de Vente</a></li>
              <li><a href="#" class="hover:text-white transition">Politique de Confidentialité</a></li>
            </ul>
          </div>
          <div>
            <h4 class="font-bold mb-4">Contact</h4>
            <p class="text-white text-opacity-70 text-sm mb-2">📞 +33 (0)1 23 45 67 89</p>
            <p class="text-white text-opacity-70 text-sm mb-4">📧 contact@medsupply.fr</p>
            <div class="flex gap-4">
              <a href="#" class="hover:text-accent transition"><i data-lucide="facebook" class="w-5 h-5"></i></a>
              <a href="#" class="hover:text-accent transition"><i data-lucide="twitter" class="w-5 h-5"></i></a>
              <a href="#" class="hover:text-accent transition"><i data-lucide="linkedin" class="w-5 h-5"></i></a>
            </div>
          </div>
        </div>

        <hr class="border-white border-opacity-20 mb-8">

        <div class="flex flex-col md:flex-row justify-between items-center text-sm text-white text-opacity-70">
          <p>© 2026 MedSupply Cloud. Tous droits réservés.</p>
          <div class="flex gap-6 mt-4 md:mt-0">
            <a href="#" class="hover:text-white transition">Conditions légales</a>
            <a href="#" class="hover:text-white transition">Cookies</a>
          </div>
        </div>
      </div>
    </footer>
  `;

  if (window.lucide) lucide.createIcons();
}

function createFooterContainer() {
  const container = document.createElement("div");
  container.id = "footer-container";
  document.body.appendChild(container);
  return container;
}

function handleNewsletterSubscribe(event) {
  event.preventDefault();
  const email = event.target.querySelector('input[type="email"]').value;
  showToast(
    "Merci de votre inscription ! Vous recevrez bientôt nos offres.",
    "success",
  );
  event.target.reset();
}

// Product Card Component
function createProductCard(product) {
  const finalPrice = product.price * (1 - (product.discount || 0) / 100);

  return `
    <div class="bg-white rounded-lg shadow-md hover:shadow-xl transition-all duration-300 overflow-hidden group">
      <!-- Image Container -->
      <div class="relative overflow-hidden bg-background h-64">
        <img src="${product.image}" alt="${product.name}" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300">
        
        <!-- Badges -->
        <div class="absolute top-4 left-4 flex flex-col gap-2">
          ${
            product.badge
              ? `
            <span class="bg-accent text-white text-xs font-bold px-3 py-1 rounded-full">
              ${product.badge}
            </span>
          `
              : ""
          }
          ${
            product.discount
              ? `
            <span class="bg-danger text-white text-xs font-bold px-3 py-1 rounded-full">
              -${product.discount}%
            </span>
          `
              : ""
          }
        </div>

        <!-- Stock Badge -->
        ${
          product.stock < 10
            ? `
          <span class="absolute top-4 right-4 bg-warning text-white text-xs font-bold px-3 py-1 rounded-full">
            Stock Limité
          </span>
        `
            : ""
        }

        <!-- Quick Add Button -->
        <button onclick="quickAddToCart(${product.id})" class="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-50 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-all">
          <span class="bg-accent text-white px-6 py-2 rounded-lg font-medium hover:bg-secondary transition">
            Ajouter au Panier
          </span>
        </button>
      </div>

      <!-- Content -->
      <div class="p-4">
        <a href="/medsupply-cloud/pages/product-detail.html?id=${product.id}" class="text-sm text-textSecondary hover:text-accent transition">
          ${product.category}
        </a>
        <a href="/medsupply-cloud/pages/product-detail.html?id=${product.id}" class="block text-lg font-bold text-textPrimary mb-2 hover:text-accent transition line-clamp-2">
          ${product.name}
        </a>

        <!-- Rating -->
        <div class="flex items-center gap-2 mb-3">
          <div class="flex text-warning">
            ${[...Array(5)]
              .map(
                (_, i) => `
              <i data-lucide="${i < Math.floor(product.rating) ? "star" : "star"}" class="w-4 h-4 ${i < Math.floor(product.rating) ? "fill-warning" : "text-gray-300"}"></i>
            `,
              )
              .join("")}
          </div>
          <span class="text-xs text-textSecondary">(${product.reviews})</span>
        </div>

        <!-- Price -->
        <div class="flex items-center gap-2 mb-4">
          <span class="text-2xl font-bold text-primary">${finalPrice.toFixed(2)}€</span>
          ${
            product.discount
              ? `
            <span class="text-sm text-textSecondary line-through">${product.price.toFixed(2)}€</span>
          `
              : ""
          }
        </div>

        <!-- CTA -->
        <a href="/medsupply-cloud/pages/product-detail.html?id=${product.id}" class="block w-full bg-accent text-white text-center py-2 rounded-lg font-medium hover:bg-secondary transition">
          Voir Détails
        </a>
      </div>
    </div>
  `;
}

function quickAddToCart(productId) {
  addToCart(productId, 1);
  showToast("Produit ajouté au panier!", "success");

  // Update cart icon
  const cart = getCart();
  const cartCount = cart.reduce((sum, item) => sum + item.quantity, 0);
  const cartBadge = document
    .querySelector('[data-lucide="shopping-cart"]')
    ?.parentElement?.querySelector("span");
  if (cartBadge) {
    cartBadge.textContent = cartCount;
  }
}

// Initialize global components
document.addEventListener("DOMContentLoaded", () => {
  renderNavbar();
  renderFooter();
});
