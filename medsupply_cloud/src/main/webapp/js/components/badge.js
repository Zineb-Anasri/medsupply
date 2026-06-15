// Status -> badge config. Single source of truth for status pills across the app.
// Values mirror the backend enums (see openapi.json / services *StatusTransition).
const statusConfig = {
  // ---- Quote (devis) : PENDING -> REVIEWED -> APPROVED/REJECTED -> CONVERTED ----
  PENDING:    { label: "En attente",    class: "bg-yellow-100 text-yellow-800" },
  REVIEWED:   { label: "Devis chiffré", class: "bg-blue-100 text-blue-800" },
  APPROVED:   { label: "Approuvé",      class: "bg-green-100 text-green-800" },
  REJECTED:   { label: "Rejeté",        class: "bg-red-100 text-red-800" },
  CONVERTED:  { label: "Converti",      class: "bg-indigo-100 text-indigo-800" },
  VALIDATED:  { label: "Validé",        class: "bg-green-100 text-green-800" },

  // ---- Order (commande) : PENDING -> PROCESSING -> SHIPPED -> DELIVERED -> COMPLETED ----
  PROCESSING: { label: "En traitement", class: "bg-blue-100 text-blue-800" },
  CONFIRMED:  { label: "Confirmée",     class: "bg-blue-100 text-blue-800" },
  SHIPPED:    { label: "Expédiée",      class: "bg-indigo-100 text-indigo-800" },
  COMPLETED:  { label: "Terminée",      class: "bg-green-100 text-green-800" },
  CANCELLED:  { label: "Annulée",       class: "bg-red-100 text-red-800" },

  // ---- Delivery (livraison) : PENDING -> PREPARING -> IN_TRANSIT -> DELIVERED -> RECEIVED ----
  PREPARING:  { label: "En préparation",class: "bg-purple-100 text-purple-800" },
  IN_TRANSIT: { label: "En transit",    class: "bg-amber-100 text-amber-800" },
  DELIVERED:  { label: "Livrée",        class: "bg-green-100 text-green-800" },
  RECEIVED:   { label: "Réceptionnée",  class: "bg-emerald-100 text-emerald-800" },

  // ---- Payment (paiement) : PENDING / PARTIAL / PAID / OVERDUE / CANCELLED ----
  UNPAID:     { label: "Non payé",      class: "bg-red-100 text-red-800" },
  PARTIAL:    { label: "Partiel",       class: "bg-orange-100 text-orange-800" },
  PAID:       { label: "Payé",          class: "bg-green-100 text-green-800" },
  OVERDUE:    { label: "En retard",     class: "bg-red-200 text-red-900 font-bold" },

  // ---- Tender / marketplace : OPEN / CLOSED / AWARDED ----
  OPEN:       { label: "Ouvert",        class: "bg-green-100 text-green-800" },
  CLOSED:     { label: "Clôturé",       class: "bg-gray-100 text-gray-700" },
  AWARDED:    { label: "Attribué",      class: "bg-blue-100 text-blue-800" },

  // ---- Maintenance request : OPEN -> ASSIGNED -> IN_PROGRESS -> COMPLETED -> CLOSED ----
  ASSIGNED:    { label: "Assignée",      class: "bg-blue-100 text-blue-800" },
  IN_PROGRESS: { label: "En cours",      class: "bg-amber-100 text-amber-800" },

  // ---- Maintenance contract ----
  ACTIVE:     { label: "Actif",         class: "bg-green-100 text-green-800" },
  INACTIVE:   { label: "Inactif",       class: "bg-gray-100 text-gray-800" },
  EXPIRED:    { label: "Expiré",        class: "bg-red-100 text-red-800" },
  SUSPENDED:  { label: "Suspendu",      class: "bg-orange-100 text-orange-800" },

  // ---- Intervention completion ----
  DONE:        { label: "Terminée",     class: "bg-green-100 text-green-800" },

  // ---- Maintenance priority ----
  LOW:        { label: "Basse",         class: "bg-slate-100 text-slate-700" },
  MEDIUM:     { label: "Moyenne",       class: "bg-blue-100 text-blue-800" },
  HIGH:       { label: "Haute",         class: "bg-orange-100 text-orange-800" },
  URGENT:     { label: "Urgente",       class: "bg-red-100 text-red-800" },

  // ---- Client types ----
  HOSPITAL:       { label: "Hôpital",     class: "bg-blue-100 text-blue-800" },
  LABORATORY:     { label: "Laboratoire", class: "bg-purple-100 text-purple-800" },
  CLINIC:         { label: "Clinique",    class: "bg-teal-100 text-teal-800" },
  DOCTOR:         { label: "Médecin",     class: "bg-green-100 text-green-800" },
  MEDICAL_OFFICE: { label: "Cabinet",     class: "bg-orange-100 text-orange-800" },
  RESELLER:       { label: "Revendeur",   class: "bg-slate-100 text-slate-700" },
};

function renderBadge(status) {
  const cfg = statusConfig[status] || { label: status || "—", class: "bg-gray-100 text-gray-700" };
  return `<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${cfg.class}">${cfg.label}</span>`;
}

// Expose so other helpers (format.js) can reuse the same mapping.
if (typeof window !== "undefined") {
  window.statusConfig = statusConfig;
  window.renderBadge = renderBadge;
}
