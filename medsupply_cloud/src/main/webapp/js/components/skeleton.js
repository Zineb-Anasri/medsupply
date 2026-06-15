/**
 * Skeleton Loading Component
 * Provides skeleton loading states for various UI elements
 */

function renderTableSkeleton(rows = 5, columns = 6) {
  return `
    <div class="animate-pulse">
      ${Array(rows).fill(0).map(() => `
        <div class="flex items-center gap-4 py-4 border-b border-gray-100">
          ${Array(columns).fill(0).map(() => `
            <div class="flex-1">
              <div class="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
              <div class="h-3 bg-gray-200 rounded w-1/2"></div>
            </div>
          `).join('')}
        </div>
      `).join('')}
    </div>
  `;
}

function renderCardSkeleton(count = 4) {
  return `
    <div class="animate-pulse grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      ${Array(count).fill(0).map(() => `
        <div class="bg-white rounded-lg border border-gray-200 p-4">
          <div class="h-4 bg-gray-200 rounded w-1/3 mb-4"></div>
          <div class="h-8 bg-gray-200 rounded w-2/3 mb-2"></div>
          <div class="h-3 bg-gray-200 rounded w-1/2"></div>
        </div>
      `).join('')}
    </div>
  `;
}

function renderKPISkeleton(count = 4) {
  return `
    <div class="animate-pulse grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      ${Array(count).fill(0).map(() => `
        <div class="bg-white rounded-lg border border-gray-200 p-6">
          <div class="flex items-center justify-between mb-4">
            <div class="w-10 h-10 bg-gray-200 rounded-lg"></div>
            <div class="h-3 bg-gray-200 rounded w-16"></div>
          </div>
          <div class="h-8 bg-gray-200 rounded w-1/2 mb-2"></div>
          <div class="h-3 bg-gray-200 rounded w-1/3"></div>
        </div>
      `).join('')}
    </div>
  `;
}

function renderListSkeleton(items = 5) {
  return `
    <div class="animate-pulse space-y-4">
      ${Array(items).fill(0).map(() => `
        <div class="bg-white rounded-lg border border-gray-200 p-4">
          <div class="flex items-center gap-4">
            <div class="w-12 h-12 bg-gray-200 rounded-lg"></div>
            <div class="flex-1">
              <div class="h-4 bg-gray-200 rounded w-1/2 mb-2"></div>
              <div class="h-3 bg-gray-200 rounded w-1/3"></div>
            </div>
            <div class="h-8 bg-gray-200 rounded w-20"></div>
          </div>
        </div>
      `).join('')}
    </div>
  `;
}

function renderGridSkeleton(items = 6) {
  return `
    <div class="animate-pulse grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      ${Array(items).fill(0).map(() => `
        <div class="bg-white rounded-lg border border-gray-200 overflow-hidden">
          <div class="h-48 bg-gray-200"></div>
          <div class="p-4">
            <div class="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
            <div class="h-3 bg-gray-200 rounded w-1/2 mb-4"></div>
            <div class="h-6 bg-gray-200 rounded w-1/3"></div>
          </div>
        </div>
      `).join('')}
    </div>
  `;
}

function showSkeleton(containerId, type, options = {}) {
  const container = document.getElementById(containerId);
  if (!container) return;

  let skeletonHTML = '';
  
  switch (type) {
    case 'table':
      skeletonHTML = renderTableSkeleton(options.rows || 5, options.columns || 6);
      break;
    case 'card':
      skeletonHTML = renderCardSkeleton(options.count || 4);
      break;
    case 'kpi':
      skeletonHTML = renderKPISkeleton(options.count || 4);
      break;
    case 'list':
      skeletonHTML = renderListSkeleton(options.items || 5);
      break;
    case 'grid':
      skeletonHTML = renderGridSkeleton(options.items || 6);
      break;
    default:
      skeletonHTML = renderCardSkeleton(options.count || 4);
  }

  container.innerHTML = skeletonHTML;
}

function hideSkeleton(containerId) {
  const container = document.getElementById(containerId);
  if (!container) return;
  container.innerHTML = '';
}
