// Central mock data file for MedSupply Cloud

const MOCK_USERS = [
  {
    userId: "ADMIN-001",
    email: "admin@medsupply.fr",
    password: "Admin123!",
    role: "ADMIN",
    firstName: "Dr. Mohamed",
    lastName: "Khalil",
    phone: "+33 (0)1 23 45 67 89",
    avatar: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&h=150&fit=crop",
    company: "Centre Médical Paris Nord",
    address: {
      street: "123 Rue de la Santé",
      city: "Paris",
      zipCode: "75001",
      country: "France"
    },
    registeredDate: "2025-01-15"
  },
  {
    userId: "CLIENT-001",
    email: "jean.dubois@hospital.fr",
    password: "Hospital123!",
    role: "CLIENT",
    firstName: "Jean",
    lastName: "Dubois",
    phone: "+33 (0)2 12 34 56 78",
    company: "Hôpital Universitaire de Lyon",
    address: {
      street: "14 Avenue des Lumières",
      city: "Lyon",
      zipCode: "69003",
      country: "France"
    },
    registeredDate: "2025-06-10"
  },
  {
    userId: "SUP-001",
    email: "supplier@medsupply.fr",
    password: "Supplier123!",
    role: "SUPPLIER",
    firstName: "Paul",
    lastName: "Legrand",
    phone: "+33 (0)4 56 78 90 12",
    company: "Fournitures Médicales Pro",
    address: {
      street: "78 Boulevard de l'Industrie",
      city: "Grenoble",
      zipCode: "38000",
      country: "France"
    },
    registeredDate: "2025-04-04"
  }
];

const MOCK_CATEGORIES = [
  { id: 1, name: "Équipements Médicaux", slug: "equipements-medicaux", icon: "🏥" },
  { id: 2, name: "Appareils de Diagnostic", slug: "appareils-diagnostic", icon: "🔬" },
  { id: 3, name: "Fournitures Médicales", slug: "fournitures-medicales", icon: "💊" },
  { id: 4, name: "Équipement Chirurgical", slug: "equipement-chirurgical", icon: "✂️" }
];

const MOCK_PRODUCTS = [
  {
    id: 1,
    name: "Moniteur Multiparamètres ECG",
    price: 4500,
    discount: 10,
    category: "Appareils de Diagnostic",
    description: "Moniteur cardiaque portable avec écran LCD haute résolution. ECG, SpO2, fréquence cardiaque et alarme intelligente.",
    image: "https://images.unsplash.com/photo-1579154204601-01d82b27ebf5?w=800&h=800&fit=crop",
    stock: 15,
    rating: 4.8,
    reviews: 234,
    badge: "Populaire"
  },
  {
    id: 2,
    name: "Tensiomètre Automatique Digital",
    price: 290,
    discount: 15,
    category: "Équipements Médicaux",
    description: "Tensiomètre bras automatique avec mémoire 90 mesures. Lecture rapide et régulateur de pression intelligent.",
    image: "https://images.unsplash.com/photo-1631217314830-4875012f5f03?w=800&h=800&fit=crop",
    stock: 45,
    rating: 4.6,
    reviews: 189,
    badge: "Solde"
  },
  {
    id: 3,
    name: "Oxymètre de Pouls Professionnel",
    price: 180,
    discount: 0,
    category: "Appareils de Diagnostic",
    description: "Oxymètre portable de mesure SpO2, fréquence cardiaque et indice de perfusion. Écran OLED multicolore.",
    image: "https://images.unsplash.com/photo-1576091160550-112173fba483?w=800&h=800&fit=crop",
    stock: 87,
    rating: 4.9,
    reviews: 456,
    badge: "Nouveau"
  },
  {
    id: 4,
    name: "Thermomètre Infrarouge Sans Contact",
    price: 95,
    discount: 20,
    category: "Équipements Médicaux",
    description: "Thermomètre frontal infrarouge. Mesure instantanée, mémoire 32 lectures et alerte de fièvre.",
    image: "https://images.unsplash.com/photo-1584308666744-24d5f400f6f5?w=800&h=800&fit=crop",
    stock: 120,
    rating: 4.7,
    reviews: 312,
    badge: "Solde"
  },
  {
    id: 5,
    name: "Kit Stéthoscope Cardiologie",
    price: 320,
    discount: 5,
    category: "Équipement Chirurgical",
    description: "Stéthoscope double membrane en acier inoxydable. Embouts auriculaires hypoallergéniques pour usage clinique.",
    image: "https://images.unsplash.com/photo-1576091160675-112d4ae2fa28?w=800&h=800&fit=crop",
    stock: 34,
    rating: 4.8,
    reviews: 278,
    badge: null
  },
  {
    id: 6,
    name: "Lampe Loupe Chirurgicale LED",
    price: 890,
    discount: 12,
    category: "Équipement Chirurgical",
    description: "Lampe loupe LED 5x avec bras flexible, idéal pour chirurgie dentaire et interventions de précision.",
    image: "https://images.unsplash.com/photo-1587316745621-3757c7076f61?w=800&h=800&fit=crop",
    stock: 8,
    rating: 4.9,
    reviews: 145,
    badge: "Stock Limité"
  },
  {
    id: 7,
    name: "Gants Médicaux Latex Premium",
    price: 25,
    discount: 0,
    category: "Fournitures Médicales",
    description: "Boîte de 100 gants sans poudre. Hypoallergéniques, normes CE, adaptées aux salles blanches.",
    image: "https://images.unsplash.com/photo-1584308666744-24d5f400f6f5?w=800&h=800&fit=crop",
    stock: 500,
    rating: 4.5,
    reviews: 523,
    badge: null
  },
  {
    id: 8,
    name: "Masques Chirurgicaux 3 Plis",
    price: 35,
    discount: 8,
    category: "Fournitures Médicales",
    description: "Boîte de 50 masques de protection. Norme FR14683, confort respirant et élastiques souples.",
    image: "https://images.unsplash.com/photo-1631217314830-4875012f5f03?w=800&h=800&fit=crop",
    stock: 350,
    rating: 4.6,
    reviews: 412,
    badge: "Populaire"
  },
  {
    id: 9,
    name: "Électrocardiographe Portable 12 Dérivations",
    price: 3200,
    discount: 15,
    category: "Appareils de Diagnostic",
    description: "ECG portable 12 dérivations avec écran tactile, connectivité Bluetooth et export PDF.",
    image: "https://images.unsplash.com/photo-1579154204601-01d82b27ebf5?w=800&h=800&fit=crop",
    stock: 12,
    rating: 4.9,
    reviews: 89,
    badge: "Nouveau"
  },
  {
    id: 10,
    name: "Oreiller Support Cervical Ergonomique",
    price: 125,
    discount: 20,
    category: "Équipements Médicaux",
    description: "Oreiller mémoire haute densité. Support cervical optimal et housse respirante amovible.",
    image: "https://images.unsplash.com/photo-1576091160550-112173fba483?w=800&h=800&fit=crop",
    stock: 67,
    rating: 4.7,
    reviews: 234,
    badge: "Solde"
  },
  {
    id: 11,
    name: "Seringues Stériles 10ml",
    price: 45,
    discount: 10,
    category: "Fournitures Médicales",
    description: "Boîte de 100 seringues stériles 10ml avec aiguilles 25G. Emballage individuel sécurisé.",
    image: "https://images.unsplash.com/photo-1584308666744-24d5f400f6f5?w=800&h=800&fit=crop",
    stock: 200,
    rating: 4.8,
    reviews: 167,
    badge: null
  },
  {
    id: 12,
    name: "Bandages Élastiques Compression",
    price: 28,
    discount: 0,
    category: "Fournitures Médicales",
    description: "Paquet de 10 bandages élastiques. Compression progressive, réutilisables et confortables.",
    image: "https://images.unsplash.com/photo-1631217314830-4875012f5f03?w=800&h=800&fit=crop",
    stock: 150,
    rating: 4.6,
    reviews: 298,
    badge: "Populaire"
  }
];

const MOCK_CUSTOMERS = [
  {
    id: "CUST-001",
    firstName: "Jean",
    lastName: "Dubois",
    email: "jean.dubois@hospital.fr",
    phone: "+33 (0)2 12 34 56 78",
    company: "Hôpital Universitaire de Lyon",
    location: "Lyon",
    joinedDate: "2025-06-10",
    totalOrders: 12,
    totalSpent: 45230,
    status: "Active",
    lastOrder: "ORD-2026-001"
  },
  {
    id: "CUST-002",
    firstName: "Marie",
    lastName: "Leclerc",
    email: "marie.leclerc@clinic.fr",
    phone: "+33 (0)3 45 67 89 01",
    company: "Clinique du Soleil",
    location: "Marseille",
    joinedDate: "2025-05-15",
    totalOrders: 8,
    totalSpent: 32100,
    status: "Active",
    lastOrder: "ORD-2026-002"
  },
  {
    id: "CUST-003",
    firstName: "Pierre",
    lastName: "Bernard",
    email: "pierre.bernard@medical.fr",
    phone: "+33 (0)4 56 78 90 12",
    company: "Cabinet Médical Bernard",
    location: "Toulouse",
    joinedDate: "2025-04-20",
    totalOrders: 15,
    totalSpent: 67890,
    status: "VIP",
    lastOrder: "ORD-2026-003"
  },
  {
    id: "CUST-004",
    firstName: "Sophie",
    lastName: "Durand",
    email: "sophie.durand@health.fr",
    phone: "+33 (0)5 67 89 01 23",
    company: "Pharmacie Durand",
    location: "Nice",
    joinedDate: "2025-03-10",
    totalOrders: 5,
    totalSpent: 18500,
    status: "Active",
    lastOrder: "ORD-2026-004"
  },
  {
    id: "CUST-005",
    firstName: "Luc",
    lastName: "Moreau",
    email: "luc.moreau@dental.fr",
    phone: "+33 (0)6 78 90 12 34",
    company: "Clinic Dentaire Moreau",
    location: "Bordeaux",
    joinedDate: "2025-02-05",
    totalOrders: 9,
    totalSpent: 42150,
    status: "Active",
    lastOrder: "ORD-2026-005"
  },
  {
    id: "CUST-006",
    firstName: "Isabelle",
    lastName: "Gérard",
    email: "isabelle.gerard@hospital.fr",
    phone: "+33 (0)7 89 01 23 45",
    company: "Hôpital Saint-Louis",
    location: "Paris",
    joinedDate: "2025-01-20",
    totalOrders: 18,
    totalSpent: 89765,
    status: "VIP",
    lastOrder: "ORD-2026-006"
  }
];

const MOCK_ORDERS = [
  {
    id: "ORD-2026-001",
    customerId: "CUST-001",
    customerName: "Jean Dubois",
    date: "2026-06-08",
    status: "Livré",
    items: [
      { productId: 2, name: "Tensiomètre Automatique Digital", price: 290, quantity: 1 },
      { productId: 7, name: "Gants Médicaux Latex Premium", price: 25, quantity: 5 }
    ],
    total: 415,
    estimatedDelivery: "2026-06-09",
    trackingStatus: "Remis",
    deliveryLocation: "Lyon"
  },
  {
    id: "ORD-2026-002",
    customerId: "CUST-002",
    customerName: "Marie Leclerc",
    date: "2026-06-07",
    status: "En livraison",
    items: [
      { productId: 1, name: "Moniteur Multiparamètres ECG", price: 4500, quantity: 1 }
    ],
    total: 4500,
    estimatedDelivery: "2026-06-11",
    trackingStatus: "En transit",
    deliveryLocation: "Marseille"
  },
  {
    id: "ORD-2026-003",
    customerId: "CUST-003",
    customerName: "Pierre Bernard",
    date: "2026-06-05",
    status: "Confirmé",
    items: [
      { productId: 3, name: "Oxymètre de Pouls Professionnel", price: 180, quantity: 2 },
      { productId: 8, name: "Masques Chirurgicaux 3 Plis", price: 35, quantity: 10 }
    ],
    total: 710,
    estimatedDelivery: "2026-06-12",
    trackingStatus: "Préparation",
    deliveryLocation: "Toulouse"
  },
  {
    id: "ORD-2026-004",
    customerId: "CUST-004",
    customerName: "Sophie Durand",
    date: "2026-06-03",
    status: "Livré",
    items: [
      { productId: 6, name: "Lampe Loupe Chirurgicale LED", price: 890, quantity: 1 }
    ],
    total: 890,
    estimatedDelivery: "2026-06-08",
    trackingStatus: "Remis",
    deliveryLocation: "Nice"
  },
  {
    id: "ORD-2026-005",
    customerId: "CUST-005",
    customerName: "Luc Moreau",
    date: "2026-06-01",
    status: "Livré",
    items: [
      { productId: 8, name: "Masques Chirurgicaux 3 Plis", price: 35, quantity: 10 },
      { productId: 11, name: "Seringues Stériles 10ml", price: 45, quantity: 4 }
    ],
    total: 890,
    estimatedDelivery: "2026-06-06",
    trackingStatus: "Remis",
    deliveryLocation: "Bordeaux"
  },
  {
    id: "ORD-2026-006",
    customerId: "CUST-006",
    customerName: "Isabelle Gérard",
    date: "2026-05-30",
    status: "En livraison",
    items: [
      { productId: 12, name: "Bandages Élastiques Compression", price: 28, quantity: 6 },
      { productId: 5, name: "Kit Stéthoscope Cardiologie", price: 320, quantity: 1 }
    ],
    total: 152,
    estimatedDelivery: "2026-06-10",
    trackingStatus: "En transit",
    deliveryLocation: "Paris"
  },
  {
    id: "ORD-2026-007",
    customerId: "CUST-001",
    customerName: "Jean Dubois",
    date: "2026-05-28",
    status: "Annulé",
    items: [
      { productId: 9, name: "Électrocardiographe Portable 12 Dérivations", price: 3200, quantity: 1 }
    ],
    total: 3200,
    estimatedDelivery: "2026-06-05",
    trackingStatus: "Annulé",
    deliveryLocation: "Lyon"
  }
];

const MOCK_ADMIN_ORDERS = [...MOCK_ORDERS];

const MOCK_PAYMENTS = [
  {
    paymentId: "PAY-1001",
    orderId: "ORD-2026-001",
    method: "Carte Bancaire",
    status: "Terminé",
    amount: 415,
    date: "2026-06-08"
  },
  {
    paymentId: "PAY-1002",
    orderId: "ORD-2026-002",
    method: "Virement",
    status: "En attente",
    amount: 4500,
    date: "2026-06-07"
  },
  {
    paymentId: "PAY-1003",
    orderId: "ORD-2026-003",
    method: "Carte Bancaire",
    status: "Terminé",
    amount: 710,
    date: "2026-06-05"
  },
  {
    paymentId: "PAY-1004",
    orderId: "ORD-2026-004",
    method: "Facture",
    status: "Terminé",
    amount: 890,
    date: "2026-06-03"
  },
  {
    paymentId: "PAY-1005",
    orderId: "ORD-2026-005",
    method: "Carte Bancaire",
    status: "Terminé",
    amount: 890,
    date: "2026-06-01"
  },
  {
    paymentId: "PAY-1006",
    orderId: "ORD-2026-006",
    method: "Virement",
    status: "En cours",
    amount: 152,
    date: "2026-05-30"
  }
];

const MOCK_DELIVERIES = [
  {
    deliveryId: "DLV-9001",
    orderId: "ORD-2026-002",
    customerName: "Marie Leclerc",
    status: "En transit",
    route: [
      { label: "Entrepôt Paris", time: "2026-06-07 09:30" },
      { label: "Aéroport de Marseille", time: "2026-06-08 14:12" },
      { label: "Centre de distribution Sud", time: "2026-06-09 08:20" }
    ],
    eta: "2026-06-11"
  },
  {
    deliveryId: "DLV-9002",
    orderId: "ORD-2026-003",
    customerName: "Pierre Bernard",
    status: "Préparation",
    route: [
      { label: "Entrepôt Paris", time: "2026-06-05 10:12" },
      { label: "Préparation commande", time: "2026-06-05 16:45" }
    ],
    eta: "2026-06-12"
  },
  {
    deliveryId: "DLV-9003",
    orderId: "ORD-2026-004",
    customerName: "Sophie Durand",
    status: "Remis",
    route: [
      { label: "Entrepôt Paris", time: "2026-06-03 08:20" },
      { label: "Livraison Nice", time: "2026-06-07 11:50" },
      { label: "Livraison confirmée", time: "2026-06-08 14:05" }
    ],
    eta: "2026-06-08"
  },
  {
    deliveryId: "DLV-9004",
    orderId: "ORD-2026-006",
    customerName: "Isabelle Gérard",
    status: "En transit",
    route: [
      { label: "Entrepôt Paris", time: "2026-05-30 07:50" },
      { label: "Départ camion 23", time: "2026-05-30 12:25" },
      { label: "Point relais Sud Est", time: "2026-06-01 09:30" }
    ],
    eta: "2026-06-10"
  }
];

const MOCK_SALES_STATS = {
  totalRevenue: 289456,
  totalOrders: 156,
  totalCustomers: 42,
  averageOrderValue: 1855,
  monthlyGrowth: 23.5,
  conversionRate: 3.2,
  returnsRate: 1.8,
  pendingDeliveries: 14
};

const MOCK_MONTHLY_SALES = [
  { month: "Jan", revenue: 12450, orders: 18, customers: 8 },
  { month: "Fév", revenue: 15230, orders: 22, customers: 11 },
  { month: "Mar", revenue: 18900, orders: 28, customers: 14 },
  { month: "Avr", revenue: 22340, orders: 32, customers: 16 },
  { month: "Mai", revenue: 28765, orders: 42, customers: 22 },
  { month: "Juin", revenue: 35456, orders: 48, customers: 24 }
];

const MOCK_PRODUCT_PERFORMANCE = [
  { productId: 1, name: "Moniteur ECG", sold: 45, revenue: 202500, trend: 12 },
  { productId: 2, name: "Tensiomètre", sold: 78, revenue: 21970, trend: 18 },
  { productId: 3, name: "Oxymètre", sold: 92, revenue: 16560, trend: 25 },
  { productId: 4, name: "Thermomètre", sold: 134, revenue: 10160, trend: 8 },
  { productId: 6, name: "Lampe Loupe", sold: 23, revenue: 18165, trend: 15 },
  { productId: 8, name: "Masques", sold: 267, revenue: 8820, trend: 35 }
];

const MOCK_ACTIVITY = [
  { id: 1, type: "order", message: "Nouvelle commande ORD-2026-006 reçue", time: "2 min", icon: "shopping-bag" },
  { id: 2, type: "customer", message: "Nouveau client Luc Moreau inscrit", time: "15 min", icon: "user-plus" },
  { id: 3, type: "payment", message: "Paiement reçu pour ORD-2026-005", time: "1h", icon: "check-circle" },
  { id: 4, type: "inventory", message: "Stock faible pour Lampe Loupe", time: "3h", icon: "alert-circle" },
  { id: 5, type: "delivery", message: "Livraison effectuée pour ORD-2026-004", time: "5h", icon: "truck" },
  { id: 6, type: "review", message: "Nouvel avis client: 5★ pour Oxymètre", time: "8h", icon: "star" }
];

const MOCK_TOP_PRODUCTS = [
  { id: 4, name: "Thermomètre Infrarouge", stock: 120, status: "In Stock", revenue: 10160 },
  { id: 3, name: "Oxymètre de Pouls", stock: 87, status: "In Stock", revenue: 16560 },
  { id: 2, name: "Tensiomètre Automatique", stock: 45, status: "In Stock", revenue: 21970 },
  { id: 8, name: "Masques Chirurgicaux", stock: 350, status: "In Stock", revenue: 8820 },
  { id: 7, name: "Gants Médicaux", stock: 500, status: "In Stock", revenue: 7500 }
];

const MOCK_LOW_STOCK = [
  { id: 1, name: "Moniteur ECG", stock: 15, reorderLevel: 20, status: "Warning" },
  { id: 6, name: "Lampe Loupe", stock: 8, reorderLevel: 10, status: "Critical" },
  { id: 5, name: "Kit Stéthoscope", stock: 34, reorderLevel: 25, status: "OK" }
];

const MOCK_REGIONS = [
  { region: "Île-de-France", orders: 45, revenue: 89234, customers: 12 },
  { region: "Auvergne-Rhône-Alpes", orders: 28, revenue: 52340, customers: 8 },
  { region: "Nouvelle-Aquitaine", orders: 22, revenue: 41230, customers: 6 },
  { region: "Occitanie", orders: 32, revenue: 58900, customers: 9 },
  { region: "Provence-Alpes-Côte d'Azur", orders: 19, revenue: 35290, customers: 5 },
  { region: "Pays de la Loire", orders: 10, revenue: 12462, customers: 2 }
];

const MOCK_QUOTE_STATS = {
  pending: 6,
  approved: 14,
  rejected: 3
};

const MOCK_NOTIFICATIONS = [
  {
    notificationId: "NOTIF-001",
    title: "Commande #ORD-2026-005 confirmée",
    message: "La commande a été traitée et est prête au départ.",
    type: "ORDER_CONFIRMED",
    createdAt: "2026-06-09T10:30:00Z",
    isRead: false
  },
  {
    notificationId: "NOTIF-002",
    title: "Stock faible : Lampe Loupe Chirurgicale",
    message: "Le niveau du stock est inférieur au seuil d'alerte.",
    type: "ORDER_SHIPPED",
    createdAt: "2026-06-09T08:15:00Z",
    isRead: false
  },
  {
    notificationId: "NOTIF-003",
    title: "Nouvelle candidature fournisseur reçue",
    message: "Un nouveau fournisseur a soumis sa demande d'intégration.",
    type: "NEW_TENDER_OFFER",
    createdAt: "2026-06-08T16:00:00Z",
    isRead: true
  },
  {
    notificationId: "NOTIF-004",
    title: "Paiement reçu pour ORD-2026-004",
    message: "La transaction a été validée avec succès.",
    type: "PAYMENT_RECEIVED",
    createdAt: "2026-06-08T12:40:00Z",
    isRead: true
  }
];

// Geolocation placeholder for the admin map
const MOCK_DELIVERY_LOCATIONS = [
  { city: "Paris", coordinates: [48.8566, 2.3522], label: "Hôpital Saint-Louis" },
  { city: "Marseille", coordinates: [43.2965, 5.3698], label: "Clinique du Soleil" },
  { city: "Lyon", coordinates: [45.7640, 4.8357], label: "Centre Hospitalier Lyon" },
  { city: "Toulouse", coordinates: [43.6047, 1.4442], label: "Cabinet Bernard" }
];

function initializeMockSession() {
  if (!sessionStorage.getItem("medsupply_user") || !sessionStorage.getItem("medsupply_role")) {
    const defaultUser = MOCK_USERS.find(user => user.role === "ADMIN");
    sessionStorage.setItem("medsupply_user", JSON.stringify(defaultUser));
    sessionStorage.setItem("medsupply_role", defaultUser.role);
  }
}

function initializeCart() {
  if (!localStorage.getItem("cart")) {
    localStorage.setItem("cart", JSON.stringify([]));
  }
}

function getCart() {
  const cart = localStorage.getItem("cart");
  return cart ? JSON.parse(cart) : [];
}

function addToCart(productId, quantity = 1) {
  const cart = getCart();
  const product = getProductById(productId);
  if (!product) return false;

  const existingItem = cart.find(item => item.id === productId);
  if (existingItem) {
    existingItem.quantity = Math.min(existingItem.quantity + quantity, product.stock);
  } else {
    cart.push({
      id: productId,
      name: product.name,
      price: product.price,
      discount: product.discount,
      image: product.image,
      quantity: Math.min(quantity, product.stock)
    });
  }

  localStorage.setItem("cart", JSON.stringify(cart));
  return true;
}

function removeFromCart(productId) {
  let cart = getCart();
  cart = cart.filter(item => item.id !== productId);
  localStorage.setItem("cart", JSON.stringify(cart));
}

function updateCartQuantity(productId, quantity) {
  const cart = getCart();
  const item = cart.find(item => item.id === productId);
  const product = getProductById(productId);
  if (!item || !product) return;

  item.quantity = Math.max(1, Math.min(quantity, product.stock));
  localStorage.setItem("cart", JSON.stringify(cart));
}

function clearCart() {
  localStorage.setItem("cart", JSON.stringify([]));
}

function getCartTotal() {
  const cart = getCart();
  return cart.reduce((total, item) => {
    const price = item.price * (1 - (item.discount || 0) / 100);
    return total + price * item.quantity;
  }, 0);
}

function getProductById(id) {
  return MOCK_PRODUCTS.find(product => product.id === id);
}

function getCurrentUser() {
  const user = sessionStorage.getItem("medsupply_user");
  return user ? JSON.parse(user) : null;
}

function getUserByEmail(email) {
  return MOCK_USERS.find(user => user.email.toLowerCase() === email.toLowerCase());
}

function getCustomerById(id) {
  return MOCK_CUSTOMERS.find(customer => customer.id === id);
}

function getOrderById(id) {
  return MOCK_ORDERS.find(order => order.id === id);
}

function markNotificationAsRead(notificationId) {
  const notification = MOCK_NOTIFICATIONS.find(item => item.notificationId === notificationId);
  if (notification) notification.isRead = true;
}

function markAllNotificationsAsRead() {
  MOCK_NOTIFICATIONS.forEach(item => item.isRead = true);
}

function getUnreadNotificationCount() {
  return MOCK_NOTIFICATIONS.filter(item => !item.isRead).length;
}

function initializeMockData() {
  initializeCart();
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initializeMockData);
} else {
  initializeMockData();
}
