const statusConfig = {
  PENDING:    { label: "En attente",    class: "bg-yellow-100 text-yellow-800" },
  PROCESSING: { label: "En traitement", class: "bg-blue-100 text-blue-800" },
  VALIDATED:  { label: "Validé",        class: "bg-green-100 text-green-800" },
  REJECTED:   { label: "Rejeté",        class: "bg-red-100 text-red-800" },
  CONFIRMED:  { label: "Confirmée",     class: "bg-blue-100 text-blue-800" },
  PREPARING:  { label: "En préparation",class: "bg-purple-100 text-purple-800" },
  SHIPPED:    { label: "Expédiée",      class: "bg-indigo-100 text-indigo-800" },
  DELIVERED:  { label: "Livrée",        class: "bg-green-100 text-green-800" },
  CANCELLED:  { label: "Annulée",       class: "bg-red-100 text-red-800" },
  UNPAID:     { label: "Non payé",      class: "bg-red-100 text-red-800" },
  PARTIAL:    { label: "Partiel",       class: "bg-orange-100 text-orange-800" },
  PAID:       { label: "Payé",          class: "bg-green-100 text-green-800" },
  OVERDUE:    { label: "En retard",     class: "bg-red-200 text-red-900 font-bold" },
  HOSPITAL:   { label: "Hôpital",       class: "bg-blue-100 text-blue-800" },
  LABORATORY: { label: "Laboratoire",   class: "bg-purple-100 text-purple-800" },
  CLINIC:     { label: "Clinique",      class: "bg-teal-100 text-teal-800" },
  DOCTOR:     { label: "Médecin",       class: "bg-green-100 text-green-800" },
  MEDICAL_OFFICE: { label: "Cabinet",   class: "bg-orange-100 text-orange-800" },
  RESELLER:   { label: "Revendeur",     class: "bg-slate-100 text-slate-700" },
  OPEN:       { label: "Ouvert",        class: "bg-green-100 text-green-800" },
  CLOSED:     { label: "Fermé",         class: "bg-gray-100 text-gray-800" },
  EXPIRED:    { label: "Expiré",        class: "bg-red-100 text-red-800" },
  AWARDED:    { label: "Attribué",      class: "bg-blue-100 text-blue-800" },
  ACTIVE:     { label: "Actif",         class: "bg-green-100 text-green-800" },
  INACTIVE:   { label: "Inactif",       class: "bg-gray-100 text-gray-800" },
};

function renderBadge(status) {
  const cfg = statusConfig[status] || { label: status, class: "bg-gray-100 text-gray-700" };
  return `<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${cfg.class}">${cfg.label}</span>`;
}
