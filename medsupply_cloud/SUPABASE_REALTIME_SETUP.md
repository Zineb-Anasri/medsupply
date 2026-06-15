# Supabase Realtime Integration Setup

## Overview

This document explains how to configure Supabase Realtime for the MedSupply Cloud project to enable real-time updates for:
- Delivery GPS tracking
- Payment status updates
- Instant notifications
- Stock level updates

## Architecture

**Java Backend (Servlets + JDBC)**
- Updates database via JDBC operations
- No WebSocket implementation needed
- Business logic validation remains in Java

**Supabase Realtime**
- Automatically broadcasts database changes
- Handles event propagation to subscribed clients
- No additional code required in Java backend

**Frontend (JavaScript)**
- Subscribes to Supabase Realtime channels
- Receives real-time updates
- Updates UI automatically

## Tables to Enable for Realtime

Enable Realtime for the following tables in Supabase:

### 1. deliveries
- **Purpose:** GPS tracking and delivery status updates
- **Events to monitor:** INSERT, UPDATE
- **Key fields:** `current_latitude`, `current_longitude`, `status`, `last_location_update`

### 2. payments
- **Purpose:** Payment status and amount updates
- **Events to monitor:** INSERT, UPDATE
- **Key fields:** `status`, `paid_amount`, `due_date`

### 3. notifications
- **Purpose:** Instant notification delivery
- **Events to monitor:** INSERT, UPDATE
- **Key fields:** `is_read`, `type`, `created_at`

### 4. products
- **Purpose:** Stock level updates
- **Events to monitor:** UPDATE
- **Key fields:** `stock_quantity`, `updated_at`

## Supabase Configuration Steps

### Step 1: Enable Realtime in Supabase Dashboard

1. Go to your Supabase project dashboard
2. Navigate to **Database** → **Replication**
3. Enable Realtime for the tables listed above
4. Select the events to replicate (INSERT, UPDATE, DELETE)

### Step 2: Configure Row Level Security (RLS)

Ensure RLS policies allow realtime subscriptions:

```sql
-- Enable RLS on tables
ALTER TABLE deliveries ENABLE ROW LEVEL SECURITY;
ALTER TABLE payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE products ENABLE ROW LEVEL SECURITY;

-- Allow realtime subscriptions
CREATE POLICY "Enable realtime for deliveries" ON deliveries
  FOR SELECT USING (true);

CREATE POLICY "Enable realtime for payments" ON payments
  FOR SELECT USING (true);

CREATE POLICY "Enable realtime for notifications" ON notifications
  FOR SELECT USING (true);

CREATE POLICY "Enable realtime for products" ON products
  FOR SELECT USING (true);
```

### Step 3: Configure Realtime Filters (Optional)

For security, you can configure filters to limit what users can see in realtime:

```sql
-- Example: Users only see their own notifications
ALTER PUBLICATION supabase_realtime
  ADD TABLE notifications WHERE (user_id = current_user_id());
```

## Java Backend Integration

### RealtimeService Helper Class

The `RealtimeService` class provides documented methods that trigger database updates which Supabase Realtime automatically broadcasts:

```java
RealtimeService realtimeService = new RealtimeService();

// Delivery GPS tracking
realtimeService.updateDeliveryLocation(deliveryId, latitude, longitude);
realtimeService.updateDeliveryStatus(deliveryId, "IN_TRANSIT");

// Payment updates
realtimeService.updatePaymentStatus(paymentId, "PAID");
realtimeService.updatePaymentAmount(paymentId, new BigDecimal("1000.00"));

// Notifications
realtimeService.createNotification(userId, "Payment Received", "Your payment has been processed", "PAYMENT");
realtimeService.markNotificationAsRead(notificationId);

// Stock updates
realtimeService.updateProductStock(productId, 50);
```

### Existing Services Already Trigger Realtime

The existing services already have methods that update the database. These will automatically trigger Supabase Realtime events:

- **DeliveryService:** `updateDeliveryStatus()`, `updateGpsLocation()`
- **PaymentService:** `updateStatus()`, `updatePaidAmount()`
- **NotificationService:** `createNotification()`, `markAsRead()`
- **ProductService:** `updateStock()`

## Frontend Integration (JavaScript)

### Example: Subscribe to Delivery Updates

```javascript
import { createClient } from '@supabase/supabase-js'

const supabase = createClient(SUPABASE_URL, SUPABASE_KEY)

// Subscribe to delivery updates
supabase
  .channel('delivery-updates')
  .on('postgres_changes', {
    event: 'UPDATE',
    schema: 'public',
    table: 'deliveries',
    filter: 'order_id=eq.123e4567-e89b-12d3-a456-426614174000'
  }, (payload) => {
    console.log('Delivery updated:', payload.new)
    // Update UI with new GPS coordinates or status
    updateDeliveryMap(payload.new)
  })
  .subscribe()
```

### Example: Subscribe to Notifications

```javascript
// Subscribe to new notifications
supabase
  .channel('notifications')
  .on('postgres_changes', {
    event: 'INSERT',
    schema: 'public',
    table: 'notifications',
    filter: 'user_id=eq.1'
  }, (payload) => {
    console.log('New notification:', payload.new)
    // Show notification toast
    showNotificationToast(payload.new)
  })
  .subscribe()
```

### Example: Subscribe to Stock Updates

```javascript
// Subscribe to stock updates
supabase
  .channel('stock-updates')
  .on('postgres_changes', {
    event: 'UPDATE',
    schema: 'public',
    table: 'products'
  }, (payload) => {
    console.log('Stock updated:', payload.new)
    // Update product availability in UI
    updateProductAvailability(payload.new)
  })
  .subscribe()
```

## Realtime Event Types

### INSERT Events
- New delivery created
- New payment recorded
- New notification sent
- New product added

### UPDATE Events
- Delivery GPS location updated
- Delivery status changed
- Payment status updated
- Payment amount updated
- Notification marked as read
- Product stock updated

### DELETE Events
- Delivery deleted
- Payment deleted
- Notification deleted
- Product deleted

## Performance Considerations

1. **Filter Subscriptions:** Always use filters to limit the data sent to clients
2. **Batch Updates:** For bulk operations, consider debouncing UI updates
3. **Connection Management:** Handle connection errors and reconnection logic
4. **Security:** Use RLS policies to ensure users only see data they're authorized to see

## Testing Realtime

### Test Delivery GPS Tracking

1. Enable Realtime on `deliveries` table
2. Call `realtimeService.updateDeliveryLocation(deliveryId, lat, lng)`
3. Verify frontend receives the UPDATE event

### Test Payment Updates

1. Enable Realtime on `payments` table
2. Call `realtimeService.updatePaymentStatus(paymentId, "PAID")`
3. Verify frontend receives the UPDATE event

### Test Notifications

1. Enable Realtime on `notifications` table
2. Call `realtimeService.createNotification(userId, title, message, type)`
3. Verify frontend receives the INSERT event

### Test Stock Updates

1. Enable Realtime on `products` table
2. Call `realtimeService.updateProductStock(productId, newStock)`
3. Verify frontend receives the UPDATE event

## Troubleshooting

### Realtime Not Working

1. Check Realtime is enabled in Supabase Dashboard
2. Verify RLS policies allow SELECT on tables
3. Check publication includes the table
4. Verify frontend subscription is active

### No Events Received

1. Check filter conditions in subscription
2. Verify table name matches exactly
3. Check event type (INSERT/UPDATE/DELETE)
4. Ensure database connection is active

### Security Issues

1. Review RLS policies
2. Check user authentication
3. Verify API key permissions
4. Test with different user roles

## Summary

The Supabase Realtime integration is designed to be:
- **Simple:** No WebSocket code needed in Java backend
- **Automatic:** Database updates automatically trigger realtime events
- **Secure:** RLS policies control what users can see
- **Scalable:** Supabase handles event propagation efficiently

The Java backend continues to use standard JDBC operations. Supabase Realtime automatically broadcasts changes to subscribed frontend clients, enabling real-time updates without additional backend complexity.
