function formatCurrency(amount) {
  return new Intl.NumberFormat('fr-MA', {
    minimumFractionDigits: 2, maximumFractionDigits: 2
  }).format(amount) + ' MAD';
}

function formatDate(dateStr) {
  return new Date(dateStr).toLocaleDateString('fr-FR', {
    day: '2-digit', month: 'long', year: 'numeric'
  });
}

function formatRelativeTime(dateStr) {
  const diff = Date.now() - new Date(dateStr).getTime();
  const minutes = Math.floor(diff / 60000);
  if (minutes < 1)  return 'À l\'instant';
  if (minutes < 60) return `Il y a ${minutes} min`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24)   return `Il y a ${hours}h`;
  return `Il y a ${Math.floor(hours / 24)} jours`;
}

function getStatusBadge(status) {
  const config = {
    PENDING:      { label: 'En attente',     class: 'bg-yellow-100 text-yellow-800' },
    PROCESSING:   { label: 'En traitement',  class: 'bg-blue-100 text-blue-800' },
    VALIDATED:    { label: 'Validé',         class: 'bg-green-100 text-green-800' },
    REJECTED:     { label: 'Rejeté',         class: 'bg-red-100 text-red-800' },
    CONFIRMED:    { label: 'Confirmée',      class: 'bg-blue-100 text-blue-800' },
    PREPARING:    { label: 'En préparation', class: 'bg-purple-100 text-purple-800' },
    SHIPPED:      { label: 'Expédiée',       class: 'bg-indigo-100 text-indigo-800' },
    IN_TRANSIT:   { label: 'En transit',     class: 'bg-amber-100 text-amber-800' },
    DELIVERED:    { label: 'Livrée',         class: 'bg-green-100 text-green-800' },
    CANCELLED:    { label: 'Annulée',        class: 'bg-red-100 text-red-800' },
    UNPAID:       { label: 'Non payé',       class: 'bg-red-100 text-red-800' },
    PARTIAL:      { label: 'Partiel',        class: 'bg-orange-100 text-orange-800' },
    PAID:         { label: 'Payé',           class: 'bg-green-100 text-green-800' },
    OVERDUE:      { label: 'En retard',      class: 'bg-red-200 text-red-900 font-bold' },
    ACTIVE:       { label: 'Actif',          class: 'bg-green-100 text-green-800' },
    EXPIRED:      { label: 'Expiré',         class: 'bg-red-100 text-red-800' },
    OPEN:         { label: 'Ouvert',         class: 'bg-green-100 text-green-800' },
    CLOSED:       { label: 'Clôturé',        class: 'bg-gray-100 text-gray-700' },
    AWARDED:      { label: 'Attribué',       class: 'bg-blue-100 text-blue-800' },
    HOSPITAL:     { label: 'Hôpital',        class: 'bg-blue-100 text-blue-800' },
    LABORATORY:   { label: 'Laboratoire',    class: 'bg-purple-100 text-purple-800' },
    CLINIC:       { label: 'Clinique',       class: 'bg-teal-100 text-teal-800' },
    DOCTOR:       { label: 'Médecin',        class: 'bg-green-100 text-green-800' },
    MEDICAL_OFFICE:{ label: 'Cabinet',       class: 'bg-orange-100 text-orange-800' },
    RESELLER:     { label: 'Revendeur',      class: 'bg-slate-100 text-slate-700' },
  };
  const c = config[status] || { label: status, class: 'bg-gray-100 text-gray-700' };
  return `<span class="inline-flex items-center px-2.5 py-0.5 
    rounded-full text-xs font-medium ${c.class}">${c.label}</span>`;
}

function renderStockBadge(quantity, minQuantity) {
  if (quantity === 0) {
    return `<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">Rupture</span>`;
  } else if (quantity <= minQuantity) {
    return `<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-amber-100 text-amber-800">Stock faible</span>`;
  } else {
    return `<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">En stock</span>`;
  }
}

function renderClientTypeBadge(type) {
  const config = {
    HOSPITAL: { label: 'Hôpital', class: 'bg-blue-100 text-blue-800' },
    LABORATORY: { label: 'Laboratoire', class: 'bg-purple-100 text-purple-800' },
    CLINIC: { label: 'Clinique', class: 'bg-teal-100 text-teal-800' },
    DOCTOR: { label: 'Médecin', class: 'bg-green-100 text-green-800' },
    MEDICAL_OFFICE: { label: 'Cabinet', class: 'bg-orange-100 text-orange-800' },
    RESELLER: { label: 'Revendeur', class: 'bg-slate-100 text-slate-700' },
  };
  const c = config[type] || { label: type, class: 'bg-gray-100 text-gray-700' };
  return `<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${c.class}">${c.label}</span>`;
}
