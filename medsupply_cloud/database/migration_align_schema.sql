-- ============================================================================
-- MedSupply Cloud — schema alignment migration
-- ----------------------------------------------------------------------------
-- The application code expects a richer schema than the current Supabase
-- project provides. Introspection showed 3 missing tables and many missing
-- columns, which caused the runtime errors:
--   * HTTP 404 "Could not find the table ..."        -> missing tables
--   * HTTP 400 "Could not find the '<col>' column"    -> missing columns
--   * HTTP 409 foreign-key conflict on quotes.client_id (see code fix: the app
--     now sends the CLIENT PROFILE id (clients.id), not the user id).
--
-- This script is IDEMPOTENT and ADDITIVE (it never drops data). Run it once in
-- the Supabase SQL editor (Dashboard -> SQL Editor -> New query -> Run).
-- ============================================================================

-- 1) quote_items : the app stores proposed/admin prices + timestamps -----------
alter table quote_items add column if not exists proposed_price numeric;
alter table quote_items add column if not exists admin_price    numeric;
alter table quote_items add column if not exists created_at      timestamptz default now();
alter table quote_items add column if not exists updated_at      timestamptz default now();
alter table quote_items alter column unit_price drop not null;

-- 2) orders : updated_at ------------------------------------------------------
alter table orders add column if not exists updated_at timestamptz default now();
alter table orders alter column order_number drop not null;

-- 3) order_items : line total + timestamps ------------------------------------
alter table order_items add column if not exists total_price numeric;
alter table order_items add column if not exists created_at  timestamptz default now();
alter table order_items add column if not exists updated_at  timestamptz default now();

-- 4) payments : full payment lifecycle (status / partial / due date / method) --
alter table payments add column if not exists paid_amount           numeric default 0;
alter table payments add column if not exists status                text    default 'PENDING';
alter table payments add column if not exists due_date              date;
alter table payments add column if not exists payment_method        text;
alter table payments add column if not exists transaction_reference text;
alter table payments add column if not exists created_at            timestamptz default now();
alter table payments add column if not exists updated_at            timestamptz default now();
-- legacy columns the app does not populate -> must be nullable
alter table payments alter column payment_date drop not null;
alter table payments alter column method       drop not null;
alter table payments alter column reference     drop not null;

-- 5) deliveries : address/contact + GPS tracking ------------------------------
alter table deliveries add column if not exists delivery_address       text;
alter table deliveries add column if not exists contact_phone          text;
alter table deliveries add column if not exists contact_person         text;
alter table deliveries add column if not exists current_latitude       double precision;
alter table deliveries add column if not exists current_longitude      double precision;
alter table deliveries add column if not exists last_location_update   timestamptz;
alter table deliveries add column if not exists tracking_url           text;
alter table deliveries add column if not exists estimated_delivery_date timestamptz;
alter table deliveries add column if not exists actual_delivery_date    timestamptz;
alter table deliveries add column if not exists delivery_notes          text;
alter table deliveries add column if not exists created_at              timestamptz default now();
alter table deliveries alter column driver_name drop not null;

-- 6) tenders : updated_at (the app reads/writes it) ---------------------------
alter table tenders add column if not exists updated_at timestamptz default now();

-- 7) notifications : type (QUOTE/ORDER/PAYMENT/DELIVERY/MAINTENANCE/TENDER/SYSTEM)
alter table notifications add column if not exists type text default 'SYSTEM';

-- 8) maintenance_contracts : warranty duration + reference + updated_at -------
alter table maintenance_contracts add column if not exists warranty_duration  integer;
alter table maintenance_contracts add column if not exists contract_reference text;
alter table maintenance_contracts add column if not exists updated_at         timestamptz default now();

-- 9) maintenance_interventions : diagnosis/actions/cost/etc. ------------------
alter table maintenance_interventions add column if not exists diagnosis         text;
alter table maintenance_interventions add column if not exists actions_performed text;
alter table maintenance_interventions add column if not exists replaced_parts    text;
alter table maintenance_interventions add column if not exists intervention_cost numeric;
alter table maintenance_interventions add column if not exists completion_status text default 'PENDING';
alter table maintenance_interventions add column if not exists notes             text;
alter table maintenance_interventions add column if not exists updated_at        timestamptz default now();
alter table maintenance_interventions alter column description drop not null;
alter table maintenance_interventions alter column result      drop not null;
-- NOTE: the app links an intervention to a maintenance REQUEST through the
-- existing contract_id column (it reuses that column to hold the request id).

-- 10) NEW TABLE : tender_items -----------------------------------------------
create table if not exists tender_items (
  tender_item_id uuid primary key default gen_random_uuid(),
  tender_id      uuid references tenders(id) on delete cascade,
  product_id     uuid references products(id),
  quantity       integer not null default 1
);

-- 11) NEW TABLE : supplier_bids ----------------------------------------------
create table if not exists supplier_bids (
  bid_id      uuid primary key default gen_random_uuid(),
  tender_id   uuid references tenders(id) on delete cascade,
  supplier_id uuid,                         -- supplier profile id (suppliers.id)
  price       numeric not null,
  created_at  timestamptz default now()
);

-- 12) NEW TABLE : maintenance_requests ---------------------------------------
create table if not exists maintenance_requests (
  request_id   uuid primary key default gen_random_uuid(),
  contract_id  uuid references maintenance_contracts(id),
  client_id    uuid,                        -- client profile id (clients.id)
  product_id   uuid references products(id),
  description  text,
  priority     text default 'MEDIUM',
  status       text default 'OPEN',
  request_date timestamptz default now(),
  created_at   timestamptz default now(),
  updated_at   timestamptz default now()
);

-- 13) Realtime + RLS (optional): if RLS is enabled and blocking the anon key,
--     allow read access on the new tables (adjust to your security policy).
alter table tender_items        enable row level security;
alter table supplier_bids       enable row level security;
alter table maintenance_requests enable row level security;
do $$ begin
  if not exists (select 1 from pg_policies where tablename='tender_items' and policyname='anon_all_tender_items') then
    create policy anon_all_tender_items on tender_items for all using (true) with check (true);
  end if;
  if not exists (select 1 from pg_policies where tablename='supplier_bids' and policyname='anon_all_supplier_bids') then
    create policy anon_all_supplier_bids on supplier_bids for all using (true) with check (true);
  end if;
  if not exists (select 1 from pg_policies where tablename='maintenance_requests' and policyname='anon_all_maintenance_requests') then
    create policy anon_all_maintenance_requests on maintenance_requests for all using (true) with check (true);
  end if;
end $$;
