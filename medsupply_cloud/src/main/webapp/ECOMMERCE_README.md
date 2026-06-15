# MedSupply Cloud - E-Commerce Platform

A professional, production-ready e-commerce platform for medical supplies built with HTML, Tailwind CSS, and Vanilla JavaScript.

## 🚀 Features

### Product Management
- ✅ 12+ realistic medical products
- ✅ 4 product categories (Équipements, Diagnostic, Fournitures, Chirurgical)
- ✅ Product filtering by category, price, and rating
- ✅ Advanced search functionality
- ✅ Product recommendations
- ✅ Discount badges and pricing
- ✅ Stock management indicators

### Shopping Cart & Checkout
- ✅ Add/Remove products from cart
- ✅ Persistent cart (localStorage)
- ✅ Quantity management
- ✅ Real-time price calculations with discounts
- ✅ Multi-step checkout process
- ✅ Shipping options (Standard, Express, Pickup)
- ✅ Payment method selection
- ✅ Order confirmation with order number
- ✅ Professional checkout UI

### User Features
- ✅ Mock user authentication (simulated session)
- ✅ User profile page
- ✅ Order history with detailed order tracking
- ✅ Address management
- ✅ User settings and preferences
- ✅ Responsive role-based interface

### UI/UX
- ✅ Professional navbar with cart icon
- ✅ Product grid with hover effects
- ✅ Product detail pages with gallery
- ✅ Rating and review system
- ✅ Professional footer with links and newsletter
- ✅ Responsive design (mobile, tablet, desktop)
- ✅ Smooth animations and transitions
- ✅ Toast notifications
- ✅ Trust badges (security, delivery, returns)

### Mock Data
- ✅ 12 products with realistic details
- ✅ Product images from Unsplash
- ✅ 3 sample orders
- ✅ Pre-configured user with profile
- ✅ Category structure
- ✅ Rating and review data

## 📁 Project Structure

```
medsupply-cloud-frontend/
├── index.html                 # Home page
├── pages/
│   ├── products.html         # Product listing with filters
│   ├── product-detail.html   # Product detail page
│   ├── cart.html             # Shopping cart
│   ├── checkout.html         # Checkout & payment
│   ├── profile.html          # User profile & orders
│   └── auth/
│       └── login.html        # Login page (demo)
├── js/
│   ├── mock-data.js          # All mock data & cart functions
│   ├── api.js               # API client
│   ├── auth.js              # Authentication helpers
│   ├── router.js            # Route protection
│   └── components/
│       ├── navbar.js        # Global navbar & footer
│       ├── toast.js         # Notifications
│       ├── header.js        # Header component
│       └── sidebar.js       # Sidebar component
├── assets/
│   ├── css/
│   │   └── custom.css
│   └── img/
└── README.md                 # This file
```

## 🎯 Quick Start

### Option 1: Python HTTP Server
```bash
cd medsupply-cloud-frontend
python -m http.server 3000
```
Then open: `http://localhost:3000`

### Option 2: Node.js http-server
```bash
cd medsupply-cloud-frontend
npx http-server -p 3000
```
Then open: `http://localhost:3000`

### Option 3: VS Code Live Server
1. Install "Live Server" extension
2. Right-click on `index.html`
3. Select "Open with Live Server"

## 🧪 Testing Checklist

### Authentication & Navigation
- [ ] Home page loads without any API calls
- [ ] Navbar displays correctly with cart icon
- [ ] Footer displays all sections
- [ ] Navigation between pages works
- [ ] User profile shows pre-loaded mock user
- [ ] Mock session is maintained across pages

### Product Features
- [ ] All 12 products display on home page
- [ ] Product listing page shows all products
- [ ] Category filtering works
- [ ] Price range filtering works
- [ ] Rating filtering works
- [ ] Search functionality works
- [ ] Sorting by price/rating works
- [ ] Product detail page loads correctly
- [ ] Related products section shows

### Shopping Experience
- [ ] Add to cart button works
- [ ] Quick add to cart works
- [ ] Cart icon updates with count
- [ ] Cart page displays all items
- [ ] Quantity can be adjusted
- [ ] Items can be removed from cart
- [ ] Cart persists after page reload
- [ ] Price calculations are correct with discounts
- [ ] Checkout button redirects to checkout page

### Checkout Process
- [ ] Checkout form validates empty fields
- [ ] Shipping options display correctly
- [ ] Payment methods display correctly
- [ ] Order summary shows correct items
- [ ] Order summary shows correct total
- [ ] Success modal appears after payment
- [ ] Order number is generated
- [ ] Cart clears after successful order

### User Profile
- [ ] Profile page displays user information
- [ ] Order history shows all mock orders
- [ ] Order status displays correctly
- [ ] Order dates format correctly
- [ ] Address section shows correctly
- [ ] All tabs work (Profile, Orders, Addresses, Settings)

### Responsive Design
- [ ] Layout works on desktop (1024px+)
- [ ] Layout works on tablet (768px-1024px)
- [ ] Layout works on mobile (<768px)
- [ ] Mobile menu functions properly
- [ ] Images are responsive
- [ ] Text is readable on all sizes

### Visual & Interactions
- [ ] All images load correctly
- [ ] Colors match design system
- [ ] Hover effects work smoothly
- [ ] Animations are smooth
- [ ] Toast notifications appear
- [ ] No console errors
- [ ] All links work
- [ ] Icons display correctly

## 📊 Mock Data Details

### Products (12 total)
1. Moniteur Multiparamètres ECG - 4500€
2. Tensiomètre Automatique - 290€
3. Oxymètre de Pouls - 180€
4. Thermomètre Infrarouge - 95€
5. Kit Stéthoscope - 320€
6. Lampe Loupe Chirurgicale - 890€
7. Gants Médicaux Latex - 25€
8. Masques Chirurgicaux - 35€
9. Électrocardiographe Portable - 3200€
10. Oreiller Support Cervical - 125€
11. Seringues Stériles - 45€
12. Bandages Élastiques - 28€

### Categories (4 total)
- Équipements Médicaux
- Appareils de Diagnostic
- Fournitures Médicales
- Équipement Chirurgical

### Mock User
```json
{
  "id": "USER-001",
  "firstName": "Dr. Mohamed",
  "lastName": "Khalil",
  "email": "dr.khalil@medsupply.fr",
  "role": "ADMIN",
  "company": "Centre Médical Paris Nord"
}
```

### Mock Orders (3 total)
- CMD-001: 2 items, Livré, 415€
- CMD-002: 1 item, En cours, 4500€
- CMD-003: 2 items, En attente, 710€

## 🔧 Key Functions (mock-data.js)

### Cart Management
```javascript
getCart()                    // Get current cart items
addToCart(productId, qty)   // Add product to cart
removeFromCart(productId)   // Remove from cart
updateCartQuantity(id, qty) // Update quantity
clearCart()                 // Clear entire cart
getCartTotal()              // Get total price
```

### Product Functions
```javascript
getProductById(id)          // Get product details
MOCK_PRODUCTS               // All products array
MOCK_CATEGORIES             // Categories array
MOCK_ORDERS                 // Orders array
getCurrentUser()            // Get current user
```

### Cart Component
```javascript
createProductCard(product)  // Render product card
quickAddToCart(productId)   // Quick add to cart button
```

## 🎨 Design System

### Colors
- Primary: `#0A2647` (Deep Navy)
- Secondary: `#144272` (Medium Navy)
- Accent: `#2C74B3` (Sky Blue)
- Success: `#16A34A` (Green)
- Warning: `#D97706` (Amber)
- Danger: `#DC2626` (Red)
- Background: `#F8FAFC` (Light Gray)
- Text Primary: `#0F172A` (Dark)
- Text Secondary: `#64748B` (Gray)

### Typography
- Font: Inter (Google Fonts)
- Headings: Bold (700)
- Body: Regular (400, 500)

### Spacing
- Uses Tailwind's default scale
- Consistent padding and margins

## 🔒 Security Notes

⚠️ **Important**: This is a **demo/presentation version** using mock data only.

- No real API calls are made
- No authentication validation
- All data is frontend-only
- Session is not secure (for demo only)
- Payment is not processed (simulated only)
- Use for demonstration/presentation purposes only

## 📱 Browser Support

- Chrome 90+
- Firefox 88+
- Edge 90+
- Safari 14+

## 🚀 Performance

- No external dependencies (except Tailwind CDN)
- Fast page loads with mock data
- Smooth animations with Tailwind
- Optimized images from Unsplash
- Local storage for cart persistence

## 📝 Customization

### Add a New Product
Edit `js/mock-data.js`:
```javascript
{
  id: 13,
  name: "Product Name",
  price: 100,
  discount: 10,
  category: "Category Name",
  description: "Description",
  image: "image-url",
  stock: 50,
  rating: 4.5,
  reviews: 100,
  badge: "New"
}
```

### Modify User Profile
Edit `js/mock-data.js` in `MOCK_USER` object.

### Change Colors
Update `tailwind.config` in each HTML file.

## 🎓 Presentation Tips

1. **Show Product Browsing**: Navigate through home → products → filters
2. **Demonstrate Shopping**: Add items → show cart → update quantities
3. **Complete Purchase**: Go through checkout → see order confirmation
4. **Check Profile**: Show user profile and order history
5. **Responsive Design**: Switch to mobile view to show responsive layout

## 📞 Support

For questions or issues about this demo:
- Check the code comments
- Review mock-data.js structure
- Inspect browser console for any errors

---

**Made with ❤️ for academic presentation & demonstration purposes**
