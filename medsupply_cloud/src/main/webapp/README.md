# MedSupply Cloud Frontend

Frontend application for MedSupply Cloud B2B medical supply platform.

## Tech Stack

- HTML5
- Tailwind CSS v3 (via CDN)
- Vanilla JavaScript ES6+
- Chart.js (via CDN)
- Lucide Icons (via CDN)
- SweetAlert2 (via CDN)

## Prerequisites

1. Backend server running on `http://localhost:8080/medsupply-cloud/api`
2. Modern web browser (Chrome, Firefox, Edge, Safari)

## Quick Start

### Option 1: Using Python Simple HTTP Server

```bash
cd medsupply-cloud-frontend
python -m http.server 3000
```

Then open: `http://localhost:3000`

### Option 2: Using Node.js http-server

```bash
cd medsupply-cloud-frontend
npx http-server -p 3000
```

Then open: `http://localhost:3000`

### Option 3: Using VS Code Live Server

1. Install "Live Server" extension in VS Code
2. Right-click on `index.html`
3. Select "Open with Live Server"

## Testing Checklist

### 1. Authentication Flow

- [ ] Load `http://localhost:3000` - should redirect to login page
- [ ] Login page displays correctly with French UI
- [ ] Register page loads and form validation works
- [ ] Registration creates account (requires backend)
- [ ] Login with valid credentials works
- [ ] Invalid login shows error message
- [ ] Successful login redirects to correct dashboard based on role

### 2. Admin Dashboard

- [ ] Admin login redirects to `/pages/admin/dashboard.html`
- [ ] Sidebar displays admin navigation items
- [ ] Header shows user info and notification bell
- [ ] Stats cards display (orders, revenue, clients, products)
- [ ] Charts render correctly (orders by month, revenue by month)
- [ ] Recent activity section loads
- [ ] Sidebar toggle works
- [ ] Logout button works

### 3. Client Dashboard

- [ ] Client login redirects to `/pages/client/dashboard.html`
- [ ] Sidebar displays client navigation items
- [ ] Quick action cards work (new quote, catalog, marketplace)
- [ ] Stats cards display (orders, quotes, payments, deliveries)
- [ ] Recent orders section loads
- [ ] Notifications section loads
- [ ] Sidebar toggle works
- [ ] Logout button works

### 4. Supplier Dashboard

- [ ] Supplier login redirects to `/pages/supplier/dashboard.html`
- [ ] Sidebar displays supplier navigation items
- [ ] Quick action cards work (marketplace, offers)
- [ ] Stats cards display (tenders, bids, won, products)
- [ ] Open tenders section loads
- [ ] My bids section loads
- [ ] Sidebar toggle works
- [ ] Logout button works

### 5. Shared Pages

- [ ] Notifications page loads and displays notifications
- [ ] Mark as read functionality works
- [ ] Mark all as read functionality works
- [ ] 404 page displays correctly

### 6. Responsive Design

- [ ] Layout works on desktop (1024px+)
- [ ] Layout works on tablet (768px-1024px)
- [ ] Layout works on mobile (<768px)
- [ ] Mobile menu toggle works
- [ ] Sidebar collapses correctly on mobile

### 7. JavaScript Components

- [ ] API fetch wrapper works with auth headers
- [ ] 401 responses redirect to login
- [ ] Toast notifications display correctly
- [ ] Modal open/close works
- [ ] Table renderer works
- [ ] Loader shows/hides correctly
- [ ] Badge renderer displays correct colors
- [ ] Format functions work (currency, date, relative time)
- [ ] Validation helpers work

## Known Issues

### Backend API Required

The frontend requires the backend API to be running at `http://localhost:8080/medsupply-cloud/api`. Without the backend:
- Login will fail
- Dashboard data won't load
- API calls will show errors

### CORS Configuration

If you encounter CORS errors, ensure your backend allows requests from the frontend origin.

### File Paths

The application expects to be served from the root directory. If serving from a subdirectory, update the base paths in:
- `js/api.js` - API_BASE constant
- All HTML files - script and link tags

## File Structure

```
medsupply-cloud-frontend/
├── assets/
│   ├── css/
│   │   └── custom.css
│   └── img/
│       └── logo.svg
├── js/
│   ├── api.js
│   ├── auth.js
│   ├── router.js
│   ├── components/
│   │   ├── badge.js
│   │   ├── header.js
│   │   ├── loader.js
│   │   ├── modal.js
│   │   ├── sidebar.js
│   │   ├── table.js
│   │   └── toast.js
│   └── utils/
│       ├── format.js
│       └── validate.js
├── pages/
│   ├── admin/
│   │   └── dashboard.html
│   ├── auth/
│   │   ├── login.html
│   │   ├── pending.html
│   │   └── register.html
│   ├── client/
│   │   └── dashboard.html
│   └── supplier/
│       └── dashboard.html
├── 404.html
├── index.html
└── notifications.html
```

## Design System

- **Primary:** #0A2647 (deep navy)
- **Secondary:** #144272 (medium navy)
- **Accent:** #2C74B3 (sky blue)
- **Success:** #16A34A
- **Warning:** #D97706
- **Danger:** #DC2626
- **Background:** #F8FAFC
- **Surface:** #FFFFFF
- **Border:** #E2E8F0
- **Text Primary:** #0F172A
- **Text Secondary:** #64748B

## Browser Compatibility

- Chrome 90+
- Firefox 88+
- Edge 90+
- Safari 14+

## Next Steps

After testing the foundation, the following pages still need to be created:

**Admin Pages:**
- clients.html, client-detail.html
- products.html, product-form.html
- quotes.html, quote-detail.html
- orders.html, order-detail.html
- payments.html
- stocks.html
- suppliers.html
- deliveries.html, delivery-detail.html
- maintenance.html
- marketplace.html

**Client Pages:**
- catalog.html, product-detail.html
- quotes.html, quote-new.html
- orders.html, order-detail.html
- payments.html
- maintenance.html
- marketplace.html

**Supplier Pages:**
- marketplace.html
- offers.html
