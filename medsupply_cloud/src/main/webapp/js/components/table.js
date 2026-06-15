function renderTable(data, columns, containerId) {
  const container = document.getElementById(containerId);
  if (!container) return;
  
  if (!data || data.length === 0) {
    container.innerHTML = `
      <div class="text-center py-8 text-gray-500">
        <p>Aucune donnée disponible</p>
      </div>
    `;
    return;
  }
  
  const headers = columns.map(col => `<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">${col.header}</th>`).join('');
  
  const rows = data.map(row => {
    const cells = columns.map(col => {
      const value = row[col.key];
      const cellContent = col.render ? col.render(value, row) : value;
      return `<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${cellContent}</td>`;
    }).join('');
    return `<tr class="hover:bg-gray-50">${cells}</tr>`;
  }).join('');
  
  container.innerHTML = `
    <div class="overflow-x-auto">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>${headers}</tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          ${rows}
        </tbody>
      </table>
    </div>
  `;
}
